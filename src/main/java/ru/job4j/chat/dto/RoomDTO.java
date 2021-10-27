package ru.job4j.chat.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RoomDTO {
    private String name;
    private LocalDateTime created;
    private int authorId;
}

