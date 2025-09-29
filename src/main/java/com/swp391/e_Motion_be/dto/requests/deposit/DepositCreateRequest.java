package com.swp391.e_Motion_be.dto.requests.deposit;

import com.swp391.e_Motion_be.enums.DepositStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DepositCreateRequest {
    @NotNull(message = "New status must not be blank")
    @Pattern(regexp = "^(HOLD|RELEASED|FORFEITED)$", message = "Status must be one of the following: HOLD, RELEASED, FORFEITED")
    DepositStatus status;
    long depositAmount;
    String reservationCode;
    Long rentalId;
}
