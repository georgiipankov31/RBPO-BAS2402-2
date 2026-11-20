package ru.mtuci.rbpo.lab2.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.UUID;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ru.mtuci.rbpo.lab2.dto.AccountInfo;
import ru.mtuci.rbpo.lab2.dto.LoginRequest;
import ru.mtuci.rbpo.lab2.dto.RefreshRequest;
import ru.mtuci.rbpo.lab2.dto.RegistrationRequest;
import ru.mtuci.rbpo.lab2.dto.TokenPair;
import ru.mtuci.rbpo.lab2.dto.TokenRegistrationRequest;
import ru.mtuci.rbpo.lab2.model.SessionStatus;
import ru.mtuci.rbpo.lab2.model.UserSession;
import ru.mtuci.rbpo.lab2.repository.AccountRepository;
import ru.mtuci.rbpo.lab2.repository.UserSessionRepository;

@Service
public class UserSessionService {
    private final AccountService accounts;
    private final AccountRepository accountRepository;
    private final AuthenticationManager authenticationManager;
    private final UserSessionRepository sessions;
    private final JwtService jwt;
    private final long accessLifetime;
    private final long refreshLifetime;

    public UserSessionService(AccountService accounts, AccountRepository accountRepository,
            AuthenticationManager authenticationManager, UserSessionRepository sessions, JwtService jwt,
            @Value("${security.jwt.access-lifetime-seconds}") long accessLifetime,
            @Value("${security.jwt.refresh-lifetime-seconds}") long refreshLifetime) {
        if (accessLifetime <= 0 || refreshLifetime <= 0) {
            throw new IllegalArgumentException("Сроки действия токенов должны быть положительными");
        }
        this.accounts = accounts;
        this.accountRepository = accountRepository;
        this.authenticationManager = authenticationManager;
        this.sessions = sessions;
        this.jwt = jwt;
        this.accessLifetime = accessLifetime;
        this.refreshLifetime = refreshLifetime;
    }

    @Transactional
    public TokenPair login(LoginRequest request) {
        if (request.email() == null || request.email().isBlank()
                || request.password() == null || request.password().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Укажите email и пароль");
        }
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.password()));
        return issuePair(email, request.deviceId());
    }

    @Transactional
    public TokenPair register(TokenRegistrationRequest request) {
        AccountInfo account = accounts.register(
                new RegistrationRequest(request.name(), request.email(), request.password()));
        return issuePair(account.email(), request.deviceId());
    }

    // При повторном refresh сохраняем REVOKED, хотя клиент получает отказ.
    @Transactional(noRollbackFor = BadCredentialsException.class)
    public TokenPair refresh(RefreshRequest request) {
        if (request.refreshToken() == null || request.refreshToken().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Укажите refreshToken");
        }
        Claims claims;
        try {
            claims = jwt.checkRefreshToken(request.refreshToken());
        } catch (JwtException | IllegalArgumentException e) {
            throw new BadCredentialsException("Недействительный refresh");
        }
        UserSession session = sessions.findByRefreshToken(request.refreshToken());
        if (session == null || !session.username().equals(claims.getSubject())
                || !session.refreshTokenExpiry().isAfter(Instant.now())) {
            throw new BadCredentialsException("Недействительный refresh");
        }
        AccountInfo account = accountRepository.findByEmail(session.username());
        if (account == null || !account.enabled()) {
            throw new BadCredentialsException("Недействительный refresh");
        }
        if (session.status() == SessionStatus.USED) {
            sessions.changeStatus(session.id(), SessionStatus.REVOKED);
            throw new BadCredentialsException("Недействительный refresh");
        }
        if (session.status() != SessionStatus.ACTIVE) {
            throw new BadCredentialsException("Недействительный refresh");
        }
        sessions.changeStatus(session.id(), SessionStatus.USED);
        return issuePair(session.username(), session.deviceId());
    }

    private TokenPair issuePair(String username, String deviceId) {
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        Instant accessExpiry = now.plusSeconds(accessLifetime);
        Instant refreshExpiry = now.plusSeconds(refreshLifetime);
        String access = jwt.createAccessToken(username, now, accessExpiry);
        String refresh = jwt.createRefreshToken(username, now, refreshExpiry);
        sessions.save(new UserSession(UUID.randomUUID(), username, deviceId, access, refresh,
                accessExpiry, refreshExpiry, SessionStatus.ACTIVE));
        return new TokenPair(access, refresh);
    }
}
