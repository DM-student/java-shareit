package ru.practicum.shareit.item.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.logging.log4j.message.Message;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
public class Item {
    private Long id;

    @NotNull(message = "Нет владельца!")
    private Long ownerId;

    @NotBlank(message = "Название пустое!")
    @Size(max = 32, message = "Название слишком длинное!")
    private String name;

    @NotBlank
    @Size(max = 378, message = "Описание слишком длинное!")
    private String description;

    @NotNull(message = "Параметр доступности обязан присутствовать!")
    private Boolean available;

    public void mergeFrom(Item otherItem) {
        if(otherItem.id != null) {
            id = otherItem.id;
        }
        if(otherItem.ownerId != null) {
            ownerId = otherItem.ownerId;
        }
        if(otherItem.name != null) {
            name = otherItem.name;
        }
        if(otherItem.description != null) {
            description = otherItem.description;
        }
        if(otherItem.available != null) {
            available = otherItem.available;
        }
    }
}
