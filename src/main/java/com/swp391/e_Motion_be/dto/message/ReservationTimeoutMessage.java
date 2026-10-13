package com.swp391.e_Motion_be.dto.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationTimeoutMessage implements Serializable {
    private Long reservationId;
    private String reservationCode;
    private LocalDateTime createdAt;
}
