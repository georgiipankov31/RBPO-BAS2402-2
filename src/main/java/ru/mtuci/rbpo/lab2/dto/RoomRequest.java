package ru.mtuci.rbpo.lab2.dto;

public record RoomRequest(
        String number,
        int capacity,
        boolean available
) {
}
