package com.mylearning.reactiveprogrammingusingreactor;

import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

public class MonoAndFluxGeneratorServiceTest {

    MonoAndFluxGeneratorService monoAndFluxGeneratorService = new MonoAndFluxGeneratorService();

    @Test
    void namesFlux1() {

        var namesFlux = monoAndFluxGeneratorService.namesFlux();
        StepVerifier.create(namesFlux)
                .expectNext("alex")
                .expectNext("ben")
                .expectNext("chloe")
                .verifyComplete();
    }

    @Test
    void namesFlux2() {

        var namesFlux = monoAndFluxGeneratorService.namesFlux();
        StepVerifier.create(namesFlux)
                .expectNext("alex")
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void namesFlux3() {

        var namesFlux = monoAndFluxGeneratorService.namesFlux();
        StepVerifier.create(namesFlux)
                .expectNext("alex", "ben", "chloe")
                .verifyComplete();
    }

    @Test
    void namesMono() {

        var stringMono = monoAndFluxGeneratorService.namesMono();
        StepVerifier.create(stringMono)
                .expectNext("alex")
                .expectComplete()
                .verify();
    }

    @Test
    void namesFlux_map() {
        var namesFlux = monoAndFluxGeneratorService.namesFlux_map();
        StepVerifier.create(namesFlux)
                .expectNext("ALEX")
                .expectNext("BEN")
                .expectNext("CHLOE")
                .verifyComplete();

    }

    @Test
    void namesFlux_immutability() {
        var namesFlux = monoAndFluxGeneratorService.namesFlux_immutability();
        StepVerifier.create(namesFlux)
                .expectNext("alex")
                .expectNext("ben")
                .expectNext("chloe")
                .verifyComplete();
    }

    @Test
    void namesMono_map_filter() {
        var namesMono = monoAndFluxGeneratorService.namesMono_map_filter(3);
        StepVerifier.create(namesMono)
                .expectNext("ALEX")
                .expectComplete()
                .verify();
    }


    @Test
    void namesFlux_flatmap() {
        var namesFlux = monoAndFluxGeneratorService.namesFlux_flatmap(2);
        StepVerifier.create(namesFlux)
                .expectNext("A")
                .expectNext("L")
                .expectNext("E")
                .expectNext("X")
                .expectNext("B")
                .expectNext("E")
                .expectNext("N")
                .expectNext("C")
                .expectNext("H")
                .expectNext("L")
                .expectNext("O")
                .expectNext("E")
                .verifyComplete();
    }

    @Test
    void name_defaultIfEmpty() {
        var name = monoAndFluxGeneratorService.name_defaultIfEmpty();
        StepVerifier.create(name)
                .expectNext("Default")
                .verifyComplete();
    }

    @Test
    void name_switchIfEmpty() {
        var name = monoAndFluxGeneratorService.name_switchIfEmpty();
        StepVerifier.create(name)
                .expectNext("Default")
                .verifyComplete();
    }

    @Test
    void namesFlux_flatmap_async() {
        // given
        int stringLength = 2;

        var namesFlux = monoAndFluxGeneratorService.namesFlux_flatmap_async(stringLength);

        StepVerifier.create(namesFlux)
                .expectNextCount(12)
                .verifyComplete();
    }
}
