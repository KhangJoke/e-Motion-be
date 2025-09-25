package com.swp391.e_Motion_be.repository;

import com.swp391.e_Motion_be.entity.Station;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StationRepository extends JpaRepository<Station, Long> {
    boolean existsByName(String name);
    Station findByName(String name);
    List<Station> findByAddress(String address);
}
