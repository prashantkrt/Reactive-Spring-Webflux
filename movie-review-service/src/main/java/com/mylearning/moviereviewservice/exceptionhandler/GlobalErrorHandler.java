package com.mylearning.moviereviewservice.exceptionhandler;

import com.mylearning.moviereviewservice.exception.ReviewDataException;
import com.mylearning.moviereviewservice.exception.ReviewNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@Order(-2)
//If the GlobalErrorHandler isn’t invoked,then another error-handling mechanism(e.g., Spring’s default handler) might be processing the exception, bypassing your logs.
// Add the @Order annotation to your GlobalErrorHandler to ensure it takes precedence over the default error handler.
public class GlobalErrorHandler implements ErrorWebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        log.error("Global exception caught: {}", ex.getMessage(), ex);
        System.out.println("Hello from GlobalErrorHandler");

        DataBufferFactory dataBufferFactory = exchange.getResponse().bufferFactory();
        DataBuffer errorMessage = dataBufferFactory.wrap(ex.getMessage().getBytes()); //call a method on it to create a new DataBuffer containing your bytes.
        if (ex instanceof ReviewDataException) {
            exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
            return exchange.getResponse().writeWith(Mono.just(errorMessage));
        }
        if (ex instanceof ReviewNotFoundException) {
            exchange.getResponse().setStatusCode(HttpStatus.NOT_FOUND);
            return exchange.getResponse().writeWith(Mono.just(errorMessage));
        }
        exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        return exchange.getResponse().writeWith(Mono.just(errorMessage)); // step that actually sends the data to the client in WebFlux.
    }

    //exchange.getResponse() → gets the current HTTP response object for this request.
    //writeWith(Publisher<DataBuffer>) → tells WebFlux:
    //"Here’s a reactive stream (Publisher) of DataBuffer objects that I want you to send as the response body."
    //Mono.just(errorMessage) → creates a reactive publisher that emits exactly one item — in this case, the DataBuffer we created with our error message.

    // Flow:
    // You wrap your data in a DataBuffer (e.g., errorMessage).
    // You pass it to writeWith(...) inside a reactive stream.
    // WebFlux subscribes to that stream and writes the bytes to the network when the response is being sent.

}



/*

=> A DataBuffer in Spring WebFlux is just a container for raw bytes

DataBufferFactory in Spring WebFlux is the component that creates DataBuffer objects,
which are the raw chunks of data that actually get written to the network response.

Think of it like:
A box where you put your data (text, JSON, image, etc.) in byte form before sending it to the network.
The DataBufferFactory is the factory that makes those boxes.

DataBufferFactory factory = exchange.getResponse().bufferFactory();
DataBuffer buffer = factory.wrap("Hello WebFlux".getBytes(StandardCharsets.UTF_8));
exchange.getResponse().writeWith(Mono.just(buffer));

"Hello WebFlux" → converted to bytes.
wrap() puts those bytes into a DataBuffer.
writeWith() sends it to the client.


@Override
public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
    DataBufferFactory bufferFactory = exchange.getResponse().bufferFactory();
    byte[] content = "Hello from DataBufferFactory".getBytes(StandardCharsets.UTF_8);

    return exchange.getResponse()
            .writeWith(Mono.just(bufferFactory.wrap(content)));

}

output as : Hello from DataBufferFactory

=> dataBufferFactory → a reusable factory object that knows how to create DataBuffer instances.
=> wrap(...) → you call a method on it to create a new DataBuffer containing your bytes.
=> You are not modifying the factory — you’re using it to produce a DataBuffer.
=> The factory stays the same for future calls.


*/
