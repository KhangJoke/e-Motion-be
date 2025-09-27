package com.swp391.e_Motion_be.dto.responses;

import com.swp391.e_Motion_be.enums.DepositStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DepositResponse {
    @Enumerated(EnumType.STRING)
    DepositStatus status;
    long amount;
    LocalDateTime createdAt;
}
