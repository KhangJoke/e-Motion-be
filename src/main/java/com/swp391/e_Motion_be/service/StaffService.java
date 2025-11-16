package com.swp391.e_Motion_be.service;

import com.swp391.e_Motion_be.dto.requests.staff.DispatchStaffRequest;
import com.swp391.e_Motion_be.dto.responses.StaffResponse;
import com.swp391.e_Motion_be.entity.Staff;
import com.swp391.e_Motion_be.entity.Station;
import com.swp391.e_Motion_be.entity.User;
import com.swp391.e_Motion_be.enums.ErrorCode;
import com.swp391.e_Motion_be.enums.Role;
import com.swp391.e_Motion_be.exception.AppException;
import com.swp391.e_Motion_be.mapper.StaffMapper;
import com.swp391.e_Motion_be.repository.StaffRepository;
import com.swp391.e_Motion_be.repository.StationRepository;
import com.swp391.e_Motion_be.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StaffService {
    private final StaffRepository staffRepository;
    private final StationRepository stationRepository;
    private final UserRepository userRepository;
    private final StaffMapper staffMapper;

    public Staff createStaff(User user, Long stationId){
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new AppException(ErrorCode.STATION_NOT_FOUND));

        if(staffRepository.existsByUser_Email(user.getEmail())){
            throw new AppException(ErrorCode.USER_ALREADY_ASSIGNED_AS_STAFF);
        }

        Staff staff = new Staff();
        staff.setUser(user); // nếu chưa có user thì sẽ tự lưu với persist
        staff.setStation(station);

        return staffRepository.save(staff);
    }

    public void updateStaff(String email, Long stationId){
        Staff staff = staffRepository.findByUser_EmailAndIsDeleteFalse(email)
                .orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_FOUND));

        Station newStation = stationRepository.findById(stationId)
                .orElseThrow(() -> new AppException(ErrorCode.STATION_NOT_FOUND));

        staff.setStation(newStation);
        staffRepository.save(staff);
    }

    public void deleteStaff(String email) {
        Staff staff = staffRepository.findByUser_EmailAndIsDeleteFalse(email)
                .orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_FOUND));

        User user = staff.getUser();
        staff.setDelete(true);
        staffRepository.save(staff);
        user.setRole(Role.ROLE_USER);
        userRepository.save(user);
    }

    public List<StaffResponse> getAllStaffs(){
        return staffRepository.findAll().stream().map(staffMapper::toStaffResponse).toList();
    }

    public StaffResponse getStaffByUserEmail(String email){
        Staff staff = staffRepository.findByUser_EmailAndIsDeleteFalse(email)
                .orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_FOUND));

        return staffMapper.toStaffResponse(staff);
    }

    @Transactional
    public void dispatchStaffs(DispatchStaffRequest request) {
        Station station = stationRepository.findById(request.getStationId())
                .orElseThrow(() -> new AppException(ErrorCode.STATION_NOT_FOUND));
        for (long id : request.getStaffIds()) {
            Staff staff = staffRepository.findById(id)
                    .orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_FOUND));
            staff.setStation(station);
            staffRepository.save(staff);
        }
    }
}
