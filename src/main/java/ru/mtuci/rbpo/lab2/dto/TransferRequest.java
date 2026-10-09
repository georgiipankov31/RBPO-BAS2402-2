package ru.mtuci.rbpo.lab2.dto;

public record TransferRequest(
        long contractId,
        long targetRoomId,
        String status
) {
}
