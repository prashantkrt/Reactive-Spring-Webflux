package com.mylearning.moviereviewservice.handler;

import com.mylearning.moviereviewservice.domain.Review;
import com.mylearning.moviereviewservice.exception.ReviewNotFoundException;
import com.mylearning.moviereviewservice.repository.ReviewRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class ReviewHandler {

    private final ReviewRepository reviewRepository;

    public ReviewHandler(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

// Got error: If reviewRepository.save(review) is reactive (returns Mono<Review>), then bodyValue(...) is wrong, because bodyValue expects a plain object, not a Mono.

//    Inside bodyValue(...), you’re passing reviewRepository.save(review), which is a Mono<Review>.
//    bodyValue(...) does not accept reactive types — it expects a plain object like Review, String, etc.
//    That’s why Spring throws: 'body' should be an object, for reactive types use a variant specifying a publisher...


//    public Mono<ServerResponse> addReview(ServerRequest serverRequest) {
//        log.info("ReviewHandler.createReview");
//
//        return serverRequest.bodyToMono(Review.class) //Mono<Review>
//                .flatMap(review -> {  // Mono<ServerResponse>
//                    return ServerResponse.status(HttpStatus.CREATED).bodyValue(reviewRepository.save(review));
//                });
//    }

    // Fixed code
    // First flatMap unwraps the Mono<Review> from save() into a Review object for the next step.
    // The Second flatMap sends the actual object (Review) into bodyValue(...).
    // bodyValue is happy because it gets a plain Review.
    public Mono<ServerResponse> addReview(ServerRequest serverRequest) {
        log.info("ReviewHandler.createReview");

        return serverRequest.bodyToMono(Review.class) // Mono<Review>
                .flatMap(review -> reviewRepository.save(review))
                .flatMap(review -> ServerResponse.status(HttpStatus.CREATED).bodyValue(review));
    }

    public Mono<ServerResponse> getReview(ServerRequest serverRequest) {
        log.info("ReviewHandler.getReview");
        String id = serverRequest.pathVariable("id");
        return ServerResponse.ok().body(reviewRepository.findById(id), Review.class);
    }

    public Mono<ServerResponse> getAllReview(ServerRequest serverRequest) {
        log.info("ReviewHandler.getAllReview");
        return ServerResponse.ok().body(reviewRepository.findAll(), Review.class);
    }

    public Mono<ServerResponse> updateReview(ServerRequest serverRequest) {
        log.info("ReviewHandler.updateReview");
        String id = serverRequest.pathVariable("id");

        return serverRequest.bodyToMono(Review.class)
                .flatMap(updatedReview ->
                        reviewRepository.findById(id)
                                .switchIfEmpty(Mono.error(new ReviewNotFoundException(
                                        "Review not Found for the given Review Id: " + id
                                )))
                                .flatMap(existingReview -> {
                                    existingReview.setComment(updatedReview.getComment());
                                    existingReview.setRating(updatedReview.getRating());
                                    existingReview.setMovieInfoId(updatedReview.getMovieInfoId());

                                    return reviewRepository.save(existingReview);
                                })
                )
                .flatMap(savedReview ->
                        ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(savedReview)
                );
    }


    public Mono<ServerResponse> deleteReview(ServerRequest serverRequest) {
        log.info("ReviewHandler.deleteReview");
        String id = serverRequest.pathVariable("id");

        return reviewRepository.findById(id)
                .switchIfEmpty(Mono.error(new ReviewNotFoundException(
                        "Review not Found for the given Review Id: " + id
                )))
                .flatMap(review -> reviewRepository.delete(review)
                        .then(ServerResponse.ok()
                                .bodyValue("Review deleted successfully"))
                );
    }
}


