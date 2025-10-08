package com.swp391.e_Motion_be.service;

import com.swp391.e_Motion_be.dto.requests.VehicleLog.VehicleLogCreationRequest;
import com.swp391.e_Motion_be.dto.requests.VehicleLog.VehicleLogUpdateRequest;
import com.swp391.e_Motion_be.dto.responses.VehicleLogResponse;
import com.swp391.e_Motion_be.entity.*;
import com.swp391.e_Motion_be.enums.ErrorCode;
import com.swp391.e_Motion_be.enums.vehicle.VehicleStatus;
import com.swp391.e_Motion_be.exception.AppException;
import com.swp391.e_Motion_be.mapper.VehicleLogMapper;
import com.swp391.e_Motion_be.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VehicleLogService {
    private final VehicleLogRepository vehicleLogRepository;
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;
    private final VehicleLogMapper vehicleLogMapper;
    private final StaffRepository staffRepository;
    private final RentalRepository rentalRepository;

    // FIND ALL
    public List<VehicleLogResponse> findAllVehicleLogs() {
        List<VehicleLog> logs = vehicleLogRepository.findAll();

        if (logs.isEmpty()) {
            throw new AppException(ErrorCode.VEHICLE_LOG_LIST_EMPTY);
        }

        return logs.stream()
                .map(vehicleLogMapper::toResponse)
                .collect(Collectors.toList());
    }

    // FIND BY VEHICLE ID
    public List<VehicleLogResponse> findVehicleLogByVehicleId(Long vehicleId) {
        List<VehicleLog> logs = vehicleLogRepository.findVehicleLogByVehicleId(vehicleId);

        if (logs.isEmpty()) {
            throw new AppException(ErrorCode.VEHICLE_LOG_LIST_EMPTY);
        }

        return logs.stream()
                .map(vehicleLogMapper::toResponse)
                .collect(Collectors.toList());
    }

    // FIND BY USER ID
    public List<VehicleLogResponse> findVehicleLogByStaffId(Long staffId) {
        List<VehicleLog> logs = vehicleLogRepository.findByStaff_Id(staffId);

        if (logs.isEmpty()) {
            throw new AppException(ErrorCode.VEHICLE_LOG_LIST_EMPTY);
        }

        return logs.stream()
                .map(vehicleLogMapper::toResponse)
                .collect(Collectors.toList());
    }

    // FIND BY ID
    public VehicleLogResponse findVehicleLogById(Long vehicleId) {
        VehicleLog vehicleLog = vehicleLogRepository.findById(vehicleId)
                .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_LOG_NOT_EXIST));
        return vehicleLogMapper.toResponse(vehicleLog);
    }

    //CREATE
    public VehicleLogResponse createVehicleLog(VehicleLogCreationRequest request) {
        // lấy ra các entity liên quan
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_ID_NOT_FOUND));
        Staff staff = staffRepository.findById(request.getStaffId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTS));
        Rental rental = rentalRepository.findById(request.getRentalId())
                .orElseThrow(() -> new AppException(ErrorCode.RENTAL_NOT_FOUND));

        double totalCost = request.getRepairCost().values()
                .stream()
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .sum();

        VehicleLog vehicleLog = vehicleLogMapper.toEntity(request);
        vehicleLog.setVehicle(vehicle);
        vehicleLog.setStaff(staff);
        vehicleLog.setRental(rental);
        vehicleLog.setCost(totalCost);


        // Update vehicle status -> maintance
        vehicle.setStatus(VehicleStatus.MAINTAINED);

        vehicleLogRepository.save(vehicleLog);
        return vehicleLogMapper.toResponse(vehicleLog);
    }

    //UPDATE
    public VehicleLogResponse updateVehicleLog(Long logId, VehicleLogUpdateRequest request) {
        VehicleLog vehicleLog = vehicleLogRepository.findById(logId)
                .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_LOG_NOT_EXIST));

        vehicleLogMapper.updateVehicleLogFromRequest(vehicleLog, request);
        return vehicleLogMapper.toResponse(vehicleLog);
    }

    //DELETE
    public void deleteVehicleLog(Long logId) {
        if (!vehicleLogRepository.existsById(logId)) {
            throw new AppException(ErrorCode.VEHICLE_LOG_NOT_EXIST);
        }
        vehicleLogRepository.deleteById(logId);
    }
}
