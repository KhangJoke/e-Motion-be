package com.swp391.e_Motion_be.service;

import com.swp391.e_Motion_be.dto.requests.rental.RentalCreateFromReservationRequest;
import com.swp391.e_Motion_be.dto.requests.rental.RentalCreateRequest;
import com.swp391.e_Motion_be.dto.requests.rental.RentalUpdateRequest;
import com.swp391.e_Motion_be.dto.responses.RentalResponse;
import com.swp391.e_Motion_be.entity.*;
import com.swp391.e_Motion_be.enums.ErrorCode;
import com.swp391.e_Motion_be.enums.RentalStatus;
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
    private final DepositRepository depositRepository;

    private final RentalMapper rentalMapper;

    public List<RentalResponse> getAllRentals(){
       return rentalRepository.findAll().stream()
                .map(rentalMapper::toRentalResponse)
                .toList();
    }

    @Transactional
    public RentalResponse createRentalFromReservation(RentalCreateFromReservationRequest request){
        Reservation reservation = reservationRepository.findByCode(request.getReservationCode())
                .orElseThrow(() -> new AppException(ErrorCode.RESERVATION_NOT_FOUND));
        Staff staff = staffRepository.findById(request.getStaffId())
                .orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_FOUND));
        Rental rental = rentalMapper.fromReservationToRental(reservation);
        rental.setReservation(reservation);
        rental.setStaff(staff);
        rental.setRentFee(calculateFee(rental));
        rentalRepository.save(rental);
        // create Deposit
        Deposit deposit = Deposit.builder()
                .amount(rental.getRentFee())
                .rental(rental)
                .build();
        depositRepository.save(deposit);
        return rentalMapper.toRentalResponse(rental);
    }

    @Transactional
    public RentalResponse createRental(RentalCreateRequest request){
        boolean hasConflict = rentalRepository.findByVehicle_IdAndStatusNotIn(request.getVehicleId(), List.of(RentalStatus.COMPLETED, RentalStatus.CANCELLED))
                .stream().anyMatch(r -> r.getStartTime().minusHours(3).isBefore(request.getEndTime())
                && r.getEndTime().plusHours(3).isAfter(request.getStartTime()));
        if(hasConflict){
            throw new AppException(ErrorCode.RENTAL_HAS_CONFLICT);
        }
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_NOT_EXIST));
        Station station = stationRepository.findById(request.getStationId())
                .orElseThrow(() -> new AppException(ErrorCode.STATION_NOT_FOUND));
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(()->new AppException(ErrorCode.USER_NOT_EXISTS));
        Staff staff = staffRepository.findById(request.getStaffId())
                .orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_FOUND));
        Rental rental = rentalMapper.toRentalEntity(request, vehicle, station, user, staff);
        // save rental
        rental.setRentFee(calculateFee(rental));
        rentalRepository.save(rental);
        // create Deposit
        Deposit deposit = Deposit.builder()
                .amount(rental.getRentFee())
                .rental(rental)
                .build();
        depositRepository.save(deposit);
        return rentalMapper.toRentalResponse(rental);
    }

    public List<RentalResponse> getRentalsByStatus(String status){
        RentalStatus rentalStatus;
        try{
            rentalStatus = RentalStatus.valueOf(status.toUpperCase()); // parse string sang enum
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.INVALID_RENTAL_STATUS);
        }
        return rentalRepository.findByStatus(rentalStatus).stream()
                .map(rentalMapper::toRentalResponse)
                .toList();
    }

    public RentalResponse updateRentalStatus(RentalUpdateRequest request){
        RentalStatus rentalStatus;
        try{
            rentalStatus = RentalStatus.valueOf(request.getStatus().toUpperCase()); // parse string sang enum
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.INVALID_RENTAL_STATUS);
        }
        Rental rental = rentalRepository.findById(request.getId())
                .orElseThrow(() -> new AppException(ErrorCode.RENTAL_NOT_FOUND));
        rental.setStatus(rentalStatus);
        rentalRepository.save(rental);
        return  rentalMapper.toRentalResponse(rental);
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
