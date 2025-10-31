package com.swp391.e_Motion_be.mapper;

import com.swp391.e_Motion_be.dto.requests.rental.RentalCreateRequest;
import com.swp391.e_Motion_be.dto.responses.rental.RentalResponse;
import com.swp391.e_Motion_be.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {DepositMapper.class, RentalCheckListMapper.class, VehicleLogMapper.class, VehicleMapper.class})
public interface RentalMapper {
    @Mapping(target = "id", ignore = true) //bỏ qua id vì rental có id riêng
    @Mapping(target = "status", constant = "PENDING") // set cứng
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "payments", ignore = true)
    @Mapping(target = "vehicle", source = "vehicle")
    @Mapping(target = "station", source = "station")
    @Mapping(target = "user", source = "user")
    @Mapping(target = "staff", source = "staff")
    Rental toRentalEntity(RentalCreateRequest request, Vehicle vehicle, Station station, User user, Staff staff);
    @Mapping(source = "vehicle", target = "vehicle")
    @Mapping(source = "reservation.id", target = "reservationId")
    @Mapping(source = "user.email", target = "userEmail")
    @Mapping(source = "staff.id", target = "staffId")
    @Mapping(source = "station.id", target = "stationId")
    @Mapping(source = "deposit", target = "rentalDeposit")
    @Mapping(source = "reservation.deposit", target = "reservationDeposit")
    @Mapping(source = "rentalCheckLists", target = "rentalCheckLists")
    @Mapping(source = "vehicleLog", target = "vehicleLog")
    RentalResponse toRentalResponse(Rental rental);
    @Mapping(target = "id", ignore = true) //bỏ qua id vì rental có id riêng
    @Mapping(target = "status", constant = "PENDING") // set cứng
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "deposit", ignore = true)
    Rental fromReservationToRental(Reservation reservation);
}
