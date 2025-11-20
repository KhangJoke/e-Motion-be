package com.swp391.e_Motion_be.service;

import com.swp391.e_Motion_be.dto.requests.deposit.DepositCreateRequest;
import com.swp391.e_Motion_be.dto.requests.deposit.DepositUpdateRequest;
import com.swp391.e_Motion_be.dto.responses.DepositResponse;
import com.swp391.e_Motion_be.entity.Deposit;
import com.swp391.e_Motion_be.entity.Rental;
import com.swp391.e_Motion_be.entity.Reservation;
import com.swp391.e_Motion_be.enums.ErrorCode;
import com.swp391.e_Motion_be.exception.AppException;
import com.swp391.e_Motion_be.mapper.DepositMapper;
import com.swp391.e_Motion_be.repository.DepositRepository;
import com.swp391.e_Motion_be.repository.RentalRepository;
import com.swp391.e_Motion_be.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepositService {

    private final DepositRepository depositRepository;
    private final DepositMapper depositMapper;
    private final ReservationRepository reservationRepository;
    private final RentalRepository rentalRepository;

    public List<DepositResponse> getAllDeposits() {
        return depositRepository.findAll().stream()
                .map(depositMapper:: toDepositResponse)
                .toList();
    }

    public DepositResponse createDeposit(DepositCreateRequest request) {
        Deposit deposit = depositMapper.toDepositEntity(request);
        if(request.getReservationId() != null){
            Reservation reservation = reservationRepository.findById(request.getReservationId())
                    .orElseThrow(() -> new AppException(ErrorCode.RESERVATION_NOT_FOUND));
            deposit.setReservation(reservation);
        }else{
            Rental rental = rentalRepository.findById(request.getRentalId())
                    .orElseThrow(() -> new AppException(ErrorCode.RENTAL_NOT_FOUND));
            deposit.setRental(rental);
        }
        return depositMapper.toDepositResponse(depositRepository.save(deposit));
    }

    public DepositResponse updateDepositStatus(DepositUpdateRequest request){
        Deposit deposit;
        if(request.getReservationId() != null){
            deposit = depositRepository.findById(request.getReservationId())
                    .orElseThrow(() -> new AppException(ErrorCode.DEPOSIT_NOT_FOUND));
        }else{
            deposit = depositRepository.findByRental_Id(request.getRentalId())
                    .orElseThrow(() -> new AppException(ErrorCode.DEPOSIT_NOT_FOUND));
        }
        if(deposit == null){
            throw new AppException(ErrorCode.DEPOSIT_NOT_FOUND);
        }
        deposit.setStatus(request.getStatus());
        return depositMapper.toDepositResponse((depositRepository.save(deposit)));
    }
}
