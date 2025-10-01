package com.swp391.e_Motion_be.mapper;

import com.swp391.e_Motion_be.dto.requests.payment.RefundRequest;
import com.swp391.e_Motion_be.dto.responses.PaymentResponse;
import com.swp391.e_Motion_be.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
    @Mapping(source = "user.email", target = "userEmail")
    @Mapping(source = "rental.id", target = "rentalId")
    @Mapping(source = "deposit.id", target = "depositId")
    PaymentResponse toPaymentResponse(Payment payment);

    @Mapping(source = "payDate", target = "transactionDate")
    RefundRequest toRefundRequest(Payment payment);
}
