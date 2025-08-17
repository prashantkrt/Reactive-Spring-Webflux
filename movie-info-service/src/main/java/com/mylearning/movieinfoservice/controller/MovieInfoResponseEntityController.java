package com.mylearning.movieinfoservice.controller;

import com.mylearning.movieinfoservice.exception.MovieInfoException;
import com.mylearning.movieinfoservice.exception.MovieInfoNotfoundException;
import com.mylearning.movieinfoservice.model.MovieInfo;
import com.mylearning.movieinfoservice.service.MovieInfoService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@RestController
@RequestMapping("/api/v1/movies/response-entity")
@Slf4j
public class MovieInfoResponseEntityController {

    private final MovieInfoService movieInfoService;

    //event streaming
    Sinks.Many<MovieInfo> movieInfoSink = Sinks.many().replay().latest();

    public MovieInfoResponseEntityController(MovieInfoService movieInfoService) {
        this.movieInfoService = movieInfoService;
    }

    @PostMapping("/addMovieInfos")
    public Mono<ResponseEntity<MovieInfo>> addMovieInfo(@RequestBody @Valid MovieInfo movieInfo) {
        return movieInfoService
                .addMovieInfo(movieInfo)
                .doOnNext(info -> log.info("Fetched movie: {}", info.getName()))
                .doOnNext(info -> movieInfoSink.tryEmitNext(info)) // SSE => Server Sent Events ,PRODUCES => emit to all subscribers
                .map(saved -> ResponseEntity.status(HttpStatus.CREATED).body(saved))
                .doOnSuccess(resp -> log.info("Returned {} products",
                        resp.getBody()))
                .switchIfEmpty(Mono.error(new MovieInfoException("Unable to add movie")))
                .log(); // keep for debugging if needed
    }

    // SSE => Server Sent Events controller
    //@GetMapping(value = "/movieInfos/stream", produces = MediaType.APPLICATION_NDJSON_VALUE) // for the other microservices
    @GetMapping(value = "/movieInfos/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE) // for the browser or postman
    public Flux<MovieInfo> streamMovieInfos() {
        return movieInfoSink.asFlux();
    }

    @GetMapping("/getMovieInfos")
    public Flux<ResponseEntity<MovieInfo>> getMovieInfos() {
        return movieInfoService.getMovieInfos()
                .map(movieInfo -> ResponseEntity.ok().body(movieInfo))
                .switchIfEmpty(Mono.error(new MovieInfoNotfoundException("Not found"))) //   This exception will propagate to your Global Exception Handler
                .doOnComplete(() -> log.info("All movie infos fetched successfully."))
                .log();
    }

    @GetMapping("/getMovieInfo/{movieId}")
    public Mono<ResponseEntity<MovieInfo>> getMovieInfo(@PathVariable String movieId) {
        return movieInfoService.getMovieInfo(movieId)
                .map(movieInfo -> ResponseEntity.ok().body(movieInfo))
                .switchIfEmpty(Mono.error(new MovieInfoNotfoundException("Not found")))
                .doOnSuccess(resp -> log.info(" Returned {} products", resp.getBody()))
                .log();
    }

    @DeleteMapping("/deleteMovieInfo/{movieId}")
    public Mono<ResponseEntity<Void>> deleteMovieInfo(@PathVariable String movieId) {
        return movieInfoService.deleteMovieInfo(movieId)
                .then(Mono.just(ResponseEntity.noContent().build())); // 204 No Content
    }

    @PutMapping("/updateMovieInfo")
    public Mono<ResponseEntity<MovieInfo>> updateMovieInfo(@RequestBody(required = true) @Valid MovieInfo movieInfo) {
        if (movieInfo.getMovieId() == null) {
            return Mono.just(ResponseEntity.badRequest().build());
        }
        return movieInfoService.updateMovieInfo(movieInfo)
                .map(savedMovieInfo -> ResponseEntity.ok().body(savedMovieInfo))
                .switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).build()))
                .doOnSuccess(resp -> log.info("Updated the {} product", resp.getBody()))
                .log();
    }

    @PutMapping("/updateMovieInfo2")
    public Mono<ResponseEntity<MovieInfo>> updateMovieInfo2(@RequestBody @Valid MovieInfo movieInfo) {
        return movieInfoService.updateMovieInfo(movieInfo)
                .map(savedMovieInfo -> {
                    log.info("Successfully updated movie: {}", savedMovieInfo);
                    return ResponseEntity.ok(savedMovieInfo);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("MovieInfo with ID {} not found for update", movieInfo.getMovieId());
                    return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
                }))
                .log(); // Optional for debugging
    }

}
