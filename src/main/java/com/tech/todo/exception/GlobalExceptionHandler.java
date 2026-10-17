package com.tech.todo.exception;

import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TodoApiException.class)
    public ResponseEntity<ErrorDetails> handleTodoApiException(TodoApiException todoApiException,
        WebRequest webRequest) {

        ErrorDetails errorDetails = new ErrorDetails(
            LocalDateTime.now(),
            todoApiException.getMessage(),
            webRequest.getDescription(false)
        );

        HttpStatus status = todoApiException.getHttpStatus() != null
            ? todoApiException.getHttpStatus()
            : HttpStatus.BAD_REQUEST;
        return new ResponseEntity<>(errorDetails, status);
    }

}
