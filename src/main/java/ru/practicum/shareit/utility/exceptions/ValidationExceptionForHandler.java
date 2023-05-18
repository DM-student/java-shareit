package ru.practicum.shareit.utility.exceptions;

public class ValidationExceptionForHandler extends BaseExceptionForHandler {
    public ValidationExceptionForHandler(String details, Object data) {
        super(details, data);
    }
}
