package ru.mtuci.rbpo.lab2.dto;

public record AccountInfo(long id, long userId, String email, String role, boolean enabled) {
}
