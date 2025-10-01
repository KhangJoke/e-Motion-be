package com.swp391.e_Motion_be.validator.validateReservationOrRentalId;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = OneOfReservationOrRentalValidator.class)
@Documented
public @interface OneOfReservationOrRental {
    String message() default "Either reservationCode or rentalId must be provided, but not both"; // message default when error
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
