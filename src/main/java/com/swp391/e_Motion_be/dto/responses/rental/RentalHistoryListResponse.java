package com.swp391.e_Motion_be.dto.responses.rental;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RentalHistoryListResponse {
    private Long id;
    private String vehicleName;
    private String vehicleImage;
    private String stationName;
    private String status;
    private LocalDateTime createdAt;
    private String paymentUrl;
}
