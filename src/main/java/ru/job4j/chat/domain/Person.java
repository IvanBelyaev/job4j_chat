package ru.job4j.chat.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import ru.job4j.chat.validation.Operation;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Past;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Entity
@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Min(value = 0, message = "id must be 0", groups = {Operation.OnCreate.class})
    @Max(value = 0, message = "id must be 0", groups = {Operation.OnCreate.class})
    @Min(value = 1, message = "id must be greater than 0", groups = {Operation.OnUpdate.class})
    private int id;

    @Column(nullable = false, unique = true)
    @NotBlank(message = "Person's name must not be empty",
            groups = {Operation.OnCreate.class, Operation.OnUpdate.class})
    private String name;

    @Column(nullable = false)
    @NotBlank(message = "Password must not be empty", groups = {Operation.OnCreate.class, Operation.OnUpdate.class})
    @Length(min = 5, message = "Password must be more than 4 characters", groups = {Operation.OnCreate.class, Operation.OnUpdate.class})
    private String password;

    @Column(nullable = false)
    @Past(message = "Created must be from the past", groups = {Operation.OnCreate.class, Operation.OnUpdate.class})
    private LocalDateTime created = LocalDateTime.now();

    @Column(name = "role_id", nullable = false)
    private int roleId;

    @Transient
    private List<Room> rooms = new CopyOnWriteArrayList<>();

    public Person(String name) {
        this.name = name;
    }
}
