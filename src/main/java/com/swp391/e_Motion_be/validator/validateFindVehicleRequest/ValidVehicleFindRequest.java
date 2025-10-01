package com.swp391.e_Motion_be.validator.validateFindVehicleRequest;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = VehicleFindRequestValidator.class)
@Documented
public @interface ValidVehicleFindRequest {
    String message() default "Invalid time range: startTime must be before endTime";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
