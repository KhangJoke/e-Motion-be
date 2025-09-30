package com.swp391.e_Motion_be.dto.requests.reservation;

import com.swp391.e_Motion_be.enums.ReservationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateReservationStatusRequest {
    @NotBlank(message = "Reservation code must not be blank")
    @Size(min = 6, max = 6, message = "Reservation code must be exactly 6 characters")
    @Pattern(regexp = "^[0-9]{6}$", message = "Reservation code must be 6 digits")
    private String reservationCode;
    @NotNull(message = "New status must not be blank")
    private ReservationStatus newStatus;
}
