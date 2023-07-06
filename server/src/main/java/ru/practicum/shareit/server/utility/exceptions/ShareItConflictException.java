package ru.practicum.shareit.server.utility.exceptions;

public class ShareItConflictException extends ShareItBaseExceptionBaseException {
    public ShareItConflictException(String details, Object data) {
        super(details, data);
    }
}
