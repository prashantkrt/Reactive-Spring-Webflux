package com.mylearning.moviereviewservice.domain;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document
public class Review {
    @Id
    private String reviewId;
    @NotNull(message = "rating.movieInfoId : must not be null")
    private Long movieInfoId;
    private String comment;
    //@Min(value = 0L, message = "rating.negative : please pass a non-negative value")
    @PositiveOrZero(message = "rating.negative : please pass a non-negative value")
    private Double rating;
}

// @NotNull
// private String name; // "": OK, null: ❌

// @NotEmpty
// private String name; // "": ❌, null: ❌, "   ": ✅

// @NotBlank
// private String name; // "": ❌, null: ❌, "   ": ❌