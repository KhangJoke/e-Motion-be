package com.swp391.e_Motion_be.mapper;

import com.swp391.e_Motion_be.dto.responses.PaymentResponse;
import com.swp391.e_Motion_be.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring" , uses = {RentalMapper.class , DepositMapper.class})
public interface PaymentMapper {
    @Mapping(source = "user.email", target = "userEmail")
    @Mapping(source = "rental", target = "rentalResponse")
    @Mapping(source = "deposit", target = "depositResponse")
    PaymentResponse toPaymentResponse(Payment payment);
}
