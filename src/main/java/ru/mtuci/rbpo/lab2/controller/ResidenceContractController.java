package ru.mtuci.rbpo.lab2.controller;

import java.time.LocalDate;
import java.util.List;

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

import com.fasterxml.jackson.databind.node.ObjectNode;

import ru.mtuci.rbpo.lab2.dto.ResidenceContractRequest;
import ru.mtuci.rbpo.lab2.model.ResidenceContract;
import ru.mtuci.rbpo.lab2.service.HousingService;

@RestController
@RequestMapping("/api/residence-contracts")
public class ResidenceContractController {
    private final HousingService service;

    public ResidenceContractController(HousingService service) {
        this.service = service;
    }
 
    @GetMapping
    public List<ResidenceContract> list() {
        return service.contracts();
    }

    @GetMapping("/{id}")
    public ResidenceContract get(@PathVariable long id) {
        return service.contract(id);
    }

    @PostMapping
    public ResponseEntity<ResidenceContract> create(@RequestBody ResidenceContractRequest r) {
        ResidenceContract result =
                service.saveContract(
                        null,
                        r.applicationId(),
                        r.roomId(),
                        r.checkInDate(),
                        r.checkOutDate(),
                        r.status());
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PatchMapping("/{id}")
    public ResidenceContract update(@PathVariable long id, @RequestBody ObjectNode patch) {
        ResidenceContract old = service.contract(id);
        long applicationId = old.applicationId();
        long roomId = old.roomId();
        LocalDate checkInDate = old.checkInDate();
        LocalDate checkOutDate = old.checkOutDate();
        String status = old.status();

        if (patch.has("applicationId")) {
            applicationId = patch.get("applicationId").asLong();
        }
        if (patch.has("roomId")) {
            roomId = patch.get("roomId").asLong();
        }
        if (patch.has("checkInDate")) {
            if (patch.get("checkInDate").isNull()) {
                checkInDate = null;
            } else {
                checkInDate = LocalDate.parse(patch.get("checkInDate").asText());
            }
        }
        if (patch.has("checkOutDate")) {
            if (patch.get("checkOutDate").isNull()) {
                checkOutDate = null;
            } else {
                checkOutDate = LocalDate.parse(patch.get("checkOutDate").asText());
            }
        }
        if (patch.has("status")) {
            status = patch.get("status").asText(null);
        }

        return service.saveContract(id, applicationId, roomId, checkInDate, checkOutDate, status);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        service.deleteContract(id);
        return ResponseEntity.noContent().build();
    }
}
