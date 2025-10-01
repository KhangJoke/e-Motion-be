package com.swp391.e_Motion_be.service;

import com.swp391.e_Motion_be.dto.requests.rental.RentalCreateFromReservationRequest;
import com.swp391.e_Motion_be.dto.requests.rental.RentalCreateRequest;
import com.swp391.e_Motion_be.dto.responses.RentalResponse;
import com.swp391.e_Motion_be.entity.*;
import com.swp391.e_Motion_be.enums.ErrorCode;
import com.swp391.e_Motion_be.exception.AppException;
import com.swp391.e_Motion_be.mapper.RentalMapper;
import com.swp391.e_Motion_be.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RentalService {

    private final RentalRepository rentalRepository;
    private final ReservationRepository reservationRepository;
    private final StaffRepository staffRepository;
    private final VehicleRepository vehicleRepository;
    private final StationRepository stationRepository;
    private final UserRepository userRepository;

    private final RentalMapper rentalMapper;

    public List<RentalResponse> getAllRentals(){
       return rentalRepository.findAll().stream()
                .map(rentalMapper::toRentalResponse)
                .toList();
    }

    public RentalResponse createRentalFromReservation(RentalCreateFromReservationRequest request){
        Reservation reservation = reservationRepository.findByCode(request.getReservationCode())
                .orElseThrow(() -> new AppException(ErrorCode.RESERVATION_NOT_FOUND));
        Staff staff = staffRepository.findById(request.getStaffId())
                .orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_FOUND));
        Rental rental = rentalMapper.fromReservationToRental(reservation);
        rental.setReservation(reservation);
        rental.setStaff(staff);
        rental.setRentFee(calculateFee(rental));
        return rentalMapper.toRentalResponse(rentalRepository.save(rental));
    }

    @Transactional
    public RentalResponse createRental(RentalCreateRequest request){
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_NOT_EXIST));
        Station station = stationRepository.findById(request.getStationId())
                .orElseThrow(() -> new AppException(ErrorCode.STATION_NOT_FOUND));
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(()->new AppException(ErrorCode.USER_NOT_EXISTS));
        Staff staff = staffRepository.findById(request.getStaffId())
                .orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_FOUND));
        Rental rental = rentalMapper.toRentalEntity(request);
        rental.setVehicle(vehicle);
        rental.setStation(station);
        rental.setUser(user);
        rental.setStaff(staff);
        rental.setRentFee(calculateFee(rental));
        rentalRepository.save(rental);
        return rentalMapper.toRentalResponse(rental);
    }

    private double calculateFee(Rental rental) {
        LocalDateTime start = rental.getStartTime();
        LocalDateTime end = rental.getEndTime();

        long hours = Duration.between(start, end).toHours();
        long days = hours / 24;
        long remainHours = hours % 24;

        double fee = 0;

        // Tính theo ngày
        if (days > 0) {
            fee += days * rental.getVehicle().getPricePerDay();
        }
        // Tính theo giờ (phần dư)
        if (remainHours > 0) {
            fee += remainHours * rental.getVehicle().getPricePerHour();
        }
        return fee;
    }
}
