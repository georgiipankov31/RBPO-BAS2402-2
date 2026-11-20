package ru.mtuci.rbpo.lab2.configuration;

import java.io.IOException;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

import ru.mtuci.rbpo.lab2.service.JwtService;

public class JwtTokenFilter extends OncePerRequestFilter {
    private final JwtService jwt;
    private final UserDetailsService users;

    public JwtTokenFilter(JwtService jwt, UserDetailsService users) {
        this.jwt = jwt;
        this.users = users;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return "POST".equals(request.getMethod()) && ("/auth/login".equals(path)
                || "/auth/register".equals(path) || "/auth/refresh".equals(path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null) {
            try {
                if (!header.regionMatches(true, 0, "Bearer ", 0, 7)) {
                    throw new BadCredentialsException("Ожидается Bearer-токен");
                }
                String username = jwt.checkAccessToken(header.substring(7).trim()).getSubject();
                UserDetails user = users.loadUserByUsername(username);
                if (!user.isEnabled() || !user.isAccountNonLocked()
                        || !user.isAccountNonExpired() || !user.isCredentialsNonExpired()) {
                    throw new BadCredentialsException("Учетная запись недоступна");
                }
                var authentication = new UsernamePasswordAuthenticationToken(
                        user, null, user.getAuthorities());
                var context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(authentication);
                SecurityContextHolder.setContext(context);
            } catch (JwtException | IllegalArgumentException | AuthenticationException e) {
                SecurityContextHolder.clearContext();
                response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
