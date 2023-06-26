package ru.practicum.shareit.server.utility.exceptions;

public class ShareItNotFoundException extends ShareItBaseExceptionBaseException {
    public ShareItNotFoundException(String details, Object data) {
        super(details, data);
    }
}
