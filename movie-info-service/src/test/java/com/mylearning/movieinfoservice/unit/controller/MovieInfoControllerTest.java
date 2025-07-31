package com.mylearning.movieinfoservice.unit.controller;

import com.mylearning.movieinfoservice.controller.MovieInfoController;
import com.mylearning.movieinfoservice.model.MovieInfo;
import com.mylearning.movieinfoservice.service.MovieInfoServiceImpl;
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

@WebFluxTest(controllers = MovieInfoController.class)
@AutoConfigureWebTestClient
public class MovieInfoControllerTest {

    //@MockBean deprecated and now becomes MockitoBean
    @MockitoBean
    private MovieInfoServiceImpl movieInfoService;

    @Autowired
    private WebTestClient webTestClient;

    private final String MOVIE_INFO_PATH = "/api/v1/movies";

    @Test
    void addNewMovieInfo() {

        var movieInfo = new MovieInfo(
                "blabla",
                "The Imitation Game",
                2014,
                List.of("Benedict Cumberbatch", "Keira Knightley"),
                LocalDate.parse("2014-11-28"),
                "During World War II, mathematician Alan Turing tries to crack the Enigma code with help from fellow mathematicians."
        );

        Mockito.when(movieInfoService.addMovieInfo(Mockito.any(MovieInfo.class))).thenReturn(Mono.just(movieInfo));

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

    // testing my validation added to request body on moveInfo
    // name cannot be blank
    // year must be positive
    // cast must be present
    @Test
    void addNewMovieInfo_validation() {

        var movieInfo = new MovieInfo(
                null,
                "",       // movieInfo.name should be not be blank
                -2014,          // movieInfo.year should be positive
                List.of(""), // movieInfo.cast should not be blank
                LocalDate.parse("2014-11-28"),
                "During World War II, mathematician Alan Turing tries to crack the Enigma code with help from fellow mathematicians."
        );


        webTestClient
                .post()
                .uri(MOVIE_INFO_PATH + "/addMovieInfos")
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(String.class)
                .consumeWith(result -> {
                    var error = result.getResponseBody();
                    assert error != null;
                    System.out.println(error);
                    String expectedErrorMessage = "movieInfo.cast should not be blank,movieInfo.name should be not be blank,movieInfo.name should be not be blank";
                    assertEquals(expectedErrorMessage, error);
                });
    }

    @Test
    void getAllMovieInfos() {
        // given
        var movieinfos = List.of(
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

        //when
        Mockito.when(movieInfoService.getMovieInfos()).thenReturn(Flux.fromIterable(movieinfos));

        //then
        webTestClient
                .get()
                .uri(MOVIE_INFO_PATH + "/getMovieInfos")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class);
    }

    @Test
    void getMovieInfoById() {
        var id = "abc";
        var movieInfo = new MovieInfo(
                "xyz789",
                "The Prestige",
                2006,
                List.of("Hugh Jackman", "Christian Bale"),
                LocalDate.of(2006, 10, 20),
                "Two rival magicians in 19th-century London engage in a battle of wits, illusions, and obsession."
        );

        Mockito.when(movieInfoService.getMovieInfo(Mockito.any(String.class)))
                .thenReturn(Mono.just(movieInfo));

        webTestClient
                .get()
                .uri(MOVIE_INFO_PATH + "/getMovieInfo/{id}", id)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var info = movieInfoEntityExchangeResult.getResponseBody();
                    assert info != null;
                });
    }


    @Test
    void updateMovieInfo() {

        // given
        var id = "abc";
        var updatedMovieInfo = new MovieInfo(
                "xyz789",
                "The Prestige",
                2006,
                List.of("Hugh Jackman", "Christian Bale"),
                LocalDate.of(2006, 10, 20),
                "Two rival magicians in 19th-century London engage in a battle of wits, illusions, and obsession."
        );

        //when
        Mockito.when(movieInfoService.updateMovieInfo(Mockito.isA(MovieInfo.class)))
                .thenReturn(Mono.just(updatedMovieInfo));

        //then
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
                    assertEquals("The Prestige", movieInfo.getName());
                });
    }

    @Test
    void deleteMovieInfoById() {
        var id = "abc";

        Mockito.when(movieInfoService.deleteMovieInfo(Mockito.isA(String.class)))
                .thenReturn(Mono.empty());

        webTestClient
                .delete()
                .uri(MOVIE_INFO_PATH + "/deleteMovieInfo/{id}", id)
                .exchange()
                .expectStatus()
                .isNoContent();
    }

}
