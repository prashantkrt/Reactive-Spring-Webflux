package com.mylearning.movieinfoservice.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Document
public class MovieInfo {

    @Id
    private String movieId; // we don't need to manually configure auto-generation of IDs for documents.

    @NotBlank(message = "movieInfo.name should be not be blank") // "" , " " amd null are not valid
    private String name;

    @NotNull // null is not valid but "" and " " are valid
    @Positive(message = "movieInfo.year should be positive")
    private Integer year;

    @NotNull
    private List<@NotBlank(message = "movieInfo.cast should not be blank") String> cast;


    @NotNull(message = "movieInfo.releaseDate must not be null")
    private LocalDate releaseDate;
    private String description;

}

// note  @NotEmpty // "" and null are not valid but " " is valid
