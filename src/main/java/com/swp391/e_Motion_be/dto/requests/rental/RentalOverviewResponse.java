package com.swp391.e_Motion_be.dto.requests.rental;

import com.swp391.e_Motion_be.entity.Rental;
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
    private Rental rental;
    private double reservationDeposit;
    private double rentalDeposit;
    private double checkListFee;
    private Map<String, Double> vehicleDamages;
    private double vehicleDamageFee;
    private boolean refundEligible;
}
