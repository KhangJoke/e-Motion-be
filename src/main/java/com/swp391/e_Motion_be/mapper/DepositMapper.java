package com.swp391.e_Motion_be.mapper;

import com.swp391.e_Motion_be.dto.requests.deposit.DepositCreateRequest;
import com.swp391.e_Motion_be.dto.responses.DepositResponse;
import com.swp391.e_Motion_be.entity.Deposit;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DepositMapper {
    Deposit toDespositEntity(DepositCreateRequest request);
    DepositResponse toDespositResponse(Deposit deposit);
}
