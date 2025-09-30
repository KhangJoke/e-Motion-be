package com.swp391.e_Motion_be.dto.requests.checklist;

import com.swp391.e_Motion_be.enums.CheckType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RentalCheckListCreateRequest {

    @NotNull(message = "Check type is required")
    private CheckType checkType;

    @NotBlank(message = "Image URL must not be blank")
    @Size(min = 10, message = "Image URL must be at least 10 characters")
    private String imgUrl;

    @NotNull(message = "Kilometer is required")
    @Min(value = 0, message = "Kilometers must be greater than 0")
    private Long kilometers;

    @NotNull(message = "Current battery is required")
    @DecimalMin(value = "0.0", message = "Current battery must be greater than or equal to 0")
    @DecimalMax(value = "100.0", message = "Current battery must be less than or equal to 100")
    private double currentBattery;

    @NotNull(message = "Rental ID is required")
    private Long rentalId;
}
