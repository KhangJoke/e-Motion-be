package com.swp391.e_Motion_be.service;

import com.swp391.e_Motion_be.dto.requests.vehicle.VehicleCreationRequest;
import com.swp391.e_Motion_be.dto.requests.vehicle.VehicleFindRequest;
import com.swp391.e_Motion_be.dto.requests.vehicle.VehicleUpdateRequest;
import com.swp391.e_Motion_be.dto.responses.vehicle.VehicleDetailResponse;
import com.swp391.e_Motion_be.dto.responses.vehicle.VehicleListResponse;
import com.swp391.e_Motion_be.dto.responses.vehicle.VehicleSearchResponse;
import com.swp391.e_Motion_be.entity.Station;
import com.swp391.e_Motion_be.entity.Vehicle;
import com.swp391.e_Motion_be.enums.ErrorCode;
import com.swp391.e_Motion_be.enums.vehicle.VehicleStatus;
import com.swp391.e_Motion_be.exception.AppException;
import com.swp391.e_Motion_be.mapper.VehicleMapper;
import com.swp391.e_Motion_be.repository.StationRepository;
import com.swp391.e_Motion_be.repository.VehicleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleMapper vehicleMapper;
    private final StationRepository stationRepository;

    // Find by ID
    public VehicleDetailResponse findVehicleById(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_NOT_EXIST));
        return vehicleMapper.toVehicleDetailResponse(vehicle);
    }

    // Find by PlateNumber
    public VehicleDetailResponse findVehicleByPlateNumber(String plateNumber) {
        Vehicle vehicle = vehicleRepository.findByPlateNumber(plateNumber)
                .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_NOT_EXIST));
        return vehicleMapper.toVehicleDetailResponse(vehicle);
    }

    // CREATE
    public VehicleDetailResponse createVehicle(VehicleCreationRequest request) {
        //Check Station is FOUNd or NOT
        Station station = stationRepository.findById(request.getStationId())
                .orElseThrow(() -> new AppException(ErrorCode.STATION_NOT_FOUND));

        if (vehicleRepository.findByPlateNumber(request.getPlateNumber()).isPresent()) {
            throw new AppException(ErrorCode.VEHICLE_EXIST);
        }
        Vehicle vehicle = vehicleMapper.toVehicleEntity(request);

        vehicle.setStation(station);

        //SAVE
        vehicleRepository.save(vehicle);
        return vehicleMapper.toVehicleDetailResponse(vehicle);
    }
    private double roundToNearest10(double value) {
        return Math.round(value / 10.0) * 10.0;
    }
    // UPDATE
    @Transactional
    public VehicleDetailResponse updateVehicle(Long id, VehicleUpdateRequest request) {

        Vehicle existing = vehicleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_NOT_EXIST));

        if (!existing.getPlateNumber().equals(request.getPlateNumber())
                && vehicleRepository.findByPlateNumber(request.getPlateNumber()).isPresent()) {
            throw new AppException(ErrorCode.VEHICLE_EXIST);
        }

        vehicleMapper.updateVehicleFromRequest(existing, request);

        if (request.getStationId() != null) {
            Station station = stationRepository.findById(request.getStationId())
                    .orElseThrow(() -> new AppException(ErrorCode.STATION_NOT_FOUND));
            existing.setStation(station);
        }

        // No need to call save() — transaction will automatically flush changes
        return vehicleMapper.toVehicleDetailResponse(existing);
    }

    //DELETE
    public void deleteVehicleById(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_NOT_EXIST));

        vehicleRepository.delete(vehicle);
    }

    // Search bằng thanh tìm kiếm
    public VehicleSearchResponse searchVehicles(VehicleFindRequest request) {
        // lấy ra tất cả các xe có thể sử dụng
        List<Vehicle> vehicles = vehicleRepository.findByStation_CityAndStatusIn(request.getCity(), List.of(VehicleStatus.AVAILABLE, VehicleStatus.INUSE));

        List<Vehicle> availables = new ArrayList<>();
        List<Vehicle> unavailables = new ArrayList<>();

        // duyệt qua từng xe và chia vào list phù hợp
        for(Vehicle v : vehicles) {
            boolean hasConflic = v.getReservations().stream()
                    .anyMatch(r -> r.getStartTime().isBefore(request.getEndTime()) && r.getEndTime().isAfter(request.getStartTime()))
                    ||
                    v.getRentals().stream()
                            .anyMatch(r -> r.getStartTime().isBefore(request.getEndTime()) && r.getEndTime().isAfter(request.getStartTime()));

            if (hasConflic) {
                unavailables.add(v);
            } else {
                availables.add(v);
            }
        }

        long hours = Duration.between(request.getStartTime(), request.getEndTime()).toHours();

        return new VehicleSearchResponse(
                availables.stream().map(v -> vehicleMapper.toVehicleListResponse(v, hours)).toList(),
                unavailables.stream().map(v -> vehicleMapper.toVehicleListResponse(v, hours)).toList()
        );
    }

    public boolean isAvailable(long id) {
        List<Vehicle> availableVehicle = vehicleRepository.findByStatus(VehicleStatus.AVAILABLE);
        return availableVehicle.stream().anyMatch(v -> v.getId() == id);
    }
}