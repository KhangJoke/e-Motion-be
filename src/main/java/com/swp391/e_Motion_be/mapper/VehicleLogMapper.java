package com.swp391.e_Motion_be.mapper;

import com.swp391.e_Motion_be.dto.requests.VehicleLog.VehicleLogCreationRequest;
import com.swp391.e_Motion_be.dto.requests.VehicleLog.VehicleLogUpdateRequest;
import com.swp391.e_Motion_be.dto.responses.VehicleLogResponse;
import com.swp391.e_Motion_be.entity.VehicleLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface VehicleLogMapper {
    @Mapping(source = "vehicleId", target = "vehicle.id") // handle manually in service
    @Mapping(source = "staffId", target = "staff.id")    // handle manually in service
    VehicleLog toEntity(VehicleLogCreationRequest request);

    @Mapping(source = "vehicle.id", target = "vehicleId")
    @Mapping(source = "staff.id", target = "staffId")
    VehicleLogResponse toResponse(VehicleLog vehicleLog);

    void updateVehicleLogFromRequest(@MappingTarget VehicleLog vehicleLog, VehicleLogUpdateRequest request);
}
