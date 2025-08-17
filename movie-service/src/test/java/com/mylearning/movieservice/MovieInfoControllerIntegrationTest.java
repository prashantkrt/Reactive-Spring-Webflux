package com.mylearning.movieservice;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.mylearning.movieservice.model.Movie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Duration;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@AutoConfigureWireMock(port = 8085)
//Spins up the http server automatically,
//Spins up a WireMock server (a fake HTTP server for mocking external services) and Starts WireMock on 8085
@TestPropertySource(
        properties = {
                "restClient.movie-info-service.url=http://localhost:8085/api/v1/movies/response-entity/getMovieInfo",
                "restClient.review-service.url=http://localhost:8085/api/v1/review/search",

        })
public class MovieInfoControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        WireMock.reset();
    }

    @Test
    void retrieveMovieById() {
        //given
        var movieId = "abc";

        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/api/v1/movies/response-entity/getMovieInfo/" + movieId))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("movieInfo.json")));

        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/api/v1/review/search?movieInfoId=" + movieId))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("reviews.json")));

        //when
        webTestClient.get()
                .uri("/api/v1/movies/{id}", "abc")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Movie.class)
                .consumeWith(movieEntityExchangeResult -> {
                            var movie = movieEntityExchangeResult.getResponseBody();
                            assert Objects.requireNonNull(movie).getReviewList().size() == 2;
                            assertEquals("Batman Begins", movie.getMovieInfo().getName());
                        }
                );
    }

    @Test
    void retrieveMovieById_404() {
        //given
        var movieId = "abc";
        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/api/v1/movies/response-entity/getMovieInfo/" + movieId))
                .willReturn(WireMock.aResponse()
                        .withStatus(404)));

        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/api/v1/review/search"))
                .withQueryParam("movieInfoId", WireMock.equalTo(movieId))
                .willReturn(WireMock.aResponse()
                        .withStatus(404)));


        // we wanted to stub for a POST request
        // Stub for POST request with a body and response from __files
        //    WireMock.stubFor(WireMock.post(WireMock.urlPathEqualTo("/api/v1/movies/add"))
        //            .withRequestBody(WireMock.equalToJson("{ \"id\": \"101\", \"name\": \"Interstellar\" }"))
        //            .willReturn(WireMock.aResponse()
        //                    .withStatus(201)
        //                    .withHeader("Content-Type", "application/json")
        //                    .withBodyFile("movie-data-response.json"))); // loads from __files

        // or
        // WireMock.stubFor(WireMock.post(WireMock.urlPathEqualTo("/api/v1/movies"))
        //        .withRequestBody(WireMock.matchingJsonPath("$.id", WireMock.equalTo("101")))
        //        .withRequestBody(WireMock.matchingJsonPath("$.name", WireMock.equalTo("Interstellar")))
        //        .willReturn(WireMock.aResponse()
        //                .withHeader("Content-Type", "application/json")
        //                .withStatus(201)
        //                .withBodyFile("movie-created.json")));

        webTestClient.get()
                .uri("/api/v1/movies/{id}", "abc")
                .exchange()
                .expectStatus().is4xxClientError();

    }

    @Test
    void retrieveMovieById_Reviews_404() {
        //given
        var movieId = "abc";
        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/api/v1/movies/response-entity/getMovieInfo/" + movieId))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("movieinfo.json")));

        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/api/v1/review/search"))
                .withQueryParam("movieInfoId", WireMock.equalTo(movieId))
                .willReturn(WireMock.aResponse()
                        .withStatus(404)));


        //when
        webTestClient.get()
                .uri("/api/v1/movies/{id}", "abc")
                .exchange()
                .expectStatus().is2xxSuccessful();

    }

    @Test
    void retrieveMovieById_5XX() {
        //given
        var movieId = "abc";
        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/api/v1/movies/response-entity/getMovieInfo/" + movieId))
                .willReturn(WireMock.aResponse()
                        .withStatus(500)
                        .withBody("MovieInfo Service Unavailable")));

        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/api/v1/review/search"))
                .withQueryParam("movieInfoId", WireMock.equalTo(movieId))
                .willReturn(WireMock.aResponse()
                        .withStatus(500)));


        //when
        webTestClient.get()
                .uri("/api/v1/movies/{id}", "abc")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(String.class)
                .value(message -> {
                    assertEquals("Server Exception in MovieInfoService: MovieInfo Service Unavailable", message);
                });

        //then
        WireMock.verify(1, WireMock.getRequestedFor(WireMock.urlEqualTo("/api/v1/movies/response-entity/getMovieInfo/" + movieId)));
    }

    @Test
    void retrieveMovieById_reviews_5XX() {

        //given
        var movieId = "abc";

        WebTestClient webTestClientWithTimeout = webTestClient.mutate()
                .responseTimeout(Duration.ofSeconds(15)) // increase as needed
                .build();

        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/api/v1/movies/response-entity/getMovieInfo/" + movieId))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("movieinfo.json")));


        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/api/v1/review/search"))
                .withQueryParam("movieInfoId", WireMock.equalTo(movieId))
                .willReturn(WireMock.aResponse()
                        .withStatus(500)
                        .withBody("Review Service Unavailable")));

        //when
        webTestClientWithTimeout.get()
                .uri("/api/v1/movies/{id}", "abc")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(String.class)
                .value(message -> {
                    assertEquals("Server error: Review Service Unavailable", message);
                });

        // then
        // First attempt → counts as 1.
        // Retry #1 → second request.
        // Retry #2 → third request.
        // Retry #3 → fourth request.
        WireMock.verify(1, WireMock.getRequestedFor(WireMock.urlPathMatching("/api/v1/movies/response-entity/getMovieInfo/" + movieId)));
        WireMock.verify(4, WireMock.getRequestedFor(WireMock.urlPathMatching("/api/v1/review/search")));
    }



}
