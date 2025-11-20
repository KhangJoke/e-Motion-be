package com.swp391.e_Motion_be.repository;

import com.swp391.e_Motion_be.entity.ImgVehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ImgVehicleRepository extends JpaRepository<ImgVehicle, Long> {
    List<ImgVehicle> findByVehicle_Id(Long vid);
    Optional<ImgVehicle> findByVehicle_IdAndUrl(Long vid, String url);
}
