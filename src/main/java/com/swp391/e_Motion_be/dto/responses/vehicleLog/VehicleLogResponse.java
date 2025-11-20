package com.swp391.e_Motion_be.dto.responses.vehicleLog;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.swp391.e_Motion_be.dto.convert.PlainDoubleToNumberSerializer;
import com.swp391.e_Motion_be.dto.vehicleLog.VehicleLogItem;
import com.swp391.e_Motion_be.enums.RentalStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VehicleLogResponse {
    private Long id;
    private List<VehicleLogItem> repairItems;
    @JsonSerialize(using  = PlainDoubleToNumberSerializer.class)
    private Double cost;
    private List<String> imgs;
    private LocalDateTime createdAt;
    private Long vehicleId;
    private Long staffId;
    private Long rentalId;
    private RentalStatus rentalStatus;
}
