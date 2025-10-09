package com.swp391.e_Motion_be.service;

import com.swp391.e_Motion_be.dto.requests.checklist.RentalCheckListCreateRequest;
import com.swp391.e_Motion_be.dto.responses.RentalCheckListResponse;
import com.swp391.e_Motion_be.entity.Rental;
import com.swp391.e_Motion_be.entity.RentalCheckList;
import com.swp391.e_Motion_be.entity.Staff;
import com.swp391.e_Motion_be.enums.CheckType;
import com.swp391.e_Motion_be.enums.ErrorCode;
import com.swp391.e_Motion_be.enums.RentalStatus;
import com.swp391.e_Motion_be.enums.vehicle.VehicleStatus;
import com.swp391.e_Motion_be.exception.AppException;
import com.swp391.e_Motion_be.mapper.RentalCheckListMapper;
import com.swp391.e_Motion_be.repository.RentalCheckListRepository;
import com.swp391.e_Motion_be.repository.RentalRepository;
import com.swp391.e_Motion_be.repository.StaffRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RentalCheckListService {
    @Value("${price.per.battery}")
    private double pricePerBattery;
    @Value("${penalty.fee.rate}")
    private double penaltyFeeRate;
    @Value("${price.day.rate}")
    private double priceDayRate;

    private final RentalCheckListRepository rentalCheckListRepository;
    private final RentalCheckListMapper rentalCheckListMapper;
    private final RentalRepository rentalRepository;
    private final StaffRepository staffRepository;
    private final EmailService emailService;

    public RentalCheckListResponse createCheckList(RentalCheckListCreateRequest request) {
        // --- Kiểm tra trùng check ---
        boolean alreadyExists = rentalCheckListRepository.findByRental_Id(request.getRentalId())
                .stream()
                .anyMatch(c -> c.getType() == request.getType());
        if (alreadyExists) {
            if (request.getType() == CheckType.CHECK_IN) throw new AppException(ErrorCode.ALREADY_CHECKED_IN);
            if (request.getType() == CheckType.CHECK_OUT) throw new AppException(ErrorCode.ALREADY_CHECKED_OUT);
        }

        // lấy ra các entity liên quan
        Rental rental = rentalRepository.findById(request.getRentalId())
                .orElseThrow(() -> new AppException(ErrorCode.RENTAL_NOT_FOUND));
        Staff staff = staffRepository.findByUser_Email(request.getStaffEmail())
                .orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_FOUND));

        RentalCheckList checkList = rentalCheckListMapper.toCheckListEntity(request);
        checkList.setRental(rental);
        checkList.setStaff(staff);
        // lưu vào db trước
        rentalCheckListRepository.save(checkList);

        if(request.getType().equals(CheckType.CHECK_IN)){
            // update status vehicle, rental khi bắt đầu thuê
            rental.setStatus(RentalStatus.ONGOING);
            rental.getVehicle().setStatus(VehicleStatus.ONGOING);
            rentalCheckListRepository.save(checkList);
        }else{
            // lưu phí phát sinh và cập nhật status rental, vehicle
            double fee = calculateFee(rental.getId());
            checkList.setFee(fee);
            rental.setStatus(RentalStatus.PENDING_FEE);
            rental.getVehicle().setStatus(VehicleStatus.CHECKING);
            rentalCheckListRepository.save(checkList);

            sendRentalReturnedNotification(rental);
        }

        return rentalCheckListMapper.toRentalCheckListResponse(checkList);
    }

    // tính phí phát sinh
    public double calculateFee(Long rentalId){
        List<RentalCheckList> checkLists = rentalCheckListRepository.findByRental_Id(rentalId);
        RentalCheckList checkIn = checkLists.stream()
                .filter(c -> c.getType() == CheckType.CHECK_IN)
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.CHECKIN_NOT_FOUND));

        RentalCheckList checkOut = checkLists.stream()
                .filter(c -> c.getType() == CheckType.CHECK_OUT)
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.CHECKOUT_NOT_FOUND));

        double  fee = 0.0;

        double batteryDiff = checkOut.getCurrentBattery() - checkIn.getCurrentBattery();
        if(batteryDiff < 0){
            fee += Math.abs(batteryDiff) * pricePerBattery;
        }

        Rental rental = checkOut.getRental();
        double pricePerDay = rental.getVehicle().getPricePer4Hours()*priceDayRate;

        LocalDateTime actualReturnTime = checkOut.getCreatedAt();
        LocalDateTime expectedReturnTime = rental.getEndTime();

        if (actualReturnTime.isAfter(expectedReturnTime)) {
            long lateHours = Math.max(0, Duration.between(expectedReturnTime, actualReturnTime).toHours());
            fee += lateHours * (penaltyFeeRate * pricePerDay);
        }

        return fee;
    }


    public void sendRentalReturnedNotification(Rental rental) {
        String subject = "Your vehicle has been successfully returned to the station.";

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
                + "<title>Vehicle Returned Successfully</title>"
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
                + "<h1 style='margin: 0; font-size: 24px;'>Notification: Your vehicle has been successfully returned to the station!</h1>"
                + "</div>"

                // Content
                + "<div style='padding: 20px 30px; line-height: 1.6; color: #333333;'>"
                + "<p>Dear: <strong>" + rental.getUser().getFullName() + "</strong>,</p>"
                + "<p>Thank you for using E-Motion. Your vehicle has been successfully returned to the station."
                + " We hope you had a great experience!</p>"

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
                + "<td style='padding:10px; border-bottom:1px solid #eee;'><strong>" + checkOut.getFee() + " VND </strong> </td></tr>"
                + "</table>"

                + "<p>We hope to see you again soon. For feedback or support, please click the "
                + "<strong style='color:#3B82F6'>button</strong> below or contact us immediately.</p>"

                + "<a href='[Đường link liên hệ/Gia hạn]' "
                + "style='display:block; width:80%; margin:30px auto; padding:15px 25px; background-color:#3B82F6; "
                + "color:#ffffff !important; text-align:center; text-decoration:none; border-radius:5px; font-size:16px; font-weight:bold;'>"
                + "Feedback / Contact Support</a>"

                + "<p>Thank you very much for using our service.</p>"
                + "<p>Best regards,</p><br>"
                + "<p>E-Motion</p>"
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
}
