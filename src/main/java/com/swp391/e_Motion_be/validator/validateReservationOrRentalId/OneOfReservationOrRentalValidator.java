package com.swp391.e_Motion_be.validator.validateReservationOrRentalId;

import com.swp391.e_Motion_be.dto.requests.deposit.DepositCreateRequest;
import com.swp391.e_Motion_be.dto.requests.deposit.DepositUpdateRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class OneOfReservationOrRentalValidator
        implements ConstraintValidator<OneOfReservationOrRental, Object> {

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        String reservationCode = null;
        Long rentalId = null;

        if (value instanceof DepositCreateRequest request) {
            reservationCode = request.getReservationCode();
            rentalId = request.getRentalId();
        } else if (value instanceof DepositUpdateRequest request) {
            reservationCode = request.getReservationCode();
            rentalId = request.getRentalId();
        }

        boolean hasReservation = reservationCode != null;
        boolean hasRental = rentalId != null;

        // chỉ có 1 trong 2 mới được
        return hasReservation ^ hasRental;
    }
}

