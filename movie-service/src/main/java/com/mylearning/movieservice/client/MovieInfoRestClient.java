package com.mylearning.movieservice.client;

import com.mylearning.movieservice.exception.MoviesInfoClientException;
import com.mylearning.movieservice.exception.MoviesInfoServerException;
import com.mylearning.movieservice.model.MovieInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.net.ConnectException;
import java.time.Duration;

@Component
@Slf4j
public class MovieInfoRestClient {

    @Value("${restClient.movie-info-service.url}")
    private String movieInfoServiceUrl;

    private final WebClient webClient;

    public MovieInfoRestClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<MovieInfo> retrieveMovieInfo(String movieId) {
        var url = movieInfoServiceUrl.concat("/{movieId}");

        return webClient.get()
                .uri(url, movieId)
                .retrieve()
                .onStatus(httpStatus -> httpStatus.is4xxClientError(), clientResponse -> {
                    log.error("Client Exception in MovieInfoService: " + clientResponse.statusCode().value());
                    if (clientResponse.statusCode().equals(HttpStatus.NOT_FOUND)) { // 404
                        return Mono.error(new MoviesInfoClientException(
                                "There is no MovieInfo available for the passed in Id : " + movieId,
                                clientResponse.statusCode().value()
                        ));
                    }
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(response -> Mono.error(new MoviesInfoClientException(
                                    response, clientResponse.statusCode().value()
                            )));
                })
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> {
                    log.error("Server Exception in MovieInfoService: " + clientResponse.statusCode().value());
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(response -> Mono.error(new MoviesInfoServerException(
                                    "Server Exception in MovieInfoService: " + response,
                                    clientResponse.statusCode().value()
                            )));
                })
                .bodyToMono(MovieInfo.class) // decode only if 2xx success
                // retry 3 times with fixed delay
                .retryWhen(
                        Retry.fixedDelay(3, Duration.ofSeconds(2))
                                .filter(ex -> ex instanceof MoviesInfoServerException ||ex instanceof WebClientRequestException wcre && wcre.getCause() instanceof ConnectException) // only retry for server exceptions
                                //.onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> retrySignal.failure())  // after 3 retries,throw the last exception
                                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                                    //  max number of retries
                                    System.out.println("Configured retries: " + retryBackoffSpec.maxAttempts);
                                    // Using retrySignal (runtime info)
                                    System.out.println("Retries actually done: " + retrySignal.totalRetries());
                                    System.out.println("Last exception: " + retrySignal.failure().getMessage());
                                    return retrySignal.failure(); // propagate last failure, .failure() returns that last exception.
                                }))
                .log();

        /*
        | Attempt | Action                        | Output                                                         |
        | ------- | ----------------------------- | -------------------------------------------------------------- |
        | 1       | Call fails                    | —                                                              |
        | 2       | Retry #1                      | —                                                              |
        | 3       | Retry #2                      | —                                                              |
        | 4       | Retry #3 (last attempt) fails | Prints "Retries configured: 3" and "Number of retries done: 3" |
        */
    }

}
