package com.mylearning.moviereviewservice.validator;

import com.mylearning.moviereviewservice.domain.Review;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

// With the Validator interface, we can implement our own validation logic.
// we don't have to add annotations to our Review class which includes @NotNull, @PositiveOrZero etc
@Component
@Slf4j
public class ReviewValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
       return Review.class.equals(clazz);
        //Review.class.equals(clazz) → This means:
        //Only validate exactly Review objects (not subclasses).
        //If you wanted to accept subclasses too, we’d use: return Review.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ValidationUtils.rejectIfEmpty(errors,"movieInfoId", "movieInfoId.null", "Pass a valid movieInfoId" );
        ValidationUtils.rejectIfEmpty(errors,"rating", "rating.null", "Pass a valid rating" );
        Review review = (Review) target;
        log.info("Review : {}" , review);
        if(review.getRating()!=null && review.getRating()<0.0){
            errors.rejectValue("rating", "rating.negative", "rating is negative and please pass a non-negative value");
        }
        //rejectValue() is a method from Spring’s Errors or BindingResult interface.
        //It’s used inside validators to manually add an error for a specific field when you detect invalid data.
        //void rejectValue(String field, String errorCode, String defaultMessage);
        //field → The exact name of the field in your object that has an error.

    }

}


// The Validator interface forces us to implement two methods: supports() and validate().