package com.swp391.e_Motion_be.mapper;

import com.swp391.e_Motion_be.dto.requests.ImgVehicle.ImgVehicleCreationRequest;
import com.swp391.e_Motion_be.dto.requests.ImgVehicle.ImgVehicleUpdateRequest;
import com.swp391.e_Motion_be.dto.responses.ImgVehicleResponse;
import com.swp391.e_Motion_be.entity.ImgVehicle;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ImgVehicleMapper {
    @Mapping(source = "vehicleId", target = "vehicle.id") // handle Vehicle object via service
    ImgVehicle toEntity(ImgVehicleCreationRequest request);
    @Mapping(source = "vehicle.id", target = "vehicleId")
    ImgVehicleResponse toResponse(ImgVehicle entity);
    void updateEntityFromRequest(@MappingTarget ImgVehicle entity, ImgVehicleUpdateRequest request);
}
