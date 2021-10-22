package ru.job4j.chat.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.client.RestTemplate;
import ru.job4j.chat.ChatApplication;
import ru.job4j.chat.domain.Person;
import ru.job4j.chat.domain.Room;
import ru.job4j.chat.repository.PersonRepository;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ChatApplication.class)
@AutoConfigureMockMvc
public class PersonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PersonRepository personRepository;

    @MockBean
    private RestTemplate restTemplate;

    @Test
    public void whenFindAllThenReturnsAllPersons() throws Exception {
        Person sergey = new Person("Sergey");
        sergey.setId(1);
        sergey.setRoleId(1);
        Person oleg = new Person("Oleg");
        oleg.setId(2);
        oleg.setRoleId(5);
        when(personRepository.findAll()).thenReturn(List.of(sergey, oleg));
        when(restTemplate.exchange(
                any(String.class),
                eq(HttpMethod.GET),
                eq(null),
                eq(new ParameterizedTypeReference<List<Room>>() {}))
        )
                .thenReturn(new ResponseEntity<>(List.of(new Room("First room")), HttpStatus.OK));
        mockMvc.perform(MockMvcRequestBuilders
                .get("/person/")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Sergey")))
                .andExpect(jsonPath("$[1].name", is("Oleg")))
                .andExpect(jsonPath("$[1].roleId", is(5)))
                .andExpect(jsonPath("$[1].rooms[0].name", is("First room")));
    }

    @Test
    public void whenFindByIdWithExistingIDThenReturnsPersonWithThisId() throws Exception {
        Person oleg = new Person("Oleg");
        oleg.setId(1);
        oleg.setRoleId(5);
        when(personRepository.findById(1)).thenReturn(Optional.of(oleg));
        when(restTemplate.exchange(
                any(String.class),
                eq(HttpMethod.GET),
                eq(null),
                eq(new ParameterizedTypeReference<List<Room>>() {}))
        )
                .thenReturn(new ResponseEntity<>(List.of(new Room("First room")), HttpStatus.OK));
        mockMvc.perform(MockMvcRequestBuilders
                .get("/person/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Oleg")))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.roleId", is(5)))
                .andExpect(jsonPath("$.rooms[0].name", is("First room")));
    }
}
