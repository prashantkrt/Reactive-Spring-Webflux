package com.mylearning.movieinfoservice.repository;

import com.mylearning.movieinfoservice.model.MovieInfo;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovieInfoRepository extends ReactiveMongoRepository<MovieInfo, String> {
}
