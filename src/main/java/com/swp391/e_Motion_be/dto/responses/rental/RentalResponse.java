package com.swp391.e_Motion_be.dto.responses.rental;

import com.swp391.e_Motion_be.dto.responses.DepositResponse;
import com.swp391.e_Motion_be.dto.responses.RentalCheckListResponse;
import com.swp391.e_Motion_be.dto.responses.VehicleLogResponse;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RentalResponse {
    long id;
    String status;
    LocalDateTime startTime;
    LocalDateTime endTime;
    double rentFee;
    LocalDateTime createdAt;
    long vehicleId;
    Long reservationId;
    String userEmail;
    DepositResponse deposit;
    List<RentalCheckListResponse> rentalCheckLists;
    VehicleLogResponse vehicleLog;
    long stationId;
    long staffId;
}
