package com.mylearning.movieservice.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Validated
public class MovieInfo {
    private String movieId;
    @NotBlank(message = "movieInfo.name must be present")
    private String name;
    @NotNull(message = "movieInfo.year must be present")
    @Positive(message = "movieInfo.year must be a Positive Value")
    private Integer year;
    @NotNull
    private List<@NotBlank(message = "movieInfo.cast must be present") String> cast;
    private LocalDate releaseDate;
    private String description;
}