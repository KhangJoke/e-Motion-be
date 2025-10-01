package com.swp391.e_Motion_be.service;

import com.swp391.e_Motion_be.dto.requests.vehicle.VehicleCreationRequest;
import com.swp391.e_Motion_be.dto.requests.vehicle.VehicleFindRequest;
import com.swp391.e_Motion_be.dto.requests.vehicle.VehicleUpdateRequest;
import com.swp391.e_Motion_be.dto.responses.VehicleResponse;
import com.swp391.e_Motion_be.entity.Station;
import com.swp391.e_Motion_be.entity.Vehicle;
import com.swp391.e_Motion_be.enums.ErrorCode;
import com.swp391.e_Motion_be.enums.VehicleStatus;
import com.swp391.e_Motion_be.enums.VehicleType;
import com.swp391.e_Motion_be.exception.AppException;
import com.swp391.e_Motion_be.mapper.VehicleMapper;
import com.swp391.e_Motion_be.repository.StationRepository;
import com.swp391.e_Motion_be.repository.VehicleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleMapper vehicleMapper;
    private final StationRepository stationRepository;

    // Find by ID
    public VehicleResponse findVehicleById(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_NOT_EXIST));
        return vehicleMapper.toVehicleResponse(vehicle);
    }

    // Find by PlateNumber
    public VehicleResponse findVehicleByPlateNumber(String plateNumber) {
        Vehicle vehicle = vehicleRepository.findByPlateNumber(plateNumber)
                .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_NOT_EXIST));
        return vehicleMapper.toVehicleResponse(vehicle);
    }

    // Find all
    public List<VehicleResponse> findAllVehicle() {
        return vehicleRepository.findAll()
                .stream()
                .map(vehicleMapper::toVehicleResponse)
                .toList();
    }

    // Find by status
    public List<VehicleResponse> findVehicleByStatus(VehicleStatus status) {
        return vehicleRepository.findByVehicleStatus(status)
                .stream()
                .map(vehicleMapper::toVehicleResponse)
                .toList();
    }

    // Find by type
    public List<VehicleResponse> findVehicleByType(VehicleType type) {
        return vehicleRepository.findByVehicleType(type)
                .stream()
                .map(vehicleMapper::toVehicleResponse)
                .toList();
    }

    // Find by name
    public List<VehicleResponse> searchVehiclesByName(String name) {
        return vehicleRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(vehicleMapper::toVehicleResponse)
                .toList();
    }

    // CREATE
    public VehicleResponse createVehicle(VehicleCreationRequest request) {
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
        return vehicleMapper.toVehicleResponse(vehicle);
    }

    // UPDATE
    @Transactional
    public VehicleResponse updateVehicle(Long id, VehicleUpdateRequest request) {

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
        return vehicleMapper.toVehicleResponse(existing);
    }

    //DELETE
    public void deleteVehicleById(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_NOT_EXIST));

        vehicleRepository.delete(vehicle);
    }

    // Search bằng thanh tìm kiếm
    public List<VehicleResponse> searchVehicles(VehicleFindRequest request) {
        List<Vehicle> vehicles = vehicleRepository.findByStation_CityAndVehicleStatus(request.getCity(), VehicleStatus.AVAILABLE);
        return vehicles.stream()
                .filter(v -> v.getReservations().stream()
                        .noneMatch(r -> r.getStartTime().isBefore(request.getEndTime()) && r.getEndTime().isAfter(request.getStartTime())))
                .filter(v -> v.getRentals().stream()
                        .noneMatch(r -> r.getStartTime().isBefore(request.getEndTime()) && r.getEndTime().isAfter(request.getStartTime())))
                .map(vehicleMapper::toVehicleResponse)
                .toList();
    }
}