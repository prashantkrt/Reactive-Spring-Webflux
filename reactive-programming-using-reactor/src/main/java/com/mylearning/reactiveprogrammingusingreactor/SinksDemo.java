package com.mylearning.reactiveprogrammingusingreactor;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.stream.IntStream;

/*

| Sink Type                  | Description                                 | Bracket Explanation                                                              |
| -------------------------- | ------------------------------------------- | -------------------------------------------------------------------------------- |
| Sinks.one()                | Single value to many                        | [ 1 value or error, multiple subscribers get same value ]                        |
| Sinks.empty()              | Complete without data                       | [ No data, just complete or error ]                                              |
| Sinks.many().unicast()     | Multi-values, 1 subscriber                  | [ Only one subscriber, late joiner gets all buffered messages ]                  |
| Sinks.many().multicast()   | Multi-values, many subscribers              | [ Many subscribers, no replay, subscribers get data from subscribe point ]       |
| Sinks.many().replay()      | Multi-values, many subscribers, with replay | [ Many subscribers, with replay of previous messages to late joiners ]           |


| Method         |  Where You Use              |    Purpose                          |
| ---------------| ----------------------------| ------------------------------------|
| tryEmitNext()  | Sinks.Many (Flux-style)     |   Push multiple values into stream  |
| tryEmitValue() | Sinks.One (Mono-style)      |   Push a single value into stream   |
| tryEmitError() | Sinks.Many (Flux-style)     |   Push an error into stream         |
| tryEmitComplete()| Sinks.Many (Flux-style)     |   Push a complete signal into stream|

| Sink Type              | Subscribers | Values | Replay | Use Case               |
| ---------------------- | ----------- | ------ | ------ | ---------------------- |
|  Sinks.One             | Many        | 1      |   Yes  | API Response           | // Even if subscribers subscribe after value is emitted, they will still get the value (replay behavior happens).
|  Sinks.Empty           | Many        | 0      |   N/A  | Completion signal      |
|  Sinks.Many.unicast    | One         | ∞      |   No   | Private data stream    | // Replay to Late Subscriber? => NO since in Unicast = Only 1 subscriber allowed.If subscriber subscribes AFTER emission, it gets buffered data, only if subscribed before completion.After completion → new subscribers are NOT allowed → Late joining is not possible after terminal state.
|  Sinks.Many.multicast  | Many        | ∞      |   No   | Live data to many      | // No replay
|  Sinks.Many.replay     | Many        | ∞      |   Yes  | Replaying chat/history | // Yes replay

*/
public class SinksDemo {
    public static void main(String[] args) {


//        //Latest
//        Sinks.Many<Integer> replaySinksLatest = Sinks.many().replay().latest(); //latest() keeps only the most recent value emitted.
//
//        Flux<Integer> fluxLatest = replaySinksLatest.asFlux();
//        fluxLatest.subscribe(i -> System.out.println("Subscriber 1: " + i));
//        fluxLatest.subscribe(i -> System.out.println("Subscriber 2: " + i));
//
//        replaySinksLatest.tryEmitNext(1);
//        replaySinksLatest.tryEmitNext(2);
//        replaySinksLatest.tryEmitNext(3);
//
//        fluxLatest.subscribe(i -> System.out.println("Subscriber 3: " + i));
//        replaySinksLatest.tryEmitNext(4);
//        replaySinksLatest.tryEmitNext(5);
//
//        fluxLatest.subscribe(i -> System.out.println("Subscriber 4: " + i));
//
//        //All
//        Sinks.Many<Integer> replaySinksAll = Sinks.many().replay().all();
//
//       //  FAIL_FAST : Behavior: Immediately fail if the emission cannot succeed. No retries.
//        Sinks.Many<Integer> sink = Sinks.many().replay().latest();
//        sink.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST);
//
//       // FAIL_NON_SERIALIZED Behavior: Retry emission if the failure is due to non-serialized access. Otherwise, fail.
//        sink.emitNext(2, (signalType, emitResult) -> {
//            return Sinks.EmitResult.FAIL_NON_SERIALIZED.equals(emitResult);
//        });


        // It can hold up to 256 elements by default
        // we are trying to emit 300 elements before subscribers in this case buffer will hold only till 256 since the internal buffer has limited capacity – default size ~256.
        Sinks.Many<Integer> multiCast = Sinks.many().multicast().onBackpressureBuffer();
        IntStream.rangeClosed(0, 300)
                .forEach(i -> multiCast.tryEmitNext(i));

        Flux<Integer> integerFlux1 = multiCast.asFlux();

        //Subscriber 1 starts seeing data
        integerFlux1
                .subscribe(s -> {
                    System.out.println("Subscriber 1 of integerFlux1 : " + s);
                });
        // it won't be printed since multicast doesn't replay and subscriber 2 is late
        // Subscriber 2 is late (subscribed after Subscriber 1). Since multicast() doesn’t replay past items, it sees nothing.
        integerFlux1.subscribe(s -> {
            System.out.println("Subscriber 2 of integerFlux1 : " + s);
        });

        System.out.println("--------------------------------------------------------");

        Sinks.Many<Integer> multiCast2 = Sinks.many().multicast().onBackpressureBuffer();

        IntStream.rangeClosed(0, 500)
                .forEach(i -> {
                    try {
                        Thread.sleep(10); // 10ms delay
                        multiCast2.tryEmitNext(i);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });

        Flux<Integer> integerFlux2 = multiCast2.asFlux();

        integerFlux2
                .delaySubscription(Duration.ofMillis(40))
                .subscribe(s -> {
                    System.out.println("Subscriber 1 of integerFlux2 : " + s);
                });
        integerFlux2
                .delaySubscription(Duration.ofMillis(40)) //it will take all
                .subscribe(s -> {
                    System.out.println("Subscriber 2 of integerFlux2 : " + s);
                });
        integerFlux2
                .delaySubscription(Duration.ofMillis(45))
                .subscribe(s -> {
                    System.out.println("Subscriber 3 of integerFlux2 : " + s);
                });

        Flux<Integer> integerFlux3 = multiCast2.asFlux();
        integerFlux3
                .delaySubscription(Duration.ofMillis(45))
                .subscribe(s -> {
                    System.out.println("Subscriber 4 of integerFlux3 : " + s);
                });


        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
}
