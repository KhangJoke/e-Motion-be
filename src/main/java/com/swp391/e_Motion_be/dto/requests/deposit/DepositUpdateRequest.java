package com.swp391.e_Motion_be.dto.requests.deposit;

import com.swp391.e_Motion_be.enums.DepositStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DepositUpdateRequest {
    @NotNull(message = "New status must not be blank")
    @Pattern(regexp = "^(HOLD|RELEASED|FORFEITED)$", message = "Status must be one of the following: HOLD, RELEASED, FORFEITED")
    @Enumerated(EnumType.STRING)
    DepositStatus oldStatus;
    @NotNull(message = "New status must not be blank")
    @Pattern(regexp = "^(HOLD|RELEASED|FORFEITED)$", message = "Status must be one of the following: HOLD, RELEASED, FORFEITED")
    @Enumerated(EnumType.STRING)
    DepositStatus newStatus;
    @NotNull(message = "User Email is required")
    String email;
    @NotNull(message = "Type must not be blank")
    @Pattern(regexp = "^(RESERVATION|DEPOSIT)$", message = "Type must be one of the following: RESERVATION, DEPOSIT")
    @Enumerated(EnumType.STRING)
    DepositType type;
}
