package com.mylearning.movieinfoservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class MovieInfoNotfoundException extends RuntimeException {
    public MovieInfoNotfoundException(String message) {
        super(message);
    }
}
