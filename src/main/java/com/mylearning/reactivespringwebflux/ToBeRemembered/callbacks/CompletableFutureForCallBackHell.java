package com.mylearning.reactivespringwebflux.ToBeRemembered.callbacks;

import java.util.concurrent.CompletableFuture;

//ix with CompletableFuture – we can chain tasks sequentially using thenRun() for void tasks or thenCompose() for value-returning tasks.
public class CompletableFutureForCallBackHell {
    public static void main(String[] args) {
        System.out.println("Start work");

        CompletableFuture
                .runAsync(() -> doTask1())
                .thenRun(() -> doTask2())
                .thenRun(() -> doTask3())
                .thenRun(() -> System.out.println("All tasks done!"));

        // Give time for async tasks to complete before the program exits
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {}

    }

    static void doTask1() {
        System.out.println("Task 1 started");
        sleep(500);
        System.out.println("Finished Task 1");
    }

    static void doTask2() {
        System.out.println("Task 2 started");
        sleep(500);
        System.out.println("Finished Task 2");
    }

    static void doTask3() {
        System.out.println("Task 3 started");
        sleep(500);
        System.out.println("Finished Task 3");
    }

    static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {}
    }
}

// Start work
// Task 1 started
// Finished Task 1
// Task 2 started
// Finished Task 2
// Task 3 started
// Finished Task 3
// All tasks done!
