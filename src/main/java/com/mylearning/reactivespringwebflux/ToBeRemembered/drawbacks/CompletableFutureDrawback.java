package com.mylearning.reactivespringwebflux.ToBeRemembered.drawbacks;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CompletableFutureDrawback {

    /*
      ** CompletableFuture<List<Result>> => will need to wait for the whole collection to build and readily available**

      => When we use CompletableFuture<List<Result>>, we only get the final List<Result> once all the async tasks are completed.
      => Until the whole collection (the list of results) is ready, we don’t get anything — we must wait for every task to finish.
    */


    /*
    => We can’t process partial results.
    => If some tasks are slower, we’re blocked from getting anything until everything finishes.
    => It’s not a streaming approach — it’s a bulk return at the end.
     */
    public static void main(String[] args) throws Exception {
        // example
        // CompletableFuture<List> => Wait for all results, get them at once
        // CompletableFuture<Result> per item and can process each item as it completes
        CompletableFuture<List<String>> future = CompletableFuture.supplyAsync(() -> {
            List<String> list = new ArrayList<>();
            list.add(fetchData("A")); // Imagine each takes time
            list.add(fetchData("B"));
            list.add(fetchData("C"));
            return list;
        }); // Disadvantage: You will get the result only after A, B, and C are fetched completely.

        // Also using get()Blocking get to see the result
        List<String> result = future.get();
        System.out.println("Final Result: " + result);
    }

    // Simulated data fetching with delay
    public static String fetchData(String input) {
        try {
            Thread.sleep(1000); // Simulate delay
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "Data-" + input;
    }

}
