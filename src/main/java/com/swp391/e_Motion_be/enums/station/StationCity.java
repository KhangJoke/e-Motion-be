package com.swp391.e_Motion_be.enums.station;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.swp391.e_Motion_be.deserializer.StationCityDeserializer;
import com.swp391.e_Motion_be.enums.ErrorCode;
import com.swp391.e_Motion_be.exception.AppException;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@JsonDeserialize(using = StationCityDeserializer.class)
public enum StationCity {
    TP_HCM("Hồ Chí Minh"),
    HANOI("Hà Nội");

    private final String name;

    @JsonValue
    public String getName() {
        return name;
    }

    // Chuyển từ displayName sang enum
    public static StationCity fromDisplayName(String name) {
        for (StationCity city : values()) {
            if (city.getName().equalsIgnoreCase(name)) {
                return city;
            }
        }
        throw new AppException(ErrorCode.STATION_CITY_INVALID);
    }
}
