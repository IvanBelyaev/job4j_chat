package ru.job4j.chat.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
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
import ru.job4j.chat.dto.MessageDTO;
import ru.job4j.chat.repository.MessageRepository;

import java.time.LocalDateTime;
import java.util.List;

@Transactional
@RestController
@RequestMapping("/message")
public class MessageController {
    private final MessageRepository messageRepository;
    private final RestTemplate restTemplate;

    public MessageController(MessageRepository messageRepository, RestTemplate restTemplate) {
        this.messageRepository = messageRepository;
        this.restTemplate = restTemplate;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Message> findById(@PathVariable int id) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Message with id = " + id + " not found"
                ));
        return ResponseEntity.ok(message);
    }

    @GetMapping("/roomId/{roomId}")
    public ResponseEntity<List<Message>> findAllRoomMessages(@PathVariable int roomId) {
        return ResponseEntity.ok(messageRepository.findByRoomId(roomId));
    }

    @PostMapping("/")
    public ResponseEntity<Message> create(@RequestBody Message message) {
        checkText(message.getText());
        Person author = restTemplate.getForObject("http://localhost:8080/person/" + message.getAuthorId(), Person.class);
        Room room = restTemplate.getForObject("http://localhost:8080/room/" + message.getRoomId(), Room.class);
        return new ResponseEntity<>(
                messageRepository.save(message),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/{id}/text/")
    public ResponseEntity<Void> updateText(@PathVariable int id, @RequestBody String text) {
        checkText(text);
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Message with id = " + id + " not found"
                ));
        message.setText(text);
        messageRepository.save(message);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Message> changeSomeFields(@PathVariable int id, @RequestBody MessageDTO patch) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Message with id = " + id + " not found"
                ));
        String text = patch.getText();
        LocalDateTime created = patch.getCreated();
        int roomId = patch.getRoomId();
        int authorId = patch.getAuthorId();
        if (text != null) {
            checkText(text);
            message.setText(text);
        }
        if (created != null) {
            checkCreated(created);
            message.setCreated(created);
        }
        if (roomId != 0) {
            checkRoomId(roomId);
            message.setRoomId(roomId);
        }
        if (authorId != 0) {
            checkAuthorId(authorId);
            message.setAuthorId(authorId);
        }
        return ResponseEntity.ok(messageRepository.save(message));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        Message message = new Message();
        message.setId(id);
        messageRepository.delete(message);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/roomId/{roomId}")
    public ResponseEntity<Void> deleteAllRoomMessages(@PathVariable int roomId) {
        messageRepository.deleteAllByRoomId(roomId);
        return ResponseEntity.ok().build();
    }

    private void checkText(String text) {
        if (text == null || text.isEmpty()) {
            throw new IllegalArgumentException("text of message must not be empty");
        }
    }

    private void checkCreated(LocalDateTime created) {
        if (created.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Created must be from the past");
        }
    }

    private void checkRoomId(int roomId) {
        restTemplate.getForObject("http://localhost:8080/room/" + roomId, Room.class);
    }

    private void checkAuthorId(int authorId) {
        restTemplate.getForObject("http://localhost:8080/person/" + authorId, Person.class);
    }
}
