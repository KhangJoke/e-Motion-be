package com.swp391.e_Motion_be.dto.responses;

import java.time.LocalDateTime;


public class RentalCheckListResponse {
    private Long id;
    private String type;
    private double fee;
    private double currentBattery;
    private Long rentalId;
    private String staffEmail;
    private LocalDateTime createdAt;
}
