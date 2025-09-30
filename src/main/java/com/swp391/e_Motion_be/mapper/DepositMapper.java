package com.swp391.e_Motion_be.mapper;

import com.swp391.e_Motion_be.dto.requests.deposit.DepositCreateRequest;
import com.swp391.e_Motion_be.dto.responses.DepositResponse;
import com.swp391.e_Motion_be.entity.Deposit;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DepositMapper {
    Deposit toDepositEntity(DepositCreateRequest request);
    @Mapping(source = "reservation.code", target = "reservationCode")
    @Mapping(source = "rental.id", target = "rentalId")
    DepositResponse toDepositResponse(Deposit deposit);
}
