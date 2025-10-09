package com.swp391.e_Motion_be.dto.responses;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.swp391.e_Motion_be.dto.convert.PlainDoubleToNumberSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VehicleLogResponse {
    private Long id;
    @JsonSerialize(contentUsing = PlainDoubleToNumberSerializer.class)
    private Map<String, Double> repairCost;
    @JsonSerialize(using  = PlainDoubleToNumberSerializer.class)
    private Double cost;
    private List<String> imgs;
    private LocalDateTime createdAt;
    private Long vehicleId;
    private Long staffId;
    private Long rentalId;
}
