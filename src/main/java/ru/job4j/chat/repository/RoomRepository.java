package ru.job4j.chat.repository;

import org.springframework.data.repository.CrudRepository;
import ru.job4j.chat.domain.Room;

import java.util.List;

public interface RoomRepository extends CrudRepository<Room, Integer> {
    List<Room> findByAuthorId(int authorId);
    void deleteAllByAuthorId(int authorId);
}
