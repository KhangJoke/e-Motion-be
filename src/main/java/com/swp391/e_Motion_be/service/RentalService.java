package com.swp391.e_Motion_be.service;

import com.swp391.e_Motion_be.dto.requests.deposit.DepositCreateRequest;
import com.swp391.e_Motion_be.dto.requests.payment.CreatePaymentUrlRequest;
import com.swp391.e_Motion_be.dto.requests.payment.RefundRequest;
import com.swp391.e_Motion_be.dto.requests.rental.*;
import com.swp391.e_Motion_be.dto.responses.VnpayResponse;
import com.swp391.e_Motion_be.dto.responses.rental.*;
import com.swp391.e_Motion_be.dto.vehicleLog.VehicleLogItem;
import com.swp391.e_Motion_be.entity.*;
import com.swp391.e_Motion_be.enums.*;
import com.swp391.e_Motion_be.enums.payment.PaymentStatus;
import com.swp391.e_Motion_be.enums.payment.PaymentType;
import com.swp391.e_Motion_be.enums.vehicle.VehicleStatus;
import com.swp391.e_Motion_be.exception.AppException;
import com.swp391.e_Motion_be.mapper.RentalMapper;
import com.swp391.e_Motion_be.repository.*;
import com.swp391.e_Motion_be.service.document.DocumentService;
import com.swp391.e_Motion_be.service.user.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RentalService {

    private final RentalRepository rentalRepository;
    private final ReservationRepository reservationRepository;
    private final DocumentRepository documentRepository;
    private final StaffRepository staffRepository;
    private final VehicleRepository vehicleRepository;
    private final StationRepository stationRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    private final DocumentService documentService;
    private final EmailService emailService;
    private final RentalMapper rentalMapper;
    private final DepositService depositService;
    private final PaymentService paymentService;
    private final UserService userService;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${price.8h.rate}")
    private double price8hRate;
    @Value("${price.12h.rate}")
    private double price12hRate;
    @Value("${price.day.rate}")
    private double priceDayRate;


    public List<RentalListResponse> getAllRentals(){
       return rentalRepository.findAll().stream()
                .map(rentalMapper::toRentalListResponse)
                .toList();
    }

    public RentalResponse getRentalById(Long id){
        Rental rental = rentalRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RENTAL_NOT_FOUND));
        return rentalMapper.toRentalResponse(rental);
    }

    // Hàm tạo rental khi renter có thuê trước
    @Transactional
    public RentalResponse createRentalFromReservation(RentalCreateFromReservationRequest request){
        Reservation reservation = reservationRepository.findByCode(request.getReservationCode())
                .orElseThrow(() -> new AppException(ErrorCode.RESERVATION_NOT_FOUND));
        if(!reservation.getStatus().equals(ReservationStatus.CONFIRM) && !reservation.getStatus().equals(ReservationStatus.OVERDUE)){
            throw new AppException(ErrorCode.RESERVATION_STATUS_INVALID);
        }
        Staff staff = staffRepository.findById(request.getStaffId())
                .orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_FOUND));
        Rental rental = rentalMapper.fromReservationToRental(reservation);
        rental.setReservation(reservation);
        rental.setStaff(staff);
        reservation.setStatus(ReservationStatus.COMPLETED);
        reservationRepository.save(reservation);
        return createRentalCommon(rental, reservation.getUser(),reservation.getVehicle(), reservation.getDeposit().getAmount());
    }

    // Hàm tạo rental khi renter thuê trực tiếp tại trạm
    @Transactional
    public RentalResponse createRental(RentalCreateRequest request){
        // Time minimum 4hours validation
        long hour = Duration.between(request.getStartTime(), request.getEndTime()).toHours();
        if(hour < 4){
            throw new AppException(ErrorCode.DURATION_MINIUM);
        }
        if(request.getEndTime().isAfter(request.getStartTime().plusMonths(1))) {
            throw new AppException(ErrorCode.RESERVATION_END_TIME_INVALID);
        }
        // kiểm tra có tồn tại object ko
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_NOT_EXIST));
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(()->new AppException(ErrorCode.USER_NOT_EXISTS));
        Staff staff = staffRepository.findById(request.getStaffId())
                .orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_FOUND));

        Rental rental = rentalMapper.toRentalEntity(request, vehicle, vehicle.getStation(), user, staff);
        return createRentalCommon(rental, user, vehicle,0);
    }

    // Hàm này chứa các action chung của 2 hàm cách tạo rental
    private RentalResponse createRentalCommon(Rental rental, User user, Vehicle vehicle, double reservationDepositAmount){
        // Kiểm tra CCCD và GPLX của renter
        if(!documentRepository.existsByUser_IdAndType(user.getId(), DocumentType.CCCD)){
            throw new AppException(ErrorCode.USER_NEED_HAS_CCCD);
        }
        if(!documentRepository.existsByUser_IdAndType(user.getId(), DocumentType.LICENSE)){
            throw new AppException(ErrorCode.USER_NEED_HAS_LICENSE);
        }
        // Check expired document
        if(documentService.checkExpiredDocumentByUserEmail(user.getEmail())){
            throw new AppException(ErrorCode.DOCUMENT_EXPIRED);
        }
        // Kiểm tra user có đơn thuê nào chưa trả ko
        boolean hasOngoingRental = rentalRepository.existsByUser_IdAndStatusNotIn(user.getId(), List.of(RentalStatus.COMPLETED, RentalStatus.CANCELLED));
        if(hasOngoingRental){
            throw new AppException(ErrorCode.USER_HAS_ONGOING_RENTAL);
        }
        // Kiểm tra xe cho thuê có đang available ko
        if(!vehicle.getStatus().equals(VehicleStatus.AVAILABLE)){
            throw new AppException(ErrorCode.VEHICLE_NOT_READY);
        }

        // save rental
        // set status của xe sang đang thuê
        vehicle.setStatus(VehicleStatus.UNAVAILABLE);//hold vehicle for rental
        rental.setRentFee(calculateRentalFee(rental.getVehicle(), rental.getStartTime(), rental.getEndTime())); // Tiền thuê
        rental.setDiscountPoint(0); // Mặc định chưa dùng điểm
        rentalRepository.save(rental);
        // Create deposit
        DepositCreateRequest depositCreateRequest = new DepositCreateRequest(
                DepositStatus.PENDING,
                vehicle.getDepositFee() - reservationDepositAmount, // Cọc xe
                null,
                rental.getId()
        );
        depositService.createDeposit(depositCreateRequest);
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

    // hàm thay đổi status của rental
    public RentalResponse updateRentalStatus(RentalUpdateStatusRequest request){
        Rental rental = rentalRepository.findById(request.getId())
                .orElseThrow(() -> new AppException(ErrorCode.RENTAL_NOT_FOUND));
        rental.setStatus(request.getStatus());
        rentalRepository.save(rental);
        return  rentalMapper.toRentalResponse(rental);
    }

    public double calculateRentalFee(Vehicle vehicle, LocalDateTime startTime, LocalDateTime endTime) {
        long hours = Duration.between(startTime, endTime).toHours();
        double fee = 0;
        double pricePer4Hours = vehicle.getPricePer4Hours();

        if(hours < 4){
            throw new AppException(ErrorCode.INVALID_TIME_RANGE);
        }
        else if (hours < 8) {
            fee += pricePer4Hours/4 * hours;
        } else if (hours < 12) {
            fee += (pricePer4Hours*price8hRate/8 )* hours;
        } else if (hours < 24) {
            fee += (pricePer4Hours*price12hRate/12) * hours;
        } else {
            fee += (pricePer4Hours*priceDayRate/24) * hours;
        }
        fee = BigDecimal.valueOf(fee)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
        return fee;
    }

    public RentalOverviewResponse getRentalOverviewById(Long id) {
        Rental rental = rentalRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RENTAL_NOT_FOUND));

        RentalCheckList rentalCheckList = rental.getRentalCheckLists().stream()
                .filter(r -> "CHECK_OUT".equalsIgnoreCase(r.getType().toString()))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.RENTAL_CHECKLIST_NOT_FOUND));

        double checkListFee = rentalCheckList.getFee();
        double reservationDepositAmount = rental.getReservation()!=null ? rental.getReservation().getDeposit().getAmount() : 0;
        double rentalDepositAmount = rental.getDeposit().getAmount();

        VehicleLog vehicleLog = rental.getVehicleLog();
       List<VehicleLogItem> vehicleDamages = vehicleLog != null ? vehicleLog.getRepairItems() : null;
        double vehicleDamageFee = vehicleLog != null ? vehicleLog.getCost() : 0;

        double totalCharges = vehicleDamageFee + checkListFee;
        double totalDeposits = reservationDepositAmount + rentalDepositAmount;

        return RentalOverviewResponse.builder()
                .rentalResponse(rentalMapper.toRentalResponse(rental))
                .checkListFee(checkListFee)
                .reservationDeposit(reservationDepositAmount)
                .rentalDeposit(rentalDepositAmount)
                .vehicleDamages(vehicleDamages)
                .vehicleDamageFee(vehicleDamageFee)
                .refundEligible(totalCharges <= totalDeposits)
                .build();
    }


    @Transactional
    public void notifyExpiringRentals() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.plusHours(1); // trong vòng 1h tới
        List<Rental> expiringRentals = rentalRepository.findByStatusAndEndTimeBetweenAndExpiringNotifiedFalse(
                RentalStatus.ONGOING,
                now,
                threshold
        );
        expiringRentals.forEach(rental -> {
            emailService.sendRentalExpiringEmail(rental);
            rental.setExpiringNotified(true);
            rentalRepository.save(rental);
        });
    }

    @Transactional
    public void notifyOverdueRentals() {
        LocalDateTime now = LocalDateTime.now();
        List<Rental> overdueRentals = rentalRepository.findByStatusAndEndTimeBeforeAndOverdueNotifiedFalse(
                RentalStatus.ONGOING,
                now
        );
        overdueRentals.forEach(rental -> {
            emailService.sendRentalOverdueEmail(rental);
            rental.setStatus(RentalStatus.OVERDUE);
            rental.setOverdueNotified(true);
            rentalRepository.save(rental);
        });
    }

    @Transactional
    public void notifyCancelRentals() {
        LocalDateTime limitTime = LocalDateTime.now().minusHours(1);
        List<Rental> cancelRentals = rentalRepository.findByStatusInAndStartTimeBeforeAndCancelNotifiedFalse(
                List.of(RentalStatus.PENDING, RentalStatus.CONFIRM, RentalStatus.CONTRACTING),
                limitTime
        );
        cancelRentals.forEach(rental -> {
            emailService.sendRentalCancelEmail(rental);
            rental.getVehicle().setStatus(VehicleStatus.AVAILABLE);
            rental.setStatus(RentalStatus.CANCELLED);
            rental.setCancelNotified(true);
            rentalRepository.save(rental);
        });
    }

    @Transactional
    public VnpayResponse processCheckInPayment(Long id, int point, String ipAddr) throws Exception {
        Rental rental = rentalRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RENTAL_NOT_FOUND));

        if(!rental.getStatus().equals(RentalStatus.CONTRACTING) || !rental.getContractStatus().equals(ContractStatus.SIGNED)){
            throw new AppException(ErrorCode.INVALID_RENTAL_STATUS);
        }
        int discountFee = point * 1000;
        double rentalDepositAmount = rental.getDeposit().getAmount();
        CreatePaymentUrlRequest request = new CreatePaymentUrlRequest();
        request.setRentalId(rental.getId());
        request.setDepositId(rental.getDeposit().getId());
        request.setType(PaymentType.RENTAL);
        request.setAmount(rental.getRentFee()+rentalDepositAmount - discountFee);
        request.setDescription("Check-in Payment for Rental ID: " + rental.getId());
        request.setUserEmail(rental.getUser().getEmail());
        VnpayResponse response = paymentService.createPaymentUrl(request, ipAddr);
        rental.setDiscountPoint(point);
        User user = rental.getUser();
        if(user.getPoint() < point){
            throw new AppException(ErrorCode.USER_POINT_NOT_ENOUGH);
        }
        user.setPoint(user.getPoint() - point);
        rentalRepository.save(rental);
        return response;
    }

    @Transactional
    public CheckOutProcessResponse processCheckOutPayment(Long id, String remoteAddr) {
        Rental rental = rentalRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RENTAL_NOT_FOUND));

        if (!rental.getStatus().equals(RentalStatus.PENDING_FEE)) {
            throw new AppException(ErrorCode.INVALID_RENTAL_STATUS);
        }

        RentalOverviewResponse overview = getRentalOverviewById(id);
        double totalCharges = overview.getVehicleDamageFee() + overview.getCheckListFee();
        double totalDeposits = overview.getRentalDeposit() + overview.getReservationDeposit();
        double balance = totalCharges - totalDeposits;

        // TRƯỜNG HỢP 1: Khách hàng cần trả thêm tiền
        if (balance > 0) {
            CreatePaymentUrlRequest request = new CreatePaymentUrlRequest();
            request.setRentalId(rental.getId());
            request.setType(PaymentType.PENALTY_FEE_RENTAL);
            request.setAmount(balance);
            request.setDescription("Check-out Payment for Rental ID: " + rental.getId());
            request.setUserEmail(rental.getUser().getEmail());
            if(rental.getVehicleLog()==null){
                rental.getVehicle().setStatus(VehicleStatus.AVAILABLE);
            }
            try {
                String url = paymentService.createPaymentUrl(request, remoteAddr).getUrl();
                emailService.sendPaymentStatusToEmail(rental.getPayments()
                        .stream()
                        .filter(p -> p.getType() == PaymentType.PENALTY_FEE_RENTAL)
                        .findFirst()
                        .orElse(null),url);
                return CheckOutProcessResponse.builder()
                        .processStatus("PAYMENT_REQUIRED")
                        .paymentUrl(url)
                        .rental(rentalMapper.toRentalResponse(rental))
                        .build();
            } catch (Exception e) {
                throw new AppException(ErrorCode.CREATE_PAYMENT_URL_FAILED);
            }
        }
        // TRƯỜNG HỢP 2: Hoàn tiền hoặc không làm gì
        else {
            if (balance < 0) {
                RefundRequest refundRequest = new RefundRequest();
                refundRequest.setIpAddr(remoteAddr);
                refundRequest.setTxnRef(
                        paymentRepository.findByRental_idAndTypeAndStatus(rental.getId(),PaymentType.RENTAL, PaymentStatus.SUCCESS).getTxnRef()
                );
                refundRequest.setAmount(Math.abs(balance));
                refundRequest.setFullRefund(false);

                paymentService.refundPayment(refundRequest);
            }
            // Cộng điểm cho user
            long hours = Duration.between(rental.getStartTime(), rental.getEndTime()).toHours();
            int pointPerHour = rental.getVehicle().getPoint();
            int earnedPoints = (int) (hours * pointPerHour);

            User user = rental.getUser();
            user.setPoint(user.getPoint() + earnedPoints);
            userRepository.save(user);

            rental.setStatus(RentalStatus.COMPLETED);
            if(rental.getVehicleLog()==null){
                rental.getVehicle().setStatus(VehicleStatus.AVAILABLE);
            }
            Payment payment = paymentRepository.findByRental_IdAndType(rental.getId(), PaymentType.REFUND).orElseThrow(
                    () -> new AppException(ErrorCode.PAYMENT_NOT_EXISTS)
            );
            emailService.sendPaymentStatusToEmail(payment,null);

            return CheckOutProcessResponse.builder()
                    .processStatus("COMPLETED")
                    .paymentUrl(null) // Không có URL
                    .rental(rentalMapper.toRentalResponse(rental))
                    .build();
        }
    }

    public VnpayResponse extendRentalReturnTime(Long id, LocalDateTime newReturnTime, String ipAddr) throws Exception {
        Rental rental = rentalRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RENTAL_NOT_FOUND));

        // Validate new return time
        if (!isExactHour(newReturnTime) || !isExactHour(newReturnTime)) {
            throw new AppException(ErrorCode.TIME_MUST_BE_EXACT_HOUR);
        }
        // New return time must be at least 1 hour after current end time
        if (newReturnTime.isBefore(rental.getEndTime().plusHours(1))) {
            throw new AppException(ErrorCode.RENTAL_EXTEND_TIME_INVALID);
        }
        // Extension requests must be made at least 2 hours before current start time
        if(rental.getStartTime().isBefore(LocalDateTime.now().plusHours(3))) {
            throw new AppException(ErrorCode.RENTAL_EXTEND_TIME_INVALID);
        }
        // Only CONFIRM reservations can be extended
        if(rental.getStatus() != RentalStatus.CONFIRM && rental.getStatus() != RentalStatus.ONGOING && rental.getStatus() != RentalStatus.OVERDUE) {
            throw new AppException(ErrorCode.RENTAL_EXTEND_TIME_INVALID);
        }
        // Check if vehicle is available for the extended period
        if (isVehicleUnavailable(rental.getVehicle().getId(), rental.getStartTime(), newReturnTime, null, rental.getId())) {
            throw new AppException(ErrorCode.VEHICLE_NOT_AVAILABLE);
        }

        // Calculate new fee with extended time
        Rental tempRental = Rental.builder()
                .startTime(rental.getStartTime())
                .endTime(newReturnTime)
                .vehicle(rental.getVehicle())
                .build();
        double newFee = calculateRentalFee(tempRental.getVehicle(), tempRental.getStartTime(), tempRental.getEndTime());

        // Store pending values
        rental.setPendingEndTime(newReturnTime);
        rental.setPendingRentFee(newFee);
        rental.setPreStatus(rental.getStatus());
        rental.setStatus(RentalStatus.PENDING_EXTEND_FEE);
        rentalRepository.save(rental);

        CreatePaymentUrlRequest request = CreatePaymentUrlRequest.builder()
                .rentalId(rental.getId())
                .type(PaymentType.RENTAL_EXTENSION)
                .amount(newFee - rental.getRentFee())
                .description("Extension Payment for Rental ID: " + rental.getId())
                .userEmail(rental.getUser().getEmail())
                .build();

        return paymentService.createPaymentUrl(request, ipAddr);
    }

    // Check if vehicle is unavailable due to existing reservations or rentals
    private boolean isVehicleUnavailable(Long vehicleId, LocalDateTime startTime, LocalDateTime endTime, Long excludeReservationId, Long excludeRentalId) {
        // Time minimum 4hours validation
        long hour = Duration.between(startTime, endTime).toHours();
        if(hour < 4){
            throw new AppException(ErrorCode.RENT_TIME_MUST_MINIMUM_4_HOURS);
        }

        int conflictCount = vehicleRepository.doesConflictExistForVehicle(
                vehicleId,
                startTime,
                endTime,
                List.of("PENDING", "CONFIRM"),
                List.of("COMPLETED", "CANCELLED", "OVERDUE"),
                excludeReservationId,
                excludeRentalId
        );

        return conflictCount > 0;
    }

    // Check if the time is on the exact hour (e.g., 1:00, 2:00)
    private boolean isExactHour(LocalDateTime dateTime) {
        return dateTime.getMinute() == 0 && dateTime.getSecond() == 0;
    }

    public List<RentalResponse> getRentalByEmailUserContain(String email) {
        List<Rental> rentals = rentalRepository.findByUserEmailContains(email);
        if (rentals == null || rentals.isEmpty()) {
            throw new AppException(ErrorCode.RENTAL_NOT_FOUND);
        }
        return rentals.stream()
                .map(rentalMapper::toRentalResponse)
                .toList();
    }

    public List<RentalResponse> getRentalByEmailUserContainAndStatusIn(String email, List<RentalStatus> status) {
        List<Rental> rentals = rentalRepository.findByUserEmailContainsAndStatusIn(email, status);
        if (rentals == null || rentals.isEmpty()) {
            throw new AppException(ErrorCode.RENTAL_NOT_FOUND);
        }
        return rentals.stream()
                .map(rentalMapper::toRentalResponse)
                .toList();
    }

    public PageAndFilterRentalResponse findByPageAndFilterAndSearch(PageAndFilterRentalRequest request) {
        User user = userService.currentUser();

        List<RentalStatus> statusList = (request.getStatus() == null || request.getStatus().isEmpty())
                ? Arrays.asList(RentalStatus.values())
                : request.getStatus();

        Pageable pageable = PageRequest.of(request.getPage() - 1, request.getLimit(), Sort.by("id").descending());
        Page<Rental> rentalPage;

        if(user.getRole() == Role.ROLE_STAFF){
            Station station = stationRepository.findById(user.getStaff().getStation().getId())
                    .orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_FOUND));
            rentalPage = rentalRepository.findByStatusInAndUser_EmailContainsAndStation_Id(statusList, request.getSearch(), station.getId(), pageable);
        }else{
            rentalPage = rentalRepository.findByStatusInAndUser_EmailContains(statusList, request.getSearch(), pageable);
        }

        List<RentalListResponse> rentals = rentalPage.getContent().stream()
                .map(rentalMapper::toRentalListResponse)
                .toList();

        return new PageAndFilterRentalResponse(rentals, rentalPage.getTotalPages());
    }

    public List<RentalResponse> getRentalOfStation(Long stationId){
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new AppException(ErrorCode.STATION_NOT_FOUND));

        return rentalRepository.findByStation_Id(station.getId()).stream()
                .sorted(Comparator.comparing(Rental::getCreatedAt).reversed())
                .map(rentalMapper::toRentalResponse)
                .toList();
    }

    public PageAndFilterRentalHistoryResponse getRentalsByUserEmail(PageAndFilterRentalHistoryRequest request) {
        User user = userService.currentUser();

        if(user == null || !user.getEmail().equals(request.getEmail())){
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        List<RentalStatus> statusList = (request.getStatus() == null || request.getStatus().isEmpty())
                ? Arrays.asList(RentalStatus.values())
                : request.getStatus();

        Pageable pageable = PageRequest.of(request.getPage() - 1, request.getLimit(), Sort.by("id").descending());
        Page<Rental> rentalPage;

        rentalPage = rentalRepository.findByStatusInAndUser_IdAndVehicle_NameContains(statusList,user.getId(), request.getSearch(), pageable);

        List<RentalHistoryListResponse> rentals = rentalPage.stream()
                .map(rental -> {
                    RentalHistoryListResponse rentalHistoryListResponse = rentalMapper.toRentalHistoryListResponse(rental);
                    rentalHistoryListResponse.setVehicleImage(rental.getVehicle().getImages().stream().filter(ImgVehicle::isMain).findFirst().orElse(null).getUrl());
                    return rentalHistoryListResponse;
                })
                .toList();

        return new PageAndFilterRentalHistoryResponse(rentals, rentalPage.getTotalPages());
    }

    public RentalResponse getOwnRentalDetails(long id) {
        User user = userService.currentUser();

        Rental rental = rentalRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RENTAL_NOT_FOUND));
        if(!rental.getUser().getEmail().equals(user.getEmail())){
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        RentalResponse response = rentalMapper.toRentalResponse(rental);
        String redisValue = (String) redisTemplate.opsForValue().get("extendRental:" + rental.getId());
        if(redisValue != null){
            response.setPaymentUrl(redisValue);
        }
        return response;
    }

    public Rental getById(long id) {
        return rentalRepository.findById(id)
                .orElse(null);
    }

    public boolean cancelRental(long rentalId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new AppException(ErrorCode.RENTAL_NOT_FOUND));

        if(rental.getStatus() != RentalStatus.PENDING && (rental.getStatus() != RentalStatus.CONTRACTING && rental.getContractStatus() != ContractStatus.PENDING)){
            throw new AppException(ErrorCode.INVALID_RENTAL_STATUS);
        }

        rental.setStatus(RentalStatus.CANCELLED);
        rental.getVehicle().setStatus(VehicleStatus.AVAILABLE);
        rentalRepository.save(rental);
        return true;
    }
}
