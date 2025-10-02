package com.swp391.e_Motion_be.mapper;

import com.swp391.e_Motion_be.dto.requests.rental.RentalCreateRequest;
import com.swp391.e_Motion_be.dto.responses.RentalResponse;
import com.swp391.e_Motion_be.entity.Rental;
import com.swp391.e_Motion_be.entity.Reservation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RentalMapper {
    Rental toRentalEntity(RentalCreateRequest request);
    @Mapping(source = "vehicle.id", target = "vehicleId")
    @Mapping(source = "reservation.id", target = "reservationId")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "staff.id", target = "staffId")
    @Mapping(source = "station.id", target = "stationId")
    RentalResponse toRentalResponse(Rental rental);
    @Mapping(target = "id", ignore = true) //bỏ qua id vì rental có id riêng
    @Mapping(target = "status", constant = "ONGOING") // set cứng
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "deposit", ignore = true)
    Rental fromReservationToRental(Reservation reservation);
}
