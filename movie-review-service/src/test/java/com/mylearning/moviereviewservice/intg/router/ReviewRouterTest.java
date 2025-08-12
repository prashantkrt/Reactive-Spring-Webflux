package com.mylearning.moviereviewservice.intg.router;

import com.mylearning.moviereviewservice.domain.Review;
import com.mylearning.moviereviewservice.repository.ReviewRepository;
import lombok.extern.slf4j.Slf4j;
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
import reactor.test.StepVerifier;

import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
@Slf4j
class ReviewRouterTest {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    ReviewRepository reviewRepository;

    @BeforeEach
    void setUp() {
        List<Review> reviews = List.of(
                new Review("1", 1L, "Awesome Movie", 9.0),
                new Review("2", 1L, "Awesome Movie1", 9.0),
                new Review("3", 2L, "Excellent Movie", 8.0));
        Flux<Review> reviewFlux = reviewRepository.saveAll(reviews);
        Review review = reviewFlux.blockLast();
    }

    @AfterEach
    void tearDown() {
        Mono<Void> voidMono = reviewRepository.deleteAll();
        voidMono.block();
    }

    @Test
    void addReview1() {

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
                    assert body.getRating() != null;
                    Assertions.assertEquals(1L, body.getMovieInfoId());
                    Assertions.assertEquals("Awesome Movie", body.getComment());
                    Assertions.assertEquals(9.0, body.getRating());
                });
    }

    @Test
    void getReview11() {

        webTestClient
                .get()
                .uri("/api/v1/review/1")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.movieInfoId").isEqualTo(1L)
                .jsonPath("$.comment").isEqualTo("Awesome Movie");
    }


    @Test
    void getReview2() {

        webTestClient
                .get()
                .uri("/api/v1/review/1")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(Review.class)
                .consumeWith(response -> {
                    Review body = response.getResponseBody();
                    assert body != null;
                    assert body.getReviewId() != null;
                    assert body.getRating() != null;
                    Assertions.assertEquals(1L, body.getMovieInfoId());
                    Assertions.assertEquals("Awesome Movie", body.getComment());
                    Assertions.assertEquals(9.0, body.getRating());
                });

    }


    @Test
    void getReview3() {

        Flux<Review> responseBody = webTestClient
                .get()
                .uri("/api/v1/review/1")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .returnResult(Review.class)
                .getResponseBody();

        StepVerifier.create(responseBody)
                .expectNextCount(1)
                .verifyComplete();

    }


    @Test
    void getAllReviews1() {

        log.info("Count in handler: {}", reviewRepository.count().block()); // Count in handler: 3

        webTestClient
                .get()
                .uri("/api/v1/review/stream")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Review.class)
                .hasSize(3);
    }


    @Test
    void getAllReviews2() {

        webTestClient
                .get()
                .uri("/api/v1/review/stream")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .returnResult(Review.class)
                .consumeWith(response -> {
                    StepVerifier.create(response.getResponseBody())
                            .expectNextCount(3)
                            .verifyComplete();
                });
    }


    @Test
    void getAllReviews3() {

        var flux = webTestClient
                .get()
                .uri("/api/v1/review/stream")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .returnResult(Review.class)
                .getResponseBody();

        StepVerifier.create(flux)
                .expectNext(
                        new Review("1", 1L, "Awesome Movie", 9.0),
                        new Review("2", 1L, "Awesome Movie1", 9.0),
                        new Review("3", 2L, "Excellent Movie", 8.0))
                .expectComplete()
                .verify();

    }


    @Test
    void updateReview1() {

        var list = List.of(
                new Review("4", 10L, "Mind-blowing Sci-Fi", 9.8),
                new Review("5", 11L, "Heartwarming Drama", 8.7),
                new Review("6", 12L, "Action-Packed Thriller", 9.1)
        );
        reviewRepository.saveAll(list).blockLast();

        webTestClient
                .post()
                .uri("/api/v1/review")
                .bodyValue(new Review("4", 100L, "Mind-blowing Sci-Fi Movie", 8.8))
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.movieInfoId").isEqualTo(100L)
                .jsonPath("$.comment").isEqualTo("Mind-blowing Sci-Fi Movie")
                .jsonPath("$.rating").isEqualTo(8.8);

    }


    @Test
    void updateReview2() {

        var list = List.of(
                new Review("4", 10L, "Mind-blowing Sci-Fi", 9.8),
                new Review("5", 11L, "Heartwarming Drama", 8.7),
                new Review("6", 12L, "Action-Packed Thriller", 9.1)
        );
        reviewRepository.saveAll(list).blockLast();

        webTestClient
                .post()
                .uri("/api/v1/review")
                .bodyValue(new Review("4", 100L, "Mind-blowing Sci-Fi Movie", 8.8))
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(Review.class)
                .consumeWith(response -> {
                    Review body = response.getResponseBody();
                    assert body != null;
                    assert body.getReviewId() != null;
                    assert body.getRating() != null;
                    Assertions.assertEquals(100L, body.getMovieInfoId());
                    Assertions.assertEquals("Mind-blowing Sci-Fi Movie", body.getComment());
                });
    }

    @Test
    void deleteReview1() {

        var list = List.of(
                new Review("4", 10L, "Mind-blowing Sci-Fi", 9.8),
                new Review("5", 11L, "Heartwarming Drama", 8.7),
                new Review("6", 12L, "Action-Packed Thriller", 9.1)
        );
        reviewRepository.saveAll(list).blockLast();

         webTestClient
                .delete()
                .uri("/api/v1/review/{id}", "4")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("Review deleted successfully");

         webTestClient
                 .get()
                 .uri("/api/v1/review/stream")
                 .exchange()
                 .expectStatus()
                 .is2xxSuccessful()
                 .expectBodyList(Review.class)
                 .hasSize(5);


         webTestClient
                 .delete()
                 .uri("/api/v1/review/{id}", "5")
                 .exchange()
                 .expectStatus().isOk()
                 .expectBody(String.class)
                 .isEqualTo("Review deleted successfully");

         webTestClient
                 .delete()
                 .uri("/api/v1/review/{id}", "5")
                 .exchange()
                 .expectStatus().isNotFound()
                 .expectBody()
                 .jsonPath("$.error").isEqualTo("Not Found");
    }


}