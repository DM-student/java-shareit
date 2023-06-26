package ru.practicum.shareit.gateway.utility.exceptions;

public class ShareItProvidedDataException extends ShareItBaseExceptionBaseException {
    public ShareItProvidedDataException(String details, Object data) {
        super(details, data);
    }
}
