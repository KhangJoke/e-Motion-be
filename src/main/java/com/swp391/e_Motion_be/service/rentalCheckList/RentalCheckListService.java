package com.swp391.e_Motion_be.service.rentalCheckList;

import com.swp391.e_Motion_be.dto.requests.checklist.RentalCheckListCreateRequest;
import com.swp391.e_Motion_be.dto.requests.checklist.RentalCheckListUpdateRequest;
import com.swp391.e_Motion_be.dto.responses.RentalCheckListResponse;
import com.swp391.e_Motion_be.entity.Rental;
import com.swp391.e_Motion_be.entity.RentalCheckList;
import com.swp391.e_Motion_be.entity.Staff;
import com.swp391.e_Motion_be.enums.ErrorCode;
import com.swp391.e_Motion_be.enums.Role;
import com.swp391.e_Motion_be.exception.AppException;
import com.swp391.e_Motion_be.mapper.RentalCheckListMapper;
import com.swp391.e_Motion_be.repository.RentalCheckListRepository;
import com.swp391.e_Motion_be.repository.RentalRepository;
import com.swp391.e_Motion_be.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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


        checkList.setType(request.getCheckType());
        checkList.setImgUrl(request.getImgUrl());
        checkList.setKilometers(request.getKilometers());
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
}
