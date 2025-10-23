package com.swp391.e_Motion_be.repository;

import com.swp391.e_Motion_be.entity.Reservation;
import com.swp391.e_Motion_be.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation,Long> {
    Optional<Reservation> findByCode(String code);
    List<Reservation> findByCodeContains(String code);
    List<Reservation> findByStatus(ReservationStatus status);
    List<Reservation> findByUserEmail(String email);
    List<Reservation> findByStationName(String stationName);
    List<Reservation> findByVehicleId(Long vehicleId);
    List<Reservation> findByEndTimeBefore(LocalDateTime time);
    List<Reservation> findByVehicle_IdAndStatusIn(Long vehicleId, List<ReservationStatus> status);
    boolean existsByUser_EmailAndStatusNotIn(String email, List<ReservationStatus> statuses);
    List<Reservation> findByStatusAndEndTimeBetweenAndExpiringNotifiedFalse(ReservationStatus status, LocalDateTime from, LocalDateTime to);
    List<Reservation> findByStatusInAndEndTimeBeforeAndOverdueNotifiedFalse(List<ReservationStatus> status, LocalDateTime time);
    List<Reservation> findByUserEmailContains(String userEmail);
    List<Reservation> findByUserEmailContainsAndStatus(String keyword, ReservationStatus status);
    List<Reservation> findByCodeContainsAndStatus(String keyword, ReservationStatus status);
}
