package com.swp391.e_Motion_be.mapper;

import com.swp391.e_Motion_be.dto.requests.vehicle.VehicleCreationRequest;
import com.swp391.e_Motion_be.dto.requests.vehicle.VehicleUpdateRequest;
import com.swp391.e_Motion_be.dto.responses.vehicle.VehicleDetailResponse;
import com.swp391.e_Motion_be.dto.responses.vehicle.VehicleListResponse;
import com.swp391.e_Motion_be.entity.ImgVehicle;
import com.swp391.e_Motion_be.entity.Vehicle;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface VehicleMapper {
    @Mapping(source = "stationId", target = "station.id")// handle manually in service
    Vehicle toVehicleEntity(VehicleCreationRequest request);

    @Mapping(source = "station.id", target = "stationId")
    @Mapping(source = "type", target = "type")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "category", target = "category")
    @Mapping(source = "station.address", target = "address")
    @Mapping(source = "station.city", target = "city") // map Station -> city
    @Mapping(target = "images", expression = "java(getImageUrls(vehicle))")
    @Mapping(target = "pricePer8Hours", expression = "java(roundToNearest10(vehicle.getPricePer4Hours() * 1.4))")
    @Mapping(target = "pricePer12Hours", expression = "java(roundToNearest10(vehicle.getPricePer4Hours() * 1.6))")
    @Mapping(target = "pricePerDay", expression = "java(roundToNearest10(vehicle.getPricePer4Hours() * 2))")
    VehicleDetailResponse toVehicleDetailResponse(Vehicle vehicle);

    @Mapping(source = "station.id", target = "stationId")
    @Mapping(source = "station.city", target = "city") // map Station -> city
    @Mapping(source = "type", target = "type")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "category", target = "category")
    @Mapping(target = "isMain", expression = "java(getMainImage(vehicle))")
    VehicleListResponse toVehicleListResponse(Vehicle vehicle);

    void updateVehicleFromRequest(@MappingTarget Vehicle vehicle, VehicleUpdateRequest request);

    default String getMainImage(Vehicle vehicle) {
        if (vehicle.getImages() == null) return null;
        return vehicle.getImages().stream()
                .filter(ImgVehicle::isMain)
                .findFirst()
                .map(ImgVehicle::getUrl)
                .orElse(null);
    }

    default List<String> getImageUrls(Vehicle vehicle) {
        if (vehicle.getImages() == null) return List.of();
        return vehicle.getImages().stream()
                .map(ImgVehicle::getUrl)
                .toList();
    }

    default double roundToNearest10(double value) {
        return Math.round(value / 10.0) * 10.0;
    }
}
