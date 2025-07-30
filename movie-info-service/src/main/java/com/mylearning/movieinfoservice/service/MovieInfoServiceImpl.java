package com.mylearning.movieinfoservice.service;

import com.mylearning.movieinfoservice.model.MovieInfo;
import com.mylearning.movieinfoservice.repository.MovieInfoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class MovieInfoServiceImpl implements MovieInfoService {

    private final MovieInfoRepository movieInfoRepository;

    public MovieInfoServiceImpl(MovieInfoRepository movieInfoRepository) {
        this.movieInfoRepository = movieInfoRepository;
    }

    @Override
    public Mono<MovieInfo> addMovieInfo(MovieInfo movieInfo) {
        return  movieInfoRepository.save(movieInfo).log();
    }

    @Override
    public Flux<MovieInfo> getMovieInfos() {
        return movieInfoRepository.findAll().log();
    }

    @Override
    public Mono<MovieInfo> getMovieInfo(String movieId) {
        return movieInfoRepository.findById(movieId).log();
    }

    @Override
    public Mono<Void> deleteMovieInfo(String movieId) {
        return movieInfoRepository.deleteById(movieId).log();
    }

    @Override
    public Mono<MovieInfo> updateMovieInfo(MovieInfo movieInfo) {
        if (movieInfo != null) {
            return addMovieInfo(movieInfo);
        }
        return Mono.fromRunnable(() -> log.info("MovieInfo is null"));
    }
}
