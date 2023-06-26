package ru.practicum.shareit.gateway.utility.exceptions;

public class ShareItBaseExceptionBaseException extends RuntimeException {
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

    public ShareItBaseExceptionBaseException(String info, Object data) {
        errorInfo = info;
        if (data != null) {
            errorData = data.toString();
        } else {
            errorData = null;
        }
    }
}
