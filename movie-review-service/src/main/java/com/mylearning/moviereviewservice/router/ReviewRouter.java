package com.mylearning.moviereviewservice.router;

import com.mylearning.moviereviewservice.handler.ReviewHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

/*
.route().GET(String pattern, HandlerFunction<ServerResponse> handler)

@FunctionalInterface
public interface HandlerFunction<T extends ServerResponse> {
 Mono<T> handle(ServerRequest request);  returns ServerResponse
 }
*/
@Configuration
public class ReviewRouter {

    @Bean
    public RouterFunction<ServerResponse> reviewRoute(ReviewHandler reviewHandler) {
        return route()
                .GET("/api/v1/hello", (request) -> {
                    return ServerResponse.ok().bodyValue("Hello World");
                })
                .GET("/api/v1/greet/{name}", request -> {
                    return ServerResponse.ok().bodyValue("Hello " + request.pathVariable("name"));
                })
                .build();
    }
}
// route().GET(String pattern, HandlerFunction<ServerResponse> handler)
// HandlerFunction<ServerResponse>
// => A functional interface: takes a ServerRequest and returns a Mono<ServerResponse>.
// => Used to handle HTTP requests in a reactive, non-blocking way.
// => Can think of it like: HandlerFunction<ServerResponse> = Function<ServerRequest, Mono<ServerResponse>>

