package ru.practicum.shareit.utility.exceptions;

public class ShareItProvidedDataException extends ShareItBaseExceptionBaseException {
    public ShareItProvidedDataException(String details, Object data) {
        super(details, data);
    }
}
