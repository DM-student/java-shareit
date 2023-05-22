package ru.practicum.shareit.utility.exceptions;

public class ShareItValidationException extends ShareItBaseExceptionBaseException {
    public ShareItValidationException(String details, Object data) {
        super(details, data);
    }
}
