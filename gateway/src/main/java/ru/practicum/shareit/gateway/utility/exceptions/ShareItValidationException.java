package ru.practicum.shareit.gateway.utility.exceptions;

public class ShareItValidationException extends ShareItBaseExceptionBaseException {
    public ShareItValidationException(String details, Object data) {
        super(details, data);
    }
}
