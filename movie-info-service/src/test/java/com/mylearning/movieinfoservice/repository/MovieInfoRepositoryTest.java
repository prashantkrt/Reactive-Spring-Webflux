package com.mylearning.movieinfoservice.repository;

import com.mylearning.movieinfoservice.model.MovieInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.util.List;

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
        Flux<MovieInfo> movieInfoFlux = movieInfoRepository.findAll();
    }


}
