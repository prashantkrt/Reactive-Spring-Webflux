package com.mylearning.moviereviewservice.repository;

import com.mylearning.moviereviewservice.domain.Review;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface ReviewRepository extends ReactiveMongoRepository<Review,String> {
    Flux<Review> findReviewsByMovieInfoId(Long movieInfoId);
}
