package ru.practicum.shareit.user.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@NoArgsConstructor
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ElementCollection
    @CollectionTable(name = "users_to_items", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "item_id")
    private Set<Long> itemsIds = new HashSet<>();

    @Column(name = "name")
    @NotBlank(message = "Имя пустое!")
    @Size(max = 32, message = "Имя слишком длинное!")
    private String name;

    @Column(name = "email", unique = true)
    @NotBlank(message = "Адрес почты пуст!")
    @Email(message = "Адрес почты указан неверно!")
    @Size(max = 320, message = "Адрес почты слишком длинный!")
    private String email;

    public User getClearCopy() {
        User user = new User(id, name, email);
        user.itemsIds = this.itemsIds;
        return user;
    }

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
