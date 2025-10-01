package com.swp391.e_Motion_be.dto.requests.rental;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RentalCreateFromReservationRequest {
    @NotNull(message = "Reservation Code must be not null")
    String reservationCode;
    @NotNull(message = "Staff ID must be not null")
    long staffId;
}
