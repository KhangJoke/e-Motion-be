package com.swp391.e_Motion_be.mapper;

import com.swp391.e_Motion_be.util.CurrencyUtil;
import com.swp391.e_Motion_be.dto.requests.checklist.RentalCheckListCreateRequest;
import com.swp391.e_Motion_be.dto.responses.RentalCheckListResponse;
import com.swp391.e_Motion_be.entity.RentalCheckList;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public abstract class RentalCheckListMapper {
    public abstract RentalCheckList toCheckListEntity(RentalCheckListCreateRequest request);
    @Mapping(source = "rental.id", target = "rentalId")
    @Mapping(source = "staff.user.email", target = "staffEmail")
    @Mapping(target = "fee", expression = "java(formatFee(rentalCheckOut.getFee()))")
    public abstract RentalCheckListResponse toRentalCheckListResponse(RentalCheckList rentalCheckOut);

    String formatFee(Double fee){
        return CurrencyUtil.formatVnCurrency(fee);
    }
}
