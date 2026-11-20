package ru.mtuci.rbpo.lab2.dto;

public record TokenRegistrationRequest(String name, String email, String password, String deviceId) {
}
