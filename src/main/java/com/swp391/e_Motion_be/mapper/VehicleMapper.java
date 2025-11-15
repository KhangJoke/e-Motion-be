package com.swp391.e_Motion_be.mapper;

import com.swp391.e_Motion_be.dto.requests.vehicle.VehicleCreationRequest;
import com.swp391.e_Motion_be.dto.requests.vehicle.VehicleUpdateRequest;
import com.swp391.e_Motion_be.dto.responses.vehicle.VehicleDetailResponse;
import com.swp391.e_Motion_be.dto.responses.vehicle.VehicleListResponse;
import com.swp391.e_Motion_be.dto.responses.vehicle.VehicleUpdateResponse;
import com.swp391.e_Motion_be.entity.ImgVehicle;
import com.swp391.e_Motion_be.entity.Vehicle;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

@Mapper(componentModel = "spring", uses = {StationMapper.class, ImgVehicleMapper.class})
public abstract class VehicleMapper {

    @Value("${price.8h.rate}")
    double price8hRate;
    @Value("${price.12h.rate}")
    double price12hRate;
    @Value("${price.day.rate}")
    double priceDayRate;

    @Mapping(source = "stationId", target = "station.id")
    @Mapping(target = "images", ignore = true)
    public abstract Vehicle toVehicleEntity(VehicleCreationRequest request);

    @Mapping(target = "pricePer8Hours", expression = "java(getPriceEachRate(vehicle, price8hRate))")
    @Mapping(target = "pricePer12Hours", expression = "java(getPriceEachRate(vehicle, price12hRate))")
    @Mapping(target = "pricePerDay", expression = "java(getPriceEachRate(vehicle, priceDayRate))")
    public abstract VehicleDetailResponse toVehicleDetailResponse(Vehicle vehicle);

    @Mapping(source = "station.id", target = "stationId")
    public abstract VehicleUpdateResponse toVehicleUpdateResponse(Vehicle vehicle);

    @Mapping(target = "seats", expression = "java(vehicle.getSeats())")
    @Mapping(target = "main", expression = "java(getMainImage(vehicle))")
    @Mapping(target = "hourRate", expression = "java(getHourRate(hours))")
    @Mapping(target = "priceRate", expression = "java(getPriceRate(vehicle, hours))")
    @Mapping(target = "id", source = "vehicle.id")
    @Mapping(target = "batteryLevel", expression = "java(vehicle.getBatteryLevel())")
    @Mapping(target = "point", expression = "java(vehicle.getPoint())")
    public abstract VehicleListResponse toVehicleListResponse(Vehicle vehicle, long hours);

    @Mapping(target = "images", ignore = true)
    public abstract void updateVehicleFromRequest(@MappingTarget Vehicle vehicle, VehicleUpdateRequest request);

    String getMainImage(Vehicle vehicle) {
        if (vehicle.getImages() == null) return null;
        return vehicle.getImages().stream()
                .filter(ImgVehicle::isMain)
                .findFirst()
                .map(ImgVehicle::getUrl)
                .orElse(null);
    }

    double getPriceEachRate(Vehicle vehicle, double rate) {
        return vehicle.getPricePer4Hours() * rate;
    }

    double getPriceRate(Vehicle vehicle, long hours) {
        if(hours < 8) return vehicle.getPricePer4Hours();
        else if(hours < 12) return vehicle.getPricePer4Hours()*price8hRate;
        else if (hours < 24) return vehicle.getPricePer4Hours()*price12hRate;
        else return vehicle.getPricePer4Hours()*priceDayRate;
    }

    int getHourRate(long hours) {
        if(hours < 8) return 4;
        else if(hours < 12) return 8;
        else if (hours < 24) return 12;
        else return 24;
    }
}
