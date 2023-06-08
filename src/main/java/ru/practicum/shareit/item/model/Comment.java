package ru.practicum.shareit.item.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Entity
@NoArgsConstructor
@Table(name = "comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    @NotNull(message = "Id пользователя не был указан.")
    private Long userId;

    @NotNull(message = "Id предмета не был указан.")
    @Column(name = "item_id")
    private Long itemId;

    @NotBlank(message = "Текст пуст.")
    @Column(name = "text")
    private String text;

    @Column(name = "created")
    @NotNull(message = "Дата отсутствует.")
    private LocalDateTime created;
}
