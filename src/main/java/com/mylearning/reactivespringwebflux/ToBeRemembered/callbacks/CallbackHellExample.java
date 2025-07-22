package com.mylearning.reactivespringwebflux.ToBeRemembered.callbacks;

/*
    Callback hell happens when we have too many nested callbacks.
    The code becomes messy, hard to read — like a “pyramid of doom”.

    simple => pizza guy → who calls ice-cream guy → who calls juice guy → who calls someone else… all nested callbacks → messy :(
*/


public class CallbackHellExample {
    public static void main(String[] args) {
        System.out.println("Start work");

        /*
         * @FunctionalInterface
         * public interface Runnable {
         *     void run();
         * }
         */
        doTask1(() ->
                doTask2(() ->
                        doTask3(() ->
                                System.out.println("All tasks done!")
                        )
                )
        );
    }

    // task 1
    static void doTask1(Runnable callback) {
        System.out.println("Task 1 started");
        callback.run();
        System.out.println("Finished task 1");
    }

    // task 2
    static void doTask2(Runnable callback) {
        System.out.println("Task 2 started");
        callback.run();
        System.out.println("Finished task 2");
    }

    // task 3
    static void doTask3(Runnable callback) {
        System.out.println("Task 3 started");
        callback.run();
        System.out.println("Finished task 3");
    }
}

//Start work
//Task 1 started
//Task 2 started
//Task 3 started
//All tasks done!
//Finished task 3
//Finished task 2
//Finished task 1