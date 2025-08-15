package com.mylearning.movieservice.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@Slf4j
public class MovieInfoRestClient {

    @Value("${restClient.movie-info-service.url}")
    private String movieInfoServiceUrl;

    private final WebClient webClient;

    public MovieInfoRestClient(WebClient webClient) {
        this.webClient = webClient;
    }
}
