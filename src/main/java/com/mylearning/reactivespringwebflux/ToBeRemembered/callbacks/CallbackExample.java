package com.mylearning.reactivespringwebflux.ToBeRemembered.callbacks;

// A callback means giving a method to another method to call later — when the work is done.
// simply => Hey, after you finish your work, call me back using this method!

// 1. Callback Interface
interface Callback {
    void call();
}

// 2. Class which does some work
class Worker {

    public void doTask(Callback callback) {

        System.out.println("Doing some task...");

        // Simulate work
        try {
            Thread.sleep(1000);
        } catch (Exception ignore) {

        }
        System.out.println("Task finished!");

        // 3. Call the callback
        callback.call();
    }
}
public class CallbackExample {
    public static void main(String[] args) {
        Worker worker = new Worker();
        worker.doTask(() -> System.out.println("Hey, I got the callback!"));
    }
}
