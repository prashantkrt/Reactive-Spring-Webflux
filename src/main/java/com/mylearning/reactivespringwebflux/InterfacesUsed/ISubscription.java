package com.mylearning.reactivespringwebflux.InterfacesUsed;


/*
 * public interface Subscription {
 *     void request(long n);
 *     void cancel();
 * }
 */
public interface ISubscription<T> {
    void request(long n);
    void cancel();
}
