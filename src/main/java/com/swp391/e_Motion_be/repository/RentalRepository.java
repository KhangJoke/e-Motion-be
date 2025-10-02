package com.swp391.e_Motion_be.repository;

import com.swp391.e_Motion_be.entity.Rental;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RentalRepository extends JpaRepository<Rental,Long> {
    Optional<Rental> getRentalById(Long id);
}
