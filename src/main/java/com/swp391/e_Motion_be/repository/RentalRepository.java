package com.swp391.e_Motion_be.repository;

import com.swp391.e_Motion_be.entity.Rental;
import com.swp391.e_Motion_be.enums.RentalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface RentalRepository extends JpaRepository<Rental,Long> {
    List<Rental> findByStatusAndEndTimeBetween(RentalStatus status, LocalDateTime from, LocalDateTime to);
    List<Rental> findByVehicle_IdAndStatusNotIn(Long vehicleId, List<RentalStatus> status);
    List<Rental> findByStatus(RentalStatus status);
    List<Rental> findByStatusAndEndTimeBeforeAndOverdueNotifiedFalse(RentalStatus status, LocalDateTime time);
    boolean existsByUser_IdAndStatusNotIn(long userId, List<RentalStatus> status);
    boolean existsByUser_EmailAndStatusNotIn(String user_email, List<RentalStatus> status);
    @Query(value = """
        SELECT EXISTS (
            SELECT 1
            FROM rentals r
            WHERE r.vehicle_id = :vehicleId
              AND r.rental_status NOT IN (:excludedStatuses)
              AND :startTime < r.end_time
              AND :endTime > r.start_time
        )
    """, nativeQuery = true)
    int existsConflict(
            @Param("vehicleId") Long vehicleId,
            @Param("excludedStatuses") List<String> excludedStatuses,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
}
