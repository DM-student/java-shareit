package ru.practicum.shareit.server.utility.exceptions;

public class ShareItValidationException extends ShareItBaseExceptionBaseException {
    public ShareItValidationException(String details, Object data) {
        super(details, data);
    }
}
