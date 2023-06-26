package ru.practicum.shareit.server.item.dto;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.server.data.UserDataBaseStorage;
import ru.practicum.shareit.server.item.model.Comment;

@Component
public class CommentDtoMapper implements ApplicationContextAware, InitializingBean {
    private UserDataBaseStorage userStorage;

    public CommentDto mapToDto(Comment comment) {
        return new CommentDto(comment.getId(), comment.getText(),
                userStorage.findById(comment.getUserId()).get().getName(), comment.getCreated());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        userStorage = context.getBean(UserDataBaseStorage.class);
    }

    private ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        context = ctx;
    }
}
