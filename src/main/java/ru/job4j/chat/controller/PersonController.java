package ru.job4j.chat.controller;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
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
import ru.job4j.chat.domain.Person;
import ru.job4j.chat.domain.Role;
import ru.job4j.chat.domain.Room;
import ru.job4j.chat.repository.PersonRepository;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Transactional
@RestController
@RequestMapping("/person")
public class PersonController {
    private final PersonRepository personRepository;
    private final RestTemplate restTemplate;
    private final PasswordEncoder encoder;

    public PersonController(PersonRepository personRepository, RestTemplate restTemplate, PasswordEncoder encoder) {
        this.personRepository = personRepository;
        this.restTemplate = restTemplate;
        this.encoder = encoder;
    }

    @GetMapping("/")
    public List<Person> findAll() {
        return StreamSupport.stream(
                this.personRepository.findAll().spliterator(), false
        )
                .map(person -> {
                    List<Room> roomCreatedByPerson = restTemplate.exchange(
                            "http://localhost:8080/room/authorId/" + person.getId(),
                            HttpMethod.GET, null, new ParameterizedTypeReference<List<Room>>() {
                            }
                    ).getBody();
                    person.setRooms(roomCreatedByPerson);
                    return person;
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Person> findById(@PathVariable int id) {
        var person = this.personRepository.findById(id);
        HttpStatus status = HttpStatus.NOT_FOUND;
        if (person.isPresent()) {
            List<Room> roomCreatedByPerson = restTemplate.exchange(
                    "http://localhost:8080/room/authorId/" + person.get().getId(),
                    HttpMethod.GET, null, new ParameterizedTypeReference<List<Room>>() {
                    }
            ).getBody();
            person.get().setRooms(roomCreatedByPerson);
            status = HttpStatus.OK;
        }
        return new ResponseEntity<Person>(person.orElse(new Person()), status);
    }

    /**
     * Creates person with default Role.
     * @param person new person
     * @return person with id. Person has default Role (USER).
     */
    @PostMapping({"/", "/sign-up/"})
    public ResponseEntity<Person> create(@RequestBody Person person) {
        Role userRole = restTemplate.getForObject("http://localhost:8080/role/name/ROLE_USER", Role.class);
        person.setRoleId(userRole.getId());
        person.setPassword(encoder.encode(person.getPassword()));
        return new ResponseEntity<Person>(
                this.personRepository.save(person),
                HttpStatus.CREATED
        );
    }

    /**
     * Updates person's properties except property roleId.
     * @param person person with new properties.
     * @return status of operation.
     */
    @PutMapping("/")
    public ResponseEntity<Void> update(@RequestBody Person person) {
        Person personInDb = restTemplate.getForObject("http://localhost:8080/person/" + person.getId(), Person.class);
        person.setRoleId(personInDb.getRoleId());
        this.personRepository.save(person);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/roleId/")
    public ResponseEntity<Void> changeRole(@PathVariable int id, @RequestBody int roleId) {
        Role roleWithId = restTemplate.getForObject("http://localhost:8080/role/" + roleId, Role.class);
        Person person = restTemplate.getForObject("http://localhost:8080/person/" + id, Person.class);
        person.setRoleId(roleWithId.getId());
        personRepository.save(person);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        Person person = new Person();
        person.setId(id);
        restTemplate.delete("http://localhost:8080/room/authorId/" + person.getId());
        this.personRepository.delete(person);
        return ResponseEntity.ok().build();
    }
}
