package ru.job4j.chat.repository;

import org.springframework.data.repository.CrudRepository;
import ru.job4j.chat.domain.Person;

public interface PersonRepository extends CrudRepository<Person, Integer> {
}
