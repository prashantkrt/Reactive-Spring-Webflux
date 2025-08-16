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
import reactor.core.publisher.Mono;

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
                .log();
    }

}
