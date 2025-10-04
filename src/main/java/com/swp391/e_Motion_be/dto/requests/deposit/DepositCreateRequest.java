package com.swp391.e_Motion_be.dto.requests.deposit;

import com.swp391.e_Motion_be.enums.DepositStatus;
import com.swp391.e_Motion_be.validator.validateReservationOrRentalId.OneOfReservationOrRental;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@OneOfReservationOrRental
public class DepositCreateRequest {
    @NotNull(message = "New status must not be blank")
    DepositStatus status;
    double amount;
    Long reservationId;
    Long rentalId;
}
