package com.mylearning.functionweb.router;

import com.mylearning.functionweb.handler.ProductHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

// static RouterFunction<ServerResponse> route(RequestPredicate predicate, HandlerFunction<ServerResponse> handlerFunction)
// RequestPredicate is something like GET("/products") or POST("/x")
// HandlerFunction is like handler::getAllProducts

/*
    @FunctionalInterface
    public interface HandlerFunction<T extends ServerResponse> {
     Mono<T> handle(ServerRequest request);
    }

     @FunctionalInterface
     public interface RequestPredicate {
      boolean test(ServerRequest request);
    }
*/
@Configuration
public class ProductRouter {

    public RouterFunction<ServerResponse> productRoute(ProductHandler handler) {

        RouterFunction<ServerResponse> routes = RouterFunctions.route(
                new RequestPredicate() {
                    @Override
                    public boolean test(ServerRequest request) {
                        return request.method() == HttpMethod.GET && request.path().equals("/products");
                    }
                },
                new HandlerFunction<ServerResponse>() {
                    @Override
                    public Mono<ServerResponse> handle(ServerRequest request) {
                        return handler.getAllProducts(request); // call actual logic
                    }
                }
        );

        return routes;

    }

    /*
     * Internally:
     * public static RequestPredicate GET(String pattern) {
     *     return RequestPredicates.method(HttpMethod.GET).and(RequestPredicates.path(pattern));
     * }
     *
     *  =>  So GET("/products") is actually returns:
     *     RequestPredicates.method(HttpMethod.GET).and(RequestPredicates.path("/products"));
     *
     *  => RequestPredicate predicate = RequestPredicates.method(HttpMethod.GET).and(RequestPredicates.path("/products"));
     *
     *  => method(HttpMethod.GET) and path("/products") each return RequestPredicate objects with internal logic.
     *   Let’s peek into method():
     *   => Let's peek into method(HttpMethod.GET):
     *   public static RequestPredicate method(HttpMethod httpMethod) {
     *        return request -> request.method() == httpMethod;
     *        or
     *       return new RequestPredicate() {
     *           @Override
     *           public boolean test(ServerRequest request) {
     *               return request.method() == httpMethod;
     *            }
     *       };
     *    }
     *   Now, let’s understand path("/products"):
     *   public static RequestPredicate path(String pattern) {
     *     return request -> request.path().equals(pattern);
     *
     *     // OR:
     *     return new RequestPredicate() {
     *         @Override
     *         public boolean test(ServerRequest request) {
     *             return request.path().equals(pattern);
     *         }
     *     };
     * }
     *
     * => So RequestPredicates.method(HttpMethod.GET).and(RequestPredicates.path("/products")) gives us:
     *     RequestPredicate predicate = request ->
     *        request.method() == HttpMethod.GET &&
     *        request.path().equals("/products");
     *
     */


    /*
     * @FunctionalInterface
     * public interface RequestPredicate {
     *     boolean test(ServerRequest request);
     *
     *     default RequestPredicate and(RequestPredicate other) { ... }
     *     default RequestPredicate or(RequestPredicate other) { ... }
     *     default RequestPredicate negate() { ... }
     * }
     *
     * and RequestPredicates utility class which has:
     * static RequestPredicate method(HttpMethod httpMethod);
     * static RequestPredicate path(String pattern);
     *
     * here is the source code:
     * public abstract class RequestPredicates {
     *
     *     public static RequestPredicate method(HttpMethod httpMethod) {
     *         return request -> request.method() == httpMethod;
     *     }
     *
     *     public static RequestPredicate path(String pattern) {
     *         return request -> request.path().equals(pattern);
     *     }
     *
     *     // other helper methods: GET(), POST(), etc.
     * }
     */


    @Bean
    public RouterFunction<ServerResponse> productRoutes(ProductHandler handler) {
        return route(GET("/products"), handler::getAllProducts)
                .andRoute(GET("/products/{id}"), handler::getProductById)
                .andRoute(POST("/products"), handler::createProduct)
                .andRoute(PUT("/products/{id}"), handler::updateProduct)
                .andRoute(DELETE("/products/{id}"), handler::deleteProduct)
                .andRoute(
                        request -> request.method() == HttpMethod.GET && request.path().equals("/dummy/products"),
                        request -> ServerResponse.ok().bodyValue("All products...")
                );
    }

//    @Bean
//    public RouterFunction<ServerResponse> productRoutes(ProductHandler handler) {
//        return RouterFunctions.route(
//                        request -> request.method() == HttpMethod.GET && request.path().equals("/products"),
//                        request -> handler.getAllProducts(request)
//                )
//                .andRoute(
//                        request -> request.method() == HttpMethod.GET && request.path().matches("^/products/[^/]+$"),
//                        request -> handler.getProductById(request)
//                )
//                .andRoute(
//                        request -> request.method() == HttpMethod.POST && request.path().equals("/products"),
//                        request -> handler.createProduct(request)
//                )
//                .andRoute(
//                        request -> request.method() == HttpMethod.PUT && request.path().matches("^/products/[^/]+$"),
//                        request -> handler.updateProduct(request)
//                )
//                .andRoute(
//                        request -> request.method() == HttpMethod.DELETE && request.path().matches("^/products/[^/]+$"),
//                        request -> handler.deleteProduct(request)
//                );
//    }


}

/*

  | Part        | Meaning                                                                        |
  | ------------| ------------------------------------------------------------------------------|
  | ^           | Anchors the regex to start of the string                                       |
  | /products/  | Matches the literal string /products/ exactly                                  |
  | [^/]+       | Matches one or more characters that are not a forward slash (/)                |
  | $           | Anchors the regex to end of the string                                         |

    [^/] Explanation
    [] → defines a character class
    ^ inside [] → means negation (NOT)
    [^/] → match any character except /
    + → one or more of those characters

    URL Path              | Match? | Why
    ---------------------|--------|----------------------------------------------
    /products/123        | Yes    | 123 matches [^/]+
    /products/abc-xyz    | Yes    | abc-xyz matches [^/]+
    /products/           | No     | Nothing after /products/ — doesn't match +
    /products/123/extra  | No     | The /extra part violates the [^/]+$ rule

*/
