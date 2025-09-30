package com.swp391.e_Motion_be.repository;

import com.swp391.e_Motion_be.entity.Station;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StationRepository extends JpaRepository<Station, Long> {
    boolean existsByName(String name);
    Optional<Station> findByName(String name);
    Optional<Station> findById(Long id);
    List<Station> findByAddress(String address);
}
