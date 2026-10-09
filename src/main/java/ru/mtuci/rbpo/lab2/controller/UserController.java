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

import ru.mtuci.rbpo.lab2.dto.UserRequest;
import ru.mtuci.rbpo.lab2.model.User;
import ru.mtuci.rbpo.lab2.service.HousingService;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final HousingService service;

    public UserController(HousingService service) {
        this.service = service;
    }

    @GetMapping
    public List<User> list() {
        return service.users();
    }

    @GetMapping("/{id}")
    public User get(@PathVariable long id) {
        return service.user(id);
    }

    @PostMapping
    public ResponseEntity<User> create(@RequestBody UserRequest r) {
        User result = service.saveUser(null, r.name());
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PatchMapping("/{id}")
    public User update(@PathVariable long id, @RequestBody ObjectNode patch) {
        User old = service.user(id);
        String name = old.name();

        if (patch.has("name")) {
            name = patch.get("name").asText(null);
        }

        return service.saveUser(id, name);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        service.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
