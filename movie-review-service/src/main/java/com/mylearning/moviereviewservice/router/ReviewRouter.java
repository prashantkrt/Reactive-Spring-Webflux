package com.mylearning.moviereviewservice.router;

import com.mylearning.moviereviewservice.handler.ReviewHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.path;
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
//                .POST("/api/v1/review", reviewHandler::addReview)
//                .GET("/api/v1/review/{id}", reviewHandler::getReview)
//                .GET("/api/v1/review", reviewHandler::getAllReview)
//                .PUT("/api/v1/review/{id}", reviewHandler::updateReview)
//                .DELETE("/api/v1/review/{id}", reviewHandler::deleteReview)

                //    we can also use nest
                // => RouterFunctions.Builder nest(RequestPredicate predicate,Consumer<RouterFunctions.Builder> builderConsumer)
                .nest(path("/api/v1/review"), builder ->
                        builder.GET("/search", reviewHandler::getReviewByMovieInfoId)
                                .GET("/{id}", reviewHandler::getReview)
                                .POST("", reviewHandler::addReview)
                                .PUT("/{id}", reviewHandler::updateReview)
                                .DELETE("/{id}", reviewHandler::deleteReview)
                                .GET("/stream", reviewHandler::getAllReview))
                .build();
    }
}
// route().GET(String pattern, HandlerFunction<ServerResponse> handler)
// HandlerFunction<ServerResponse>
// => A functional interface: takes a ServerRequest and returns a Mono<ServerResponse>.
// => Used to handle HTTP requests in a reactive, non-blocking way.
// => Can think of it like: HandlerFunction<ServerResponse> = Function<ServerRequest, Mono<ServerResponse>>

