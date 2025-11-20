package com.swp391.e_Motion_be.mapper;

import com.swp391.e_Motion_be.dto.requests.station.StationCreationRequest;
import com.swp391.e_Motion_be.dto.requests.station.StationUpdateRequest;
import com.swp391.e_Motion_be.dto.responses.station.StationCityResponse;
import com.swp391.e_Motion_be.dto.responses.station.StationResponse;
import com.swp391.e_Motion_be.entity.Station;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface StationMapper {
    Station toStationEntity(StationCreationRequest request);
    StationResponse toStationResponse(Station station);
    StationCityResponse toStationCityResponse(Station station);
    void updateStation(StationUpdateRequest request,
                       @MappingTarget Station station);

}
