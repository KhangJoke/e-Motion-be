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
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RentalCheckListService {
    private final RentalCheckListRepository rentalCheckListRepository;
    private final RentalCheckListMapper rentalCheckListMapper;
    private final RentalRepository rentalRepository;
    private final StaffRepository staffRepository;

    private final EmailService emailService;

    public RentalCheckListResponse createCheckList(RentalCheckListCreateRequest request) {
        if(request.getType().equals(CheckType.CHECK_IN)){
            // kiểm tra đơn thuê đã có check-in chưa
            boolean alreadyCheckedIn = rentalCheckListRepository.findByRental_Id(request.getRentalId()).stream()
                    .anyMatch(c -> c.getType() == CheckType.CHECK_IN);
            if(alreadyCheckedIn) throw new AppException(ErrorCode.ALREADY_CHECKED_IN);
        }else if(request.getType().equals(CheckType.CHECK_OUT)){
            // kiểm tra đơn thuê đã có check-out chưa
            boolean alreadyCheckedOut = rentalCheckListRepository.findByRental_Id(request.getRentalId()).stream()
                    .anyMatch(c -> c.getType() == CheckType.CHECK_OUT);
            if(alreadyCheckedOut) throw new AppException(ErrorCode.ALREADY_CHECKED_OUT);
        }else{
            throw new AppException(ErrorCode.CHECKLIST_TYPE_INVALID);
        }

        // lấy ra các entity liên quan
        Rental rental = rentalRepository.findById(request.getRentalId())
                .orElseThrow(() -> new AppException(ErrorCode.RENTAL_NOT_FOUND));
        Staff staff = staffRepository.findByUser_Email(request.getStaffEmail())
                .orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_FOUND));

        RentalCheckList checkList = rentalCheckListMapper.toCheckListEntity(request);
        // chuyển trạng xe thành đang kiểm tra và trạng thuê thành đang chờ phí phát sinh
        rental.setStatus(RentalStatus.PENDING_FEE);
        rental.getVehicle().setStatus(VehicleStatus.CHECKING);
        checkList.setRental(rental);
        checkList.setStaff(staff);
        rentalCheckListRepository.save(checkList);
        // add fee phát sinh lúc check-out
        if(request.getType().equals(CheckType.CHECK_OUT)){
            checkList.setFee(calculateFee(rental.getId()));
            // lưu phí phát sinh và cập nhật status rental
            rental.setPenaltyFee(calculateFee(rental.getId()));
            checkList.setRental(rental);
            rentalCheckListRepository.save(checkList);
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
            fee += Math.abs(batteryDiff) * 12_000 ; // 12k for each percent of battery used
        }

        Rental rental = checkOut.getRental();
        double pricePerDay = rental.getVehicle().getPricePerDay();

        LocalDateTime actualReturnTime = checkOut.getCreatedAt();
        LocalDateTime expectedReturnTime = rental.getEndTime();

        if (actualReturnTime.isAfter(expectedReturnTime)) {
            long lateHours = Math.max(0, Duration.between(expectedReturnTime, actualReturnTime).toHours());
            fee += lateHours * (0.2 * pricePerDay);
        }


        return fee;
    }

    public void sendDepositPendingEmail(Rental rental) {
        String subject = "Deposit Refund Pending";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
        String endTimeFormatted = rental.getEndTime() != null
                ? rental.getEndTime().format(formatter)
                : "Not specified";

        String htmlMessage = "<html style=\"font-family: Arial, sans-serif;\">"
                + "<div style=\"background-color: #f9f9f9; padding: 20px;\">"
                + "<h2 style=\"color: #3498db; text-align: center;\">Deposit Refund Pending</h2>"
                + "<p style=\"font-size: 16px; color: #555;\">"
                + "Dear " + rental.getUser().getFullName() + ",</p>"
                + "<p style=\"font-size: 15px; color: #444;\">"
                + "We have received your vehicle return for <strong>" + rental.getVehicle().getName() + "</strong>."
                + " However, your deposit refund is still being processed.</p>"
                + "<div style=\"background-color: #ffffff; padding: 15px; border-radius: 8px; "
                + "border: 1px solid #ddd; margin: 20px 0;\">"
                + "<p style=\"font-size: 16px; color: #2c3e50;\"><strong>Renter:</strong> "
                + rental.getUser().getFullName() + "</p>"
                + "<p style=\"font-size: 16px; color: #2c3e50;\"><strong>Vehicle:</strong> "
                + rental.getVehicle().getName() + "</p>"
                + "<p style=\"font-size: 16px; color: #2c3e50;\"><strong>Return Station:</strong> "
                + rental.getStation().getName() + "</p>"
                + "<p style=\"font-size: 16px; color: #2c3e50;\"><strong>Returned At:</strong> "
                + endTimeFormatted + "</p>"
                + "</div>"
                + "<p style=\"font-size: 14px; color: #666;\">"
                + "Your deposit will be refunded within <strong>24 hours</strong> after verification. "
                + "We appreciate your patience and understanding."
                + "</p>"
                + "<p style=\"font-size: 13px; color: #999; margin-top: 30px;\">"
                + "If you have already received your refund, please disregard this email."
                + "</p>"
                + "</div>"
                + "</html>";

        try {
            emailService.sendVerificationEmail(rental.getUser().getEmail(), subject, htmlMessage);
        } catch (MessagingException e) {
            throw new AppException(ErrorCode.SEND_EMAIL_FAILED);
        }
    }

}
