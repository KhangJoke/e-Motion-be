package com.swp391.e_Motion_be.repository;

import com.swp391.e_Motion_be.entity.Vehicle;
import com.swp391.e_Motion_be.enums.station.StationCity;
import com.swp391.e_Motion_be.enums.vehicle.VehicleBrand;
import com.swp391.e_Motion_be.enums.vehicle.VehicleCategory;
import com.swp391.e_Motion_be.enums.vehicle.VehicleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    Optional<Vehicle> findByPlateNumber(String plateNumber);
    List<Vehicle> findByStation_CityAndStatusIn(StationCity city, List<VehicleStatus> statuses);
    List<Vehicle> findByStatus(VehicleStatus status);
    List<Vehicle> findByBrandAndStatus(VehicleBrand brand,VehicleStatus status);
    long countByStation_Id(Long stationId);
    List<Vehicle> findByStation_Id(Long stationId);

    @Query(value = """
        SELECT EXISTS (
            -- Kiểm tra xung đột trong bảng reservations
            SELECT 1
            FROM reservations r
            WHERE r.vehicle_id = :vehicleId
              AND r.reservation_status IN (:reservationStatuses)
              -- Áp dụng logic khoảng đệm 3 giờ
              AND :startTime < DATE_ADD(r.reserved_end_time, INTERVAL 3 HOUR)
              AND :endTime > DATE_SUB(r.reserved_start_time, INTERVAL 3 HOUR)

            UNION

            -- Kiểm tra xung đột trong bảng rentals
            SELECT 1
            FROM rentals rent
            WHERE rent.vehicle_id = :vehicleId
              AND rent.rental_status NOT IN (:excludedRentalStatuses)
              -- Áp dụng logic khoảng đệm 3 giờ
              AND :startTime < DATE_ADD(rent.end_time, INTERVAL 3 HOUR)
              AND :endTime > DATE_SUB(rent.start_time, INTERVAL 3 HOUR)
        )
    """, nativeQuery = true)
    int doesConflictExistForVehicle(
            @Param("vehicleId") Long vehicleId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("reservationStatuses") List<String> reservationStatuses,
            @Param("excludedRentalStatuses") List<String> excludedRentalStatuses
    );

    Page<Vehicle> findByIdInAndBrandInAndCategoryAndNameContains(List<Long> ids, List<VehicleBrand> brandsList, List<VehicleCategory> categoryList, String search, Pageable pageable);
}
