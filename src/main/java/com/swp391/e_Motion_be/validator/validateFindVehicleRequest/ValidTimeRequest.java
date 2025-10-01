package com.swp391.e_Motion_be.validator.validateFindVehicleRequest;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidTimeRequestValidator.class)
@Documented
public @interface ValidTimeRequest {
    String message() default "Invalid time range: startTime must be before endTime";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
