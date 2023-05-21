package ru.practicum.shareit.utility.exceptions;

public class BaseExceptionForHandler extends RuntimeException {
    private final String errorInfo;
    private final String errorData;

    @Override
    public String getMessage() {
        if (errorInfo == null) {
            return super.getMessage();
        }
        return errorInfo;
    }

    public String getData() {
        return errorData;
    }

    public BaseExceptionForHandler(String info, Object data) {
        errorInfo = info;
        if (data != null) {
            errorData = data.toString();
        } else {
            errorData = null;
        }
    }
}
