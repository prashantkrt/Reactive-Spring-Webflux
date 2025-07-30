package com.mylearning.movieinfoservice.service;

import com.mylearning.movieinfoservice.model.MovieInfo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MovieInfoService {

    public Mono<MovieInfo> addMovieInfo(MovieInfo movieInfo);

    public Flux<MovieInfo> getMovieInfos();

    public Mono<MovieInfo> getMovieInfo(String movieId);

    public Mono<Void> deleteMovieInfo(String movieId);

    public Mono<MovieInfo> updateMovieInfo(MovieInfo movieInfo);
}
