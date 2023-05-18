package ru.practicum.shareit.utility.exceptions;

public class ProvidedDataExceptionForHandler extends BaseExceptionForHandler {
    public ProvidedDataExceptionForHandler(String details, Object data) {
        super(details, data);
    }
}
