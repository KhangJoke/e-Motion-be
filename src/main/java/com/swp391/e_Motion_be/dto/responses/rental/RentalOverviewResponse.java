package com.swp391.e_Motion_be.dto.responses.rental;

import com.swp391.e_Motion_be.dto.responses.RentalResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RentalOverviewResponse {
    private RentalResponse rentalResponse;
    private double reservationDeposit;
    private double rentalDeposit;
    private double checkListFee;
    private Map<String, Double> vehicleDamages;
    private double vehicleDamageFee;
    private boolean refundEligible;
}
