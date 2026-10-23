package ru.mtuci.rbpo.lab2.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ru.mtuci.rbpo.lab2.model.HousingApplicationEntry;
import ru.mtuci.rbpo.lab2.model.ResidenceContract;
import ru.mtuci.rbpo.lab2.model.Room;
import ru.mtuci.rbpo.lab2.model.RoomTransferRequest;
import ru.mtuci.rbpo.lab2.model.User;
import ru.mtuci.rbpo.lab2.repository.HousingRepository;

@Service
@Transactional(isolation = Isolation.SERIALIZABLE)
public class HousingService {
    private final HousingRepository repository;

    public HousingService(HousingRepository repository) {
        this.repository = repository;
    }

    public List<User> users() {
        return repository.users();
    }

    public User user(long id) {
        User value = repository.user(id);
        if (value == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден");
        }
        return value;
    }

    public User saveUser(Long id, String name) {
        if (id != null) {
            user(id);
        }
        return repository.saveUser(id, name);
    }

    public void deleteUser(long id) {
        user(id);
        if (repository.hasActiveContract(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Сначала выполните выселение");
        }
        repository.deleteUser(id);
    }

    public List<Room> rooms() {
        return repository.rooms();
    }

    public Room room(long id) {
        Room value = repository.room(id);
        if (value == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Комната не найдена");
        }
        return value;
    }

    public Room saveRoom(Long id, String number, int capacity, boolean available) {
        if (id != null) {
            room(id);
            int occupied = repository.occupiedPlaces(id);
            if (capacity < occupied || (!available && occupied > 0)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Нельзя уменьшить вместимость ниже числа жильцов или закрыть занятую комнату");
            }
        }
        return repository.saveRoom(id, number, capacity, available);
    }

    public void deleteRoom(long id) {
        room(id);
        if (repository.occupiedPlaces(id) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "В комнате есть жильцы");
        }
        repository.deleteRoom(id);
    }

    public List<HousingApplicationEntry> applications() {
        return repository.applications();
    }

    public HousingApplicationEntry application(long id) {
        HousingApplicationEntry value = repository.application(id);
        if (value == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Заявка не найдена");
        }
        return value;
    }

    public HousingApplicationEntry saveApplication(Long id, long userId, String status) {
        user(userId);
        if (id == null) {
            if (status == null) {
                status = "NEW";
            }
            if (!"NEW".equals(status)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Новая заявка должна иметь статус NEW");
            }
        } else {
            HousingApplicationEntry old = application(id);
            if (!old.status().equals(status) || (!"NEW".equals(old.status()) && old.userId() != userId)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Статус меняется бизнес-операциями; менять заявителя можно только у новой заявки");
            }
        }
        return repository.saveApplication(id, userId, status);
    }

    public void deleteApplication(long id) {
        application(id);
        if (repository.applicationHasActiveContract(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Сначала выполните выселение");
        }
        repository.deleteApplication(id);
    }

    public List<ResidenceContract> contracts() {
        return repository.contracts();
    }

    public ResidenceContract contract(long id) {
        ResidenceContract value = repository.contract(id);
        if (value == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Договор не найден");
        }
        return value;
    }

    public ResidenceContract saveContract(Long id, long applicationId, long roomId,
            LocalDate checkInDate, LocalDate checkOutDate, String status) {
        HousingApplicationEntry application = application(applicationId);
        room(roomId);
        if (checkInDate == null || checkOutDate == null || checkOutDate.isBefore(checkInDate)
                || checkInDate.isAfter(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Некорректные даты проживания");
        }
        if (id == null) {
            if (status == null) {
                status = "ACTIVE";
            }
            if (!"ACTIVE".equals(status) || !"NEW".equals(application.status())
                    || checkOutDate.isBefore(LocalDate.now())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Договор создаётся действующим по новой заявке с неистекшим сроком проживания");
            }
            if (repository.hasActiveContract(application.userId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Пользователь уже проживает в общежитии");
            }
            checkFreeRoom(roomId);
            repository.setApplicationStatus(applicationId, "APPROVED");
        } else {
            ResidenceContract old = contract(id);
            if (!"ACTIVE".equals(old.status()) || !old.status().equals(status)
                    || old.applicationId() != applicationId || old.roomId() != roomId
                    || !old.checkInDate().equals(checkInDate)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "У действующего договора можно менять только дату окончания; для переселения и выселения есть операции");
            }
            if (!old.checkOutDate().equals(checkOutDate)) {
                checkExtension(old, checkOutDate);
            }
        }
        return repository.saveContract(id, applicationId, roomId, checkInDate, checkOutDate, status);
    }

    public void deleteContract(long id) {
        if ("ACTIVE".equals(contract(id).status())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Сначала выполните выселение");
        }
        repository.deleteContract(id);
    }

    public List<RoomTransferRequest> transfers() {
        return repository.transfers();
    }

    public RoomTransferRequest transfer(long id) {
        RoomTransferRequest value = repository.transfer(id);
        if (value == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Заявка на переселение не найдена");
        }
        return value;
    }

    public RoomTransferRequest saveTransfer(Long id, long contractId, long targetRoomId, String status) {
        ResidenceContract contract = contract(contractId);
        checkActiveContract(contract);
        Room target = room(targetRoomId);
        if (contract.roomId() == targetRoomId || !target.available()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Для переселения выберите другую открытую комнату");
        }
        if (status == null && id == null) {
            status = "NEW";
        }
        if (!"NEW".equals(status)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Статус меняется операцией переселения");
        }
        if (id != null) {
            RoomTransferRequest old = transfer(id);
            if (!"NEW".equals(old.status()) || old.contractId() != contractId) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Можно менять только комнату новой заявки");
            }
        }
        return repository.saveTransfer(id, contractId, targetRoomId, status);
    }

    public void deleteTransfer(long id) {
        transfer(id);
        repository.deleteTransfer(id);
    }

    public void checkFreeRoom(long roomId) {
        Room room = room(roomId);
        if (!room.available() || repository.occupiedPlaces(roomId) >= room.capacity()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Комната закрыта или нет свободных мест");
        }
    }

    public void checkActiveContract(ResidenceContract contract) {
        if (!"ACTIVE".equals(contract.status()) || contract.checkOutDate().isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Договор завершён или срок проживания истёк");
        }
    }

    public void checkExtension(ResidenceContract contract, LocalDate checkOutDate) {
        checkActiveContract(contract);
        if (checkOutDate == null || !checkOutDate.isAfter(contract.checkOutDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Новая дата должна быть позже прежней");
        }
    }
}
