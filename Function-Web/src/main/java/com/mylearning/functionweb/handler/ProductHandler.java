package com.mylearning.functionweb.handler;

import com.mylearning.functionweb.model.Product;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
// Remember:

// ServerRequest is not a Mono or Flux —
// It's just a normal object that holds details about the HTTP request,
// like headers, path variables, query params, and the body (as a reactive stream).

// To extract the body (like JSON) from ServerRequest,
// we use bodyToMono(Class<T>) or bodyToFlux(Class<T>) to get it reactively.


// Also:

// ServerResponse is a non-reactive, builder-style class used to construct the HTTP response.
// But in WebFlux, you don't return ServerResponse directly.
// Instead, you wrap it in a Mono<ServerResponse> because all I/O operations are non-blocking
// and responses must be returned asynchronously.

@Component
public class ProductHandler {

    // GET: Return all products
    public Mono<ServerResponse> getAllProducts(ServerRequest request) {
        Flux<Product> products = Flux.just(
                new Product("1", "Phone", 699.0),
                new Product("2", "Laptop", 1299.0)
        );

        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(products, Product.class); // returns a Mono<ServerResponse>
        // body(Flux<Product>, Product.class)	Converts to Mono<ServerResponse>
        // Even we passed Flux<Product> as Publisher<Product>
        // it will return Mono<ServerResponse>
        // Spring WebFlux wraps that Flux<Product> as the body of the ServerResponse.
        // Then it wraps that whole thing into a Mono<ServerResponse> — so the framework can handle it asynchronously.
    }


    public Mono<ServerResponse> getAllProducts() {
        Flux<Product> products = Flux.just(
                new Product("1", "Phone", 699.0),
                new Product("2", "Laptop", 1299.0)
        );

        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(products, Product.class); // returns a Mono<ServerResponse>
    }

    // GET: Get a product by ID from path param
    public Mono<ServerResponse> getProductById(ServerRequest request) {
        String productId = request.pathVariable("id");
        Product product = new Product(productId, "Sample Product", 999.0);

        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(product);
    }

    // POST: Create new product
    public Mono<ServerResponse> createProduct(ServerRequest request) {

        // Mono<Product> productMono = request.bodyToMono(Product.class);
        return request.bodyToMono(Product.class) //request body (like: { "name": "Tablet", "price": 500 }). This line extracts the request body and converts (deserializes) it into a Product object.It returns a Mono<Product>, because the body comes in asynchronously, and Mono represents a single future value.
                .flatMap(product -> {
                    product.setId("999"); // simulate DB-generated ID
                    Mono<ServerResponse> serverResponseMono = ServerResponse.ok()  // returns a Mono<ServerResponse>
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(product);
                    return serverResponseMono;
                });
    }

    // PUT: Update product by ID
    public Mono<ServerResponse> updateProduct(ServerRequest request) {
        String id = request.pathVariable("id");

        return request.bodyToMono(Product.class)
                .flatMap(product -> {
                    product.setId(id);
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(product);
                });
    }

    // DELETE: Delete product by ID
    public Mono<ServerResponse> deleteProduct(ServerRequest request) {
        String id = request.pathVariable("id");
        return ServerResponse.ok()
                .bodyValue(Map.of("message", "Product with ID " + id + " deleted"));
    }


}
/*
 *
 *
 * | Method         | Used When...                                          | Return Type Inside        | Resulting Type                                       |
 * | -------------- | ----------------------------------------------------- | ------------------------- | ---------------------------------------------------- |
 * |  map(...)      | You return a plain object                             | Product, String, etc.    | Mono<Product> → Mono<Mono<ServerResponse>> (BAD)      |
 * |  flatMap(...)  | You return a Mono or Flux (i.e., a reactive type)     | Mono<ServerResponse>     | Mono<ServerResponse> (GOOD )                          |
 */