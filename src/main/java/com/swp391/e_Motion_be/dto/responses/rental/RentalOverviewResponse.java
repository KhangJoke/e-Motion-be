package com.swp391.e_Motion_be.dto.responses.rental;

import com.swp391.e_Motion_be.dto.vehicleLog.VehicleLogItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RentalOverviewResponse {
    private RentalResponse rentalResponse;
    private double reservationDeposit;
    private double rentalDeposit;
    private double checkListFee;
    private List<VehicleLogItem> vehicleDamages;
    private double vehicleDamageFee;
    private boolean refundEligible;
}
