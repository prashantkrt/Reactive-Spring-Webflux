package com.mylearning.reactiveprogrammingusingreactor;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

public class FluxToMono {
    public static void main(String[] args) {

    }

    private static void fluxToMono1() {
        Flux<String> flux = Flux.just("A", "B", "C");

        // First element
        Mono<String> first = flux.next();  // Emits "A"

        // Or equivalent:
        Mono<String> firstAlt = flux.take(1).single();
    }

    private static void fluxToMono2() {
        Flux<String> flux = Flux.just("A", "B", "C");

        Mono<String> first = flux.next();        // "A"

        Mono<String> first2 = flux.elementAt(0); // "A"

        Mono<String> last = flux.last();         // "C"
    }

    private static void fluxToMono3() {
        Flux<String> flux = Flux.just("A", "B", "C");

        Mono<List<String>> listMono = flux.collectList();
        // Emits: ["A", "B", "C"]
    }

    private static void fluxToMono4() {
        Flux<String> flux = Flux.just("A", "B", "C");

        Mono<String> reduced = flux.reduce((a, b) -> a + b);
       // Emits: "ABC"

        Mono<String> reducedWithInit = flux.reduce("Start-", (a, b) -> a + b);
        // Emits: "Start-ABC"

        Mono<String> joined = flux.collect(Collectors.joining(", "));
        // Emits: "A, B, C"
    }
}
