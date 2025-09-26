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
    @Mapping(target = "vehicle", ignore = true) // handle manually in service
    @Mapping(target = "user", ignore = true)    // handle manually in service
    VehicleLog toEntity(VehicleLogCreationRequest request);

    @Mapping(source = "vehicle.id", target = "vehicleId")
    @Mapping(source = "user.id", target = "userId")
    VehicleLogResponse toResponse(VehicleLog vehicleLog);

    void updateVehicleLogFromRequest(@MappingTarget VehicleLog vehicleLog, VehicleLogUpdateRequest request);
}
