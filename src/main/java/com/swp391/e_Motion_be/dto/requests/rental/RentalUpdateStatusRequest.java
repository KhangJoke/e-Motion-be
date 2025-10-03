package com.swp391.e_Motion_be.dto.requests.rental;

import com.swp391.e_Motion_be.enums.RentalStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RentalUpdateStatusRequest {
    @NotNull(message = "User email is required")
    Long id;
    @NotNull(message = "Rental Status is required")
    RentalStatus status;
}
