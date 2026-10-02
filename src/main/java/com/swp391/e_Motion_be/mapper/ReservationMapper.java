package com.swp391.e_Motion_be.mapper;

import com.swp391.e_Motion_be.dto.requests.reservation.CreateReservationRequest;
import com.swp391.e_Motion_be.dto.responses.reservation.ReservationHistoryListResponse;
import com.swp391.e_Motion_be.dto.responses.reservation.ReservationListResponse;
import com.swp391.e_Motion_be.dto.responses.reservation.ReservationResponse;
import com.swp391.e_Motion_be.entity.Reservation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {VehicleMapper.class})
public interface ReservationMapper {
    Reservation toReservationEntity(CreateReservationRequest request);

    @Mapping(source = "user.email", target = "userEmail")
    @Mapping(source = "vehicle", target = "vehicle")
    @Mapping(source = "station.id", target = "stationId")
    @Mapping(source = "deposit.amount", target = "depositAmount")
    ReservationResponse toReservationResponse(Reservation reservation);

    @Mapping(source = "user.email", target = "userEmail")
    ReservationListResponse toReservationListResponse(Reservation reservation);

    @Mapping(source = "vehicle.name", target = "vehicleName")
    @Mapping(source = "station.name", target = "stationName")
    @Mapping(source = "station.address", target = "stationAddress")
    @Mapping(source = "deposit.amount", target = "depositAmount")
    @Mapping(source = "startTime", target = "startTime")
    @Mapping(source = "endTime", target = "endTime")
    @Mapping(target = "totalAmount", ignore = true)
    ReservationHistoryListResponse toReservationHistoryListResponse(Reservation res);
}
