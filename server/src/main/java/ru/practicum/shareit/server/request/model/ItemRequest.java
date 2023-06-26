package ru.practicum.shareit.server.request.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@Entity
@NoArgsConstructor
@Table(name = "requests")
public class ItemRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    @NotNull(message = "Id пользователя не был указан.")
    private Long userId;

    @ElementCollection
    @CollectionTable(name = "requests_to_items", joinColumns = @JoinColumn(name = "request_id"))
    @Column(name = "item_id")
    private Set<Long> itemsIds = new HashSet<>();

    @NotBlank(message = "Описание запроса пустое.")
    @Column(name = "description")
    private String description;

    @NotNull(message = "Дата создания не указана.")
    @Column(name = "created")
    private LocalDateTime created;

    public ItemRequest getClearCopy() {
        return new ItemRequest(id, userId, itemsIds, description, created);
    }

    public void mergeFrom(ItemRequest otherRequest) {
        if (otherRequest.id != null) {
            id = otherRequest.id;
        }
        if (otherRequest.userId != null) {
            userId = otherRequest.userId;
        }
        if (otherRequest.itemsIds != null) {
            itemsIds = otherRequest.itemsIds;
        }
        if (otherRequest.created != null) {
            created = otherRequest.created;
        }
        if (otherRequest.description != null) {
            description = otherRequest.description;
        }
    }
}
