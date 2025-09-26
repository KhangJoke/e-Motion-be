package com.swp391.e_Motion_be.mapper;

import com.swp391.e_Motion_be.dto.requests.vehicle.VehicleCreationRequest;
import com.swp391.e_Motion_be.dto.requests.vehicle.VehicleUpdateRequest;
import com.swp391.e_Motion_be.dto.responses.VehicleResponse;
import com.swp391.e_Motion_be.entity.Vehicle;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface VehicleMapper {
    @Mapping(source = "stationId", target = "station.id")// handle manually in service
    Vehicle toVehicleEntity(VehicleCreationRequest request);

    @Mapping(source = "station.id", target = "stationId")
    VehicleResponse toVehicleResponse(Vehicle vehicle);

    void updateVehicleFromRequest(@MappingTarget Vehicle vehicle, VehicleUpdateRequest request);
}
