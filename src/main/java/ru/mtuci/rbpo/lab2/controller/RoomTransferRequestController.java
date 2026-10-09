package ru.mtuci.rbpo.lab2.controller;

import java.util.List;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ru.mtuci.rbpo.lab2.dto.TransferRequest;
import ru.mtuci.rbpo.lab2.model.RoomTransferRequest;
import ru.mtuci.rbpo.lab2.service.HousingService;

@RestController
@RequestMapping("/api/room-transfer-requests")
public class RoomTransferRequestController {
    private final HousingService service;

    public RoomTransferRequestController(HousingService service) {
        this.service = service;
    }

    @GetMapping
    public List<RoomTransferRequest> list() {
        return service.transfers();
    }

    @GetMapping("/{id}")
    public RoomTransferRequest get(@PathVariable long id) {
        return service.transfer(id);
    }

    @PostMapping
    public ResponseEntity<RoomTransferRequest> create(@RequestBody TransferRequest r) {
        RoomTransferRequest result =
                service.saveTransfer(null, r.contractId(), r.targetRoomId(), r.status());
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PatchMapping("/{id}")
    public RoomTransferRequest update(@PathVariable long id, @RequestBody ObjectNode patch) {
        RoomTransferRequest old = service.transfer(id);
        long contractId = old.contractId();
        long targetRoomId = old.targetRoomId();
        String status = old.status();

        if (patch.has("contractId")) {
            contractId = patch.get("contractId").asLong();
        }
        if (patch.has("targetRoomId")) {
            targetRoomId = patch.get("targetRoomId").asLong();
        }
        if (patch.has("status")) {
            status = patch.get("status").asText(null);
        }

        return service.saveTransfer(id, contractId, targetRoomId, status);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        service.deleteTransfer(id);
        return ResponseEntity.noContent().build();
    }
}
