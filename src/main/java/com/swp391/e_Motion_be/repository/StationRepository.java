package com.swp391.e_Motion_be.repository;

import com.swp391.e_Motion_be.entity.Station;
import com.swp391.e_Motion_be.enums.station.StationCity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface StationRepository extends JpaRepository<Station, Long> {
    boolean existsByNameIgnoreCase(String name);
    Optional<Station> findByNameIgnoreCase(String name);
    List<Station> findByAddressIgnoreCase(String address);
    List<Station> findByCity(StationCity city);
    @Query("SELECT DISTINCT s.city FROM Station s")
    List<String> findAllCityNames();
}
