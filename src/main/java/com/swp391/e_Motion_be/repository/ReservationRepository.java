package com.swp391.e_Motion_be.repository;

import com.swp391.e_Motion_be.entity.Reservation;
import com.swp391.e_Motion_be.enums.ReservationStatus;
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
    List<Reservation> findByCodeContains(String code);
    List<Reservation> findByStatusIn(List<ReservationStatus> status);
    List<Reservation> findByUserEmail(String email);
    List<Reservation> findByStationName(String stationName);
    List<Reservation> findByVehicleId(Long vehicleId);
    List<Reservation> findByEndTimeBefore(LocalDateTime time);
    List<Reservation> findByVehicle_IdAndStatusIn(Long vehicleId, List<ReservationStatus> status);
    boolean existsByUser_EmailAndStatusNotIn(String email, List<ReservationStatus> statuses);
    List<Reservation> findByStatusInAndStartTimeBetweenAndExpiringNotifiedFalse(Collection<ReservationStatus> status, LocalDateTime endTime, LocalDateTime endTime2);
    List<Reservation> findByStatusInAndStartTimeBeforeAndOverdueNotifiedFalse(List<ReservationStatus> status, LocalDateTime time);
    List<Reservation> findByStatusInAndStartTimeAfterAndOverdueNotifiedFalse(Collection<ReservationStatus> status, LocalDateTime startTime);
    List<Reservation> findByStatusInAndStartTimeBeforeAndCancelNotifiedFalse(Collection<ReservationStatus> status, LocalDateTime startTime);
    List<Reservation> findByUserEmailContains(String userEmail);
    List<Reservation> findByUserEmailContainsAndStatusIn(String keyword, List<ReservationStatus> status);
    List<Reservation> findByCodeContainsAndStatusIn(String keyword, List<ReservationStatus> status);
}
