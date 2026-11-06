package ru.mtuci.rbpo.lab2.service;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ru.mtuci.rbpo.lab2.dto.AccountInfo;
import ru.mtuci.rbpo.lab2.dto.RegistrationRequest;
import ru.mtuci.rbpo.lab2.model.User;
import ru.mtuci.rbpo.lab2.repository.AccountRepository;
import ru.mtuci.rbpo.lab2.repository.HousingRepository;

@Service
public class AccountService {
    private final AccountRepository accounts;
    private final HousingRepository housing;
    private final PasswordEncoder encoder;

    public AccountService(AccountRepository accounts, HousingRepository housing, PasswordEncoder encoder) {
        this.accounts = accounts;
        this.housing = housing;
        this.encoder = encoder;
    }

    // Жилец и учетная запись создаются вместе или обе вставки откатываются.
    @Transactional
    public AccountInfo register(RegistrationRequest request) {
        if (request.name() == null || request.name().isBlank() || request.email() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Укажите имя и email");
        }
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (email.length() > 254 || !email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Некорректный email");
        }
        checkPassword(request.password());
        if (accounts.findByEmail(email) != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Учетная запись уже существует");
        }
        User user = housing.saveUser(null, request.name().trim());
        return accounts.create(user.id(), email, encoder.encode(request.password()));
    }

    public AccountInfo current() {
        // После входа Spring хранит в контексте логин (у нас email) и права.
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        AccountInfo account = accounts.findByEmail(email);
        if (account == null) {
            throw new AccessDeniedException("Нет доступа");
        }
        return account;
    }

    public void checkUser(long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // ADMIN может обращаться к любому жильцу; USER только к своему userId.
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return;
        }
        if (current().userId() != userId) {
            throw new AccessDeniedException("Нет доступа к чужим данным");
        }
    }

    private void checkPassword(String password) {
        if (password == null || password.length() < 8
                || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Пароль должен содержать не менее 8 символов и не более 72 байт UTF-8");
        }
        boolean upper = false;
        boolean lower = false;
        boolean digit = false;
        boolean special = false;
        for (int i = 0; i < password.length(); i++) {
            char character = password.charAt(i);
            upper = upper || Character.isUpperCase(character);
            lower = lower || Character.isLowerCase(character);
            digit = digit || Character.isDigit(character);
            special = special || (!Character.isLetterOrDigit(character) && !Character.isWhitespace(character));
        }
        if (!upper || !lower || !digit || !special) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "В пароле нужны строчные и заглавные буквы, цифры и специальный символ");
        }
    }
}
