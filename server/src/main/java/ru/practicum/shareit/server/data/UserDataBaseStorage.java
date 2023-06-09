package ru.practicum.shareit.server.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import ru.practicum.shareit.server.user.model.User;

@EnableJpaRepositories(basePackages = "ru.practicum.shareit")
public interface UserDataBaseStorage extends JpaRepository<User, Long> {
    @Query(value = "select u " +
            "from User as u " +
            "where u.email = ?1 " +
            "and not u.id = ?2")
    User findOtherUserWithSameEmail(String email, long idToExclude);

    @Query(value = "select u " +
            "from User as u " +
            "where u.email = ?1")
    User findUserByEmail(String email);
}
