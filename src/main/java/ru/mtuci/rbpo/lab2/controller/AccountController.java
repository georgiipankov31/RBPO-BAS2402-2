package ru.mtuci.rbpo.lab2.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import ru.mtuci.rbpo.lab2.dto.AccountInfo;
import ru.mtuci.rbpo.lab2.dto.RegistrationRequest;
import ru.mtuci.rbpo.lab2.service.AccountService;

@RestController
public class AccountController {
    private final AccountService service;

    public AccountController(AccountService service) {
        this.service = service;
    }

    @PostMapping("/api/register")
    public ResponseEntity<AccountInfo> register(@RequestBody RegistrationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.register(request));
    }

    @GetMapping("/api/me")
    @PreAuthorize("isAuthenticated()")
    public AccountInfo me() {
        return service.current();
    }
}
