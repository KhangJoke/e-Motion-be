package com.swp391.e_Motion_be.dto.requests.deposit;

import com.swp391.e_Motion_be.enums.DepositStatus;
import com.swp391.e_Motion_be.enums.DepositType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
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
    @NotNull(message = "Type must not be blank")
    @Pattern(regexp = "^(RESERVATION|DEPOSIT)$", message = "Type must be one of the following: RESERVATION, DEPOSIT")
    DepositType type;
    @Positive(message = "Amount must be positive")
    long amount;
    @NotNull(message = "User Email is required")
    String email;
}
