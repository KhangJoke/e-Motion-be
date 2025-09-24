package com.swp391.e_Motion_be.repository;

import com.swp391.e_Motion_be.entity.Vehicle;
import com.swp391.e_Motion_be.enums.VehicleStatus;
import com.swp391.e_Motion_be.enums.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    public List<Vehicle> findByVehicleStatus(VehicleStatus vehicleStatus);
    public List<Vehicle> findByVehicleType(VehicleType vehicleType);
    public Vehicle findByPlateNumber(String plateNumber);
    List<Vehicle> findByNameContainingIgnoreCase(String name);
}
