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
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RentalCheckListService {
    @Value("${price.per.battery}")
    private double pricePerBattery;
    @Value("${penalty.fee.rate}")
    private double penaltyFeeRate;

    private final RentalCheckListRepository rentalCheckListRepository;
    private final RentalCheckListMapper rentalCheckListMapper;
    private final RentalRepository rentalRepository;
    private final StaffRepository staffRepository;

    @Value("${price.day.rate}")
    private double priceDayRate;

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
            rental.getVehicle().setStatus(VehicleStatus.INUSE);
        }else{
            // lưu phí phát sinh và cập nhật status rental, vehicle
            checkList.setFee(calculateFee(rental.getId()));
            rental.setStatus(RentalStatus.PENDING_FEE);
            rental.getVehicle().setStatus(VehicleStatus.CHECKING);
        }
        rentalCheckListRepository.save(checkList);
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
}
