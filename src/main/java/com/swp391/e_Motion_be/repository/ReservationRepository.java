package com.swp391.e_Motion_be.repository;

import com.swp391.e_Motion_be.entity.Reservation;
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
    boolean existsByUser_EmailAndStatusNotIn(String email, List<ReservationStatus> statuses);
    @Query(value = """
        SELECT EXISTS (
            SELECT 1
            FROM reservations r
            WHERE r.vehicle_id = :vehicleId
              AND r.reservation_status IN (:statuses)
              AND :startTime < DATE_ADD(r.reserved_start_time, INTERVAL 3 HOUR)
              AND :endTime > DATE_SUB(r.reserved_end_time, INTERVAL 3 HOUR)
        )
    """, nativeQuery = true)
    int existsConflict(
            @Param("vehicleId") Long vehicleId,
            @Param("statuses") List<String> statuses,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    @Query("SELECT r FROM Reservation r JOIN FETCH r.user WHERE r.status IN (:statuses)")
    List<Reservation> findByStatusWithUser(@Param("statuses") List<ReservationStatus> statuses);

}
