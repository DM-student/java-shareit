package ru.practicum.shareit.user.model;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

@Data
public class User {
    private Long id;

    private Set<Long> itemsIds = new HashSet<>();

    @NotBlank(message = "Имя пустое!")
    @Size(max = 32, message = "Имя слишком длинное!")
    private String name;

    @NotBlank(message = "Адрес почты пуст!")
    @Email(message = "Адрес почты указан не верно!")
    @Size(max = 320, message = "Адрес почты слишком длинный!")
    private String email;

    public User(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public void mergeFrom(User otherUser) {
        if (otherUser.id != null) {
            id = otherUser.id;
        }
        if (otherUser.itemsIds != null) {
            itemsIds = otherUser.itemsIds;
        }
        if (otherUser.name != null) {
            name = otherUser.name;
        }
        if (otherUser.email != null) {
            email = otherUser.email;
        }
    }
}
