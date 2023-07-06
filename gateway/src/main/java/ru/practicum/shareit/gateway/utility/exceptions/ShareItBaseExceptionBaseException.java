package ru.practicum.shareit.gateway.utility.exceptions;

public class ShareItBaseExceptionBaseException extends RuntimeException {
    private final String errorInfo;
    private String errorData; // Если я его сделаю файнал - он будет ТРЕБОВАТЬ определения этой переменной,
    // по этому у меня и стоял там редундантный else, цель которого была просто "успокоить" компилятор и IDE.

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
        }
    }
}
