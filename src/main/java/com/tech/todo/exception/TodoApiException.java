package com.tech.todo.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
@Setter
public class TodoApiException extends RuntimeException{

    private HttpStatus httpStatus;
    private String message;

}
