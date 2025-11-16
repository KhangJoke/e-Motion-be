package com.swp391.e_Motion_be.dto.responses.vehicle;

import com.swp391.e_Motion_be.enums.vehicle.FeeType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FeeResponse {
    private String description;
    private FeeType feeType;
    private Double value;
}
