package com.mylearning.reactivespringwebflux.InterfacesUsed;


/*
 * Represents the actual source of data like DB or file etc.
 *  public interface Publisher<T> {
 *      void subscribe(Subscriber<? super T> s);
 *  }
 */

public interface IPublisher<T> {
      void subscribe(ISubscriber<? super T> s);
}



/*

Keep revising the things :)

Valid → ? super Integer → Integer, Number, Object

List<? super Integer> list = new ArrayList<Number>();

Object
   ↑
Number
   ↑
Integer

You can use Integer, Number, Object (all ancestors)

list.add(10);  => Integer is valid
list.add(Integer.valueOf(20));  => It converts a primitive int value to an Integer object.It is valid
list.add(10.5); invalid (Double is not Integer or its subclass)

Double, Long, etc.	 No (they are siblings, not parents)

Object
 ├── Number
 │    └── Integer
 └── String
 List<? super Integer> list = new ArrayList<String>();  Compile Error:
*/
