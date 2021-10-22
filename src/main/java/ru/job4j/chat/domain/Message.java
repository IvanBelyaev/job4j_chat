package ru.job4j.chat.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private int id;

    @Column(nullable = false)
    private String text;

    @Column(nullable = false)
    private LocalDateTime created = LocalDateTime.now();

    @Column(name = "room_id", nullable = false)
    private int roomId;

    @Column(name = "author_id", nullable = false)
    private int authorId;

    public Message(String text) {
        this.text = text;
    }
}
