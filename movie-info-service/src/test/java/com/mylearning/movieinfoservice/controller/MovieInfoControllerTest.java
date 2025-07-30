package com.mylearning.movieinfoservice.controller;

import com.mylearning.movieinfoservice.model.MovieInfo;
import com.mylearning.movieinfoservice.repository.MovieInfoRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/*
+------------------------+------------------------+----------------------------------------------+---------------------------+
| Method Name            | Argument Type          | Purpose / Use                                 | Returns                  |
+------------------------+------------------------+----------------------------------------------+---------------------------+
| assertNext(...)        | Consumer<T>            | Perform multiple assertions on next item      | Step<T> (for chaining)   |
|                        |                        | (like assertEquals, assertTrue)               |                          |
+------------------------+------------------------+----------------------------------------------+---------------------------+
| expectNextMatches(...) | Predicate<T>           | Apply boolean condition on next item          | Step<T> (for chaining)   |
|                        |                        | (returns true if test passes)                 |                          |
+------------------------+------------------------+----------------------------------------------+---------------------------+
| expectNext(...)        | T (single value)       | Expect exact value match                      | Step<T> (for chaining)   |
+------------------------+------------------------+----------------------------------------------+---------------------------+
| expectNextCount(...)   | long (count)           | Expect given number of items to be emitted    | Step<T> (for chaining)   |
+------------------------+------------------------+----------------------------------------------+---------------------------+
| consumeNextWith(...)   | Consumer<T>            | Consume next item and perform assertions      | Step<T> (for chaining)   |
|                        |                        | (similar to assertNext)                       |                          |
+------------------------+------------------------+----------------------------------------------+---------------------------+
| consumeWith(...)       | Consumer<EntityResult> | Inspect entire response(esp. in WebTestClient)| void                     |
| (WebTestClient)        |                        | Check responseBody, headers, etc.             |                          |
+------------------------+------------------------+----------------------------------------------+---------------------------+
*/

// @WebFluxTest(controllers = MovieInfoController.class) will load the bean specified in the controller not for repo and service
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
public class MovieInfoControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private MovieInfoRepository movieInfoRepository;

    public static final String MOVIE_INFO_PATH = "/api/v1/movies";

    @BeforeEach
    void setUp() {

        var movieInfos = List.of(
                new MovieInfo(
                        null,
                        "Inception",
                        2010,
                        List.of("Leonardo DiCaprio", "Joseph Gordon-Levitt"),
                        LocalDate.parse("2010-07-16"),
                        "A mind-bending thriller about dreams within dreams."
                ),
                new MovieInfo(
                        null,
                        "The Matrix",
                        1999,
                        List.of("Keanu Reeves", "Laurence Fishburne"),
                        LocalDate.parse("1999-03-31"),
                        "A hacker discovers the world is a simulation controlled by machines."
                ),
                new MovieInfo(
                        "abc123",
                        "Interstellar",
                        2014,
                        List.of("Matthew McConaughey", "Anne Hathaway"),
                        LocalDate.of(2014, 11, 7),  // direct date creation
                        "Explorers travel through a wormhole in space in an attempt to save humanity."
                )
        );

        movieInfoRepository.deleteAll().thenMany(movieInfoRepository.saveAll(movieInfos)).blockLast();
    }

    @Test
    void addNewMovieInfo() {

        var movieInfo = new MovieInfo(
                "xyz789",
                "The Dark Knight",
                2008,
                List.of("Christian Bale", "Heath Ledger", "Aaron Eckhart"),
                LocalDate.of(2008, 7, 18),
                "Batman faces off against the Joker, a criminal mastermind who plunges Gotham City into chaos."
        );
        webTestClient
                .post()
                .uri(MOVIE_INFO_PATH + "/addMovieInfos")
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var savedMovieInfo = movieInfoEntityExchangeResult.getResponseBody();
                    assert Objects.requireNonNull(savedMovieInfo).getMovieId() != null;

                });
    }


    @Test
    void addNewMovieInfo2() {

        var movieInfo = new MovieInfo(
                "xyz789",
                "The Dark Knight",
                2008,
                List.of("Christian Bale", "Heath Ledger", "Aaron Eckhart"),
                LocalDate.of(2008, 7, 18),
                "Batman faces off against the Joker, a criminal mastermind who plunges Gotham City into chaos."
        );
        FluxExchangeResult<MovieInfo> movieInfoFluxExchangeResult = webTestClient
                .post()
                .uri(MOVIE_INFO_PATH + "/addMovieInfos")
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .isCreated()
                .returnResult(MovieInfo.class);

        Flux<MovieInfo> responseBody = movieInfoFluxExchangeResult.getResponseBody();

        StepVerifier.create(responseBody)
                .expectNextMatches(movieInfo1 ->        // takes predicate as an argument
                        movieInfo1.getMovieId().equals("xyz789") &&
                                movieInfo1.getName().equals("The Dark Knight") &&
                                movieInfo1.getYear().equals(2008) &&
                                movieInfo1.getCast().size() == 3
                )
                .verifyComplete();

    }


    @Test
    void addNewMovieInfo3() {

        var movieInfo = new MovieInfo(
                "xyz789",
                "The Dark Knight",
                2008,
                List.of("Christian Bale", "Heath Ledger", "Aaron Eckhart"),
                LocalDate.of(2008, 7, 18),
                "Batman faces off against the Joker, a criminal mastermind who plunges Gotham City into chaos."
        );
        FluxExchangeResult<MovieInfo> movieInfoFluxExchangeResult = webTestClient
                .post()
                .uri(MOVIE_INFO_PATH + "/addMovieInfos")
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .isCreated()
                .returnResult(MovieInfo.class);

        Flux<MovieInfo> responseBody = movieInfoFluxExchangeResult.getResponseBody();

        StepVerifier.create(responseBody)
                .consumeNextWith(movieInfo1 -> {   // takes consumer as an argument
                            assertEquals("xyz789", movieInfo1.getMovieId());
                            assertTrue(movieInfo1.getName().equals("The Dark Knight") &&
                                    movieInfo1.getYear().equals(2008) &&
                                    movieInfo1.getCast().size() == 3);
                        }
                )
                .verifyComplete();

    }

    @Test
    void getAllMovieInfos() {

        webTestClient
                .get()
                .uri(MOVIE_INFO_PATH + "/getMovieInfos")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(3);
    }

    @Test
    void getAllMovieInfos2() {

        var response = webTestClient
                .get()
                .uri(MOVIE_INFO_PATH + "/getMovieInfos")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .returnResult(MovieInfo.class)
                .getResponseBody();

        StepVerifier.create(response)
                .expectNextCount(3)
                .verifyComplete();
    }


    @Test
    void getAllMovieInfos3() {

        webTestClient
                .get()
                .uri(MOVIE_INFO_PATH + "/getMovieInfos")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .consumeWith(listEntityExchangeResult -> {
                    var responseBody = listEntityExchangeResult.getResponseBody();
                    Assertions.assertNotNull(responseBody);
                    assertEquals(3, responseBody.size());
                });

    }

    @Test
    void getMovieInfoById() {

        //given
        var id = "abc123";

        webTestClient
                .get()
                .uri(MOVIE_INFO_PATH + "/getMovieInfo/{id}", id)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var movieInfo = movieInfoEntityExchangeResult.getResponseBody();
                    assert movieInfo != null;
                    assert movieInfo.getMovieId().equals(id);
                    assert movieInfo.getName().equals("Interstellar");
                    assert movieInfo.getYear().equals(2014);

                });
    }


    @Test
    void getMovieInfoById2() {

        // given
        var id = "abc123";

        // when
        Flux<MovieInfo> responseBody = webTestClient
                .get()
                .uri(MOVIE_INFO_PATH + "/getMovieInfo/{id}", id)
                .exchange()
                .expectStatus()
                .isOk()
                .returnResult(MovieInfo.class)
                .getResponseBody();

        // then
        StepVerifier.create(responseBody)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void getMovieInfoById3() {
        var id = "abc123";
        webTestClient
                .get()
                .uri(MOVIE_INFO_PATH + "/getMovieInfo/{id}", id)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Interstellar");

    }

    @Test
    void updateMovieInfo() {
        var id = "abc123";
        var updatedMovieInfo = new MovieInfo(
                "abc123",
                "Interstellar",
                2014,
                List.of("Matthew McConaughey", "Anne Hathaway"),
                LocalDate.of(2014, 11, 7),  // direct date creation
                "Explorers travel through a wormhole in space in an attempt to save humanity."
        );

        webTestClient
                .put()
                .uri(MOVIE_INFO_PATH + "/updateMovieInfo")
                .bodyValue(updatedMovieInfo)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var movieInfo = movieInfoEntityExchangeResult.getResponseBody();
                    assert movieInfo != null;
                    assertEquals("Explorers travel through a wormhole in space in an attempt to save humanity.", movieInfo.getDescription());
                });
    }

    @Test
    void deleteMovieInfoById() {

        //given
        var id = "abc123";
        var movieInfo = new MovieInfo(
                "def456",
                "Stellar Horizon",
                2026,
                List.of("John David Washington", "Zendaya"),
                LocalDate.of(2026, 6, 12),
                "A space crew ventures beyond the solar system to unlock secrets that could prevent Earth's collapse."
        );

        //when
        movieInfoRepository.save(movieInfo).block();

        //then
        webTestClient
                .delete()
                .uri(MOVIE_INFO_PATH + "/deleteMovieInfo/{id}", id)
                .exchange()
                .expectStatus()
                .isNoContent();
    }
}
