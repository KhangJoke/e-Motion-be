package com.swp391.e_Motion_be.service;

import com.swp391.e_Motion_be.dto.requests.checklist.RentalCheckListCreateRequest;
import com.swp391.e_Motion_be.dto.requests.checklist.RentalCheckListUpdateRequest;
import com.swp391.e_Motion_be.dto.responses.RentalCheckListResponse;
import com.swp391.e_Motion_be.entity.Rental;
import com.swp391.e_Motion_be.entity.RentalCheckList;
import com.swp391.e_Motion_be.entity.Staff;
import com.swp391.e_Motion_be.enums.CheckType;
import com.swp391.e_Motion_be.enums.ErrorCode;
import com.swp391.e_Motion_be.enums.Role;
import com.swp391.e_Motion_be.exception.AppException;
import com.swp391.e_Motion_be.mapper.RentalCheckListMapper;
import com.swp391.e_Motion_be.repository.RentalCheckListRepository;
import com.swp391.e_Motion_be.repository.RentalRepository;
import com.swp391.e_Motion_be.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RentalCheckListService {
    private final RentalCheckListRepository rentalCheckListRepository;
    private final RentalCheckListMapper rentalCheckListMapper;
    private final RentalRepository rentalRepository;
    private final StaffRepository staffRepository;

    public List<RentalCheckListResponse> getAllCheckLists() {
        return rentalCheckListRepository.findAll().stream()
                .map(rentalCheckListMapper::toRentalCheckListResponse)
                .toList();
    }

    public RentalCheckListResponse getCheckListById(Long id) {
        RentalCheckList checkList = rentalCheckListRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CHECKLIST_NOT_FOUND));
        return rentalCheckListMapper.toRentalCheckListResponse(checkList);
    }

    public List<RentalCheckListResponse> getCheckListsByRental(Long rentalId) {
        return rentalCheckListRepository.findByRental_Id(rentalId).stream()
                .map(rentalCheckListMapper::toRentalCheckListResponse)
                .toList();
    }

    public List<RentalCheckListResponse> getCheckListsByStaffEmail(String email) {
        return rentalCheckListRepository.findByStaff_User_Email(email).stream()
                .map(rentalCheckListMapper::toRentalCheckListResponse)
                .toList();
    }

    public RentalCheckListResponse createCheckList(RentalCheckListCreateRequest request, String staffEmail) {
        Rental rental = rentalRepository.findById(request.getRentalId())
                .orElseThrow(() -> new AppException(ErrorCode.RENTAL_NOT_FOUND));

        Staff staff = staffRepository.findByUser_Email(staffEmail).stream()
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_FOUND));

        RentalCheckList checkList = rentalCheckListMapper.toCheckListEntity(request);
        checkList.setRental(rental);
        checkList.setStaff(staff);
        checkList.setCreatedAt(LocalDateTime.now());

        rentalCheckListRepository.save(checkList);

        return rentalCheckListMapper.toRentalCheckListResponse(checkList);
    }


    public RentalCheckListResponse updateCheckList(RentalCheckListUpdateRequest request, String email) {
        RentalCheckList checkList = rentalCheckListRepository.findById(request.getId())
                .orElseThrow(() -> new AppException(ErrorCode.CHECKLIST_NOT_FOUND));

        String staffEmail = checkList.getStaff().getUser().getEmail();
        Role userRole = checkList.getStaff().getUser().getRole();

        if (!(userRole.equals(Role.ROLE_ADMIN) || staffEmail.equals(email))) {
            throw new AppException(ErrorCode.CHECKLIST_UNAUTHORIZED);
        }


        checkList.setType(request.getType());
        checkList.setCurrentBattery(request.getCurrentBattery());

        rentalCheckListRepository.save(checkList);
        return rentalCheckListMapper.toRentalCheckListResponse(checkList);
    }

    public void deleteCheckList(Long id, String email) {
        RentalCheckList checkList = rentalCheckListRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CHECKLIST_NOT_FOUND));

        String staffEmail = checkList.getStaff().getUser().getEmail();
        Role userRole = checkList.getStaff().getUser().getRole();

        if (!(userRole.equals(Role.ROLE_ADMIN) || staffEmail.equals(email))) {
            throw new AppException(ErrorCode.CHECKLIST_UNAUTHORIZED);
        }
        rentalCheckListRepository.delete(checkList);
    }

    public RentalCheckListResponse createCheckIn(RentalCheckListCreateRequest request, String email) {
        request.setType(CheckType.CHECK_IN);
        RentalCheckListResponse response = createCheckList(request, email);
        return response;
    }

    public RentalCheckListResponse createCheckOut(RentalCheckListCreateRequest request, String email) {
        request.setType(CheckType.CHECK_OUT);
        createCheckList(request, email);
        calculateFee(request.getRentalId());

        // Lấy lại check-out mới nhất để trả về
        RentalCheckList checkOut = rentalCheckListRepository.findByRental_Id(request.getRentalId()).stream()
                .filter(c -> c.getType() == CheckType.CHECK_OUT)
                .reduce((first, second) -> second)
                .orElseThrow(() -> new AppException(ErrorCode.CHECKLIST_NOT_FOUND));

        return rentalCheckListMapper.toRentalCheckListResponse(checkOut);
    }

    public void calculateFee(Long rentalId){
        List<RentalCheckList> checkLists = rentalCheckListRepository.findByRental_Id(rentalId);
        RentalCheckList checkIn = checkLists.stream()
                .filter(c -> c.getType() == CheckType.CHECK_IN)
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.CHECKLIST_NOT_FOUND));

        RentalCheckList checkOut = checkLists.stream()
                .filter(c -> c.getType() == CheckType.CHECK_OUT)
                .reduce((first, second) -> second)
                .orElseThrow(() -> new AppException(ErrorCode.CHECKLIST_NOT_FOUND));

        double fee = 0.0;

        double batteryDiff = checkOut.getCurrentBattery() - checkIn.getCurrentBattery();
        if(batteryDiff < 0){
            double percentUsed = Math.abs(batteryDiff) * 100;
            fee += percentUsed * 12000; // 12k for each percent of battery used
        }

        Rental rental = checkOut.getRental();
        double pricePerDay = rental.getVehicle().getPricePerDay();
        double pricePerHour = rental.getVehicle().getPricePerHour();

        LocalDateTime actualReturnTime = checkOut.getCreatedAt();
        LocalDateTime expectedReturnTime = rental.getEndTime();

        if (actualReturnTime.isAfter(expectedReturnTime)) {
            long late = Math.max(0, Duration.between(expectedReturnTime, actualReturnTime).toMinutes());
            double hoursLate = late / 60.0;

            if (hoursLate <= 4) {
                fee += hoursLate * pricePerHour;
            } else if (hoursLate <= 8) {
                fee += pricePerDay * 0.5;
            } else {
                fee += pricePerDay;
            }
        }

        checkOut.setFee(fee);
        rentalCheckListRepository.save(checkOut);
    }
}
