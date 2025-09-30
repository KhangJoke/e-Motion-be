package com.swp391.e_Motion_be.dto.requests.deposit;

import com.swp391.e_Motion_be.enums.DepositStatus;
import com.swp391.e_Motion_be.validator.OneOfReservationOrRental;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@OneOfReservationOrRental
public class DepositCreateRequest {
    @NotNull(message = "New status must not be blank")
    DepositStatus status;
    long amount;
    String reservationCode;
    Long rentalId;
}
