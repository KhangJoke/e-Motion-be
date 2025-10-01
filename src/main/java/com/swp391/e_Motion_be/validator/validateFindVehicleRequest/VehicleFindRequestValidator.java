package com.swp391.e_Motion_be.validator.validateFindVehicleRequest;

import com.swp391.e_Motion_be.dto.requests.vehicle.VehicleFindRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class VehicleFindRequestValidator implements ConstraintValidator<ValidVehicleFindRequest, VehicleFindRequest> {
    @Override
    public boolean isValid(VehicleFindRequest request, ConstraintValidatorContext constraintValidatorContext) {
        if(request == null) return true;
        if(request.getStartTime() == null || request.getEndTime() == null) return true;
        return request.getStartTime().isBefore(request.getEndTime());
    }
}
