package ru.mtuci.rbpo.lab2.repository;

import java.util.LinkedHashMap;
import java.util.Map;

import ru.mtuci.rbpo.lab2.model.HousingApplicationEntry;
import ru.mtuci.rbpo.lab2.model.ResidenceContract;
import ru.mtuci.rbpo.lab2.model.Room;
import ru.mtuci.rbpo.lab2.model.RoomTransferRequest;
import ru.mtuci.rbpo.lab2.model.User;

public record HousingData(
        long nextId,
        Map<Long, User> users,
        Map<Long, Room> rooms,
        Map<Long, HousingApplicationEntry> applications,
        Map<Long, ResidenceContract> contracts,
        Map<Long, RoomTransferRequest> transfers
) {

    public static HousingData empty() {
        return new HousingData(
                1,
                new LinkedHashMap<>(),
                new LinkedHashMap<>(),
                new LinkedHashMap<>(),
                new LinkedHashMap<>(),
                new LinkedHashMap<>());
    }

    public HousingData withNextId(long value) {
        return new HousingData(value, users, rooms, applications, contracts, transfers);
    }
}
