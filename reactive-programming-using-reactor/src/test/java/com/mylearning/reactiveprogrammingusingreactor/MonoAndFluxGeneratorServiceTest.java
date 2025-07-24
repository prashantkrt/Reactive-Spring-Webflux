package com.mylearning.reactiveprogrammingusingreactor;

import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;

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

    @Test
    void namesFlux_concatmap() {
        //given
        int stringLength = 2;

        var namesFlux = monoAndFluxGeneratorService.namesFlux_concatmap(stringLength);
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
    void namesMono_flatmap() {

        //given
        int stringLength = 3;

        //when
        var namesFlux = monoAndFluxGeneratorService.namesMono_flatmap(stringLength).log();

        //then
        StepVerifier.create(namesFlux)
                .expectNext(List.of("A", "L", "E", "X"))
                .verifyComplete();

    }

    @Test
    void namesMono_flatmapMany() {
        //given
        int stringLength = 3;

        //when
        var namesFlux = monoAndFluxGeneratorService.namesMono_flatmapMany(stringLength).log();

        //then
        StepVerifier.create(namesFlux)
                .expectNext("A", "L", "E", "X")
                .verifyComplete();
    }

    @Test
    void namesFlux_transform() {

        //given
        int stringLength = 3;

        //when
        var namesFlux = monoAndFluxGeneratorService.namesFlux_transform(stringLength).log();

        //then
        StepVerifier.create(namesFlux)
                .expectNext("A", "L", "E", "X")
                .expectNextCount(5) // C H L O E
                .verifyComplete();

    }

    @Test
    void namesFlux_transform2() {

        //given
        int stringLength = 6;

        //when
        var namesFlux = monoAndFluxGeneratorService.namesFlux_transform(stringLength).log();

        //then
        StepVerifier.create(namesFlux)
                .expectNext("default")
                .verifyComplete();

    }

    @Test
    void namesFlux_transform_switchIfEmpty() {

        //given
        int stringLength = 6;

        //when
        var namesFlux = monoAndFluxGeneratorService.namesFlux_transform_switchIfEmpty(stringLength).log();

        //then
        StepVerifier.create(namesFlux)
                .expectNext("D", "E", "F", "A", "U", "L", "T")
                .verifyComplete();

    }

    @Test
    void namesFlux_transform_concatWith() {

        //given
        int stringLength = 3;

        //when
        var namesFlux = monoAndFluxGeneratorService.namesFlux_transform_concatWith(stringLength).log();

        //then
        StepVerifier.create(namesFlux)
                .expectNext("4-ALEX", "5-CHLOE", "4-ANNA")
                .verifyComplete();

    }

    @Test
    void testNamesFlux_transform() {
        var namesFlux = monoAndFluxGeneratorService.namesFlux_transform();
        StepVerifier.create(namesFlux)
                .expectNextCount(3)
                .verifyComplete();
    }


    @Test
    void explore_concat() {

        //when
        var value = monoAndFluxGeneratorService.explore_concat();

        //then
        StepVerifier.create(value)
                .expectNext("A", "B", "C", "D", "E", "F")
                .verifyComplete();

    }


    @Test
    void explore_concatWith() {

        //when
        var value = monoAndFluxGeneratorService.explore_concatWith();

        //then
        StepVerifier.create(value)
                .expectNext("A", "B", "C", "D", "E", "F")
                .verifyComplete();

    }

    @Test
    void explore_concat_mono() {

        //given

        //when
        var value = monoAndFluxGeneratorService.explore_concatWith_mono();

        //then
        StepVerifier.create(value)
                .expectNext("A", "B")
                .verifyComplete();

    }

    @Test
    void explore_merge() {

        //given

        //when
        var value = monoAndFluxGeneratorService.explore_merge();

        //then
        StepVerifier.create(value)
                // .expectNext("A", "B", "C", "D", "E", "F")
                .expectNext("A", "D", "B", "E", "C", "F")
                .verifyComplete();

    }

    @Test
    void explore_mergeWith() {

        //given

        //when
        var value = monoAndFluxGeneratorService.explore_mergeWith();

        //then
        StepVerifier.create(value)

                .expectNext("A", "D", "B", "E", "C", "F")
                .verifyComplete();

    }

    @Test
    void explore_mergeWith_mono() {

        //given

        //when
        var value = monoAndFluxGeneratorService.explore_mergeWith_mono();

        //then
        StepVerifier.create(value)

                .expectNext("A", "B")
                .verifyComplete();

    }


    @Test
    void explore_mergeWith_delay() {

        var value = monoAndFluxGeneratorService.explore_mergeWith_delay();
        StepVerifier.withVirtualTime(() -> value)
                .thenAwait(Duration.ofSeconds(10))
                .expectNextCount(6)
                .verifyComplete();
        //20:55:18.224 [parallel-1] INFO reactor.Flux.Merge.1 -- onNext(A)
        //20:55:18.321 [parallel-2] INFO reactor.Flux.Merge.1 -- onNext(D)
        //20:55:18.325 [parallel-3] INFO reactor.Flux.Merge.1 -- onNext(B)
        //20:55:18.425 [parallel-5] INFO reactor.Flux.Merge.1 -- onNext(C)
        //20:55:18.523 [parallel-4] INFO reactor.Flux.Merge.1 -- onNext(E)
        //20:55:18.729 [parallel-6] INFO reactor.Flux.Merge.1 -- onNext(F)
    }

    @Test
    void explore_mergeSequential() {
        var value = monoAndFluxGeneratorService.explore_mergeSequential();
        StepVerifier.create(value)
                .expectNext("A", "B", "C", "D", "E", "F")
                .verifyComplete();
    }

    @Test
    void explore_zip() {


        //when
        var value = monoAndFluxGeneratorService.explore_zip().log();

        //then
        StepVerifier.create(value)
                .expectNext("AD", "BE", "CF")
                .verifyComplete();

    }

    @Test
    void explore_zip_1() {

        //when
        var value = monoAndFluxGeneratorService.explore_zip_1().log();

        //then
        StepVerifier.create(value)
                .expectNext("AD14", "BE25", "CF36")
                .verifyComplete();

    }

    @Test
    void explore_zip_2() {

        //when
        var value = monoAndFluxGeneratorService.explore_zip_2().log();

        //then
        StepVerifier.create(value)
                .expectNext("AB")
                .verifyComplete();

    }

    @Test
    void explore_zip_3() {

        //when
        var value = monoAndFluxGeneratorService.explore_zip_2().log();

        //then
        StepVerifier.create(value)
                .expectNext("AB")
                .verifyComplete();

    }

    @Test
    void explore_zipWith() {

        //when
        var value = monoAndFluxGeneratorService.explore_zipWith().log();

        //then
        StepVerifier.create(value)
                .expectNext("AD", "BE", "CF")
                .verifyComplete();

    }

    @Test
    void explore_zipWith_mono() {

        //when
        var value = monoAndFluxGeneratorService.explore_zipWith_mono().log();

        //then
        StepVerifier.create(value)
                .expectNext("AB")
                .verifyComplete();

    }

    @Test
    void explore_zipWithMap() {
        var value = monoAndFluxGeneratorService.explore_zipWithMap();

        StepVerifier.create(value)
                .expectNext("AD", "BE", "CF")
                .verifyComplete();
    }
}
