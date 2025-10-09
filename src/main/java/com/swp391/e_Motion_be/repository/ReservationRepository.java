package com.swp391.e_Motion_be.repository;

import com.swp391.e_Motion_be.entity.Reservation;
import com.swp391.e_Motion_be.enums.RentalStatus;
import com.swp391.e_Motion_be.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation,Long> {
    Optional<Reservation> findByCode(String code);
    List<Reservation> findByStatus(ReservationStatus status);
    List<Reservation> findByUserEmail(String email);
    List<Reservation> findByStationName(String stationName);
    List<Reservation> findByVehicleId(Long vehicleId);
    List<Reservation> findByEndTimeBefore(LocalDateTime time);
    List<Reservation> findByVehicle_IdAndStatusIn(Long vehicleId, List<ReservationStatus> status);
    boolean existsByUser_EmailAndStatusNotIn(String email, List<ReservationStatus> statuses);
    @Query("SELECT r FROM Reservation r JOIN FETCH r.user WHERE r.status IN (:statuses)")
    List<Reservation> findByStatusWithUser(@Param("statuses") List<ReservationStatus> statuses);

}
