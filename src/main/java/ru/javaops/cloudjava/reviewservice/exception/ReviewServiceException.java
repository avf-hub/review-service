package ru.javaops.cloudjava.reviewservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ReviewServiceException extends RuntimeException {
    private final HttpStatus status;

    public ReviewServiceException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
