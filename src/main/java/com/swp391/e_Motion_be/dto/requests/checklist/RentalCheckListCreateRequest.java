package com.swp391.e_Motion_be.dto.requests.checklist;

import com.swp391.e_Motion_be.enums.CheckType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RentalCheckListCreateRequest {

    @NotNull(message = "Check type is required")
    private CheckType type;

    @NotNull(message = "Current battery is required")
    @DecimalMin(value = "0.0", message = "Current battery must be greater than or equal to 0")
    @DecimalMax(value = "100.0", message = "Current battery must be less than or equal to 100")
    private double currentBattery;

    @NotNull(message = "Rental ID is required")
    private Long rentalId;
}
