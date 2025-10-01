package com.swp391.e_Motion_be.dto.responses;

import com.swp391.e_Motion_be.enums.CheckType;
import java.time.LocalDateTime;


public class RentalCheckListResponse {
    private Long id;
    private String checkType;
    private String imgUrl;
    private Long kilometers;
    private double currentBattery;
    private Long rentalId;
    private String staffEmail;
    private LocalDateTime createdAt;
}
