package com.swp391.e_Motion_be.repository;

import com.swp391.e_Motion_be.entity.Rental;
import com.swp391.e_Motion_be.entity.Reservation;
import com.swp391.e_Motion_be.entity.User;
import com.swp391.e_Motion_be.enums.RentalStatus;
import com.swp391.e_Motion_be.enums.ReservationStatus;
import com.swp391.e_Motion_be.enums.Role;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RentalRepository extends JpaRepository<Rental,Long> {
    List<Rental> findByUser_Id(Long id);
    Optional<Rental> findByVehicle_Id(Long vid);
    List<Rental> findByStatusAndEndTimeBetweenAndExpiringNotifiedFalse(RentalStatus status, LocalDateTime from, LocalDateTime to);
    List<Rental> findByVehicle_IdAndStatusNotIn(Long vehicleId, List<RentalStatus> status);
    List<Rental> findByStatus(RentalStatus status);
    List<Rental> findByStatusAndEndTimeBeforeAndOverdueNotifiedFalse(RentalStatus status, LocalDateTime time);
    List<Rental> findByStatusAndStartTimeBeforeAndCancelNotifiedFalse(RentalStatus status, LocalDateTime time);
    boolean existsByUser_IdAndStatusNotIn(long userId, List<RentalStatus> status);
    boolean existsByUser_EmailAndStatusNotIn(String user_email, List<RentalStatus> status);
    List<Rental> findByStation_Id(long stationId);
    List<Rental> findByUserEmailContains(String email);
    List<Rental> findByUserEmailContainsAndStatusIn(String email, List<RentalStatus> status);
    Page<Rental> findByStatusInAndUser_EmailContains(List<RentalStatus> statusList, String search, Pageable pageable);
    Page<Rental> findByStatusInAndUser_EmailContainsAndStation_Id(List<RentalStatus> statusList, String search, Long stationId, Pageable pageable);
}
