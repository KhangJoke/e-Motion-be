package com.swp391.e_Motion_be.dto.responses.rental;

import com.swp391.e_Motion_be.enums.ContractStatus;
import com.swp391.e_Motion_be.enums.RentalStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ContractConfirmResponse {
    Long rentalId;
    RentalStatus status;
    ContractStatus contractStatus;
    LocalDateTime activatedAt;
}
