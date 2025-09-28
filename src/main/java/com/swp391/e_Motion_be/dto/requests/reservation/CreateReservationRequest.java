package com.swp391.e_Motion_be.dto.requests.reservation;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateReservationRequest {
    @NotBlank(message = "User email must not be blank")
    @Size(min=10, max=255, message = "Email must be between 10 and 255 characters")
    @Pattern(regexp = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$", message = "Invalid email format")
    private String userEmail;
    @NotNull(message = "Vehicle ID must not be blank")
    private Long vehicleId;
    @NotNull(message = "Station ID must not be blank")
    private Long stationId;
    @NotNull(message = "Reservation start time must not be blank")
    private LocalDateTime startTime;
    @NotNull(message = "Reservation end time must not be blank")
    private LocalDateTime endTime;
}
