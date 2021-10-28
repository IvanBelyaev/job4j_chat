package ru.job4j.chat.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import ru.job4j.chat.validation.Operation;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Entity
@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Role implements GrantedAuthority {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Min(value = 0, message = "id must be 0", groups = {Operation.OnCreate.class})
    @Max(value = 0, message = "id must be 0", groups = {Operation.OnCreate.class})
    @Min(value = 1, message = "id must be greater than 0", groups = {Operation.OnUpdate.class})
    private int id;

    @Column(nullable = false, unique = true)
    @NotBlank(message = "name must not be empty",
            groups = {Operation.OnCreate.class, Operation.OnUpdate.class})
    private String name;

    public Role(String name) {
        this.name = name;
    }

    @Override
    public String getAuthority() {
        return name;
    }
}
