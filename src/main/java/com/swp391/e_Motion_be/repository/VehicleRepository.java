package com.swp391.e_Motion_be.repository;

import com.swp391.e_Motion_be.entity.Vehicle;
import com.swp391.e_Motion_be.enums.StationCity;
import com.swp391.e_Motion_be.enums.vehicle.VehicleStatus;
import com.swp391.e_Motion_be.enums.vehicle.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    List<Vehicle> findByStatus(VehicleStatus vehicleStatus);
    List<Vehicle> findByType(VehicleType vehicleType);
    Optional<Vehicle> findByPlateNumber(String plateNumber);
    List<Vehicle> findByNameContainingIgnoreCase(String name);
    List<Vehicle> findByStation_CityAndStatus(StationCity city, VehicleStatus vehicleStatus);
}
