package com.swp391.e_Motion_be.dto.requests.checklist;

import com.swp391.e_Motion_be.enums.CheckType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RentalCheckListUpdateRequest {

    @NotNull(message = "ID is required")
    private Long id;

    @NotBlank(message = "Staff email is required")
    @Email(message = "Staff email must be a valid email address")
    private String staffEmail;

    @NotNull(message = "Check type is required")
    private CheckType type;

    @NotNull(message = "Current battery is required")
    @DecimalMin(value = "0.0", message = "Current battery must be greater than or equal to 0")
    @DecimalMax(value = "100.0", message = "Current battery must be less than or equal to 100")
    private Double currentBattery;

    @NotBlank(message = "Img is required")
    private String img;

    @NotNull(message = "Rental ID is required")
    private Long rentalId;
}
