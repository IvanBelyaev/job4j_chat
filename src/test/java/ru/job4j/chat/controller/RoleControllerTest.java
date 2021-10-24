package ru.job4j.chat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.job4j.chat.ChatApplication;
import ru.job4j.chat.domain.Role;
import ru.job4j.chat.repository.RoleRepository;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ChatApplication.class)
@AutoConfigureMockMvc
public class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoleRepository roleRepository;

    @Autowired
    private ObjectMapper mapper;

    @Test
    @WithMockUser
    public void whenFindAllThenReturnsAllRoles() throws Exception {
        Role user = new Role("user");
        user.setId(1);
        Role admin = new Role("admin");
        admin.setId(2);
        when(roleRepository.findAll()).thenReturn(List.of(user, admin));
        mockMvc.perform(MockMvcRequestBuilders
                .get("/role/")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("user")))
                .andExpect(jsonPath("$[1].name", is("admin")));
    }

    @Test
    @WithMockUser
    public void whenFindByIdWithExistingIDThenReturnsRoleWithThisId() throws Exception {
        Role user = new Role("user");
        user.setId(1);
        when(roleRepository.findById(1)).thenReturn(Optional.of(user));
        mockMvc.perform(MockMvcRequestBuilders
                .get("/role/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("user")))
                .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    @WithMockUser
    public void whenGetRoleByNameThenReturnsRoleWithTheSameName() throws Exception {
        Role user = new Role("user");
        user.setId(1);
        when(roleRepository.findByName("user")).thenReturn(Optional.of(user));
        mockMvc.perform(MockMvcRequestBuilders
                .get("/role/name/user")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("user")))
                .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    @WithMockUser
    public void whenPostRoleThenCreatesNewRole() throws Exception {
        Role user = new Role("user");
        user.setId(1);
        when(roleRepository.save(user)).thenReturn(user);

        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.post("/role/")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(user));

        mockMvc.perform(mockRequest)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("user")))
                .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    @WithMockUser
    public void whenPutRoleThenUpdatesRole() throws Exception {
        Role user = new Role("user");
        user.setId(1);
        when(roleRepository.save(user)).thenReturn(user);

        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.put("/role/")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(user));

        mockMvc.perform(mockRequest)
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    public void whenDeleteRoleThenRoleIsDeleted() throws Exception {
        Role user = new Role("");
        doNothing().when(roleRepository).delete(user);

        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.delete("/role/1")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(mockRequest)
                .andExpect(status().isOk());
    }
}
