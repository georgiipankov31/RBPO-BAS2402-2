package ru.mtuci.rbpo.lab2.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ru.mtuci.rbpo.lab2.dto.LoginRequest;
import ru.mtuci.rbpo.lab2.dto.RefreshRequest;
import ru.mtuci.rbpo.lab2.dto.TokenPair;
import ru.mtuci.rbpo.lab2.dto.TokenRegistrationRequest;
import ru.mtuci.rbpo.lab2.service.UserSessionService;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {
    private final UserSessionService sessions;

    public AuthenticationController(UserSessionService sessions) {
        this.sessions = sessions;
    }

    @PostMapping("/login")
    public TokenPair login(@RequestBody LoginRequest request) {
        return sessions.login(request);
    }

    @PostMapping("/register")
    public ResponseEntity<TokenPair> register(@RequestBody TokenRegistrationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sessions.register(request));
    }

    @PostMapping("/refresh")
    public TokenPair refresh(@RequestBody RefreshRequest request) {
        return sessions.refresh(request);
    }
}
