package ru.job4j.chat.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageDTO {
    private String text;
    private LocalDateTime created;
    private int roomId;
    private int authorId;
}

