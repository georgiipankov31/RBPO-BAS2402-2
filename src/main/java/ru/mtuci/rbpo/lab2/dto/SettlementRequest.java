package ru.mtuci.rbpo.lab2.dto;

import java.time.LocalDate;

public record SettlementRequest(long roomId, LocalDate checkOutDate) {
}
