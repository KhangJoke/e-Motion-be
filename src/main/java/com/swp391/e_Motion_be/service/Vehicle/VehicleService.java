package com.swp391.e_Motion_be.service.Vehicle;

import com.swp391.e_Motion_be.entity.Vehicle;
import com.swp391.e_Motion_be.enums.ErrorCode;
import com.swp391.e_Motion_be.enums.VehicleStatus;
import com.swp391.e_Motion_be.enums.VehicleType;
import com.swp391.e_Motion_be.exception.AppException;
import com.swp391.e_Motion_be.repository.VehicleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VehicleService {
    private final VehicleRepository vehicleRepository;

    public VehicleService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    // CREATE
    public Vehicle createVehicle(Vehicle vehicle) {
        if (vehicleRepository.findByPlateNumber(vehicle.getPlateNumber()) != null) {
            throw new AppException(ErrorCode.VEHICLE_EXIST);
        }
        return vehicleRepository.save(vehicle);
    }

    // UPDATE
    public Vehicle updateVehicle(Long id, Vehicle updatedVehicle) {
        Vehicle existing = vehicleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_NOT_EXIST));

        existing.setName(updatedVehicle.getName()); //Name
        existing.setVehicleType(updatedVehicle.getVehicleType()); //Type
        existing.setVehicleStatus(updatedVehicle.getVehicleStatus()); //Status
        existing.setConsumptionRate(updatedVehicle.getConsumptionRate()); // DO hao dien
        existing.setBatteryLevel(updatedVehicle.getBatteryLevel()); //BatteryLevel
        existing.setBatteryCapacity(updatedVehicle.getBatteryCapacity()); //Capacity
        existing.setPlateNumber(updatedVehicle.getPlateNumber()); //PlateNumber
        existing.setLastMaintenance(updatedVehicle.getLastMaintenance()); //Last Maintenance

        return vehicleRepository.save(existing);
    }

    // DELETE
    public void deleteVehicleById(Long id) {
        if (vehicleRepository.findById(id).isEmpty()) {
            throw new AppException(ErrorCode.VEHICLE_NOT_EXIST);
        }
        vehicleRepository.deleteById(id);
    }

    // Find by ID
    public Vehicle findVehicleById(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_NOT_EXIST));
    }

    // Find all
    public List<Vehicle> findAllVehicle() {
        return vehicleRepository.findAll();
    }

    // Find by status
    public List<Vehicle> findVehicleByStatus(VehicleStatus status) {
        return vehicleRepository.findByVehicleStatus(status);
    }

    // Find by type
    public List<Vehicle> findVehicleByType(VehicleType type) {
        return vehicleRepository.findByVehicleType(type);
    }

    //Find by name
    public List<Vehicle> searchVehiclesByName(String name) {
        return vehicleRepository.findByNameContainingIgnoreCase(name);
    }
}
