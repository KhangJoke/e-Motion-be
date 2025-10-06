package com.swp391.e_Motion_be.service;

import com.swp391.e_Motion_be.dto.requests.deposit.DepositCreateRequest;
import com.swp391.e_Motion_be.dto.requests.rental.RentalCreateFromReservationRequest;
import com.swp391.e_Motion_be.dto.requests.rental.RentalCreateRequest;
import com.swp391.e_Motion_be.dto.requests.rental.RentalUpdateStatusRequest;
import com.swp391.e_Motion_be.dto.responses.RentalResponse;
import com.swp391.e_Motion_be.entity.*;
import com.swp391.e_Motion_be.enums.DepositStatus;
import com.swp391.e_Motion_be.enums.DocType;
import com.swp391.e_Motion_be.enums.ErrorCode;
import com.swp391.e_Motion_be.enums.RentalStatus;
import com.swp391.e_Motion_be.enums.vehicle.VehicleStatus;
import com.swp391.e_Motion_be.exception.AppException;
import com.swp391.e_Motion_be.mapper.RentalMapper;
import com.swp391.e_Motion_be.repository.*;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RentalService {

    private final RentalRepository rentalRepository;
    private final ReservationRepository reservationRepository;
    private final StaffRepository staffRepository;
    private final VehicleRepository vehicleRepository;
    private final StationRepository stationRepository;
    private final UserRepository userRepository;

    private final EmailService emailService;
    private final RentalMapper rentalMapper;
    private final DepositService depositService;
    private final DocumentRepository documentRepository;

    public List<RentalResponse> getAllRentals(){
       return rentalRepository.findAll().stream()
                .map(rentalMapper::toRentalResponse)
                .toList();
    }

    // Hàm tạo rental khi renter có thuê trước
    @Transactional
    public RentalResponse createRentalFromReservation(RentalCreateFromReservationRequest request){
        Reservation reservation = reservationRepository.findByCode(request.getReservationCode())
                .orElseThrow(() -> new AppException(ErrorCode.RESERVATION_NOT_FOUND));
        Staff staff = staffRepository.findById(request.getStaffId())
                .orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_FOUND));
        Rental rental = rentalMapper.fromReservationToRental(reservation);
        rental.setReservation(reservation);
        rental.setStaff(staff);
        return createRentalCommon(rental, reservation.getUser().getId() ,reservation.getVehicle(), reservation.getStation().getId());
    }

    // Hàm tạo rental khi renter thuê trực tiếp tại trạm
    @Transactional
    public RentalResponse createRental(RentalCreateRequest request){
        // kiểm tra có tồn tại object ko
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_NOT_EXIST));
        Station station = stationRepository.findById(request.getStationId())
                .orElseThrow(() -> new AppException(ErrorCode.STATION_NOT_FOUND));
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(()->new AppException(ErrorCode.USER_NOT_EXISTS));
        Staff staff = staffRepository.findById(request.getStaffId())
                .orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_FOUND));
        // kiểm tra thời gian thuê có conflic với các đơn đang thuê ko
        boolean hasConflict = rentalRepository.findByVehicle_IdAndStatusNotIn(request.getVehicleId(), List.of(RentalStatus.COMPLETED, RentalStatus.CANCELLED))
                .stream().anyMatch(r -> r.getStartTime().minusHours(3).isBefore(request.getEndTime())
                && r.getEndTime().plusHours(3).isAfter(request.getStartTime()));
        if(hasConflict){
            throw new AppException(ErrorCode.RENTAL_HAS_CONFLICT);
        }
        Rental rental = rentalMapper.toRentalEntity(request, vehicle, station, user, staff);
        return createRentalCommon(rental, user.getId(), vehicle, station.getId());
    }

    // Hàm này chứa các action chung của 2 hàm cách tạo rental
    private RentalResponse createRentalCommon(Rental rental, Long userId, Vehicle vehicle, Long stationId){
        // Kiểm tra CCCD và GPLX của renter
        if(!documentRepository.existsByUser_IdAndType(userId, DocType.CCCD)){
            throw new AppException(ErrorCode.USER_NEED_HAS_CCCD);
        }
        if(!documentRepository.existsByUser_IdAndType(userId, DocType.LICENSE)){
            throw new AppException(ErrorCode.USER_NEED_HAS_LICENSE);
        }
        // Kiểm tra user có đơn thuê nào chưa trả ko
        boolean hasOngoingRental = rentalRepository.existsByUser_IdAndStatusNotIn(userId, List.of(RentalStatus.COMPLETED, RentalStatus.CANCELLED));
        if(hasOngoingRental){
            throw new AppException(ErrorCode.USER_HAS_ONGOING_RENTAL);
        }
        // Kiểm tra xe cho thuê có đang available ko
        if(!vehicle.getStatus().equals(VehicleStatus.AVAILABLE)){
            throw new AppException(ErrorCode.VEHICLE_NOT_READY);
        }
        // Kiểm tra station của xe và của đơn có giống nhau ko
        if(vehicle.getStation().getId().equals(stationId)) {
            throw new AppException(ErrorCode.VEHICLE_STATION_MISMATCH);
        }
        // save rental
        rental.setRentFee(calculateFee(rental)); // Tiền thuê
        rentalRepository.save(rental);
        // Create deposit
        DepositCreateRequest depositCreateRequest = new DepositCreateRequest(
                DepositStatus.PENDING,
                vehicle.getDepositFee(), // Cọc xe
                null,
                rental.getId()
        );
        depositService.createDeposit(depositCreateRequest);
        // update status vehicle khi bắt đầu thuê
        rental.getVehicle().setStatus(VehicleStatus.INUSE);
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

    public void sendRentalExpiringEmail(Rental rental) {
        String subject = "Your Rental is About to Expire";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
        String endTimeFormatted = rental.getEndTime() != null
                ? rental.getEndTime().format(formatter)
                : "Not specified";

        String htmlMessage = "<html style=\"font-family: Arial, sans-serif;\">"
                + "<div style=\"background-color: #f9f9f9; padding: 20px;\">"
                + "<h2 style=\"color: #e67e22; text-align: center;\">Rental Expiry Reminder</h2>"
                + "<p style=\"font-size: 16px; color: #555;\">"
                + "Dear " + rental.getUser().getFullName() + ",</p>"
                + "<p style=\"font-size: 15px; color: #444;\">"
                + "Your rental for vehicle <strong>" + rental.getVehicle().getName() + "</strong> "
                + "will expire soon.</p>"
                + "<div style=\"background-color: #ffffff; padding: 15px; border-radius: 8px; "
                + "border: 1px solid #ddd; margin: 20px 0;\">"
                + "<p style=\"font-size: 18px; font-weight: bold; color: #e74c3c;\">"
                + "Renter: " + rental.getUser().getFullName() + "</p>"
                + "<p style=\"font-size: 18px; font-weight: bold; color: #e74c3c;\">"
                + "Station: " + rental.getStation().getName() + "</p>"
                + "<p style=\"font-size: 18px; font-weight: bold; color: #e74c3c;\">"
                + "Rental End Time: " + endTimeFormatted + "</p>"
                + "</div>"
                + "<p style=\"font-size: 14px; color: #666;\">"
                + "Please return the vehicle on time to avoid additional charges."
                + "</p>"
                + "<p style=\"font-size: 13px; color: #999; margin-top: 30px;\">"
                + "If you have already returned the vehicle, please ignore this email."
                + "</p>"
                + "</div>"
                + "</html>";

        try {
            emailService.sendVerificationEmail(rental.getUser().getEmail(), subject, htmlMessage);
        } catch (MessagingException e) {
            throw new AppException(ErrorCode.SEND_EMAIL_FAILED);
        }
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
        expiringRentals.forEach(rental -> {;
            sendRentalExpiringEmail(rental);
            rental.setExpiringNotified(true);
            rentalRepository.save(rental);
        });
    }

    public void sendRentalOverdueEmail(Rental rental) {
        String subject = "Your Rental is Overdue";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
        String endTimeFormatted = rental.getEndTime() != null
                ? rental.getEndTime().format(formatter)
                : "Not specified";

        String htmlMessage = "<html style=\"font-family: Arial, sans-serif;\">"
                + "<div style=\"background-color: #f9f9f9; padding: 20px;\">"
                + "<h2 style=\"color: #e67e22; text-align: center;\">Rental Overdue Reminder</h2>"
                + "<p style=\"font-size: 16px; color: #555;\">"
                + "Dear " + rental.getUser().getFullName() + ",</p>"
                + "<p style=\"font-size: 15px; color: #444;\">"
                + "Your rental for vehicle <strong>" + rental.getVehicle().getName() + "</strong> "
                + "has already expired.</p>"
                + "<div style=\"background-color: #ffffff; padding: 15px; border-radius: 8px; "
                + "border: 1px solid #ddd; margin: 20px 0;\">"
                + "<p style=\"font-size: 18px; font-weight: bold; color: #e74c3c;\">"
                + "Renter: " + rental.getUser().getFullName() + "</p>"
                + "<p style=\"font-size: 18px; font-weight: bold; color: #e74c3c;\">"
                + "Station: " + rental.getStation().getName() + "</p>"
                + "<p style=\"font-size: 18px; font-weight: bold; color: #e74c3c;\">"
                + "Rental End Time: " + endTimeFormatted + "</p>"
                + "</div>"
                + "<p style=\"font-size: 14px; color: #666;\">"
                + "Please return the vehicle immediately to avoid additional charges."
                + "</p>"
                + "<p style=\"font-size: 13px; color: #999; margin-top: 30px;\">"
                + "If you have already returned the vehicle, please ignore this email."
                + "</p>"
                + "</div>"
                + "</html>";

        try {
            emailService.sendVerificationEmail(rental.getUser().getEmail(), subject, htmlMessage);
        } catch (MessagingException e) {
            throw new AppException(ErrorCode.SEND_EMAIL_FAILED);
        }
    }

    @Transactional
    public void notifyOverdueRentals() {
        LocalDateTime now = LocalDateTime.now();
        List<Rental> overdueRentals = rentalRepository.findByStatusAndEndTimeBeforeAndOverdueNotifiedFalse(
                RentalStatus.ONGOING,
                now
        );
        overdueRentals.forEach(rental -> {
            sendRentalOverdueEmail(rental);
            rental.setStatus(RentalStatus.OVERDUE);
            rental.setOverdueNotified(true);
            rentalRepository.save(rental);
        });
    }
}
