package com.swp391.e_Motion_be.dto.requests.payment;

import com.swp391.e_Motion_be.entity.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentEmailRequest {
    private String userEmail;
    private Payment payment;
    private Deposit deposit;
    private Rental rental;
    private RentalCheckList rentalCheckList;
    private VehicleLog vehicleLog;
}
