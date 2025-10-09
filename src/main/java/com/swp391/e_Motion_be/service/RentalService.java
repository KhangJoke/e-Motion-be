package com.swp391.e_Motion_be.service;

import com.swp391.e_Motion_be.dto.requests.deposit.DepositCreateRequest;
import com.swp391.e_Motion_be.dto.requests.rental.RentalCreateFromReservationRequest;
import com.swp391.e_Motion_be.dto.requests.rental.RentalCreateRequest;
import com.swp391.e_Motion_be.dto.responses.rental.RentalOverviewResponse;
import com.swp391.e_Motion_be.dto.requests.rental.RentalUpdateStatusRequest;
import com.swp391.e_Motion_be.dto.responses.rental.RentalResponse;
import com.swp391.e_Motion_be.entity.*;
import com.swp391.e_Motion_be.enums.*;
import com.swp391.e_Motion_be.enums.vehicle.VehicleStatus;
import com.swp391.e_Motion_be.exception.AppException;
import com.swp391.e_Motion_be.mapper.RentalMapper;
import com.swp391.e_Motion_be.repository.*;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

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
    private final DocumentRepository documentRepository;
    private final ReservationService reservationService;
    private final EmailService emailService;
    private final RentalMapper rentalMapper;
    private final DepositService depositService;
    private final RentalCheckListRepository rentalCheckListRepository;

    @Value("${price.8h.rate}")
    private double price8hRate;
    @Value("${price.12h.rate}")
    private double price12hRate;
    @Value("${price.day.rate}")
    private double priceDayRate;

    public List<RentalResponse> getAllRentals(){
       return rentalRepository.findAll().stream()
                .map(rentalMapper::toRentalResponse)
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
        if(!vehicle.getStation().getId().equals(stationId)) {
            throw new AppException(ErrorCode.VEHICLE_STATION_MISMATCH);
        }

        // save rental
        // set status của xe sang đang thuê
        vehicle.setStatus(VehicleStatus.ONGOING);
        rental.setRentFee(calculateRentalFee(rental)); // Tiền thuê
        rentalRepository.save(rental);
        // Create deposit
        DepositCreateRequest depositCreateRequest = new DepositCreateRequest(
                DepositStatus.PENDING,
                vehicle.getDepositFee(), // Cọc xe
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

    private double calculateRentalFee(Rental rental) {
        LocalDateTime start = rental.getStartTime();
        LocalDateTime end = rental.getEndTime();
        long hours = Duration.between(start, end).toHours();
        double fee = 0;
        double pricePer4Hours = rental.getVehicle().getPricePer4Hours();

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
        Map<String, Double> vehicleDamages = vehicleLog != null ? vehicleLog.getRepairCost() : null;
        double vehicleDamageFee = vehicleLog != null ? vehicleLog.getCost() : 0;

        double totalCharges = vehicleDamageFee + checkListFee;
        double totalDeposits = reservationDepositAmount + rentalDepositAmount;

        return RentalOverviewResponse.builder()
                .rental(rental)
                .checkListFee(checkListFee)
                .reservationDeposit(reservationDepositAmount)
                .rentalDeposit(rentalDepositAmount)
                .vehicleDamages(vehicleDamages)
                .vehicleDamageFee(vehicleDamageFee)
                .refundEligible(totalCharges <= totalDeposits)
                .build();
    }


    public void sendRentalExpiringEmail(Rental rental) {
        String subject = "Your Rental is About to Expire";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
        String endTimeFormatted = rental.getEndTime() != null
                ? rental.getEndTime().format(formatter)
                : "Not specified";

        String htmlMessage = "<!DOCTYPE html>"
                + "<html lang='vi'>"
                + "<head>"
                + "<meta charset='UTF-8'>"
                + "<meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                + "<title>Rental Expire Reminder</title>"
                + "</head>"
                + "<body style='font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 0; color: #333333;'>"

                + "<div style='width: 100%; max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; "
                + "box-shadow: 0 4px 6px rgba(0,0,0,0.1); overflow: hidden;'>"

                // Header
                + "<div style='background-color: #3B82F6; color: #ffffff; padding: 20px; text-align: center;'>"
                + "<div style='margin-bottom: 10px;'>"
                + "<img src='https://res.cloudinary.com/dy45rrkhf/image/upload/f_auto,q_auto/v1759931450/navxfjt05woc38qvpbtw.png' "
                + "alt='Company Logo' width='100' height='70'>"
                + "</div>"
                + "<h1 style='margin: 0; font-size: 24px;'>Upcoming Notice: Your Rental Is About to Expire</h1>"
                + "</div>"

                // Content
                + "<div style='padding: 20px 30px; line-height: 1.6; color: #333333;'>"
                + "<p>Dear: <strong>" + rental.getUser().getFullName() + "</strong>,</p>"
                + "<p>We would like to remind you that your current rental period will end soon. "
                +"Please check the details below and ensure the vehicle is returned or renewed on time:</p>"

                + "<table cellpadding='0' cellspacing='0' border='0' style='width: 100%; margin: 20px 0; border-collapse: collapse;'>"
                + "<tr><th style='width:40%; padding:10px; text-align:left; border-bottom:1px solid #eee; "
                + "background-color:#f9f9f9; font-weight:bold; color:#555;'>Rental ID:</th>"
                + "<td style='padding:10px; border-bottom:1px solid #eee;'><strong>" + rental.getId() + "</strong></td></tr>"

                + "<tr><th style='width:40%; padding:10px; text-align:left; border-bottom:1px solid #eee; "
                + "background-color:#f9f9f9; font-weight:bold; color:#555;'>Vehicle Model:</th>"
                + "<td style='padding:10px; border-bottom:1px solid #eee;'><strong>" + rental.getVehicle().getName() + "</strong></td></tr>"

                + "<tr><th style='width:40%; padding:10px; text-align:left; border-bottom:1px solid #eee; "
                + "background-color:#f9f9f9; font-weight:bold; color:#555;'>End Time:</th>"
                + "<td style='padding:10px; border-bottom:1px solid #eee;'><strong>" + endTimeFormatted + "</strong></td></tr>"

                + "<tr><th style='width:40%; padding:10px; text-align:left; border-bottom:1px solid #eee; "
                + "background-color:#f9f9f9; font-weight:bold; color:#555;'>Car Return Location:</th>"
                + "<td style='padding:10px; border-bottom:1px solid #eee;'><strong>" + rental.getStation().getName() + "</strong></td></tr>"
                + "</table>"

                + "<p>Please make sure to <strong style='color:#0080ff;'>return</strong> or "
                + "<strong style='color:#0080ff;'>extend</strong> your rental before the end time to <strong style='color:#ff0000;'>avoid late fees or service interruptions</strong>.</p>"

                + "<p>If you have any questions or would like to <strong style='color:#ff8c1a'>extend your rental time</strong>, "
                + "please click the <strong style='color:#3B82F6'>button</strong> below or contact us immediately.</p>"

                + "<a href='[Liên kết gia hạn]' "
                + "style='display:block; width:80%; margin:30px auto; padding:15px 25px; background-color:#3B82F6; "
                + "color:#ffffff !important; text-align:center; text-decoration:none; border-radius:5px; font-size:16px; font-weight:bold;'>"
                + "Gia Hạn Thuê Xe Hoặc Liên Hệ Hỗ Trợ</a>"

                + "<p>Thank you very much for using our service.</p>"
                + "<p>Best regards,<br>E-Motion</p>"
                + "</div>"

                // Footer
                + "<div style='background-color:#f2f2f2; color:#555; padding:15px 25px; text-align:center; font-size:12px; "
                + "border-top:1px solid #3B82F6; line-height:1.6;'>"

                + "<div style='margin-bottom:10px;'>"
                + "<img src='https://res.cloudinary.com/dy45rrkhf/image/upload/f_auto,q_auto/v1759931502/logo_bp3y1d.png' alt='Company Logo' width='80' height='50'>"
                + "</div>"

                + "<div style='display:inline-block; text-align:left;'>"
                + "<table cellpadding='0' cellspacing='0' border='0' style='font-size:12px; color:#333; border-collapse:collapse;'>"
                + "<tr>"
                + "<td valign='top' style='padding-right:20px;'>"
                + "<strong>Hà Nội:</strong><br>E-Motion Station Hoàn Kiếm<br>E-Motion Station Cầu Giấy<br>E-Motion Station Thanh Xuân</td>"
                + "<td style='border-left:1px solid #ccc; width:1px; padding:0 20px;'></td>"
                + "<td valign='top' style='padding-left:20px;'>"
                + "<strong>TP. Hồ Chí Minh:</strong><br>E-Motion Station Tân Bình<br>E-Motion Station Thủ Đức<br>E-Motion Station Trần Hưng Đạo</td>"
                + "</tr></table></div>"

                + "<p style='margin-top:10px; text-align:center;'><strong>Hotline:</strong> 0339695701 &nbsp;|&nbsp; "
                + "<strong>Email:</strong> e.motion.vehicle1@gmail.com</p>"

                + "<h6 style='margin:10px 0 0 0; font-size:11px; color:#999; font-weight:normal; text-align:center;'>"
                + "© 2025 E-Motion. All rights reserved.</h6>"
                + "</div>"
                + "</div>"
                + "</body>"
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
        expiringRentals.forEach(rental -> {
            sendRentalExpiringEmail(rental);
            rental.setExpiringNotified(true);
            rentalRepository.save(rental);
        });
    }

    public void sendRentalOverdueEmail(Rental rental) {
        String subject = "Your Rental is Overdue";
        RentalCheckList checkOut = rentalCheckListRepository.findByRental_Id(rental.getId()).stream()
                .filter(c -> c.getType() == CheckType.CHECK_OUT)
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.CHECKOUT_NOT_FOUND));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
        String startTimeFormatted = rental.getStartTime() != null
                ? rental.getStartTime().format(formatter)
                : "Not specified";
        String endTimeFormatted = rental.getEndTime() != null
                ? rental.getEndTime().format(formatter)
                : "Not specified";
        String actualReturnedTime = checkOut.getCreatedAt() != null
                ? checkOut.getCreatedAt().format(formatter)
                : "Not specified";

        String htmlMessage = "<!DOCTYPE html>"
                + "<html lang='vi'>"
                + "<head>"
                + "<meta charset='UTF-8'>"
                + "<meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                + "<title>Your Rental is Overdue</title>"
                + "</head>"
                + "<body style='font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 0; color: #333333;'>"

                + "<div style='width: 100%; max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; "
                + "box-shadow: 0 4px 6px rgba(0,0,0,0.1); overflow: hidden;'>"

                // Header
                + "<div style='background-color: #3B82F6; color: #ffffff; padding: 20px; text-align: center;'>"
                + "<div style='margin-bottom: 10px;'>"
                + "<img src='https://res.cloudinary.com/dy45rrkhf/image/upload/f_auto,q_auto/v1759931450/navxfjt05woc38qvpbtw.png' "
                + "alt='Company Logo' width='100' height='70'>"
                + "</div>"
                + "<h1 style='margin: 0; font-size: 24px;'>Important Notice: Your Rental is Overdue</h1>"
                + "</div>"

                // Content
                + "<div style='padding: 20px 30px; line-height: 1.6; color: #333333;'>"
                + "<p>Dear: <strong>" + rental.getUser().getFullName() + "</strong>,</p>"
                + "<p>We would like to inform you that your rental period is coming to an end. "
                + "Please make arrangements to return the car on time as per the details below:</p>"

                + "<table cellpadding='0' cellspacing='0' border='0' style='width: 100%; margin: 20px 0; border-collapse: collapse;'>"
                + "<tr><th style='width:40%; padding:10px; text-align:left; border-bottom:1px solid #eee; "
                + "background-color:#f9f9f9; font-weight:bold; color:#555;'>Rental ID:</th>"
                + "<td style='padding:10px; border-bottom:1px solid #eee;'><strong>" + rental.getId() + "</strong></td></tr>"

                + "<tr><th style='width:40%; padding:10px; text-align:left; border-bottom:1px solid #eee; "
                + "background-color:#f9f9f9; font-weight:bold; color:#555;'>Vehicle Model:</th>"
                + "<td style='padding:10px; border-bottom:1px solid #eee;'><strong>" + rental.getVehicle().getName() + "</strong></td></tr>"

                + "<tr><th style='width:40%; padding:10px; text-align:left; border-bottom:1px solid #eee; "
                + "background-color:#f9f9f9; font-weight:bold; color:#555;'>Start Time:</th>"
                + "<td style='padding:10px; border-bottom:1px solid #eee;'><strong>" + startTimeFormatted + "</strong></td></tr>"

                + "<tr><th style='width:40%; padding:10px; text-align:left; border-bottom:1px solid #eee; "
                + "background-color:#f9f9f9; font-weight:bold; color:#555;'>End Time:</th>"
                + "<td style='padding:10px; border-bottom:1px solid #eee;'><strong>" + endTimeFormatted + "</strong></td></tr>"

                + "<tr><th style='width:40%; padding:10px; text-align:left; border-bottom:1px solid #eee; "
                + "background-color:#f9f9f9; font-weight:bold; color:#555;'>Actual Vehicle returned time::</th>"
                + "<td style='padding:10px; border-bottom:1px solid #eee;'><strong>" + actualReturnedTime + "</strong></td></tr>"

                + "<tr><th style='width:40%; padding:10px; text-align:left; border-bottom:1px solid #eee; "
                + "background-color:#f9f9f9; font-weight:bold; color:#555;'>Car Return Location:</th>"
                + "<td style='padding:10px; border-bottom:1px solid #eee;'><strong>" + rental.getStation().getName() + "</strong></td></tr>"

                + "<tr><th style='width:40%; padding:10px; text-align:left; border-bottom:1px solid #eee; "
                + "background-color:#f9f9f9; font-weight:bold; color:#555;'>Usage fee:</th>"
                + "<td style='padding:10px; border-bottom:1px solid #eee;'><strong>" + checkOut.getFee() + "</strong></td></tr>"
                + "</table>"

                + "<p>Please ensure the car is returned at the correct <strong style='color:#0080ff;'>Station</strong> and "
                + "<strong style='color:#0080ff;'>End Time</strong> to <strong style='color:#ff0000;'>avoid overtime charges</strong>.</p>"

                + "<p>If you have any questions or would like to <strong style='color:#ff8c1a'>extend your rental time</strong>, "
                + "please click the <strong style='color:#3B82F6'>button</strong> below or contact us immediately.</p>"

                + "<a href='[Đường link liên hệ/Gia hạn]' "
                + "style='display:block; width:80%; margin:30px auto; padding:15px 25px; background-color:#3B82F6; "
                + "color:#ffffff !important; text-align:center; text-decoration:none; border-radius:5px; font-size:16px; font-weight:bold;'>"
                + "Gia Hạn Thuê Xe Hoặc Liên Hệ Hỗ Trợ</a>"

                + "<p>Thank you very much for using our service.</p>"
                + "<p>Best regards,<br>E-Motion</p>"
                + "</div>"

                // Footer
                + "<div style='background-color:#f2f2f2; color:#555; padding:15px 25px; text-align:center; font-size:12px; "
                + "border-top:1px solid #3B82F6; line-height:1.6;'>"

                + "<div style='margin-bottom:10px;'>"
                + "<img src='https://res.cloudinary.com/dy45rrkhf/image/upload/f_auto,q_auto/v1759931502/logo_bp3y1d.png' alt='Company Logo' width='80' height='50'>"
                + "</div>"

                + "<div style='display:inline-block; text-align:left;'>"
                + "<table cellpadding='0' cellspacing='0' border='0' style='font-size:12px; color:#333; border-collapse:collapse;'>"
                + "<tr>"
                + "<td valign='top' style='padding-right:20px;'>"
                + "<strong>Hà Nội:</strong><br>E-Motion Station Hoàn Kiếm<br>E-Motion Station Cầu Giấy<br>E-Motion Station Thanh Xuân</td>"
                + "<td style='border-left:1px solid #ccc; width:1px; padding:0 20px;'></td>"
                + "<td valign='top' style='padding-left:20px;'>"
                + "<strong>TP. Hồ Chí Minh:</strong><br>E-Motion Station Tân Bình<br>E-Motion Station Thủ Đức<br>E-Motion Station Trần Hưng Đạo</td>"
                + "</tr></table></div>"

                + "<p style='margin-top:10px; text-align:center;'><strong>Hotline:</strong> 0339695701 &nbsp;|&nbsp; "
                + "<strong>Email:</strong> e.motion.vehicle1@gmail.com</p>"

                + "<h6 style='margin:10px 0 0 0; font-size:11px; color:#999; font-weight:normal; text-align:center;'>"
                + "© 2025 E-Motion. All rights reserved.</h6>"
                + "</div>"
                + "</div>"
                + "</body>"
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
