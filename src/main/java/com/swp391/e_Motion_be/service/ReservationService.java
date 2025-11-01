package com.swp391.e_Motion_be.service;

import com.swp391.e_Motion_be.dto.requests.deposit.DepositCreateRequest;
import com.swp391.e_Motion_be.dto.requests.payment.CreatePaymentUrlRequest;
import com.swp391.e_Motion_be.dto.requests.payment.RefundRequest;
import com.swp391.e_Motion_be.dto.requests.reservation.CreateReservationRequest;
import com.swp391.e_Motion_be.dto.requests.reservation.PageAndFilterReservationRequest;
import com.swp391.e_Motion_be.dto.requests.reservation.UpdateReservationStatusRequest;
import com.swp391.e_Motion_be.dto.responses.DepositResponse;
import com.swp391.e_Motion_be.dto.responses.PaymentResponse;
import com.swp391.e_Motion_be.dto.responses.reservation.PageAndFilterReservationResponse;
import com.swp391.e_Motion_be.dto.responses.reservation.ReservationListResponse;
import com.swp391.e_Motion_be.dto.responses.reservation.ReservationResponse;
import com.swp391.e_Motion_be.entity.*;
import com.swp391.e_Motion_be.enums.*;
import com.swp391.e_Motion_be.enums.payment.PaymentType;
import com.swp391.e_Motion_be.enums.vehicle.VehicleStatus;
import com.swp391.e_Motion_be.exception.AppException;
import com.swp391.e_Motion_be.mapper.ReservationMapper;
import com.swp391.e_Motion_be.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
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
    private final EmailService emailService;
    private final DocumentRepository documentRepository;

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

        // Check có đang thuê hoặc đặt trước xe khác không
        boolean hasOngoingRental = rentalRepository.existsByUser_EmailAndStatusNotIn(request.getUserEmail(), List.of(RentalStatus.COMPLETED, RentalStatus.CANCELLED));
        boolean hasOngoingReservation = reservationRepository.existsByUser_EmailAndStatusNotIn(request.getUserEmail(), List.of(ReservationStatus.COMPLETED, ReservationStatus.FAILED));
        if(hasOngoingRental || hasOngoingReservation) {
            throw new AppException(ErrorCode.USER_HAS_ONGOING_RENTAL);
        }

        // Kiểm tra CCCD và GPLX của renter
        if(!documentRepository.existsByUser_EmailAndType(request.getUserEmail(), DocumentType.CCCD)){
            throw new AppException(ErrorCode.USER_NEED_HAS_CCCD);
        }
        if(!documentRepository.existsByUser_EmailAndType(request.getUserEmail(), DocumentType.LICENSE)){
            throw new AppException(ErrorCode.USER_NEED_HAS_LICENSE);
        }

        // Check if start and end times are exact hours
        if (!isExactHour(request.getStartTime()) || !isExactHour(request.getEndTime())) {
            throw new AppException(ErrorCode.TIME_MUST_BE_EXACT_HOUR);
        }

        // Check reservation time validity
        if (request.getStartTime().isBefore(LocalDateTime.now().plusHours(3)) ||
        request.getStartTime().isAfter(LocalDateTime.now().plusYears(1))) {
            throw new AppException(ErrorCode.RESERVATION_TIME_MUST_AFTER_NOW_3HOURS);
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
        if(vehicle.getStatus().equals(VehicleStatus.CHECKING) || vehicle.getStatus().equals(VehicleStatus.MAINTAINED)) {
            throw new AppException(ErrorCode.VEHICLE_NOT_READY);
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
                "Reservation Deposit",
                user.getEmail(),
                PaymentType.RESERVATION,
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
        // Time minimum 4hours validation
        long hour = Duration.between(startTime,endTime).toHours();
        if(hour < 4){
           throw new AppException(ErrorCode.RENT_TIME_MUST_MINIMUM_4_HOURS);
        }

        int conflictCount = vehicleRepository.doesConflictExistForVehicle(
                vehicleId,
                startTime,
                endTime,
                List.of("PENDING", "CONFIRM"), // Trạng thái cần kiểm tra của Reservation
                List.of("COMPLETED", "CANCELLED", "OVERDUE")   // Trạng thái cần loại trừ của Rental
        );

        return conflictCount > 0;
    }

    // Check if the time is on the exact hour (e.g., 1:00, 2:00)
    private boolean isExactHour(LocalDateTime dateTime) {
        return dateTime.getMinute() == 0 && dateTime.getSecond() == 0;
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
        refundRequest.setAmount(depositPayment.getAmount());
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
            emailService.sendPaymentStatusToEmail(paymentRepository.findByTxnRef(refundResponse.getTxnRef()).orElse(null),null);

            log.info("Reservation cancelled: {}", code);
            return true;
        }

        log.error("Refund failed for reservation: {}", code);
        return false;
    }

    public List<ReservationListResponse> getAllReservations() {
        List<Reservation> reservations = reservationRepository.findAll();
        if (reservations.isEmpty()) {
            throw new AppException(ErrorCode.RESERVATION_NOT_FOUND);
        }
        return reservations.stream()
                .map(reservationMapper::toReservationListResponse)
                .toList();
    }

    public ReservationResponse getReservationByCode(String code) {
        Reservation reservation = reservationRepository.findByCode(code)
                .orElseThrow(() -> new AppException(ErrorCode.RESERVATION_NOT_FOUND));
        return reservationMapper.toReservationResponse(reservation);
    }

    public List<ReservationResponse> getReservationByCodeContain(String code) {
        List<Reservation> reservation = reservationRepository.findByCodeContains(code);
        if (reservation == null || reservation.isEmpty()) {
            throw new AppException(ErrorCode.RESERVATION_NOT_FOUND);
        }
        return reservation.stream()
                .map(reservationMapper::toReservationResponse)
                .toList();
    }

    public List<ReservationResponse> getReservationByUserEmailContain(String userEmail) {
        List<Reservation> reservation = reservationRepository.findByUserEmailContains(userEmail);
        if (reservation == null || reservation.isEmpty()) {
            throw new AppException(ErrorCode.RESERVATION_NOT_FOUND);
        }
        return reservation.stream()
                .map(reservationMapper::toReservationResponse)
                .toList();
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

    public List<ReservationResponse> getReservationsByStatus(List<ReservationStatus> status) {
        List<Reservation> reservations = reservationRepository.findByStatusIn(status);
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

    @Transactional
    public void notifyExpiringReservations() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.plusHours(1); // trong vòng 1h tới
        List<Reservation> expiringReservation = reservationRepository.findByStatusInAndStartTimeBetweenAndExpiringNotifiedFalse(
                List.of(ReservationStatus.CONFIRM),
                now,
                threshold
        );
        expiringReservation.forEach(reservation -> {
            emailService.sendReservationExpiringEmail(reservation);
            reservation.setExpiringNotified(true);
            reservationRepository.save(reservation);
        });
    }

    @Transactional
    public void notifyOverdueReservations() {
        LocalDateTime now = LocalDateTime.now();
        List<Reservation> overdueReservations = reservationRepository.findByStatusInAndStartTimeBeforeAndOverdueNotifiedFalse(
                List.of(ReservationStatus.CONFIRM),
                now
        );
        overdueReservations.forEach(reservation -> {
            emailService.sendReservationOverdueEmail(reservation);
            reservation.setStatus(ReservationStatus.OVERDUE);
            reservation.setOverdueNotified(true);
            reservationRepository.save(reservation);
        });
    }

    @Transactional
    public void notifyCancelReservations() {
        LocalDateTime limitTime = LocalDateTime.now().minusHours(1);
        List<Reservation> cancelReservations = reservationRepository.findByStatusInAndStartTimeBeforeAndCancelNotifiedFalse(
                List.of(ReservationStatus.CONFIRM,
                        ReservationStatus.PENDING,
                        ReservationStatus.OVERDUE),
                limitTime
        );
        cancelReservations.forEach(reservation -> {
            emailService.sendReservationCancelEmail(reservation);
            reservation.setStatus(ReservationStatus.CANCELLED);
            reservation.setCancelNotified(true);
            reservationRepository.save(reservation);
        });
    }

    public ReservationResponse extendReservationReturnTime(String code, LocalDateTime newReturnTime) {
        Reservation reservation = reservationRepository.findByCode(code)
                .orElseThrow(() -> new AppException(ErrorCode.RESERVATION_NOT_FOUND));

        // Validate new return time
        if (!isExactHour(newReturnTime) || !isExactHour(newReturnTime)) {
            throw new AppException(ErrorCode.TIME_MUST_BE_EXACT_HOUR);
        }
        // New return time must be at least 1 hour after current end time
        if (newReturnTime.isBefore(reservation.getEndTime().plusHours(1))) {
            throw new AppException(ErrorCode.RESERVATION_EXTEND_TIME_INVALID);
        }
        // Extension requests must be made at least 2 hours before current start time
        if(reservation.getStartTime().isAfter(LocalDateTime.now().plusHours(2))) {
            throw new AppException(ErrorCode.RESERVATION_EXTEND_TIME_INVALID);
        }
        // Only CONFIRM reservations can be extended
        if(reservation.getStatus() != ReservationStatus.CONFIRM) {
            throw new AppException(ErrorCode.RESERVATION_EXTEND_TIME_INVALID);
        }
        // Check if vehicle is available for the extended period
        if (isVehicleUnavailable(reservation.getVehicle().getId(), reservation.getEndTime(), newReturnTime)) {
            throw new AppException(ErrorCode.VEHICLE_NOT_AVAILABLE);
        }

        reservation.setEndTime(newReturnTime);
        Reservation updatedReservation = reservationRepository.save(reservation);

        return reservationMapper.toReservationResponse(updatedReservation);
    }

    public List<ReservationResponse> getReservationByUserEmailContainAndStatus(String keyword, List<ReservationStatus> status) {
        List<Reservation> reservation = reservationRepository.findByUserEmailContainsAndStatusIn(keyword, status);
        if (reservation == null || reservation.isEmpty()) {
            throw new AppException(ErrorCode.RESERVATION_NOT_FOUND);
        }
        return reservation.stream()
                .map(reservationMapper::toReservationResponse)
                .toList();
    }

    public List<ReservationResponse> getReservationByCodeContainAndStatus(String keyword, List<ReservationStatus> status) {
        List<Reservation> reservation = reservationRepository.findByCodeContainsAndStatusIn(keyword, status);
        if (reservation == null || reservation.isEmpty()) {
            throw new AppException(ErrorCode.RESERVATION_NOT_FOUND);
        }
        return reservation.stream()
                .map(reservationMapper::toReservationResponse)
                .toList();
    }

    public PageAndFilterReservationResponse findByPageAndFilterAndSearch(PageAndFilterReservationRequest request) {
        List<ReservationStatus> statusList = (request.getStatus() == null || request.getStatus().isEmpty())
                ? Arrays.asList(ReservationStatus.values())
                : request.getStatus();

        Pageable pageable = PageRequest.of(request.getPage() - 1, request.getLimit(), Sort.by("id").ascending());
        Page<Reservation> reservationPage = reservationRepository.searchByStatusAndCode(statusList, request.getSearch(), pageable);
        List<ReservationListResponse> reservations = reservationPage.getContent().stream()
                .map(reservationMapper::toReservationListResponse)
                .toList();

        return new PageAndFilterReservationResponse(reservations, reservationPage.getTotalPages());
    }
}