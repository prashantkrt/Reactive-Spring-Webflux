package com.mylearning.movieservice;

import com.mylearning.movieservice.client.MovieInfoRestClient;
import com.mylearning.movieservice.client.ReviewRestClient;
import com.mylearning.movieservice.controller.MovieController;
import com.mylearning.movieservice.exception.MoviesInfoClientException;
import com.mylearning.movieservice.exception.MoviesInfoServerException;
import com.mylearning.movieservice.model.Movie;
import com.mylearning.movieservice.model.MovieInfo;
import com.mylearning.movieservice.model.Review;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

@WebFluxTest(controllers = MovieController.class)
@AutoConfigureWebTestClient
public class MovieInfoControllerUnitTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private MovieInfoRestClient movieInfoRestClient;

    @MockitoBean
    private ReviewRestClient reviewRestClient;

    @Test
    void retrieveMovieById() {

        var reviewList = List.of(
                new Review(null, 1L, "Awesome Movie", 9.0),
                new Review(null, 1L, "Awesome Movie1", 9.0),
                new Review(null, 2L, "Excellent Movie", 8.0));

        var movieId = "abc";

        Mockito.when(movieInfoRestClient.retrieveMovieInfo(Mockito.anyString()))
                .thenReturn(Mono.just(new MovieInfo(movieId, "Batman Begins",
                        2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"), "Batman Begins")));

        Mockito.when(reviewRestClient.retrieveReviews(Mockito.anyString()))
                .thenReturn(Flux.fromIterable(reviewList));

        //when
        webTestClient.get()
                .uri("/api/v1/movies/{id}", "abc")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Movie.class)
                .consumeWith(movieEntityExchangeResult -> {
                            var movie = movieEntityExchangeResult.getResponseBody();
                            assert Objects.requireNonNull(movie).getReviewList().size() == 3;
                            assertEquals("Batman Begins", movie.getMovieInfo().getName());
                        }
                );
    }

    @Test
    void retrieveMovieById_404() {

        var reviewList = List.of(
                new Review(null, 1L, "Awesome Movie", 9.0),
                new Review(null, 1L, "Awesome Movie1", 9.0),
                new Review(null, 2L, "Excellent Movie", 8.0));

        Mockito.when(movieInfoRestClient.retrieveMovieInfo(Mockito.anyString()))
                .thenReturn(Mono.error(new MoviesInfoClientException("MovieNotFound", 404)));

        Mockito.when(reviewRestClient.retrieveReviews(Mockito.anyString()))
                .thenReturn(Flux.fromIterable(reviewList));

        //when
        webTestClient.get()
                .uri("/api/v1/movies/{id}", "abc")
                .exchange()
                .expectStatus().is4xxClientError()
                .expectBody(String.class)
                .consumeWith(movieEntityExchangeResult -> {
                            var errorMessage = movieEntityExchangeResult.getResponseBody();
                            assertEquals("MovieNotFound", errorMessage);
                        }
                );
    }

    @Test
    void retrieveMovieById_500() {

        var reviewList = List.of(
                new Review(null, 1L, "Awesome Movie", 9.0),
                new Review(null, 1L, "Awesome Movie1", 9.0),
                new Review(null, 2L, "Excellent Movie", 8.0));

        var errorMsg = "Service Unavailable";
        Mockito.when(movieInfoRestClient.retrieveMovieInfo(Mockito.anyString()))
                .thenReturn(Mono.error(new MoviesInfoServerException(errorMsg)));

        Mockito.when(reviewRestClient.retrieveReviews(Mockito.anyString()))
                .thenReturn(Flux.fromIterable(reviewList));

        //when
        webTestClient.get()
                .uri("/api/v1/movies/{id}", "abc")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(String.class)
                .consumeWith(movieEntityExchangeResult -> {
                            var errorMessage = movieEntityExchangeResult.getResponseBody();
                            assertEquals(errorMsg, errorMessage);
                        }
                );
    }

}
