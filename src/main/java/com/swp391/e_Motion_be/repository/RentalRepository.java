package com.swp391.e_Motion_be.repository;

import com.swp391.e_Motion_be.entity.Rental;
import com.swp391.e_Motion_be.enums.RentalStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RentalRepository extends JpaRepository<Rental,Long> {
    List<Rental> findByStatusAndEndTimeBetween(RentalStatus status, LocalDateTime from, LocalDateTime to);
    List<Rental> findByVehicle_IdAndStatusNotIn(Long vehicleId, List<RentalStatus> status);
    List<Rental> findByStatus(RentalStatus status);
    List<Rental> findByStatusAndEndTimeBeforeAndOverdueNotifiedFalse(RentalStatus status, LocalDateTime time);
    boolean existsByUser_IdAndStatusNotIn(long userId, List<RentalStatus> status);
}
