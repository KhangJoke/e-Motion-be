package com.swp391.e_Motion_be.mapper;

import com.swp391.e_Motion_be.dto.requests.station.StationCreationRequest;
import com.swp391.e_Motion_be.dto.requests.station.StationUpdateRequest;
import com.swp391.e_Motion_be.dto.responses.StationResponse;
import com.swp391.e_Motion_be.entity.Station;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface StationMapper {
    Station toStationEntity(StationCreationRequest stationCreationRequest);
    StationResponse toStationResponse(Station station);
    void updateStation(StationUpdateRequest stationUpdateRequest ,
                       @MappingTarget Station station);

}
