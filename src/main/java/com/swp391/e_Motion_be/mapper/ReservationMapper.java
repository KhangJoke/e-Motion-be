package com.swp391.e_Motion_be.mapper;

import com.swp391.e_Motion_be.dto.requests.reservation.CreateReservationRequest;
import com.swp391.e_Motion_be.dto.responses.ReservationResponse;
import com.swp391.e_Motion_be.entity.Reservation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReservationMapper {
    Reservation toReservationEntity(CreateReservationRequest request);

    @Mapping(source = "user.email", target = "userEmail")
    @Mapping(source = "vehicle.id", target = "vehicleId")
    @Mapping(source = "station.id", target = "stationId")
    ReservationResponse toReservationResponse(Reservation reservation);
}
