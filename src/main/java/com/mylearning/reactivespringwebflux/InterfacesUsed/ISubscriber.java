package com.mylearning.reactivespringwebflux.InterfacesUsed;

/*
 * public interface Subscriber<T> {
 *     void onSubscribe(Subscription s);
 *     void onNext(T t);
 *     void onError(Throwable t);
 *     void onComplete();
 * }
 */

public interface ISubscriber<T> {
    void onSubscribe(ISubscription<T> s);
    void onNext(T t);
    void onError(Throwable t);
    void onComplete();
}
