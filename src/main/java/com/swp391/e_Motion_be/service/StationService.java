package com.swp391.e_Motion_be.service;

import com.swp391.e_Motion_be.dto.requests.station.StationCreationRequest;
import com.swp391.e_Motion_be.dto.requests.station.StationUpdateRequest;
import com.swp391.e_Motion_be.dto.responses.StationResponse;
import com.swp391.e_Motion_be.entity.Station;
import com.swp391.e_Motion_be.enums.ErrorCode;
import com.swp391.e_Motion_be.exception.AppException;
import com.swp391.e_Motion_be.mapper.StationMapper;
import com.swp391.e_Motion_be.repository.StationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StationService {
    private final StationRepository stationRepository;
    private final StationMapper stationMapper;

    public StationResponse createStation(StationCreationRequest request) {
        if (stationRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.STATION_NAME_EXISTS);
        }
        Station station = stationMapper.toStationEntity(request);
        Station savedStation = stationRepository.save(station);
        return stationMapper.toStationResponse(savedStation);
    }

    public StationResponse updateStation(String name, StationUpdateRequest request) {
        Station station = stationRepository.findByName(name).orElseThrow(() -> new AppException(ErrorCode.STATION_NOT_FOUND));
        stationMapper.updateStation(request, station);
        return stationMapper.toStationResponse(stationRepository.save(station));
    }

    public void deleteStation(String name) {
        Station existing = stationRepository.findByName(name).orElseThrow(() -> new AppException(ErrorCode.STATION_NOT_FOUND));
        stationRepository.delete(existing);
    }

    public List<StationResponse> findAllStations() {
        return stationRepository.findAll().stream()
                .map(stationMapper::toStationResponse)
                .toList();
    }

    public StationResponse getStationByName(String name) {
        return stationMapper.toStationResponse(stationRepository.findByName(name).orElseThrow(() -> new AppException(ErrorCode.STATION_NOT_FOUND)));
    }

    public List<StationResponse> getStationsByAddress(String address) {
        return stationRepository.findByAddress(address).stream()
                .map(stationMapper::toStationResponse)
                .toList();
    }

}
