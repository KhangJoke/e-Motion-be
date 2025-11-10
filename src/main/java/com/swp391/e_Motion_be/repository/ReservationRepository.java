package com.swp391.e_Motion_be.repository;

import com.swp391.e_Motion_be.entity.Reservation;
import com.swp391.e_Motion_be.enums.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation,Long> {
    List<Reservation> findByUser_Id(Long id);
    Optional<Reservation> findByCode(String code);
    List<Reservation> findByUserEmailIgnoreCase(String email);
    List<Reservation> findByStationName(String stationName);
    List<Reservation> findByVehicleId(Long vehicleId);
    List<Reservation> findByEndTimeBefore(LocalDateTime time);
    List<Reservation> findByVehicle_IdAndStatusIn(Long vehicleId, List<ReservationStatus> status);
    boolean existsByUser_EmailAndStatusNotIn(String email, List<ReservationStatus> statuses);
    List<Reservation> findByStatusInAndStartTimeBetweenAndExpiringNotifiedFalse(Collection<ReservationStatus> status, LocalDateTime endTime, LocalDateTime endTime2);
    List<Reservation> findByStatusInAndStartTimeBeforeAndOverdueNotifiedFalse(List<ReservationStatus> status, LocalDateTime time);
    List<Reservation> findByStatusInAndStartTimeBeforeAndCancelNotifiedFalse(Collection<ReservationStatus> status, LocalDateTime startTime);
    Page<Reservation> findByUser_EmailContainingIgnoreCaseAndStatusIn(String keyword, List<ReservationStatus> status, Pageable pageable);
    Page<Reservation> findByCodeContainingAndStatusIn(String keyword, List<ReservationStatus> status, Pageable pageable);
    Page<Reservation> findByUser_EmailContainingIgnoreCaseAndStatusInAndStation_Id(String keyword, List<ReservationStatus> status, Long stationId, Pageable pageable);
    Page<Reservation> findByCodeContainingAndStatusInAndStation_Id(String keyword, List<ReservationStatus> status, Long stationId, Pageable pageable);
    List<Reservation> findByStatusInAndCreatedAtBefore(List<ReservationStatus> failed, LocalDateTime limitTime);
    List<Reservation> findByVehicle_IdAndStatusInAndStartTimeAfter(Long id, List<ReservationStatus> overdue, LocalDateTime now);
}
