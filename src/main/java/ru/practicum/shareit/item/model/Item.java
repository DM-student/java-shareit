package ru.practicum.shareit.item.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Data
@AllArgsConstructor
@Entity
@NoArgsConstructor
@Table(name = "users", schema = "public")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;



    private Long ownerId;

    @ElementCollection
    @CollectionTable(name="users_to_items", joinColumns=@JoinColumn(name="user_id"))
    @Column(name="user_id")
    private void sqlSetOwnerId(List<Long> ownerIds) {
        if (ownerIds == null) {
            return;
        }
        if (ownerIds.isEmpty()) {
            return;
        }
        ownerId = ownerIds.get(0);
    }

    @ElementCollection
    @CollectionTable(name="users_to_items", joinColumns=@JoinColumn(name="user_id"))
    @Column(name="user_id")
    private List<Long> sqlUploadOwnerId() {
        return List.of(ownerId);
    }

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

    // Булево значение "request" я добавлю когда буду работать над add-item-requests.

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
