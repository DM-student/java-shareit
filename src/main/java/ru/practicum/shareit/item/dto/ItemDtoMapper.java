package ru.practicum.shareit.item.dto;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.data.UserDataBaseStorage;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserDtoMapper;

@Component
public class ItemDtoMapper implements ApplicationContextAware, InitializingBean {
    @Autowired
    UserDataBaseStorage userStorage;

    UserDtoMapper userMapper;

    public ItemDto mapToDto(Item item, boolean fullData) {
        ItemDto itemDto = new ItemDto(item.getId(), null, item.getName(),
                item.getDescription(), item.getAvailable(), fullData);
        if (fullData && item.getOwnerId() != null) {
            UserDto user = userMapper.mapToDto(userStorage.findById(item.getOwnerId()).get(), false);
            itemDto.setOwner(user);
        }
        return itemDto;
    }

    public Item mapFromDto(ItemDto itemDto) {
        Item item = new Item(itemDto.getId(), null, itemDto.getName(),
                itemDto.getDescription(), itemDto.getAvailable());
        if (itemDto.getOwner() == null) {
            return item;
        }
        item.setOwnerId(itemDto.getOwner().getId());
        return item;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        userMapper = context.getBean(UserDtoMapper.class);
    }

    private ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        context = ctx;
    }
}
