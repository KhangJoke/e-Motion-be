package com.swp391.e_Motion_be.dto.responses.rental;

import com.swp391.e_Motion_be.dto.responses.DepositResponse;
import com.swp391.e_Motion_be.dto.responses.StaffResponse;
import com.swp391.e_Motion_be.dto.responses.checkList.RentalCheckListResponse;
import com.swp391.e_Motion_be.dto.responses.vehicle.VehicleDetailResponse;
import com.swp391.e_Motion_be.dto.responses.vehicleLog.VehicleLogResponse;
import com.swp391.e_Motion_be.enums.ContractStatus;
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
    VehicleDetailResponse vehicle;
    String reservationCode;
    String userEmail;
    String contractDocumentUrl;
    ContractStatus contractStatus;
    DepositResponse rentalDeposit;
    DepositResponse reservationDeposit;
    List<RentalCheckListResponse> rentalCheckLists;
    VehicleLogResponse vehicleLog;
    StaffResponse staff;
}
