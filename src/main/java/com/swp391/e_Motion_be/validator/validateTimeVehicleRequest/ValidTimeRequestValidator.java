package com.swp391.e_Motion_be.validator.validateTimeVehicleRequest;

import com.swp391.e_Motion_be.dto.requests.rental.RentalCreateRequest;
import com.swp391.e_Motion_be.dto.requests.vehicle.VehicleFindRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDateTime;

public class ValidTimeRequestValidator implements ConstraintValidator<ValidTimeRequest, Object> {
    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) return true;

        if (value instanceof VehicleFindRequest request) {
            return validate(request.getStartTime(), request.getEndTime());
        }

        if (value instanceof RentalCreateRequest request) {
            return validate(request.getStartTime(), request.getEndTime());
        }

        // Nếu class khác thì bỏ qua
        return true;
    }

    private boolean validate(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) return true;
        return start.isBefore(end);
    }
}
