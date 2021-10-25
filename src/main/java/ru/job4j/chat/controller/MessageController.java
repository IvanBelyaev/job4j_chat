package ru.job4j.chat.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
import ru.job4j.chat.repository.MessageRepository;

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
        return new ResponseEntity<>(message, HttpStatus.OK);
    }

    @GetMapping("/roomId/{roomId}")
    public List<Message> findAllRoomMessages(@PathVariable int roomId) {
        return messageRepository.findByRoomId(roomId);
    }

    @PostMapping("/")
    public ResponseEntity<Message> create(@RequestBody Message message) {
        checkTextOfMessage(message.getText());
        Person author = restTemplate.getForObject("http://localhost:8080/person/" + message.getAuthorId(), Person.class);
        Room room = restTemplate.getForObject("http://localhost:8080/room/" + message.getRoomId(), Room.class);
        return new ResponseEntity<>(
                messageRepository.save(message),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/{id}/text/")
    public ResponseEntity<Void> updateText(@PathVariable int id, @RequestBody String text) {
        checkTextOfMessage(text);
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Message with id = " + id + " not found"
                ));
        message.setText(text);
        messageRepository.save(message);
        return ResponseEntity.ok().build();
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

    private void checkTextOfMessage(String text) {
        if (text == null || text.isEmpty()) {
            throw new IllegalArgumentException("text of message must not be empty");
        }
    }
}
