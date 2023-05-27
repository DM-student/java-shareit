package ru.practicum.shareit.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import ru.practicum.shareit.user.model.User;

@EnableJpaRepositories(basePackages = "ru.practicum.shareit")
public interface UserDataBaseStorage extends JpaRepository<User, Long> {
}
