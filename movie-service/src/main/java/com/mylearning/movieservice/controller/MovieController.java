package com.mylearning.movieservice.controller;

import com.mylearning.movieservice.client.MovieInfoRestClient;
import com.mylearning.movieservice.client.ReviewRestClient;
import com.mylearning.movieservice.model.Movie;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/v1/movies")
public class MovieController {

    private final MovieInfoRestClient moviesInfoRestClient;
    private final ReviewRestClient reviewsRestClient;


    public MovieController(MovieInfoRestClient moviesInfoRestClient, ReviewRestClient reviewsRestClient) {
        this.moviesInfoRestClient = moviesInfoRestClient;
        this.reviewsRestClient = reviewsRestClient;
    }

    @GetMapping("/{id}")
    public Mono<Movie> retrieveMovieById(@PathVariable("id") String movieId){

        return moviesInfoRestClient.retrieveMovieInfo(movieId) //Mono<MovieInfo>
                .flatMap(movieInfo -> {
                    var reviewList = reviewsRestClient.retrieveReviews(movieId) //Flux<Review>
                            .collectList(); //Mono<List<Review>>
                    return reviewList.map(reviews -> new Movie(movieInfo, reviews)); // Mono<Movie>
                });
    }

}
