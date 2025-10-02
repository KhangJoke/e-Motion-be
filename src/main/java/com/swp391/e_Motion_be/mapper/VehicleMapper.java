package com.swp391.e_Motion_be.mapper;

import com.swp391.e_Motion_be.dto.requests.vehicle.VehicleCreationRequest;
import com.swp391.e_Motion_be.dto.requests.vehicle.VehicleUpdateRequest;
import com.swp391.e_Motion_be.dto.responses.vehicle.VehicleDetailResponse;
import com.swp391.e_Motion_be.dto.responses.vehicle.VehicleListResponse;
import com.swp391.e_Motion_be.entity.Vehicle;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface VehicleMapper {
    @Mapping(source = "stationId", target = "station.id")// handle manually in service
    Vehicle toVehicleEntity(VehicleCreationRequest request);

    @Mapping(source = "station.id", target = "stationId")
    @Mapping(source = "type", target = "type")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "category", target = "category")
    VehicleDetailResponse toVehicleDetailResponse(Vehicle vehicle);

    @Mapping(source = "station.id", target = "stationId")
    @Mapping(source = "station.city", target = "city") // map Station -> city
    @Mapping(source = "type", target = "type")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "category", target = "category")
    VehicleListResponse toVehicleListResponse(Vehicle vehicle);

    void updateVehicleFromRequest(@MappingTarget Vehicle vehicle, VehicleUpdateRequest request);

}
