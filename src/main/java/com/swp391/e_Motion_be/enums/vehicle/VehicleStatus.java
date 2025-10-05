package com.swp391.e_Motion_be.enums.vehicle;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.swp391.e_Motion_be.deserializer.VehicleStatusDeserializer;
import com.swp391.e_Motion_be.enums.ErrorCode;
import com.swp391.e_Motion_be.enums.station.StationCity;
import com.swp391.e_Motion_be.exception.AppException;
import lombok.AllArgsConstructor;

@JsonDeserialize(using = VehicleStatusDeserializer.class)
@AllArgsConstructor
public enum VehicleStatus {
    AVAILABLE("Sẵn sàng"),
    MAINTAINED("Đang bảo trì"),
    INUSE("Đang thuê");

    private final String description;

    @JsonValue
    public String getDescription() {
        return description;
    }

    // Chuyển từ displayName sang enum
    public static VehicleStatus fromDescription(String name) {
        for (VehicleStatus status : values()) {
            if (status.getDescription().equalsIgnoreCase(name)) {
                return status;
            }
        }
        throw new AppException(ErrorCode.VEHICLE_STATUS_INVALID);
    }
}
