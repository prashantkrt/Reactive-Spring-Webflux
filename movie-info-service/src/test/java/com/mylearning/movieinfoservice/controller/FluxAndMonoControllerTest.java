package com.mylearning.movieinfoservice.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@WebFluxTest(controllers = FluxAndMonoController.class)
@AutoConfigureWebTestClient
class FluxAndMonoControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void getFlux() {

        webTestClient.get().uri("/flux-and-mono/flux")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBodyList(Integer.class)
                .hasSize(10);
    }

    @Test
    void getFlux_approach2() {

        var flux = webTestClient
                .get()
                .uri("/flux-and-mono/flux")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .returnResult(Integer.class)
                .getResponseBody();   //getResponseBody() method returns a Flux<T>, not a Mono<T>.

        StepVerifier.create(flux)
                .expectNext(1, 2, 3, 4, 5, 4, 6, 7, 8, 9, 10)
                .expectComplete();
    }


    @Test
    void getFlux_approach3() {

        webTestClient
                .get()
                .uri("/flux-and-mono/flux")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Integer.class)// Tells WebTestClient to deserialize the response into a List<Integer>
                // consumeWith(...) allows you to inspect or assert on the deserialized body
                .consumeWith(listEntityExchangeResult -> {
                    var responseBody = listEntityExchangeResult.getResponseBody();
                    assert (responseBody != null ? responseBody.size() : 0) == 10;
                });

    }


    @Test
    void getFlux_approach4() {

        webTestClient
                .get()
                .uri("/flux-and-mono/flux")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Integer.class)
                .consumeWith(result -> {
                    var responseBody = result.getResponseBody();
                    assertNotNull(responseBody);
                    assertEquals(10, responseBody.size());
                });
    }


    @Test
    void getFluxWithDelay() {

        WebTestClient customWebTestClient = webTestClient.mutate()
                .responseTimeout(Duration.ofSeconds(120)) // Set timeout to 120 seconds
                .build();

        Flux<Integer> flux = customWebTestClient.get()
                .uri("/flux-and-mono/flux-with-delay")
                .exchange()
                .expectStatus().isOk()
                .returnResult(Integer.class)
                .getResponseBody();

        StepVerifier.create(flux)
                .expectNext(1)
                .expectNextCount(8) // Expect 2 through 9
                .expectNext(10)
                .expectComplete()
                .verify(Duration.ofSeconds(100));
    }

    @Test
    void getFluxWithDelayStream() {
        WebTestClient customWebTestClient = webTestClient.mutate()
                .responseTimeout(Duration.ofSeconds(120)) // Set timeout to 120 seconds
                .build();

        Flux<Integer> flux = customWebTestClient.get()
                .uri("/flux-and-mono/flux-with-delay")
                .exchange()
                .expectStatus().isOk()
                .returnResult(Integer.class)
                .getResponseBody();

        StepVerifier.create(flux)
                .expectNext(1)
                .expectNextCount(9) // Expect 2 through 9
                .expectComplete()
                .verify(Duration.ofSeconds(100));
    }

    @Test
    void helloWorldMono() {
        Mono<String> mono = webTestClient
                .get()
                .uri("/flux-and-mono/mono")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .returnResult(String.class)
                .getResponseBody()
                .single(); // converts Flux<T> to Mono<T>

        StepVerifier.create(mono)
                .expectNext("hello-world")
                .verifyComplete();

    }

    @Test
    void helloWorldMono2() {
        webTestClient
                .get()
                .uri("/flux-and-mono/mono")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(String.class) // This tells WebTestClient to deserialize the response body as a single String.
                .consumeWith(stringEntityExchangeResult ->{
                    var response = stringEntityExchangeResult.getResponseBody();
                    assertEquals("hello-world", response);
                });
    }


    @Test
    void stream() {
        var flux = webTestClient
                .get()
                .uri("/flux-and-mono/flux-with-interval-stream")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .returnResult(Integer.class)
                .getResponseBody();

        StepVerifier.create(flux)
                .expectNext(0, 1, 2)
                .expectNextCount(7)
                .thenCancel()
                .verify();
    }
}