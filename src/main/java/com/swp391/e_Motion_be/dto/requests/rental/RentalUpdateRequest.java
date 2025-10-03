package com.swp391.e_Motion_be.dto.requests.rental;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import jakarta.validation.constraints.Pattern;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RentalUpdateRequest {
    @NotNull
    Long id;
    @Pattern(
            regexp = "^(PENDING|CONFIRM|ONGOING|COMPLETED|CANCELLED|OVERDUE)$",
            message = "Status must be one of: PENDING, CONFIRM, ONGOING, COMPLETED, CANCELLED, OVERDUE"
    )
    String status;
}
