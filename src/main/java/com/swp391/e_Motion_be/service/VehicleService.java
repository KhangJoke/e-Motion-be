package com.swp391.e_Motion_be.service;

import com.swp391.e_Motion_be.dto.requests.vehicle.VehicleCreationRequest;
import com.swp391.e_Motion_be.dto.requests.vehicle.VehicleFindRequest;
import com.swp391.e_Motion_be.dto.requests.vehicle.VehicleUpdateRequest;
import com.swp391.e_Motion_be.dto.responses.vehicle.VehicleDetailResponse;
import com.swp391.e_Motion_be.dto.responses.vehicle.VehicleListResponse;
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
    public VehicleListResponse findVehicleByPlateNumber(String plateNumber) {
        Vehicle vehicle = vehicleRepository.findByPlateNumber(plateNumber)
                .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_NOT_EXIST));
        return vehicleMapper.toVehicleListResponse(vehicle);
    }

    // Find all
    public List<VehicleListResponse> findAllVehicle() {
        return vehicleRepository.findAll()
                .stream()
                .map(vehicleMapper::toVehicleListResponse)
                .toList();
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
    public List<VehicleListResponse> searchVehicles(VehicleFindRequest request) {
        List<Vehicle> vehicles = vehicleRepository.findByStation_CityAndStatus(request.getCity(), VehicleStatus.AVAILABLE);
        return vehicles.stream()
                .filter(v -> v.getReservations().stream()
                        .noneMatch(r -> r.getStartTime().isBefore(request.getEndTime()) && r.getEndTime().isAfter(request.getStartTime())))
                .filter(v -> v.getRentals().stream()
                        .noneMatch(r -> r.getStartTime().isBefore(request.getEndTime()) && r.getEndTime().isAfter(request.getStartTime())))
                .map(vehicleMapper::toVehicleListResponse)
                .toList();
    }

    public boolean isAvailable(long id) {
        List<Vehicle> availableVehicle = vehicleRepository.findByStatus(VehicleStatus.AVAILABLE);
        return availableVehicle.stream().anyMatch(v -> v.getId() == id);
    }
}