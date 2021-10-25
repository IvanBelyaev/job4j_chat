package ru.job4j.chat.service;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.job4j.chat.domain.Person;
import ru.job4j.chat.domain.Role;
import ru.job4j.chat.repository.PersonRepository;
import ru.job4j.chat.repository.RoleRepository;

import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private PersonRepository personRepository;
    private RoleRepository roleRepository;

    public UserDetailsServiceImpl(PersonRepository personRepository, RoleRepository roleRepository) {
        this.personRepository = personRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Person person = personRepository.findByName(username).get();
        if (person == null) {
            throw new UsernameNotFoundException(username);
        }
        Role role = roleRepository.findById(person.getRoleId()).get();
        return new User(person.getName(), person.getPassword(), List.of(role));
    }
}
