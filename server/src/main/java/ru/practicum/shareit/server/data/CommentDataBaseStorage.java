package ru.practicum.shareit.server.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import ru.practicum.shareit.server.item.model.Comment;

import java.util.List;

@EnableJpaRepositories(basePackages = "ru.practicum.shareit")
public interface CommentDataBaseStorage extends JpaRepository<Comment, Long> {
    @Query(value = "select c " +
            "from Comment as c " +
            "where c.itemId = ?1")
    List<Comment> findCommentsByItemId(Long id);
}
