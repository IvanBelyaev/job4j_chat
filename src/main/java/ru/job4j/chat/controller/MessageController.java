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
import ru.job4j.chat.domain.Message;
import ru.job4j.chat.domain.Person;
import ru.job4j.chat.domain.Room;
import ru.job4j.chat.repository.MessageRepository;

import java.util.List;
import java.util.Optional;

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
        Optional<Message> message = messageRepository.findById(id);
        return new ResponseEntity<>(
                message.orElse(new Message()),
                message.isPresent() ? HttpStatus.OK : HttpStatus.NOT_FOUND
        );
    }

    @GetMapping("/roomId/{roomId}")
    public List<Message> findAllRoomMessages(@PathVariable int roomId) {
        return messageRepository.findByRoomId(roomId);
    }

    @PostMapping("/")
    public ResponseEntity<Message> create(@RequestBody Message message) {
        Person author = restTemplate.getForObject("http://localhost:8080/person/" + message.getAuthorId(), Person.class);
        Room room = restTemplate.getForObject("http://localhost:8080/room/" + message.getRoomId(), Room.class);
        return new ResponseEntity<>(
                messageRepository.save(message),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/{id}/text/")
    public ResponseEntity<Void> updateText(@PathVariable int id, @RequestBody String text) {
        Message message = messageRepository.findById(id).get();
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
}
