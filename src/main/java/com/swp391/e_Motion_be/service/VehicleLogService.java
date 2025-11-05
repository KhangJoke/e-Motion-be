package com.swp391.e_Motion_be.service;

import com.swp391.e_Motion_be.dto.requests.vehicleLog.PageAndFilterVehicleLogRequest;
import com.swp391.e_Motion_be.dto.requests.vehicleLog.VehicleLogCreationRequest;
import com.swp391.e_Motion_be.dto.requests.vehicleLog.VehicleLogUpdateRequest;
import com.swp391.e_Motion_be.dto.responses.vehicleLog.PageAndFilterVehicleLogResponse;
import com.swp391.e_Motion_be.dto.responses.vehicleLog.VehicleLogResponse;
import com.swp391.e_Motion_be.dto.vehicleLog.VehicleLogItem;
import com.swp391.e_Motion_be.entity.Rental;
import com.swp391.e_Motion_be.entity.Staff;
import com.swp391.e_Motion_be.entity.Vehicle;
import com.swp391.e_Motion_be.entity.VehicleLog;
import com.swp391.e_Motion_be.enums.ErrorCode;
import com.swp391.e_Motion_be.enums.RentalStatus;
import com.swp391.e_Motion_be.enums.vehicle.VehicleStatus;
import com.swp391.e_Motion_be.exception.AppException;
import com.swp391.e_Motion_be.mapper.VehicleLogMapper;
import com.swp391.e_Motion_be.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

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

        double totalCost = request.getRepairItems()
                .stream()
                .filter(Objects::nonNull)
                .mapToDouble(VehicleLogItem::getCost)
                .sum();

        totalCost = Math.round(totalCost * 100.0) / 100.0;

        VehicleLog vehicleLog = vehicleLogMapper.toEntity(request);
        vehicleLog.setVehicle(vehicle);
        vehicleLog.setStaff(staff);
        vehicleLog.setRental(rental);
        vehicleLog.setCost(totalCost);
        vehicleLog.setRepairItems(request.getRepairItems());

        // Update vehicle status -> maintance
        vehicle.setStatus(VehicleStatus.MAINTAINED);

        vehicleLogRepository.save(vehicleLog);
        return vehicleLogMapper.toResponse(vehicleLog);
    }

    //UPDATE
    public VehicleLogResponse updateVehicleLog(Long logId, VehicleLogUpdateRequest request) {
        VehicleLog vehicleLog = vehicleLogRepository.findById(logId)
                .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_LOG_NOT_EXIST));

        if(vehicleLog.getRental().getStatus().equals(RentalStatus.COMPLETED)) {
            throw new AppException(ErrorCode.VEHICLE_LOG_RENTAL_COMPLETED);
        }

        double totalCost = request.getRepairItems()
                .stream()
                .filter(Objects::nonNull)
                .mapToDouble(VehicleLogItem::getCost)
                .sum();

        vehicleLog.setCost(totalCost);

        vehicleLogMapper.updateVehicleLogFromRequest(vehicleLog, request);
        vehicleLogRepository.save(vehicleLog);
        return vehicleLogMapper.toResponse(vehicleLog);
    }

    //DELETE
    public void deleteVehicleLog(Long logId) {
        if (!vehicleLogRepository.existsById(logId)) {
            throw new AppException(ErrorCode.VEHICLE_LOG_NOT_EXIST);
        }
        vehicleLogRepository.deleteById(logId);
    }

    public PageAndFilterVehicleLogResponse findByPageAndFilterAndSearch(PageAndFilterVehicleLogRequest request) {
        Pageable pageable = PageRequest.of(request.getPage() - 1, request.getLimit(), Sort.by("id").ascending());
        Page<VehicleLog> vehicleLogsPage = (request.getSearch()==null || request.getSearch()==0) ? vehicleLogRepository.findAll(pageable) : vehicleLogRepository.findByVehicle_id(request.getSearch(), pageable);

        List<VehicleLogResponse> vehicleLog = vehicleLogsPage.getContent().stream()
                .map(vehicleLogMapper::toResponse)
                .toList();

        return new PageAndFilterVehicleLogResponse(vehicleLog, vehicleLogsPage.getTotalPages());
    }
}
