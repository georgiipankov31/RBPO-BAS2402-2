package ru.mtuci.rbpo.lab2.service;

import java.time.LocalDate;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ru.mtuci.rbpo.lab2.model.HousingApplicationEntry;
import ru.mtuci.rbpo.lab2.model.ResidenceContract;
import ru.mtuci.rbpo.lab2.model.RoomTransferRequest;
import ru.mtuci.rbpo.lab2.repository.HousingRepository;

@Service
@Transactional(isolation = Isolation.SERIALIZABLE)
public class ResidenceService {
    private final HousingService housing;
    private final HousingRepository repository;

    public ResidenceService(HousingService housing, HousingRepository repository) {
        this.housing = housing;
        this.repository = repository;
    }

    public ResidenceContract settle(long applicationId, long roomId, LocalDate checkOutDate) {
        return housing.saveContract(null, applicationId, roomId, LocalDate.now(), checkOutDate, "ACTIVE");
    }

    public HousingApplicationEntry cancelApplication(long id) {
        HousingApplicationEntry application = housing.application(id);
        if (!"NEW".equals(application.status())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Отменить можно только новую заявку");
        }
        repository.setApplicationStatus(id, "CANCELLED");
        return housing.application(id);
    }

    public ResidenceContract extend(long id, LocalDate checkOutDate) {
        ResidenceContract contract = housing.contract(id);
        housing.checkExtension(contract, checkOutDate);
        return repository.saveContract(id, contract.applicationId(), contract.roomId(),
                contract.checkInDate(), checkOutDate, contract.status());
    }

    public ResidenceContract checkOut(long id) {
        ResidenceContract contract = housing.contract(id);
        if (!"ACTIVE".equals(contract.status())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Договор уже завершён");
        }
        repository.cancelPendingTransfers(id);
        repository.setApplicationStatus(contract.applicationId(), "COMPLETED");
        return repository.saveContract(id, contract.applicationId(), contract.roomId(),
                contract.checkInDate(), LocalDate.now(), "COMPLETED");
    }

    public RoomTransferRequest approveTransfer(long id) {
        RoomTransferRequest transfer = housing.transfer(id);
        if (!"NEW".equals(transfer.status())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Заявка на переселение уже обработана");
        }
        ResidenceContract contract = housing.contract(transfer.contractId());
        housing.checkActiveContract(contract);
        if (contract.roomId() == transfer.targetRoomId()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Жилец уже находится в этой комнате");
        }
        housing.checkFreeRoom(transfer.targetRoomId());
        repository.saveContract(contract.id(), contract.applicationId(), transfer.targetRoomId(),
                contract.checkInDate(), contract.checkOutDate(), contract.status());
        RoomTransferRequest result = repository.saveTransfer(id, contract.id(), transfer.targetRoomId(), "APPROVED");
        repository.cancelPendingTransfers(contract.id());
        return result;
    }
}
