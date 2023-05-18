package ru.practicum.shareit.request.model;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * TODO Sprint add-item-requests.
 */

@Data
public class ItemRequest {
    private long id = Long.MIN_VALUE;
    private long ownerId = Long.MIN_VALUE;

    @NotBlank
    private String name;
}
