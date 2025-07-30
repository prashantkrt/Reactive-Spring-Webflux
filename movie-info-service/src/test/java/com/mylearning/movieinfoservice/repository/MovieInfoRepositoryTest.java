package com.mylearning.movieinfoservice.repository;

import com.mylearning.movieinfoservice.model.MovieInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/*
| Annotation    | When It Runs                      | Runs Once or Multiple   |
| ------------- | --------------------------------- | ----------------------- |
| @BeforeAll    | Before all tests in the class     | Once (static method)    |
| @BeforeEach   | Before each test method           | Before every test       |
| @Test         | The actual test method            | Each test               |
| @AfterEach    | After each test method            | After every test        |
| @AfterAll     | After all tests in the class      | Once (static method)    |
*/

@DataMongoTest
@ActiveProfiles("test")
public class MovieInfoRepositoryTest {

    @Autowired
    private MovieInfoRepository movieInfoRepository;

    //set up dummy/test data to the database
    @BeforeEach
    void setUp() {

        // Notes:
        // LocalDate.of(y, m, d) && LocalDate.parse("yyyy-MM-dd")

        // Both .block() and .blockLast() are used in reactive programming (e.g., Project Reactor in Spring WebFlux) to trigger and wait for the completion of a reactive stream.
        // In Reactor, methods like .saveAll(...) return a reactive type, such as:
        // => Mono<T> → for 0 or 1 item
        // => Flux<T> → for 0 or more items,
        // But these do nothing until subscribed to.
        // so movieInfoRepository.saveAll(movieinfos); // <-- nothing happens yet
        // This won't save anything unless the stream is triggered/subscribed.'

        //.blockLast()
        // Subscribes to the Flux
        // Waits until the last element is emitted
        // Blocks the current thread until the operation completes

        // It's basically saying:
        // "Save all the movies, and wait until the last one is saved, before moving forward."

        /*
         * | Use Case                                            | Method                 | Description
         * ----------------------------------------------------- | -----------------------|--------------------------------------------------------------------
         * | Need the result list                                | .collectList().block() | Collects all items into a List and blocks until done
         * | Just want to wait for all items to be processed.    | .blockLast()           | Blocks until the  last element is emitted. Return that last item
         * | For a  Mono<T>                                      | .block()               | Waits for that single value. Mono<T> → returns the value of type T and Flux<T> → returns the first emitted element (T)                                         |
         */

        /*
         * | Method          | Called On | Return Type     | Description
         * ----------------------------------------------------------------------------
         * | collectList()   | Flux<T>   | Mono<List<T>>   | Collects all items into a list
         * | block()         | Mono<T>   | T               | Returns the value from a Mono
         * | block()         | Flux<T>   | T               | Returns the first item
         * | blockLast()     | Flux<T>   | T               | Returns the last item
         */

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

        // movieInfoRepository.saveAll(movieinfos).collectList().block();
        MovieInfo movieInfo = movieInfoRepository.saveAll(movieinfos).blockLast();

    }

    @AfterEach
    void tearDown() {
        // The method deleteAll() returns a Mono<Void>
        movieInfoRepository.deleteAll().block();
    }

    @Test
    void findAll() {
        //given

        //when
        Flux<MovieInfo> movieInfoFlux = movieInfoRepository.findAll().log();

        //then
        StepVerifier.create(movieInfoFlux)
                .expectNextCount(3)
                .verifyComplete();

    }

    @Test
    void findAll2() {
        //given

        //when
        Flux<MovieInfo> movieInfoFlux = movieInfoRepository.findAll().log();

        //then
        StepVerifier.create(movieInfoFlux)
                .expectNextCount(2)
                .consumeNextWith(info -> {
                    assertEquals("abc123", info.getMovieId());
                    assertEquals("Interstellar", info.getName());
                    assertEquals(2014, info.getYear());
                    assertEquals(List.of("Matthew McConaughey", "Anne Hathaway"), info.getCast());
                    assertEquals(LocalDate.of(2014, 11, 7), info.getReleaseDate());
                })
                .verifyComplete();

    }

    @Test
    void findAll3() {
        //given

        //when
        Flux<MovieInfo> movieInfoFlux = movieInfoRepository.findAll().log();

        //then
        StepVerifier.create(movieInfoFlux)
                .thenConsumeWhile(info -> Objects.nonNull(info.getName()))
                .verifyComplete();

    }

    @Test
    void findById() {
        //given

        //when
        var movieInfo = movieInfoRepository.findById("abc123").log();

        //then
        StepVerifier.create(movieInfo)
                .expectNextMatches(info -> info.getMovieId().equals("abc123"))
                .verifyComplete();

    }

    @Test
    void findById2() {
        //given

        //when
        var movieInfo = movieInfoRepository.findById("abc123").log();

        //then
        StepVerifier.create(movieInfo)
                .assertNext(info -> {
                    assertEquals("abc123", info.getMovieId());
                    assertEquals("Interstellar", info.getName());
                    assertEquals(2014, info.getYear());
                    assertEquals(List.of("Matthew McConaughey", "Anne Hathaway"), info.getCast());
                    assertEquals(LocalDate.of(2014, 11, 7), info.getReleaseDate());
                })
                .verifyComplete();

    }

    @Test
    void findById3() {
        //given

        //when
        var movieInfo = movieInfoRepository.findById("abc123").log();

        //then
        StepVerifier.create(movieInfo)
                .assertNext(info -> {
                    assertEquals("abc123", info.getMovieId());
                    assertEquals("Interstellar", info.getName());
                    assertEquals(2014, info.getYear());
                    assertEquals(List.of("Matthew McConaughey", "Anne Hathaway"), info.getCast());
                    assertEquals(LocalDate.of(2014, 11, 7), info.getReleaseDate());
                })
                .verifyComplete();

    }

    @Test
    void saveMovieInfo() {

        //given
        var movieInfo = new MovieInfo(
                "def123",
                "The Godfather",
                1972,
                List.of("Marlon Brando", "Al Pacino", "James Caan"),
                LocalDate.of(1972, 3, 15),  // direct date creation
                "The aging patriarch of an organized crime dynasty transfers control of his clandestine empire to his reluctant son."
        );
        //when
        var savedMovieInfo = movieInfoRepository.save(movieInfo);

        StepVerifier.create(savedMovieInfo)
                .assertNext(info -> {
                    assertNotNull(info.getMovieId());
                })
                .verifyComplete();
    }

    @Test
    void updateMovieInfo() {

        //given
        var savedMovieInfo = movieInfoRepository.save(new MovieInfo(
                "mar123",
                "The Martian",
                2015,
                List.of("Matt Damon", "Jessica Chastain", "Kristen Wiig"),
                LocalDate.of(2015, 10, 2),  // direct date creation
                "An astronaut becomes stranded on Mars and must use his ingenuity to survive until he can be rescued."
        ));
        var movieInfo = savedMovieInfo.block();

        // when
        movieInfo = movieInfoRepository.findById("mar123").block();
        if (movieInfo != null) {
            movieInfo.setYear(2020);
        }
        var updatedMovieInfo = movieInfoRepository.save(movieInfo);

        //then
        StepVerifier.create(updatedMovieInfo)
                .assertNext(movieInfo1 -> {
                    assertNotNull(movieInfo1.getMovieId());
                    assertEquals(2020, movieInfo1.getYear());
                })
                .expectComplete()
                .verify();

    }

    @Test
    void deleteMovieInfo() {

        //given
        var savedMovieInfo = movieInfoRepository.save(new MovieInfo(
                "mar123",
                "The Martian",
                2015,
                List.of("Matt Damon", "Jessica Chastain", "Kristen Wiig"),
                LocalDate.of(2015, 10, 2),  // direct date creation
                "An astronaut becomes stranded on Mars and must use his ingenuity to survive until he can be rescued."
        ));
        var movieInfo = savedMovieInfo.block();

        //when
        movieInfoRepository.deleteById("mar123").block();
        var movieInfos = movieInfoRepository.findAll();

        StepVerifier.create(movieInfos)
                .expectNextCount(3)
                .verifyComplete();

    }

    @Test
    void findMovieInfoByYear() {

        var movieInfosFlux = movieInfoRepository.findByYear(2014).log();

        StepVerifier.create(movieInfosFlux)
                .expectNextCount(1)
                .verifyComplete();

    }


    @Test
    void findByName() {

        var movieInfosMono = movieInfoRepository.findByName("Inception").log();

        StepVerifier.create(movieInfosMono)
                .expectNextCount(1)
                .verifyComplete();

    }

}
