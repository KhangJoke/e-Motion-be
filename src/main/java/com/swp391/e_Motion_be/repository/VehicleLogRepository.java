package com.swp391.e_Motion_be.repository;

import com.swp391.e_Motion_be.entity.VehicleLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleLogRepository extends JpaRepository<VehicleLog, Long> {
    List<VehicleLog> findVehicleLogByVehicleId(Long vehicleId);
    List<VehicleLog> findByStaff_Id(Long staffId);
    Page<VehicleLog> findByVehicle_id(Long search, Pageable pageable);
    Optional<VehicleLog> findByRental_Id(Long rentalId);
}
