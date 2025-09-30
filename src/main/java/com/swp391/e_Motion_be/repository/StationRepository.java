package com.swp391.e_Motion_be.repository;

import com.swp391.e_Motion_be.entity.Station;
import com.swp391.e_Motion_be.enums.StationCity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StationRepository extends JpaRepository<Station, Long> {
    boolean existsByNameIgnoreCase(String name);
    Optional<Station> findByNameIgnoreCase(String name);
    Optional<Station> findById(Long id);
    List<Station> findByAddressIgnoreCase(String address);
    List<Station> findByStationCity(StationCity stationCity);
}
