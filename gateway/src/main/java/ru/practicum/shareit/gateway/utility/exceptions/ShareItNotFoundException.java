package ru.practicum.shareit.gateway.utility.exceptions;

public class ShareItNotFoundException extends ShareItBaseExceptionBaseException {
    public ShareItNotFoundException(String details, Object data) {
        super(details, data);
    }
}
