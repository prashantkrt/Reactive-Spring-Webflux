package com.mylearning.movieinfoservice.exceptionhandler;

import com.mylearning.movieinfoservice.exception.MovieInfoNotfoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

//    @ExceptionHandler(value = WebExchangeBindException.class)
//    public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValidException(WebExchangeBindException ex) {
//        Map<String, Object> map = new HashMap<>();
//        map.put("status", HttpStatus.BAD_REQUEST.value());
//        map.put("timeStamp", System.currentTimeMillis());
//
//        ex.getBindingResult().getFieldErrors().forEach(fieldError -> {
//            map.put("errors", fieldError.getField() + ":" + fieldError.getDefaultMessage());
//        });
//        return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
//    }

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<String> handleRequestBodyError(WebExchangeBindException ex){
        log.error("Exception caught in handleRequestBodyError :  {} ", ex.getMessage(), ex);

        String errors = ex.getBindingResult().getAllErrors().stream()
                .map(error -> error.getDefaultMessage())
                .sorted()
                .collect(Collectors.joining(","));

        log.error("errorList : {}", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

//    @ExceptionHandler(value = MovieInfoNotfoundException.class)
//    public ResponseEntity<Map<String, Object>> handleMovieInfoNotfoundException(MovieInfoNotfoundException ex) {
//        Map<String, Object> map = new HashMap<>();
//        map.put("status", HttpStatus.NOT_FOUND.value());
//        map.put("timeStamp", System.currentTimeMillis());
//        map.put("errors", ex.getMessage());
//        return new ResponseEntity<>(map, HttpStatus.NOT_FOUND);
//    }

    @ExceptionHandler(MovieInfoNotfoundException.class)
    public ResponseEntity<String> handleMovieInfoNotfoundException(MovieInfoNotfoundException ex){
        log.error("Exception caught in handleMovieInfoNotfoundException :  {} " ,ex.getMessage(),  ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }


    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception ex) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        map.put("timeStamp", System.currentTimeMillis());
        map.put("errors", ex.getMessage());
        return new ResponseEntity<>(map, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
