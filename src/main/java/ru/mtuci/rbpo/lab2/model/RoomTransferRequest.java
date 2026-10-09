package ru.mtuci.rbpo.lab2.model;

import java.time.Instant;

public record RoomTransferRequest(
        long id,
        long contractId,
        long targetRoomId,
        Instant submittedAt,
        String status
) {
}
