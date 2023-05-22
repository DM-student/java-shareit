package ru.practicum.shareit.utility;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.practicum.shareit.utility.exceptions.*;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class ErrorHandler {
    @ExceptionHandler
    public ResponseEntity<Map<String, String>> getResponseEntity(ShareItBaseExceptionBaseException e) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        if (e.getClass() == ShareItProvidedDataException.class) {
            status = HttpStatus.BAD_REQUEST;
        }
        if (e.getClass() == ShareItValidationException.class) {
            status = HttpStatus.BAD_REQUEST;
        }
        if (e.getClass() == ShareItNotFoundException.class) {
            status = HttpStatus.NOT_FOUND;
        }
        if (e.getClass() == ShareItConflictException.class) {
            status = HttpStatus.CONFLICT;
        }

        Map<String, String> response = new HashMap<>();
        response.put("error", e.getClass().getSimpleName());
        response.put("error_info", e.getMessage());
        if (e.getData() != null) {
            response.put("error_data", e.getData());
        }
        return new ResponseEntity<>(response, status);
    }
}