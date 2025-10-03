package com.swp391.e_Motion_be.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.swp391.e_Motion_be.enums.ErrorCode;
import com.swp391.e_Motion_be.enums.station.StationCity;
import com.swp391.e_Motion_be.exception.AppException;

import java.io.IOException;

// thay đổi jackson mặc định là convert sang enum.name() mà dùng hàm tự định nghĩa để tìm name
public class StationCityDeserializer extends JsonDeserializer<StationCity> {
    @Override
    public StationCity deserialize(JsonParser p, DeserializationContext ctxt){
        String value;
        try {
            value = p.getText();
        } catch (IOException e) {
            throw new AppException(ErrorCode.STATION_CITY_INVALID);
        }
        return StationCity.fromDisplayName(value);
    }
}
