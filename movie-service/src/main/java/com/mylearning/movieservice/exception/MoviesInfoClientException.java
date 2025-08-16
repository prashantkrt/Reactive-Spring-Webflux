package com.mylearning.movieservice.exception;

import lombok.Data;

@Data
public class MoviesInfoClientException extends RuntimeException {
    private String message;
    private Integer statusCode;

    public MoviesInfoClientException(String message, Integer statusCode) {
        super(message);
        this.message = message;
        this.statusCode = statusCode;
    }

}
