package ru.mtuci.rbpo.lab2.service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import ru.mtuci.rbpo.lab2.model.HousingApplicationEntry;
import ru.mtuci.rbpo.lab2.model.ResidenceContract;
import ru.mtuci.rbpo.lab2.model.Room;
import ru.mtuci.rbpo.lab2.model.RoomTransferRequest;
import ru.mtuci.rbpo.lab2.model.User;
import ru.mtuci.rbpo.lab2.repository.HousingData;
import ru.mtuci.rbpo.lab2.repository.JsonHousingRepository;

@Service
public class HousingService {
    private final JsonHousingRepository repository;

    public HousingService(JsonHousingRepository repository) {
        this.repository = repository;
    }

    public List<User> users() {
        HousingData data = repository.getData();
        return new ArrayList<>(data.users().values());
    }

    public User user(long id) {
        HousingData data = repository.getData();
        User value = data.users().get(id);
        if (value == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return value;
    }

    public User saveUser(Long id, String name) {
        if (id != null) {
            user(id);
        }

        HousingData data = repository.getData();
        long resourceId = id == null ? data.nextId() : id;
        User value = new User(resourceId, name);
        data.users().put(value.id(), value);
        saveData(data, id == null);
        return value;
    }

    public void deleteUser(long id) {
        user(id);
        HousingData data = repository.getData();
        for (HousingApplicationEntry application : new ArrayList<>(data.applications().values())) {
            if (application.userId() == id) {
                removeApplication(data, application.id());
            }
        }
        data.users().remove(id);
        repository.save(data);
    }

    public List<Room> rooms() {
        HousingData data = repository.getData();
        return new ArrayList<>(data.rooms().values());
    }

    public Room room(long id) {
        HousingData data = repository.getData();
        Room value = data.rooms().get(id);
        if (value == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return value;
    }

    public Room saveRoom(Long id, String number, int capacity, boolean available) {
        if (id != null) {
            room(id);
        }

        HousingData data = repository.getData();
        long resourceId = id == null ? data.nextId() : id;
        Room value = new Room(resourceId, number, capacity, available);
        data.rooms().put(value.id(), value);
        saveData(data, id == null);
        return value;
    }

    public void deleteRoom(long id) {
        room(id);
        HousingData data = repository.getData();
        for (ResidenceContract contract : new ArrayList<>(data.contracts().values())) {
            if (contract.roomId() == id) {
                removeContract(data, contract.id());
            }
        }
        for (RoomTransferRequest transfer : new ArrayList<>(data.transfers().values())) {
            if (transfer.targetRoomId() == id) {
                data.transfers().remove(transfer.id());
            }
        }
        data.rooms().remove(id);
        repository.save(data);
    }

    public List<HousingApplicationEntry> applications() {
        HousingData data = repository.getData();
        return new ArrayList<>(data.applications().values());
    }

    public HousingApplicationEntry application(long id) {
        HousingData data = repository.getData();
        HousingApplicationEntry value = data.applications().get(id);
        if (value == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return value;
    }

    public HousingApplicationEntry saveApplication(
            Long id, long userId, String status) {
        HousingApplicationEntry old = id == null ? null : application(id);
        user(userId);

        HousingData data = repository.getData();
        long resourceId = id == null ? data.nextId() : id;
        HousingApplicationEntry value =
                new HousingApplicationEntry(
                        resourceId,
                        userId,
                        old == null ? Instant.now() : old.submittedAt(),
                        status);
        data.applications().put(value.id(), value);
        saveData(data, id == null);
        return value;
    }

    public void deleteApplication(long id) {
        application(id);
        HousingData data = repository.getData();
        removeApplication(data, id);
        repository.save(data);
    }

    public List<ResidenceContract> contracts() {
        HousingData data = repository.getData();
        return new ArrayList<>(data.contracts().values());
    }

    public ResidenceContract contract(long id) {
        HousingData data = repository.getData();
        ResidenceContract value = data.contracts().get(id);
        if (value == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return value;
    }

    public ResidenceContract saveContract(
            Long id,
            long applicationId,
            long roomId,
            LocalDate checkInDate,
            LocalDate checkOutDate,
            String status) {
        if (id != null) {
            contract(id);
        }
        application(applicationId);
        room(roomId);

        HousingData data = repository.getData();
        long resourceId = id == null ? data.nextId() : id;
        ResidenceContract value =
                new ResidenceContract(
                        resourceId, applicationId, roomId, checkInDate, checkOutDate, status);
        data.contracts().put(value.id(), value);
        saveData(data, id == null);
        return value;
    }

    public void deleteContract(long id) {
        contract(id);
        HousingData data = repository.getData();
        removeContract(data, id);
        repository.save(data);
    }

    public List<RoomTransferRequest> transfers() {
        HousingData data = repository.getData();
        return new ArrayList<>(data.transfers().values());
    }

    public RoomTransferRequest transfer(long id) {
        HousingData data = repository.getData();
        RoomTransferRequest value = data.transfers().get(id);
        if (value == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return value;
    }

    public RoomTransferRequest saveTransfer(
            Long id, long contractId, long targetRoomId, String status) {
        RoomTransferRequest old = id == null ? null : transfer(id);
        contract(contractId);
        room(targetRoomId);

        HousingData data = repository.getData();
        long resourceId = id == null ? data.nextId() : id;
        RoomTransferRequest value =
                new RoomTransferRequest(
                        resourceId,
                        contractId,
                        targetRoomId,
                        old == null ? Instant.now() : old.submittedAt(),
                        status);
        data.transfers().put(value.id(), value);
        saveData(data, id == null);
        return value;
    }

    public void deleteTransfer(long id) {
        transfer(id);
        HousingData data = repository.getData();
        data.transfers().remove(id);
        repository.save(data);
    }

    private void saveData(HousingData data, boolean created) {
        if (created) {
            data = data.withNextId(data.nextId() + 1);
        }
        repository.save(data);
    }

    private void removeContract(HousingData data, long id) {
        for (RoomTransferRequest transfer : new ArrayList<>(data.transfers().values())) {
            if (transfer.contractId() == id) {
                data.transfers().remove(transfer.id());
            }
        }
        data.contracts().remove(id);
    }

    private void removeApplication(HousingData data, long id) {
        for (ResidenceContract contract : new ArrayList<>(data.contracts().values())) {
            if (contract.applicationId() == id) {
                removeContract(data, contract.id());
            }
        }
        data.applications().remove(id);
    }
}
