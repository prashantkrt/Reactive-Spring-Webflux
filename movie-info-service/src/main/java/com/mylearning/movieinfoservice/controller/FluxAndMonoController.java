package com.mylearning.movieinfoservice.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@RestController
@RequestMapping("/flux-and-mono")
public class FluxAndMonoController {

    @GetMapping("flux")
    public Flux<Integer> getFlux() {
        return Flux.range(1, 10).log();
    }

    // will generate the result all at once with a delay of 10*10 seconds
    @GetMapping("flux-with-delay")
    public Flux<Integer> getFluxWithDelay() {
        return Flux.range(1, 10).delayElements(Duration.ofSeconds(10)).log();
    }


    // Will generate the result one by one stream way with a dealy of 10 seconds each
    @GetMapping(value= "flux-with-delay-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    // @GetMapping(value= "flux-with-delay-stream", produces = "text/event-stream")
    public Flux<Integer> getFluxWithDelayStream() {
        return Flux.range(1, 10).delayElements(Duration.ofSeconds(10)).log();
    }

    @GetMapping("/mono")
    public Mono<String> helloWorldMono(){
        return Mono.just("hello-world");
    }

    // Flux.interval(Duration.ofSeconds(1)) creates an infinite stream of Long values, starting from 0, emitting every 1 second.
    // Each emitted value is the next number in sequence: 0, 1, 2, 3, ...
    @GetMapping(value = "/flux-with-interval-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Long> stream(){
        return Flux.interval(Duration.ofSeconds(1))
                .log();
    }

}
