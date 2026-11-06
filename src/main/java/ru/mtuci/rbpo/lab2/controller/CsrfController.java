package ru.mtuci.rbpo.lab2.controller;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import ru.mtuci.rbpo.lab2.dto.CsrfTokenResponse;

@RestController
public class CsrfController {
    @GetMapping("/csrf-token")
    public CsrfTokenResponse csrf(CsrfToken token) {
        return new CsrfTokenResponse(token.getHeaderName(), token.getToken());
    }
}
