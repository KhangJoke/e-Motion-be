package com.swp391.e_Motion_be.repository;

import com.swp391.e_Motion_be.entity.ImgVehicle;
import com.swp391.e_Motion_be.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImgVehicleRepository extends JpaRepository<ImgVehicle, Long> {
    List<ImgVehicle> findByVehicle(Vehicle vehicle);
}
