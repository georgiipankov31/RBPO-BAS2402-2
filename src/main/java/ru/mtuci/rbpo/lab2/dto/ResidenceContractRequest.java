package ru.mtuci.rbpo.lab2.dto;

import java.time.LocalDate;

public record ResidenceContractRequest(
        long applicationId,
        long roomId,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        String status
) {
}
