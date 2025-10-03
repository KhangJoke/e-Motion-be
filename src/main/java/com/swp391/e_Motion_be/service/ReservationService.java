package com.swp391.e_Motion_be.service;

import com.swp391.e_Motion_be.dto.requests.deposit.DepositCreateRequest;
import com.swp391.e_Motion_be.dto.requests.payment.CreatePaymentUrlRequest;
import com.swp391.e_Motion_be.dto.requests.payment.RefundRequest;
import com.swp391.e_Motion_be.dto.requests.reservation.CreateReservationRequest;
import com.swp391.e_Motion_be.dto.requests.reservation.UpdateReservationStatusRequest;
import com.swp391.e_Motion_be.dto.responses.DepositResponse;
import com.swp391.e_Motion_be.dto.responses.PaymentResponse;
import com.swp391.e_Motion_be.dto.responses.ReservationResponse;
import com.swp391.e_Motion_be.entity.*;
import com.swp391.e_Motion_be.enums.DepositStatus;
import com.swp391.e_Motion_be.enums.ErrorCode;
import com.swp391.e_Motion_be.enums.ReservationStatus;
import com.swp391.e_Motion_be.enums.payment.PaymentType;
import com.swp391.e_Motion_be.exception.AppException;
import com.swp391.e_Motion_be.mapper.PaymentMapper;
import com.swp391.e_Motion_be.mapper.ReservationMapper;
import com.swp391.e_Motion_be.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationMapper reservationMapper;
    private final PaymentMapper paymentMapper;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final StationRepository stationRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;
    private final DepositRepository depositRepository;
    private final RentalRepository rentalRepository;
    private final DepositService depositService;

    @Transactional
    public Map<String, Object> createReservation(CreateReservationRequest request, HttpServletRequest httpReq) throws Exception {
        // Create Reservation
        int reservationConflict = reservationRepository.existsConflict(
                request.getVehicleId(), List.of("PENDING", "CONFIRM"), request.getStartTime(), request.getEndTime());
        if (reservationConflict == 1) {
            throw new AppException(ErrorCode.VEHICLE_NOT_AVAILABLE);
        }

        int rentalConflict = rentalRepository.existsConflict(
                request.getVehicleId(), List.of("COMPLETED", "CANCELLED"), request.getStartTime(), request.getEndTime());
        if (rentalConflict == 1) {
            throw new AppException(ErrorCode.VEHICLE_NOT_AVAILABLE);
        }

        User user = userRepository.findByEmail(request.getUserEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTS));
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_NOT_EXIST));
        Station station = stationRepository.findById(request.getStationId())
                .orElseThrow(() -> new AppException(ErrorCode.STATION_NOT_FOUND));

        if(!request.getEndTime().isAfter(LocalDateTime.now())) {
            throw new AppException(ErrorCode.RESERVATION_ENDTIME_INVALID);
        }
        Reservation reservation = reservationMapper.toReservationEntity(request);
        if(reservation.getStartTime().isBefore(LocalDateTime.now()) ||
                reservation.getEndTime().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.RESERVATION_TIME_INVALID);
        }
        reservation.setUser(user);
        reservation.setVehicle(vehicle);
        reservation.setStation(station);
        reservationRepository.save(reservation);
        // Create Reservation Deposit
        DepositCreateRequest depositCreateRequest = new DepositCreateRequest(DepositStatus.PENDING, 500000, reservation.getId(), null);
        DepositResponse depositResponse = depositService.createDeposit(depositCreateRequest);
        //Create Payment VnPay Url
        CreatePaymentUrlRequest paymentUrlRequest = new CreatePaymentUrlRequest(depositResponse.getAmount(), "Reservation Deposit", reservation.getUser().getEmail(), depositResponse.getId(), null);
        String url = paymentService.createPaymentUrl(paymentUrlRequest, httpReq.getRemoteAddr());
        //Create ApiResponse
        Map<String, Object> data = new HashMap<>();
        data.put("VnPayUrl", url);
        data.put("Reservation",reservationMapper.toReservationResponse(reservation));
        data.put("Deposit",depositResponse);
        return data;
    }

    public List<ReservationResponse> getAllReservations() {
        List<Reservation> reservations = reservationRepository.findAll();

        if (reservations.isEmpty()) {
            throw new AppException(ErrorCode.RESERVATION_NOT_FOUND);
        }

        return reservations.stream().map(reservationMapper::toReservationResponse).toList();
    }

    public ReservationResponse getReservationByCode(String code) {
        Reservation reservation = reservationRepository.findByCode(code)
                .orElseThrow(() -> new AppException(ErrorCode.RESERVATION_NOT_FOUND));

        return reservationMapper.toReservationResponse(reservation);
    }

    public ReservationResponse updateReservationStatus(UpdateReservationStatusRequest request) {
        Reservation reservation = reservationRepository.findByCode(request.getReservationCode())
                .orElseThrow(() -> new AppException(ErrorCode.RESERVATION_NOT_FOUND));

        reservation.setStatus(request.getNewStatus());

        Reservation savedReservation = reservationRepository.save(reservation);
        return reservationMapper.toReservationResponse(savedReservation);
    }

    public void deleteReservationByCode(String code) {
        Reservation reservation = reservationRepository.findByCode(code)
                .orElseThrow(() -> new AppException(ErrorCode.RESERVATION_NOT_FOUND));

        reservationRepository.delete(reservation);
    }

    @Transactional
    public boolean cancelReservation(String code, HttpServletRequest request) throws Exception {
        Reservation reservation = reservationRepository.findByCode(code)
                .orElseThrow(() -> new AppException(ErrorCode.RESERVATION_NOT_FOUND));

        //Nếu đã hết hạn thì không cho huỷ (Truoc 5 ngay)
        if(reservation.getStartTime().isBefore(LocalDateTime.now().plusDays(5))) {
            throw new AppException(ErrorCode.RESERVATION_TIME_INVALID_TO_CANCEL);
        }

        //Nếu đã huỷ rồi thì không cho huỷ
        if(reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new AppException(ErrorCode.RESERVATION_ALREADY_CANCELLED);
        }

        if(reservation.getDeposit()!=null) {
            reservation.setStatus(ReservationStatus.CANCELLED);
            reservationRepository.save(reservation);
            //Hoàn tiền deposit
            Deposit deposit = reservation.getDeposit();
            deposit.setStatus(DepositStatus.RELEASED);
            depositRepository.save(deposit);
            Payment depositPayment = paymentRepository.findByDepositIdAndType(deposit.getId(), PaymentType.RESERVATION)
                    .orElseThrow(() -> new AppException(ErrorCode.DEPOSIT_PAYMENT_NOT_FOUND));

            RefundRequest refundRequest = new RefundRequest();
            refundRequest.setIpAddr(request.getRemoteAddr());
            refundRequest.setTxnRef(depositPayment.getTxnRef());
            refundRequest.setFullRefund(true);

            PaymentResponse refundResponse = paymentService.refundPayment(refundRequest);
            return refundResponse != null;
        }
        return false;
    }

    public List<ReservationResponse> getReservationsByStatus(ReservationStatus status) {
        List<Reservation> reservations = reservationRepository.findByStatus(status);

        if (reservations == null || reservations.isEmpty()) {
            throw new AppException(ErrorCode.RESERVATION_NOT_FOUND);
        }

        return reservations.stream().map(reservationMapper::toReservationResponse).toList();
    }

    public List<ReservationResponse> getReservationsByUserEmail(String email) {
        List<Reservation> reservations = reservationRepository.findByUserEmail(email);

        if (reservations == null || reservations.isEmpty()) {
            throw new AppException(ErrorCode.RESERVATION_NOT_FOUND);
        }

        return reservations.stream().map(reservationMapper::toReservationResponse).toList();
    }

    public List<ReservationResponse> getReservationsByStationName(String name) {
        List<Reservation> reservations = reservationRepository.findByStationName(name);

        if (reservations == null || reservations.isEmpty()) {
            throw new AppException(ErrorCode.RESERVATION_NOT_FOUND);
        }

        return reservations.stream().map(reservationMapper::toReservationResponse).toList();
    }

    public List<ReservationResponse> getReservationsByVehicleId(Long vehicleId) {
        List<Reservation> reservations = reservationRepository.findByVehicleId(vehicleId);

        if (reservations == null || reservations.isEmpty()) {
            throw new AppException(ErrorCode.RESERVATION_NOT_FOUND);
        }

        return reservations.stream().map(reservationMapper::toReservationResponse).toList();
    }

    public List<ReservationResponse> getValidReservations(LocalDateTime time) {
        List<Reservation> reservations = reservationRepository.findByEndTimeBefore(time);

        if (reservations == null || reservations.isEmpty()) {
            throw new AppException(ErrorCode.RESERVATION_NOT_FOUND);
        }

        return reservations.stream().map(reservationMapper::toReservationResponse).toList();
    }
}
