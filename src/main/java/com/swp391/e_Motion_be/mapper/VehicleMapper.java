package com.swp391.e_Motion_be.mapper;

import com.swp391.e_Motion_be.dto.requests.vehicle.VehicleCreationRequest;
import com.swp391.e_Motion_be.dto.requests.vehicle.VehicleUpdateRequest;
import com.swp391.e_Motion_be.dto.responses.vehicle.VehicleDetailResponse;
import com.swp391.e_Motion_be.dto.responses.vehicle.VehicleListResponse;
import com.swp391.e_Motion_be.entity.ImgVehicle;
import com.swp391.e_Motion_be.entity.Vehicle;
import com.swp391.e_Motion_be.util.CurrencyUtil;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class VehicleMapper {

    @Value("${price.8h.rate}")
    double price8hRate;
    @Value("${price.12h.rate}")
    double price12hRate;
    @Value("${price.day.rate}")
    double priceDayRate;

    @Mapping(source = "stationId", target = "station.id")// handle manually in service
    @Mapping(source = "brand", target = "brand")
    public abstract Vehicle toVehicleEntity(VehicleCreationRequest request);

    @Mapping(source = "station.id", target = "stationId")
    @Mapping(source = "type", target = "type")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "category", target = "category")
    @Mapping(source = "station.address", target = "address")
    @Mapping(source = "brand", target = "brand")
    @Mapping(source = "station.city", target = "city") // map Station -> city
    @Mapping(target = "images", expression = "java(getImageUrls(vehicle))")
    @Mapping(target = "pricePer4Hours", expression = "java(formatFee(vehicle.getPricePer4Hours()))")
    @Mapping(target = "pricePer8Hours", expression = "java(getPriceEachRate(vehicle, price8hRate))")
    @Mapping(target = "pricePer12Hours", expression = "java(getPriceEachRate(vehicle, price12hRate))")
    @Mapping(target = "pricePerDay", expression = "java(getPriceEachRate(vehicle, priceDayRate))")
    @Mapping(target = "depositFee", expression = "java(formatFee(vehicle.getDepositFee()))")
    public abstract VehicleDetailResponse toVehicleDetailResponse(Vehicle vehicle);


    @Mapping(source = "vehicle.station.id", target = "stationId")
    @Mapping(source = "vehicle.station.city", target = "city") // map Station -> city
    @Mapping(source = "vehicle.type", target = "type")
    @Mapping(source = "vehicle.status", target = "status")
    @Mapping(source = "vehicle.brand", target = "brand")
    @Mapping(source = "vehicle.category", target = "category")
    @Mapping(target = "isMain", expression = "java(getMainImage(vehicle))")
    @Mapping(target = "hourRate", expression = "java(getHourRate(hours))")
    @Mapping(target = "priceRate", expression = "java(getPriceRate(vehicle, hours))")
    public abstract VehicleListResponse toVehicleListResponse(Vehicle vehicle, long hours);

    public abstract void updateVehicleFromRequest(@MappingTarget Vehicle vehicle, VehicleUpdateRequest request);

    String getMainImage(Vehicle vehicle) {
        if (vehicle.getImages() == null) return null;
        return vehicle.getImages().stream()
                .filter(ImgVehicle::isMain)
                .findFirst()
                .map(ImgVehicle::getUrl)
                .orElse(null);
    }

    List<String> getImageUrls(Vehicle vehicle) {
        if (vehicle.getImages() == null) return List.of();
        return vehicle.getImages().stream()
                .map(ImgVehicle::getUrl)
                .toList();
    }

    String getPriceEachRate(Vehicle vehicle, double rate) {
        return formatFee(vehicle.getPricePer4Hours() * rate);
    }

    String getPriceRate(Vehicle vehicle, long hours) {
        double fee = 0;
        if(hours < 8) fee = vehicle.getPricePer4Hours();
        else if(hours < 12) fee =  vehicle.getPricePer4Hours()*price8hRate;
        else if (hours < 24) fee =  vehicle.getPricePer4Hours()*price12hRate;
        else fee =  vehicle.getPricePer4Hours()*priceDayRate;
        return formatFee(fee);
    }

    int getHourRate(long hours) {
        if(hours < 8) return 4;
        else if(hours < 12) return 8;
        else if (hours < 24) return 12;
        else return 24;
    }

    String formatFee(double fee) {
        return CurrencyUtil.formatVnCurrency(fee);
    }

}
