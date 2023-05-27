package ru.practicum.shareit.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import ru.practicum.shareit.item.model.Item;

@EnableJpaRepositories(basePackages = "ru.practicum.shareit")
public interface ItemDataBaseStorage extends JpaRepository<Item, Long> {
}
