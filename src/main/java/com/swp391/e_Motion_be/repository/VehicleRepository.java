package com.swp391.e_Motion_be.repository;

import com.swp391.e_Motion_be.entity.Vehicle;
import com.swp391.e_Motion_be.enums.station.StationCity;
import com.swp391.e_Motion_be.enums.vehicle.VehicleBrand;
import com.swp391.e_Motion_be.enums.vehicle.VehicleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    Optional<Vehicle> findByPlateNumber(String plateNumber);
    List<Vehicle> findByStation_CityAndStatusIn(StationCity city, List<VehicleStatus> statuses);
    List<Vehicle> findByStatus(VehicleStatus status);
    List<Vehicle> findByBrandAndStatus(VehicleBrand brand,VehicleStatus status);
}
