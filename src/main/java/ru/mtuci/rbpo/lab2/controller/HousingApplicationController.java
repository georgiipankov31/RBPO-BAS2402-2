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

import ru.mtuci.rbpo.lab2.dto.HousingApplicationRequest;
import ru.mtuci.rbpo.lab2.model.HousingApplicationEntry;
import ru.mtuci.rbpo.lab2.service.HousingService;

@RestController
@RequestMapping("/api/housing-applications")
public class HousingApplicationController {
    private final HousingService service;

    public HousingApplicationController(HousingService service) {
        this.service = service;
    }

    @GetMapping
    public List<HousingApplicationEntry> list() {
        return service.applications();
    }

    @GetMapping("/{id}")
    public HousingApplicationEntry get(@PathVariable long id) {
        return service.application(id);
    }

    @PostMapping
    public ResponseEntity<HousingApplicationEntry> create(
            @RequestBody HousingApplicationRequest r) {
        HousingApplicationEntry result = service.saveApplication(null, r.userId(), r.status());
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PatchMapping("/{id}")
    public HousingApplicationEntry update(@PathVariable long id, @RequestBody ObjectNode patch) {
        HousingApplicationEntry old = service.application(id);
        long userId = old.userId();
        String status = old.status();

        if (patch.has("userId")) {
            userId = patch.get("userId").asLong();
        }
        if (patch.has("status")) {
            status = patch.get("status").asText(null);
        }

        return service.saveApplication(id, userId, status);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        service.deleteApplication(id);
        return ResponseEntity.noContent().build();
    }
}
