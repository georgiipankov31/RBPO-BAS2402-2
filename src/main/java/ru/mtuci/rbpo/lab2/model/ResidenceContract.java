package ru.mtuci.rbpo.lab2.model;

import java.time.LocalDate;

public record ResidenceContract(
        long id,
        long applicationId,
        long roomId,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        String status
) {
}
