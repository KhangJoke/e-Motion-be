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
import com.swp391.e_Motion_be.mapper.ReservationMapper;
import com.swp391.e_Motion_be.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
        log.info("Creating reservation for user: {} and vehicle: {}", request.getUserEmail(), request.getVehicleId());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();
        boolean isUser = authentication.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER"));

        if (isUser && !currentUserEmail.equals(request.getUserEmail())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // Check vehicle availability - combine both checks for efficiency
        if (isVehicleUnavailable(request.getVehicleId(), request.getStartTime(), request.getEndTime())) {
            throw new AppException(ErrorCode.VEHICLE_NOT_AVAILABLE);
        }

        // Fetch entities
        User user = userRepository.findByEmail(request.getUserEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTS));
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_NOT_EXIST));
        Station station = stationRepository.findById(request.getStationId())
                .orElseThrow(() -> new AppException(ErrorCode.STATION_NOT_FOUND));

        if(!vehicle.getStation().getId().equals(station.getId())) {
            throw new AppException(ErrorCode.VEHICLE_STATION_MISMATCH);
        }

        // Create reservation
        Reservation reservation = reservationMapper.toReservationEntity(request);
        reservation.setUser(user);
        reservation.setVehicle(vehicle);
        reservation.setStation(station);
        reservation.setStatus(ReservationStatus.PENDING);
        reservationRepository.save(reservation);

        log.info("Reservation created with ID: {}", reservation.getId());

        // Create deposit
        DepositCreateRequest depositCreateRequest = new DepositCreateRequest(
                DepositStatus.PENDING,
                500000,
                reservation.getId(),
                null
        );
        DepositResponse depositResponse = depositService.createDeposit(depositCreateRequest);

        log.info("Deposit created with ID: {}", depositResponse.getId());

        // Create VNPay payment URL
        CreatePaymentUrlRequest paymentUrlRequest = new CreatePaymentUrlRequest(
                depositResponse.getAmount(),
                "Reservation Deposit - Vehicle: " + vehicle.getId(),
                user.getEmail(),
                depositResponse.getId(),
                null
        );
        String url = paymentService.createPaymentUrl(paymentUrlRequest, httpReq.getRemoteAddr());

        log.info("Payment URL created for reservation: {}", reservation.getId());

        // Prepare response
        Map<String, Object> data = new HashMap<>();
        data.put("vnpayUrl", url);
        data.put("reservation", reservationMapper.toReservationResponse(reservation));
        data.put("deposit", depositResponse);

        return data;
    }

    // Check if vehicle is unavailable due to existing reservations or rentals
    private boolean isVehicleUnavailable(Long vehicleId, LocalDateTime startTime, LocalDateTime endTime) {
        int reservationConflict = reservationRepository.existsConflict(
                vehicleId,
                List.of("PENDING", "CONFIRM"),
                startTime,
                endTime
        );

        int rentalConflict = rentalRepository.existsConflict(
                vehicleId,
                List.of("COMPLETED", "CANCELLED"),
                startTime,
                endTime
        );

        return reservationConflict == 1 || rentalConflict == 1;
    }

    @Transactional
    public boolean cancelReservation(String code, HttpServletRequest request) {
        log.info("Processing cancellation for reservation: {}", code);

        Reservation reservation = reservationRepository.findByCode(code)
                .orElseThrow(() -> new AppException(ErrorCode.RESERVATION_NOT_FOUND));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();
        boolean isUser = authentication.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER"));

        if (isUser && !currentUserEmail.equals(reservation.getUser().getEmail())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // Validate cancellation is allowed
        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new AppException(ErrorCode.RESERVATION_ALREADY_CANCELLED);
        }

        // Check if cancellation is within allowed timeframe (5 days before start)
        if (reservation.getStartTime().isBefore(LocalDateTime.now().plusDays(5))) {
            throw new AppException(ErrorCode.RESERVATION_TIME_INVALID_TO_CANCEL);
        }

        // Process refund if deposit exists
        if (reservation.getDeposit() == null) {
            log.warn("No deposit found for reservation: {}", code);
            return false;
        }

        Deposit deposit = reservation.getDeposit();

        // Only refund if deposit is HOLD (payment was successful)
        if (deposit.getStatus() != DepositStatus.HOLD) {
            log.warn("Deposit is not in HOLD status. Current status: {}", deposit.getStatus());
            reservation.setStatus(ReservationStatus.CANCELLED);
            reservationRepository.save(reservation);
            return false;
        }

        // Find the successful payment for this deposit
        Payment depositPayment = paymentRepository.findByDepositIdAndType(
                deposit.getId(),
                PaymentType.RESERVATION
        ).orElseThrow(() -> new AppException(ErrorCode.DEPOSIT_PAYMENT_NOT_FOUND));

        // Process refund
        RefundRequest refundRequest = new RefundRequest();
        refundRequest.setIpAddr(request.getRemoteAddr());
        refundRequest.setTxnRef(depositPayment.getTxnRef());
        refundRequest.setFullRefund(true);

        PaymentResponse refundResponse = paymentService.refundPayment(refundRequest);

        if (refundResponse != null && "00".equals(refundResponse.getResponseCode())) {
            // Update deposit status only if refund was successful
            deposit.setStatus(DepositStatus.RELEASED);
            depositRepository.save(deposit);
            log.info("Refund processed successfully for reservation: {}", code);

            // Update reservation status
            reservation.setStatus(ReservationStatus.CANCELLED);
            reservationRepository.save(reservation);

            log.info("Reservation cancelled: {}", code);
            return true;
        }

        log.error("Refund failed for reservation: {}", code);
        return false;
    }

    public List<ReservationResponse> getAllReservations() {
        List<Reservation> reservations = reservationRepository.findAll();
        if (reservations.isEmpty()) {
            throw new AppException(ErrorCode.RESERVATION_NOT_FOUND);
        }
        return reservations.stream()
                .map(reservationMapper::toReservationResponse)
                .toList();
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

    public List<ReservationResponse> getReservationsByStatus(ReservationStatus status) {
        List<Reservation> reservations = reservationRepository.findByStatus(status);
        if (reservations == null || reservations.isEmpty()) {
            throw new AppException(ErrorCode.RESERVATION_NOT_FOUND);
        }
        return reservations.stream()
                .map(reservationMapper::toReservationResponse)
                .toList();
    }

    public List<ReservationResponse> getReservationsByUserEmail(String email) {
        List<Reservation> reservations = reservationRepository.findByUserEmail(email);
        if (reservations == null || reservations.isEmpty()) {
            throw new AppException(ErrorCode.RESERVATION_NOT_FOUND);
        }
        return reservations.stream()
                .map(reservationMapper::toReservationResponse)
                .toList();
    }

    public List<ReservationResponse> getReservationsByStationName(String name) {
        List<Reservation> reservations = reservationRepository.findByStationName(name);
        if (reservations == null || reservations.isEmpty()) {
            throw new AppException(ErrorCode.RESERVATION_NOT_FOUND);
        }
        return reservations.stream()
                .map(reservationMapper::toReservationResponse)
                .toList();
    }

    public List<ReservationResponse> getReservationsByVehicleId(Long vehicleId) {
        List<Reservation> reservations = reservationRepository.findByVehicleId(vehicleId);
        if (reservations == null || reservations.isEmpty()) {
            throw new AppException(ErrorCode.RESERVATION_NOT_FOUND);
        }
        return reservations.stream()
                .map(reservationMapper::toReservationResponse)
                .toList();
    }

    public List<ReservationResponse> getValidReservations(LocalDateTime time) {
        List<Reservation> reservations = reservationRepository.findByEndTimeBefore(time);
        if (reservations == null || reservations.isEmpty()) {
            throw new AppException(ErrorCode.RESERVATION_NOT_FOUND);
        }
        return reservations.stream()
                .map(reservationMapper::toReservationResponse)
                .toList();
    }
}