package ru.mtuci.rbpo.lab2.model;

import java.time.Instant;

public record HousingApplicationEntry(
        long id,
        long userId,
        Instant submittedAt,
        String status
) {
}
