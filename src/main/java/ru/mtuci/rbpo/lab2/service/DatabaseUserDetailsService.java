package ru.mtuci.rbpo.lab2.service;

import java.util.Locale;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import ru.mtuci.rbpo.lab2.repository.AccountRepository;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {
    private final AccountRepository repository;

    public DatabaseUserDetailsService(AccountRepository repository) {
        this.repository = repository;
    }

    // Spring вызывает при входе; пароль затем проверяет провайдер.
    @Override
    public UserDetails loadUserByUsername(String email) {
        UserDetails user = repository.findUserDetails(email.trim().toLowerCase(Locale.ROOT));
        if (user == null) {
            throw new UsernameNotFoundException("Учетная запись не найдена");
        }
        return user;
    }
}
