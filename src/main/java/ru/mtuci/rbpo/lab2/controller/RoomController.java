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

import ru.mtuci.rbpo.lab2.dto.RoomRequest;
import ru.mtuci.rbpo.lab2.model.Room;
import ru.mtuci.rbpo.lab2.service.HousingService;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {
    private final HousingService service;

    public RoomController(HousingService service) {
        this.service = service;
    }

    @GetMapping
    public List<Room> list() {
        return service.rooms();
    }

    @GetMapping("/{id}")
    public Room get(@PathVariable long id) {
        return service.room(id);
    }

    @PostMapping
    public ResponseEntity<Room> create(@RequestBody RoomRequest r) {
        Room result = service.saveRoom(null, r.number(), r.capacity(), r.available());
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PatchMapping("/{id}")
    public Room update(@PathVariable long id, @RequestBody ObjectNode patch) {
        Room old = service.room(id);
        String number = old.number();
        int capacity = old.capacity();
        boolean available = old.available();

        if (patch.has("number")) {
            number = patch.get("number").asText(null);
        }
        if (patch.has("capacity")) {
            capacity = patch.get("capacity").asInt();
        }
        if (patch.has("available")) {
            available = patch.get("available").asBoolean();
        }

        return service.saveRoom(id, number, capacity, available);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        service.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }
}
