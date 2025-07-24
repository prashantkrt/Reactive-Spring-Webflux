package com.mylearning.reactiveprogrammingusingreactor;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

public class MonoAndFluxGeneratorService {

    public Flux<String> namesFlux() {
        var namesList = List.of("alex", "ben", "chloe");
        return Flux.fromIterable(namesList).log(); // coming from a db or remote service
    }

    // Immutability
    // Reactor types like Flux and Mono are immutable
    // Operators like map, filter, flatMap, etc. always return a new Flux/Mono.
    public Flux<String> namesFlux_immutability() {
        var namesList = List.of("alex", "ben", "chloe");
        var namesFlux = Flux.fromIterable(namesList);
        namesFlux.map(String::toUpperCase); // this will give new flux instead of changing the existing namesFlux   <- NEW Flux created, not stored
        return namesFlux; // will emit  "alex", "ben", "chloe"

        // namesFlux.map(...) creates a new transformed Flux.
        // But we didn't store or return it.
        //So namesFlux remains unchanged, and still emits "alex", "ben", "chloe".
    }

    public Mono<String> namesMono() {
        return Mono.just("alex").log(); // mono returns a single value
    }

    public Flux<String> namesFlux_map() {
        var namesList = List.of("alex", "ben", "chloe");
        return Flux.fromIterable(namesList)
                .map(String::toUpperCase);
    }

    public Mono<String> namesMono_map_filter(int stringLength) {
        return Mono.just("alex")
                .map(String::toUpperCase)
                .filter(s -> s.length() > stringLength)
                .defaultIfEmpty("default");
    }

    public Flux<String> namesFlux_flatmap(int stringLength) {
        var namesList = List.of("alex", "ben", "chloe"); // a, l, e , x
        return Flux.fromIterable(namesList)
                //.map(s -> s.toUpperCase())
                .map(String::toUpperCase) // ALEX , BEN , CHLOE
                .filter(s -> s.length() > stringLength) // Length 2 then ALEX , BEN , CHLOE
                // ALEX,CHLOE -> A, L, E, X, B, E, N, C, H, L, O, E
                .flatMap(s -> splitString(s)); // Function<T, Publisher<R>> takes input type T and returns Flux<T>
    }

    private Flux<String> splitString(String name) {
        var charArray = name.split("");
        return Flux.fromArray(charArray);
    }

    // flatmap with Mono
    public Mono<List<String>> namesMono_flatmap(int stringLength) {
        return Mono.just("alex")
                .map(String::toUpperCase)
                .filter(s -> s.length() > stringLength)
                .flatMap(this::splitStringMono); //Mono<List of A, L, E  X>
    }
    private Mono<List<String>> splitStringMono(String s) {
        var charArray = s.split("");
        return Mono.just(List.of(charArray))
                .delayElement(Duration.ofSeconds(1));
    }


    // Mono to flux with flatmap
    public Flux<String> namesMono_flatmapMany(int stringLength) {
        Flux<String> flux = Mono.just("alex")
                //.map(s -> s.toUpperCase())
                .map(String::toUpperCase)
                .flatMapMany(this::splitString_withDelay);
        return flux;
    }

    // The Async nature of flatmap as order is not guaranteed.
    // Works asynchronously and concurrently — it does not preserve the order of the emitted inner elements
    // emissions are interleaved
    // Executes inner Publishers concurrently (asynchronously).
    // Good for parallel execution when order does not matter.
    public Flux<String> namesFlux_flatmap_async(int stringLength) {
        var namesList = List.of("alex", "ben", "chloe"); // a, l, e , x
        return Flux.fromIterable(namesList)
                //.map(s -> s.toUpperCase())
                .map(String::toUpperCase)
                .filter(s -> s.length() > stringLength)
                .flatMap(this::splitString_withDelay);
    }

    // same as flatmap but it preserves the order.
    // Waits for one inner Publisher to complete before subscribing to the next.
    // Executes inner Publishers sequentially (one after another).
    public Flux<String> namesFlux_concatmap(int stringLength) {
        var namesList = List.of("alex", "ben", "chloe"); // a, l, e , x
        return Flux.fromIterable(namesList)
                //.map(s -> s.toUpperCase())
                .map(String::toUpperCase)
                .filter(s -> s.length() > stringLength)
                //.flatMap((name)-> splitString(name));
                .concatMap(this::splitString_withDelay);
    }


    private Flux<String> splitString_withDelay(String name) {
        var delay = new Random().nextInt(1000);
        var charArray = name.split("");
        return Flux.fromArray(charArray)
                .delayElements(Duration.ofMillis(delay));
    }

    public Mono<String> name_defaultIfEmpty() {
        return Mono.<String>empty() // db or rest call
                .defaultIfEmpty("Default");
    }

    public Mono<String> name_switchIfEmpty() {
        Mono<String> defaultMono = Mono.just("Default");
        return Mono.<String>empty() // db or rest call
                .switchIfEmpty(defaultMono);
    }

    public Flux<String> namesFlux_transforming(Integer length) {
        var namesList = List.of("alex", "ben", "chloe");
        return Flux.fromIterable(namesList)
                .map(String::toUpperCase)
                .filter(name -> name.length() > length)
                .map(s -> s.length() + "-" + s)
                .doOnError(e -> {
                    System.out.println("Error is : " + e);
                })
                .doOnNext(name -> {
                    System.out.println("name is : " + name);
                })
                .doOnSubscribe(s -> {
                    System.out.println("Subscription  is : " + s);
                })
                .doOnComplete(() -> {
                    System.out.println("Completed sending all the items.");
                })
                .doFinally((signalType) -> {
                    System.out.println("value is : " + signalType);
                })
                .defaultIfEmpty("default");
    }

    // public static Function<Flux<String>, Flux<String>> transformFlux() {
    //     return new Function<Flux<String>, Flux<String>>() {
    //         @Override
    //         public Flux<String> apply(Flux<String> flux) {
    //            return flux
    //                    .map(String::toUpperCase)                           // Convert to UPPERCASE
    //                    .filter(name -> name.length() > 3)                  // Keep names longer than 3
    //                    .map(name -> name.length() + "-" + name)           // Prefix length
    //                    .doOnError(e -> System.out.println("Error is: " + e))
    //                    .doOnNext(name -> System.out.println("Name is: " + name))
    //                    .doOnSubscribe(sub -> System.out.println("Subscribed: " + sub))
    //                    .doOnComplete(() -> System.out.println("All items sent"));
    //        }
    //    };
    //}

    // public Function<Flux<String>, Flux<String>> logic(String name) {
    //    return flux -> flux
    //            .map(String::toUpperCase)
    //            .filter(s -> s.length() > 3)
    //            .map(s -> s.length() + "-" + s)
    //            .doOnError(e -> System.out.println("Error is : " + e))
    //            .doOnNext(name1 -> System.out.println("name is : " + name1))
    //            .doOnSubscribe(s -> System.out.println("Subscription is : " + s))
    //            .doOnComplete(() -> System.out.println("Completed sending all the items."));
    //}

    // Using the above method with transform()
    // input as String and return as Flux
    public <T> Function<Flux<T>, Flux<T>> logic(String name) {

        return new Function<Flux<T>, Flux<T>>() {
            @Override
            public Flux<T> apply(Flux<T> flux) {
                return flux
                        .doOnError(e -> {
                            System.out.println("Error is : " + e);
                        })
                        .doOnNext(name1 -> {
                            System.out.println("name is : " + name1);
                        })
                        .doOnSubscribe(s -> {
                            System.out.println("Subscription  is : " + s);
                        })
                        .doOnComplete(() -> {
                            System.out.println("Completed sending all the items.");
                        });
            }
        };
    }
// below is the same as above but better and clean
//    public <T> Function<Flux<T>, Flux<T>> logic(String label) {
//        return flux -> flux
//                .doOnError(e -> {
//                    System.out.println("[" + label + "] Error: " + e);
//                })
//                .doOnNext(item -> {
//                    System.out.println("[" + label + "] Received item: " + item);
//                })
//                .doOnSubscribe(s -> {
//                    System.out.println("[" + label + "] Subscribed: " + s);
//                })
//                .doOnComplete(() -> {
//                    System.out.println("[" + label + "] Completed");
//                });
//    }

    public Flux<String> namesFlux_transform() {
        var namesList = List.of("alex", "ben", "chloe");
        return Flux.fromIterable(namesList)
                .transform(logic("Subscriber"));
    }

    // better
    public Flux<String> namesFlux_transform(int stringLength) {

        Function<Flux<String>, Flux<String>> filterMap = name -> name.map(String::toUpperCase)
                .filter(s -> s.length() > stringLength);

        var namesList = List.of("alex", "ben", "chloe");
        return Flux.fromIterable(namesList)
                .transform(filterMap) // gives u the opportunity to combine multiple operations using a single call.
                .flatMap(this::splitString)
                .defaultIfEmpty("default");
    }

    public Flux<String> namesFlux_transform_switchIfEmpty(int stringLength) {

        Function<Flux<String>, Flux<String>> filterMap = name -> name.map(String::toUpperCase)
                .filter(s -> s.length() > stringLength)
                .flatMap(this::splitString);

        var defaultFlux = Flux.just("default")
                .transform(filterMap); //"D","E","F","A","U","L","T"

        var namesList = List.of("alex", "ben", "chloe");
        return Flux.fromIterable(namesList)
                .transform(filterMap) // gives u the opportunity to combine multiple operations using a single call.
                .switchIfEmpty(defaultFlux);
    }

    public Flux<String> namesFlux_transform_concatWith(int stringLength) {
        Function<Flux<String>, Flux<String>> filterMap = name -> name.map(String::toUpperCase)
                .filter(s -> s.length() > stringLength)
                .map(s -> s.length() + "-" + s);

        var namesList = List.of("alex", "ben", "chloe");
        var flux1 = Flux.fromIterable(namesList)
                .transform(filterMap);

        var flux2 = flux1.concatWith(Flux.just("anna")
                .transform(filterMap));

        return flux2;  // "4-ALEX", "5-CHLOE", "4-ANNA" if length given as 4
    }


    // "A", "B", "C", "D", "E", "F"
    public Flux<String> explore_concat() {

        var abcFlux = Flux.just("A", "B", "C");

        var defFlux = Flux.just("D", "E", "F");

        return Flux.concat(abcFlux, defFlux);
    }


    // "A", "B", "C", "D", "E", "F"
    public Flux<String> explore_concatWith() {

        var abcFlux = Flux.just("A", "B", "C");

        var defFlux = Flux.just("D", "E", "F");

        return abcFlux.concatWith(defFlux).log();
    }

    public Flux<String> explore_concatWith_mono() {

        var aMono = Mono.just("A");

        var bMono = Flux.just("B");

        return aMono.concatWith(bMono);
    }

    // "A", "D", "B", "E", "C", "F"
    // Flux is subscribed early
    // merge happens in the interleaved fashion
    public Flux<String> explore_merge() {

        var abcFlux = Flux.just("A", "B", "C")
                .delayElements(Duration.ofMillis(100));

        var defFlux = Flux.just("D", "E", "F")
                .delayElements(Duration.ofMillis(125));

        return Flux.merge(abcFlux, defFlux).log();

    }

    // "A", "D", "B", "E", "C", "F"
    // Flux is subscribed early
    public Flux<String> explore_mergeWith() {

        var abcFlux = Flux.just("A", "B", "C")
                .delayElements(Duration.ofMillis(100));

        var defFlux = Flux.just("D", "E", "F")
                .delayElements(Duration.ofMillis(125));

        return abcFlux.mergeWith(defFlux).log();
    }

    public Flux<String> explore_mergeWith_delay() {

        var abcFlux = Flux.just("A", "B", "C")
                .delayElements(Duration.ofMillis(100));

        var defFlux = Flux.just("D", "E", "F")
                .delayElements(Duration.ofMillis(200));

        return abcFlux.mergeWith(defFlux).log();
    }

    public Flux<String> explore_mergeWith_mono() {

        var aMono = Mono.just("A");

        var bMono = Flux.just("B");

        return aMono.mergeWith(bMono);
    }

    // "A","B","C","D","E","F"
    // Flux is subscribed early
    // both the publishers are subscriber at the same time
    // publishers are subscribed eagerly
    // even though the publishers are subscribed eagerly the merge happens in the sequentially order.
    public Flux<String> explore_mergeSequential() {

        var abcFlux = Flux.just("A", "B", "C")
                .delayElements(Duration.ofMillis(100));

        var defFlux = Flux.just("D", "E", "F")
                .delayElements(Duration.ofMillis(150));

        return Flux.mergeSequential(abcFlux, defFlux).log();
    }


    // AD, BE, FC
    public Flux<String> explore_zip() {

        var abcFlux = Flux.just("A", "B", "C");

        var defFlux = Flux.just("D", "E", "F");

        return Flux.zip(abcFlux, defFlux, (first, second) -> first + second);

    }

    // AD14, BE25, CF36
    public Flux<String> explore_zip_1() {

        var flux1 = Flux.just("A", "B", "C");
        var flux2 = Flux.just("D", "E", "F");
        var flux3 = Flux.just("1", "2", "3");
        var flux4 = Flux.just("4", "5", "6");

        return Flux.zip(flux1, flux2, flux3, flux4)
                .map(t4 -> t4.getT1() + t4.getT2() + t4.getT3() + t4.getT4());

    }

    public Flux<String> explore_zipWithMap()
    {
        Flux<String> flux1 = Flux.just("A", "B", "C");
        Flux<String> flux2 = Flux.just("D", "E", "F");
        return Flux.zip(flux1, flux2).map(t-> t.getT1() + t.getT2());
    }

    public Flux<String> explore_zip_2() {

        var aMono = Mono.just("A");
        var bMono = Mono.just("B");

        return Flux.zip(aMono, bMono, (first, second) -> first + second);

    }

    // AD, BE, CF
    public Flux<String> explore_zipWith() {

        var abcFlux = Flux.just("A", "B", "C");

        var defFlux = Flux.just("D", "E", "F");

        return abcFlux.zipWith(defFlux, (first, second) -> first + second);

    }

    public Mono<String> explore_zipWith_mono() {

        var aMono = Mono.just("A");

        var bMono = Mono.just("B");

        return aMono.zipWith(bMono)
                .map(t2 -> t2.getT1() + t2.getT2());

    }





    public static void main(String[] args) {

        MonoAndFluxGeneratorService monoAndFluxGeneratorService = new MonoAndFluxGeneratorService();

        //  monoAndFluxGeneratorService.namesFlux().subscribe(System.out::println);

        //  monoAndFluxGeneratorService.namesMono().subscribe(System.out::println);

        monoAndFluxGeneratorService.namesFlux_transforming(1).subscribe(System.out::println);

        // monoAndFluxGeneratorService.namesFlux_transform().subscribe(System.out::println);

    }
}
