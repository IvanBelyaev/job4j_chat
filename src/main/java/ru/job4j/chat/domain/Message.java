package ru.job4j.chat.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import ru.job4j.chat.validation.Operation;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Past;
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
    @Min(value = 0, message = "id must be 0", groups = {Operation.OnCreate.class})
    @Max(value = 0, message = "id must be 0", groups = {Operation.OnCreate.class})
    private int id;

    @Column(nullable = false)
    @NotBlank(message = "Text of message must not be empty",
            groups = {Operation.OnCreate.class})
    private String text;

    @Column(nullable = false)
    @Past(message = "Created must be from the past")
    private LocalDateTime created = LocalDateTime.now();

    @Column(name = "room_id", nullable = false)
    @Min(value = 1, message = "roomId must be greater than 0",
            groups = {Operation.OnCreate.class})
    private int roomId;

    @Column(name = "author_id", nullable = false)
    @Min(value = 1, message = "authorId must be greater than 0",
            groups = {Operation.OnCreate.class})
    private int authorId;

    public Message(String text) {
        this.text = text;
    }
}
