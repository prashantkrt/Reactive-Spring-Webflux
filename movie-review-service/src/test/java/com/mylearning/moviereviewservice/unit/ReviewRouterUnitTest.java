package com.mylearning.moviereviewservice.unit;

import com.mylearning.moviereviewservice.domain.Review;
import com.mylearning.moviereviewservice.exceptionhandler.GlobalErrorHandler;
import com.mylearning.moviereviewservice.handler.ReviewHandler;
import com.mylearning.moviereviewservice.repository.ReviewRepository;
import com.mylearning.moviereviewservice.router.ReviewRouter;
import com.mylearning.moviereviewservice.validator.ReviewValidator;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@WebFluxTest
@ContextConfiguration(classes = {ReviewRouter.class, ReviewHandler.class, ReviewValidator.class, GlobalErrorHandler.class})
@AutoConfigureWebTestClient
public class ReviewRouterUnitTest {

    @MockitoBean
    private ReviewRepository reviewRepository;

    @Autowired
    private WebTestClient webTestClient;


    @Test
    void addReview() {

        //given
        var review = new Review(null, 1L, "Awesome Movie", 9.0);

        Mockito.when(reviewRepository.save(Mockito.any(Review.class))).thenReturn(Mono.just(new Review("abc", 1L, "Awesome Movie", 9.0)));

        //when
        webTestClient
                .post()
                .uri("/api/v1/review")
                .bodyValue(review)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Review.class)
                .consumeWith(reviewResponse -> {
                    var savedReview = reviewResponse.getResponseBody();
                    assert savedReview != null;
                    assert savedReview.getReviewId() != null;
                    assertEquals("abc", savedReview.getReviewId());
                });
    }


    @Test
    void getAllReviews1() {
        //given
        var reviewList = List.of(
                new Review("1", 1L, "Awesome Movie", 9.0),
                new Review("2", 1L, "Awesome Movie1", 9.0),
                new Review("3", 2L, "Excellent Movie", 8.0));

        Mockito.when(reviewRepository.findAll()).thenReturn(Flux.fromIterable(reviewList));

        //when
        webTestClient
                .get()
                .uri("/api/v1/review/stream")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Review.class)
                .value(reviews -> {
                    assertEquals(3, reviews.size());
                });
    }


    @Test
    void getAllReviews2() {
        //given
        var reviewList = List.of(
                new Review("1", 1L, "Awesome Movie", 9.0),
                new Review("2", 1L, "Awesome Movie1", 9.0),
                new Review("3", 2L, "Excellent Movie", 8.0));

        Mockito.when(reviewRepository.findAll()).thenReturn(Flux.fromIterable(reviewList));

        //when
        webTestClient
                .get()
                .uri("/api/v1/review/stream")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Review.class)
                .consumeWith(reviews -> {
                    var response = reviews.getResponseBody();
                    Assertions.assertThat(response).isNotNull();
                    Assertions.assertThat(response).isNotEmpty();
                });
    }

    @Test
    void updateReview() {

        //given
        var reviewUpdate = new Review(null, 1L, "Not an Awesome Movie", 8.0);

        Mockito.when(reviewRepository.save(Mockito.isA(Review.class))).thenReturn(Mono.just(new Review("abc", 1L, "Not an Awesome Movie", 8.0)));
        Mockito.when(reviewRepository.findById((String) Mockito.any())).thenReturn(Mono.just(new Review("abc", 1L, "Not an Awesome Movie", 9.0)));

        //when
        webTestClient
                .put()
                .uri("/api/v1/review/{id}", "abc")
                .bodyValue(reviewUpdate)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Review.class)
                .consumeWith(reviewResponse -> {
                    var updatedReview = reviewResponse.getResponseBody();
                    assert updatedReview != null;
                    System.out.println("updatedReview : " + updatedReview);
                    assertEquals(8.0, updatedReview.getRating());
                    assertEquals("Not an Awesome Movie", updatedReview.getComment());
                });
    }


    @Test
    void deleteReview() {

        //given
        var reviewId = "abc";
        Mockito.when(reviewRepository.findById((String) Mockito.any())).thenReturn(Mono.just(new Review("abc", 1L, "Awesome Movie", 9.0)));
        Mockito.when(reviewRepository.delete(Mockito.any(Review.class))).thenReturn(Mono.empty());

        //when
        webTestClient
                .delete()
                .uri("/api/v1/review/{id}", reviewId)
                .exchange()
                .expectStatus()
                .is2xxSuccessful();
    }


    // => With Validation
    @Test
    void addReview_validation() {

        //given
        var review = new Review(null, null, "Awesome Movie", -9.0);

        Mockito.when(reviewRepository.save(Mockito.any(Review.class)))
                .thenReturn(Mono.just(new Review("abc", 1L, "Awesome Movie", 9.0)));


        //when
        webTestClient
                .post()
                .uri("/api/v1/review")
                .bodyValue(review)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .isEqualTo("rating.movieInfoId : must not be null,rating.negative : please pass a non-negative value");
    }


    @Test
    void updateReview_validation() {

        //given
        var reviewUpdate = new Review(null, 1L, "Not an Awesome Movie", 8.0);

        Mockito.when(reviewRepository.findById((String) Mockito.any())).thenReturn(Mono.empty());

        //when
        webTestClient
                .put()
                .uri("/api/v1/review/{id}", "abc")
                .bodyValue(reviewUpdate)
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody(String.class)
                .isEqualTo("Review not Found for the given Review Id: abc");
    }

    @Test
    void deleteReview_validation() {

        // Given
        var reviewId = "abc";
        Mockito.when(reviewRepository.findById((String) Mockito.any())).thenReturn(Mono.empty());

        //when
        webTestClient
                .delete()
                .uri("/api/v1/review/{id}", reviewId)
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody(String.class)
                .isEqualTo("Review not Found for the given Review Id: abc");
    }


}
