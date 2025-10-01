package com.swp391.e_Motion_be.repository;

import com.swp391.e_Motion_be.entity.VehicleLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface VehicleLogRepository extends JpaRepository<VehicleLog, Long> {
    List<VehicleLog> findVehicleLogByVehicleId(Long vehicleId);
    List<VehicleLog> findByStaff_Id(Long staffId);
}
