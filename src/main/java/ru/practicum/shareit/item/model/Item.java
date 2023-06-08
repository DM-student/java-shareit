package ru.practicum.shareit.item.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@Entity
@NoArgsConstructor
@Table(name = "items")
@SecondaryTable(name = "users_to_items", pkJoinColumns = @PrimaryKeyJoinColumn(name = "item_id"))
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(table = "users_to_items", name = "user_id")
    private Long ownerId;

    @Column(name = "name")
    @NotBlank(message = "Название пустое!")
    @Size(max = 32, message = "Название слишком длинное!")
    private String name;

    @Column(name = "description")
    @NotBlank
    @Size(max = 378, message = "Описание слишком длинное!")
    private String description;

    @Column(name = "available")
    @NotNull(message = "Параметр доступности обязан присутствовать!")
    private Boolean available;

    // Хибернейт делает свои особые классы для объектов, из-за чего некоторые штуки ломаются.
    public Item getClearCopy() {
        return new Item(id, ownerId, name, description, available);
    }


    public void mergeFrom(Item otherItem) {
        if (otherItem.id != null) {
            id = otherItem.id;
        }
        if (otherItem.ownerId != null) {
            ownerId = otherItem.ownerId;
        }
        if (otherItem.name != null) {
            name = otherItem.name;
        }
        if (otherItem.description != null) {
            description = otherItem.description;
        }
        if (otherItem.available != null) {
            available = otherItem.available;
        }
    }
}
