package com.mylearning.moviereviewservice.handler;

import com.mylearning.moviereviewservice.domain.Review;
import com.mylearning.moviereviewservice.exception.ReviewDataException;
import com.mylearning.moviereviewservice.exception.ReviewNotFoundException;
import com.mylearning.moviereviewservice.repository.ReviewRepository;
import com.mylearning.moviereviewservice.validator.ReviewValidator;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ReviewHandler {

    // Approach 1
    @Autowired
    private ReviewValidator reviewValidator;

    // Approach 2
    @Autowired
    private Validator validator;

    private final ReviewRepository reviewRepository;

    public ReviewHandler(ReviewRepository reviewRepositor) {
        this.reviewRepository = reviewRepositor;
    }
    Sinks.Many<Review> reviewsSink = Sinks.many().replay().latest();

// Got an error: If reviewRepository.save(review) is reactive (returns Mono<Review>), then bodyValue(...) is wrong, because bodyValue expects a plain object, not a Mono.

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
                .doOnNext(this::validate2) // review -> validate(review)
                .doOnNext(review -> {
                    reviewsSink.tryEmitNext(review);
                })
                .flatMap(reviewRepository::save) // review -> reviewRepository.save(review)
                .flatMap(review -> ServerResponse.status(HttpStatus.CREATED).bodyValue(review));
    }


    public Mono<ServerResponse> getReviewsStream(ServerRequest serverRequest) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_NDJSON)
                .body(reviewsSink.asFlux(), Review.class)
                .log();
    }

    // custom Validation class implementation
    // using org.springframework.validation
    // org.springframework.validation.Validator interface, used for manual, programmatic validation.
    private void validate1(Review review) {
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(review, Review.class.getName());
        reviewValidator.validate(review, errors);
        if (errors.hasErrors()) {
            String errorMessage = errors.getAllErrors()
                    .stream()
                    .map(error -> error.getCode() + " : " + error.getDefaultMessage())
                    .sorted()
                    .collect(Collectors.joining(","));
            log.error("errorMessage : {} ", errorMessage);
            throw new ReviewNotFoundException(errorMessage);
        }
    }

    // using jakarta.validation.Validator
    private void validate2(Review review) {
        Set<ConstraintViolation<Review>> violations = validator.validate(review);
        if (!violations.isEmpty()) {
            String errorMessage = violations.stream()
                    .map(error -> error.getMessage())
                    .sorted()
                    .collect(Collectors.joining(","));
            log.error("errorMessage from validator2 : {} ", errorMessage);
            throw new ReviewDataException(errorMessage);
        }
    }

    public Mono<ServerResponse> getReview(ServerRequest serverRequest) {
        log.info("ReviewHandler.getReview");
        String id = serverRequest.pathVariable("id");
        return reviewRepository.findById(id)
                .flatMap(review -> ServerResponse.ok().bodyValue(review))
                .switchIfEmpty(Mono.error(new ReviewNotFoundException("Review not Found for the given Review Id: " + id)));// return 404 if no review
//                .onErrorResume(ex -> {
//                    log.error("Error fetching review for id: {}", id, ex);
//                    return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                            .bodyValue("Something went wrong while fetching the review");
//                });
    }

    public Mono<ServerResponse> getAllReview(ServerRequest serverRequest) {
        log.info("ReviewHandler.getAllReview");
        return reviewRepository.findAll()
                .switchIfEmpty(Mono.error(new ReviewNotFoundException("No reviews found")))
                .collectList()
                .flatMap(reviews -> ServerResponse.ok().bodyValue(reviews));
    }

    //http://localhost:8081/api/v1/review/search?movieInfoId=101
    public Mono<ServerResponse> getReviewByMovieInfoId(ServerRequest serverRequest) {
        log.info("ReviewHandler.getReviewByMovieInfoId");
        //queryParam() is a method of ServerRequest used to retrieve query parameters from the request URL.
        //A query parameter is part of the URL that comes after the ? and is usually used to pass optional data to the server.
        Optional<String> movieInfoId = serverRequest.queryParam("movieInfoId");
        log.info("movieInfoId: {}", movieInfoId);

        // Check if movieInfoId is present and not blank
        if (movieInfoId.isEmpty() || movieInfoId.get().isBlank()) {
            return ServerResponse.badRequest()
                    .bodyValue("Query parameter 'movieInfoId' is required");
        }
        Flux<Review> reviewsFlux = reviewRepository.findReviewsByMovieInfoId(Long.valueOf(movieInfoId.get()));

        // Asynchronous check for empty results
        return reviewsFlux.hasElements() // hasElements() returns a Mono<Boolean>
                .flatMap(hasElements -> {
                    if (hasElements) {
                        return ServerResponse.ok().body(reviewsFlux, Review.class);
                    } else {
                        return ServerResponse.status(HttpStatus.NOT_FOUND)
                                .bodyValue("No reviews found for movieInfoId: " + movieInfoId);
                    }
                });

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

    public Mono<ServerResponse> updateReview2(ServerRequest serverRequest) {
        log.info("ReviewHandler.updateReview2");
        var id = serverRequest.pathVariable("id");

        return reviewRepository.findById(id)
                .switchIfEmpty(Mono.error(new ReviewNotFoundException(
                        "Review not Found for the given Review Id: " + id
                )))
//                .flatMap(existingReview -> serverRequest.bodyToMono(Review.class)
//                        .map(updatedReview -> {
//                            // update existing review with incoming data
//                            existingReview.setComment(updatedReview.getComment());
//                            existingReview.setRating(updatedReview.getRating());
//                            existingReview.setMovieInfoId(updatedReview.getMovieInfoId());
//                            return existingReview;
//                        })
//                )
//                .flatMap(reviewRepository::save)
//                .flatMap(savedReview -> ServerResponse.ok()
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .bodyValue(savedReview)
//                );

                //or this both are same
                .flatMap(existingReview -> serverRequest.bodyToMono(Review.class)
                        .map(updatedReview -> {
                            // update existing review with incoming data
                            existingReview.setComment(updatedReview.getComment());
                            existingReview.setRating(updatedReview.getRating());
                            existingReview.setMovieInfoId(updatedReview.getMovieInfoId());
                            return existingReview;
                        })
                        .flatMap(reviewRepository::save)
                        .flatMap(savedReview -> ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(savedReview)
                        )
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


