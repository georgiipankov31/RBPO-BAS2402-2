package ru.mtuci.rbpo.lab2.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ru.mtuci.rbpo.lab2.dto.ExtensionRequest;
import ru.mtuci.rbpo.lab2.dto.SettlementRequest;
import ru.mtuci.rbpo.lab2.model.HousingApplicationEntry;
import ru.mtuci.rbpo.lab2.model.ResidenceContract;
import ru.mtuci.rbpo.lab2.model.RoomTransferRequest;
import ru.mtuci.rbpo.lab2.service.ResidenceService;

@RestController
@RequestMapping("/api")
public class ResidenceOperationController {
    private final ResidenceService service;

    public ResidenceOperationController(ResidenceService service) {
        this.service = service;
    }

    @PostMapping("/housing-applications/{id}/settle")
    public ResidenceContract settle(@PathVariable long id, @RequestBody SettlementRequest request) {
        return service.settle(id, request.roomId(), request.checkOutDate());
    }

    @PostMapping("/housing-applications/{id}/cancel")
    public HousingApplicationEntry cancel(@PathVariable long id) {
        return service.cancelApplication(id);
    }

    @PostMapping("/residence-contracts/{id}/extend")
    public ResidenceContract extend(@PathVariable long id, @RequestBody ExtensionRequest request) {
        return service.extend(id, request.checkOutDate());
    }

    @PostMapping("/residence-contracts/{id}/check-out")
    public ResidenceContract checkOut(@PathVariable long id) {
        return service.checkOut(id);
    }

    @PostMapping("/room-transfer-requests/{id}/approve")
    public RoomTransferRequest approve(@PathVariable long id) {
        return service.approveTransfer(id);
    }
}
