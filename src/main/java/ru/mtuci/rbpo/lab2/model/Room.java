package ru.mtuci.rbpo.lab2.model;

public record Room(
        long id,
        String number,
        int capacity,
        boolean available
) {
}
