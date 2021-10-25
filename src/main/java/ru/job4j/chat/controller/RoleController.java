package ru.job4j.chat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.job4j.chat.domain.Role;
import ru.job4j.chat.exception.ThereIsNoRoleWithThisNameException;
import ru.job4j.chat.repository.RoleRepository;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Transactional
@RestController
@RequestMapping("/role")
public class RoleController {
    private final RoleRepository roleRepository;
    private final ObjectMapper objectMapper;

    public RoleController(final RoleRepository roleRepository, final ObjectMapper objectMapper) {
        this.roleRepository = roleRepository;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/")
    public List<Role> findAll() {
        return StreamSupport.stream(
                this.roleRepository.findAll().spliterator(), false
        ).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Role> findById(@PathVariable int id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Role with id = " + id + " not found"
                ));
        return new ResponseEntity<>(role, HttpStatus.OK);
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<Role> getRoleByName(@PathVariable String name) {
        Role role = roleRepository.findByName(name)
                .orElseThrow(() -> new ThereIsNoRoleWithThisNameException("There is no role named " + name));
        return new ResponseEntity<Role>(role, HttpStatus.OK);
    }

    @PostMapping("/")
    public ResponseEntity<Role> create(@RequestBody Role role) {
        checkRole(role);
        return new ResponseEntity<Role>(
                this.roleRepository.save(role),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/")
    public ResponseEntity<Void> update(@RequestBody Role role) {
        checkRole(role);
        this.roleRepository.save(role);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        Role role = new Role();
        role.setId(id);
        this.roleRepository.delete(role);
        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(ThereIsNoRoleWithThisNameException.class)
    public void handleException(Exception e, HttpServletResponse resp) throws IOException {
        resp.setStatus(HttpStatus.BAD_REQUEST.value());
        resp.setContentType("application/json");
        resp.getWriter().write(objectMapper.writeValueAsString(
                new HashMap<>() { {
                    put("message", e.getMessage());
                    put("type", e.getClass());
                } }));
    }

    private void checkRole(Role role) {
        if (role.getName() == null || role.getName().isEmpty()) {
            throw new IllegalArgumentException("Name of role must not be empty");
        }
        if (roleRepository.findByName(role.getName()).isPresent()) {
            throw new IllegalArgumentException("Name of role " + role.getName() + " already exists");
        }
    }
}
