package com.mylearning.movieservice.exception;

import lombok.Data;

@Data
public class MoviesInfoServerException extends RuntimeException {

    private String message;
    private Integer statusCode;

    public MoviesInfoServerException(String message) {
        super(message);
        this.message = message;
    }

    public MoviesInfoServerException(String message, Integer statusCode) {
        super(message);
        this.message = message;
        this.statusCode = statusCode;
    }
}
