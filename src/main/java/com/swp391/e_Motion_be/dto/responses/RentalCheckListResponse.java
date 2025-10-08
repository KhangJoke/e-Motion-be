package com.swp391.e_Motion_be.dto.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.Locale;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RentalCheckListResponse {
    private Long id;
    private String type;
    private String fee;
    private Double currentBattery;
    private String img;
    private Long rentalId;
    private String staffEmail;
    private LocalDateTime createdAt;
}
