package com.mylearning.reactivespringwebflux.InterfacesUsed;


/*
 *
 * public interface Processor<T, R> extends Subscriber<T>, Publisher<R> {
 * }
 *              +----------------+
 *   Publisher →|  Processor     |→ Subscriber
 *              +----------------+
 */
public interface IProcessor<T,R> extends ISubscription<T>, IPublisher<R> {
}

/*
   Note:
   class → can extend only one class (because of single inheritance).
   interface → can extend multiple interfaces (because they don’t hold implementation, just method signatures).
*/
