package com.mylearning.moviereviewservice.intg.router;

import com.mylearning.moviereviewservice.domain.Review;
import com.mylearning.moviereviewservice.repository.ReviewRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class ReviewRouterTest {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    ReviewRepository reviewRepository;

    @BeforeEach
    void setUp() {
        List<Review> reviews = List.of(
                new Review(null, 1L, "Awesome Movie", 9.0),
                new Review(null, 1L, "Awesome Movie1", 9.0),
                new Review(null, 2L, "Excellent Movie", 8.0));
        Flux<Review> reviewFlux = reviewRepository.saveAll(reviews);
        Review review = reviewFlux.blockLast();
    }

    @AfterEach
    void tearDown() {
        Mono<Void> voidMono = reviewRepository.deleteAll();
        voidMono.block();
    }

    @Test
    void addReview() {

        //given
        var review = new Review(null, 1L, "Awesome Movie", 9.0);

        //when then
        webTestClient
                .post()
                .uri("/api/v1/review")
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody()
                .jsonPath("$.movieInfoId").isEqualTo(1L)
                .jsonPath("$.comment").isEqualTo("Awesome Movie")
                .jsonPath("$.rating").isEqualTo(9.0);

    }


    @Test
    void addReview2() {

        //given
        var review = new Review(null, 1L, "Awesome Movie", 9.0);

        //when then
        webTestClient
                .post()
                .uri("/api/v1/review")
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(Review.class)
                .consumeWith(response -> {
                    Review body = response.getResponseBody();
                    assert body != null;
                    assert body.getReviewId() != null;
                    assert body.getRating()!= null;
                    Assertions.assertEquals(1L, body.getMovieInfoId());
                    Assertions.assertEquals("Awesome Movie", body.getComment());
                    Assertions.assertEquals(9.0, body.getRating());
                });
    }
}