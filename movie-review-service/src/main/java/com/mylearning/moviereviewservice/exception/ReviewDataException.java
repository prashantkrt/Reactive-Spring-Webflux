package com.mylearning.moviereviewservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ReviewDataException extends RuntimeException {
    public ReviewDataException(String message) {
        super(message);
    }
}
