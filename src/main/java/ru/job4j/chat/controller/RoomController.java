package ru.job4j.chat.controller;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import ru.job4j.chat.domain.Message;
import ru.job4j.chat.domain.Person;
import ru.job4j.chat.domain.Room;
import ru.job4j.chat.dto.RoomDTO;
import ru.job4j.chat.repository.RoomRepository;
import ru.job4j.chat.validation.Operation;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Transactional
@RestController
@RequestMapping("/room")
@Validated
public class RoomController {
    private final RoomRepository roomRepository;
    private final RestTemplate restTemplate;

    public RoomController(RoomRepository roomRepository, RestTemplate restTemplate) {
        this.roomRepository = roomRepository;
        this.restTemplate = restTemplate;
    }

    @GetMapping("/")
    public ResponseEntity<List<Room>> findAll() {
        List<Room> rooms = StreamSupport.stream(roomRepository.findAll().spliterator(), false)
                .collect(Collectors.toList());
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Room> findById(@PathVariable int id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Room with id = " + id + " not found"
                ));
        List<Message> messages = restTemplate.exchange(
                "http://localhost:8080/message/roomId/" + id,
                HttpMethod.GET, null, new ParameterizedTypeReference<List<Message>>() { })
                .getBody();
        room.setMessages(messages);
        return ResponseEntity.ok(room);
    }

    @GetMapping("/authorId/{authorId}")
    public ResponseEntity<List<Room>> findAllRoomCreatedByUser(@PathVariable int authorId) {
        return ResponseEntity.ok(roomRepository.findByAuthorId(authorId));
    }

    @PostMapping("/")
    @Validated(Operation.OnCreate.class)
    public ResponseEntity<Room> create(@Valid @RequestBody Room room) {
        checkName(room.getName());
        restTemplate.getForObject("http://localhost:8080/person/" + room.getAuthorId(), Person.class);
        return new ResponseEntity<>(
                roomRepository.save(room),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/{id}/name/")
    public ResponseEntity<Void> updateName(
            @PathVariable int id,
            @Valid @NotBlank(message = "name of room must not be empty") @RequestBody String name) {
        checkName(name);
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Room with id = " + id + " not found"
                ));
        room.setName(name);
        roomRepository.save(room);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Room> changeSomeFields(@PathVariable int id, @RequestBody RoomDTO patch) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Room with id = " + id + " not found"
                ));
        String name = patch.getName();
        LocalDateTime created = patch.getCreated();
        int authorId = patch.getAuthorId();
        if (name != null) {
            checkName(name);
            room.setName(name);
        }
        if (created != null) {
            checkCreated(created);
            room.setCreated(created);
        }
        if (authorId != 0) {
            checkAuthorId(authorId);
            room.setAuthorId(authorId);
        }
        return ResponseEntity.ok(roomRepository.save(room));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable int id) {
        Room room = new Room();
        room.setId(id);
        restTemplate.delete("http://localhost:8080/message/roomId/" + id);
        roomRepository.delete(room);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/authorId/{authorId}")
    public ResponseEntity<Void> deleteAllUserRooms(@PathVariable int authorId) {
        roomRepository.findByAuthorId(authorId).stream().forEach(
                room -> {
                    restTemplate.delete("http://localhost:8080/message/roomId/" + room.getId());
                }
        );
        roomRepository.deleteAllByAuthorId(authorId);
        return ResponseEntity.ok().build();
    }

    private void checkName(String roomName) {
        if (roomName == null || roomName.isEmpty()) {
            throw new IllegalArgumentException("Name of room must not be empty");
        }
        if (roomRepository.findByName(roomName).isPresent()) {
            throw new IllegalArgumentException("Room with name " + roomName + " already exists");
        }
    }

    private void checkCreated(LocalDateTime created) {
        if (created.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Created must be from the past");
        }
    }

    private void checkAuthorId(int authorId) {
        restTemplate.getForObject("http://localhost:8080/person/" + authorId, Person.class);
    }
}
