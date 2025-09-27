package com.swp391.e_Motion_be.service.staff;

import com.swp391.e_Motion_be.dto.requests.staff.StaffCreationRequest;
import com.swp391.e_Motion_be.dto.requests.staff.StaffUpdateRequest;
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

    public StaffResponse createStaff(StaffCreationRequest request){
        User user = userRepository.findById(request.getUserId()).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTS));

        Station station = stationRepository.findById(request.getStationId()).orElseThrow(() -> new AppException(ErrorCode.STATION_NOT_FOUND));

        if(staffRepository.existsByUser(user)){
            throw new AppException(ErrorCode.USER_ALREADY_ASSIGNED_AS_STAFF);
        }

        user.setRole(Role.ROLE_STAFF);
        userRepository.save(user);

        Staff staff = staffMapper.staffToEntity(request);
        staff.setUser(user);
        staff.setStation(station);
        staffRepository.save(staff);
        return staffMapper.toStaffResponse(staff);
    }

    public StaffResponse updateStaffById(Long id, StaffUpdateRequest request){
        Staff staff = staffRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_FOUND));
        staff.setStation(stationRepository.findById(request.getStationId()).orElseThrow(() -> new AppException(ErrorCode.STATION_NOT_FOUND)));
        staffRepository.save(staff);

        return staffMapper.toStaffResponse(staff);
    }

    public void deleteStaffById(Long id){
        Staff staff = staffRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_FOUND));
        User user = staff.getUser();
        if(user != null){
            user.setRole(Role.ROLE_USER);
            userRepository.save(user);
        }

        staffRepository.delete(staff);
    }

    public List<StaffResponse> getAllStaffs(){
        return staffRepository.findAll().stream().map(staffMapper::toStaffResponse).toList();
    }

    public StaffResponse getStaffById(Long id){
        Staff staff = staffRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_FOUND));
        return staffMapper.toStaffResponse(staff);
    }
}
