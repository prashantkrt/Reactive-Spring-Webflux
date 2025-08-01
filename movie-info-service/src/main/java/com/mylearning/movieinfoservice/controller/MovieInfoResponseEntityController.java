package com.mylearning.movieinfoservice.controller;

import com.mylearning.movieinfoservice.model.MovieInfo;
import com.mylearning.movieinfoservice.service.MovieInfoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/movies/response-entity")
public class MovieInfoResponseEntityController {

    private final MovieInfoService movieInfoService;

    public MovieInfoResponseEntityController(MovieInfoService movieInfoService) {
        this.movieInfoService = movieInfoService;
    }

    @PostMapping("/addMovieInfos")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<MovieInfo> addMovieInfo(@RequestBody(required = true) @Valid MovieInfo movieInfo) {
        return movieInfoService.addMovieInfo(movieInfo);
    }

    @GetMapping("/getMovieInfos")
    @ResponseStatus(HttpStatus.OK)
    public Flux<MovieInfo> getMovieInfos() {
        return movieInfoService.getMovieInfos();
    }

    @GetMapping("/getMovieInfo/{movieId}")
    public Mono<MovieInfo> getMovieInfo(@PathVariable String movieId) {
        return movieInfoService.getMovieInfo(movieId);
    }

    @DeleteMapping("/deleteMovieInfo/{movieId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteMovieInfo(@PathVariable String movieId) {
        return movieInfoService.deleteMovieInfo(movieId);
    }

    @PutMapping("/updateMovieInfo")
    public Mono<MovieInfo> updateMovieInfo(@RequestBody(required = true) @Valid MovieInfo movieInfo) {
        return movieInfoService.updateMovieInfo(movieInfo);
    }

}
