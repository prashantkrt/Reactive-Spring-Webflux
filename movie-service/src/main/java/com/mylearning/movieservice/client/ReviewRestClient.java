package com.mylearning.movieservice.client;

import com.mylearning.movieservice.exception.ReviewsClientException;
import com.mylearning.movieservice.exception.ReviewsServerException;
import com.mylearning.movieservice.model.Review;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.net.ConnectException;
import java.time.Duration;

@Component
@Slf4j
public class ReviewRestClient {

    @Value("${restClient.review-service.url}")
    private String reviewServiceUrl;

    private final WebClient webClient;

    public ReviewRestClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Flux<Review> retrieveReviews(String movieId) {
        if (movieId == null || movieId.trim().isEmpty()) {
            return Flux.error(new IllegalArgumentException("movieId must not be null or empty"));
        }

        var url = UriComponentsBuilder.fromHttpUrl(reviewServiceUrl)
                .queryParam("movieInfoId", movieId)
                .buildAndExpand()
                .toUriString();

        //ResponseSpec onStatus(Predicate<HttpStatusCode> statusPredicate, Function<ClientResponse, Mono<? extends Throwable>> exceptionFunction);
        return webClient.get()
                .uri(url)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {
                    log.error("4xx error, status code: {}", clientResponse.statusCode().value());
                    if (clientResponse.statusCode().equals(HttpStatus.NOT_FOUND)) {
                        return Mono.empty();
                    }
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(response -> Mono.error(new ReviewsClientException("Client error: " + response)));
                })
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> {
                    log.error("5xx error, status code: {}", clientResponse.statusCode().value());
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(response -> Mono.error(new ReviewsServerException("Server error: " + response)));
                })
                .bodyToFlux(Review.class)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                        .filter(throwable -> throwable instanceof ReviewsServerException || throwable instanceof WebClientRequestException webClientRequestException && webClientRequestException.getCause() instanceof ConnectException)
                        .onRetryExhaustedThrow((spec, signal) -> {
                            log.error("Retry exhausted");
                            return signal.failure();
                        }))
                .log();
    }

}

