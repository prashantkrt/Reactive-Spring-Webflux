package com.mylearning.movieinfoservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class MovieInfoException extends RuntimeException {
    public MovieInfoException(String message) {
        super(message);
    }
}
