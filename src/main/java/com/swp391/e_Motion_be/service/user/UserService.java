package com.swp391.e_Motion_be.service.user;

import com.swp391.e_Motion_be.dto.requests.user.ChangePasswordUserRequest;
import com.swp391.e_Motion_be.dto.requests.user.UpdateProfileRequest;
import com.swp391.e_Motion_be.dto.responses.stats.StationStatsResponse;
import com.swp391.e_Motion_be.dto.responses.stats.TotalStatsResponse;
import com.swp391.e_Motion_be.dto.responses.UserResponse;
import com.swp391.e_Motion_be.entity.Rental;
import com.swp391.e_Motion_be.entity.Station;
import com.swp391.e_Motion_be.entity.User;
import com.swp391.e_Motion_be.entity.Vehicle;
import com.swp391.e_Motion_be.enums.ErrorCode;
import com.swp391.e_Motion_be.enums.RentalStatus;
import com.swp391.e_Motion_be.enums.Role;
import com.swp391.e_Motion_be.enums.vehicle.VehicleStatus;
import com.swp391.e_Motion_be.exception.AppException;
import com.swp391.e_Motion_be.mapper.UserMapper;
import com.swp391.e_Motion_be.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final ReservationRepository reservationRepository;
    private final RentalRepository rentalRepository;

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final StationRepository stationRepository;

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toUserResponse)
                .toList();
    }

    public UserResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = ((User) authentication.getPrincipal()).getEmail();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTS));

        return userMapper.toUserResponse(user);
    }

    public UserResponse getUserByEmail(String email) {
        return userMapper.toUserResponse(userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTS)));
    }

    public void deleteUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTS));
        userRepository.delete(user);
    }

    public void changePassword(ChangePasswordUserRequest input) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = ((User) authentication.getPrincipal()).getEmail();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTS));

        // Check old password
        if (!passwordEncoder.matches(input.getOldPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.OLD_PASSWORD_NOT_MATCH);
        }

        // Check confirm password
        if (!input.getNewPassword().equals(input.getConfirmNewPassword())) {
            throw new AppException(ErrorCode.CONFIRM_PASSWORD_NOT_MATCH);
        }

        // Save encoded new password
        user.setPassword(passwordEncoder.encode(input.getNewPassword()));
        userRepository.save(user);
    }


    public UserResponse updateProfile(UpdateProfileRequest input) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = ((User) authentication.getPrincipal()).getEmail();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTS));

        user.setFullName(input.getFullName());
        user.setPhone(input.getPhone());

        userRepository.save(user);

        return userMapper.toUserResponse(user);
    }

    public TotalStatsResponse getDataAdminDashboard(){
        List<Rental> rentals = rentalRepository.findAll();
        List<Vehicle> vehicles = vehicleRepository.findAll();

        long totalUsers = userRepository.countByRole(Role.ROLE_USER);
        long totalVehicles = vehicles.size();
        long totalReservations = reservationRepository.count();
        double totalRevenue = calculateRevenue(rentals);
        double usageRate = calculateUsageRate(vehicles); // tỷ lệ xe đang sử dụng / tổng số xe
        List<Integer> peakHours = getPeakHours(rentals);

        return new TotalStatsResponse(
                totalUsers,
                totalVehicles,
                totalReservations,
                rentals.size(),
                totalRevenue,
                usageRate,
                peakHours);
    }

    public List<StationStatsResponse> getStationDetailDashboard(){
        List<Station> stations = stationRepository.findAll();
        return stations.stream().map(station -> {
            List<Rental> rentals = rentalRepository.findByStation_Id(station.getId());

            long totalVehicles = vehicleRepository.countByStation_Id(station.getId());
            double usageRate = calculateUsageRate(vehicleRepository.findByStation_Id(station.getId()));
            double revenue = calculateRevenue(rentals);
            List<Integer> peakHours = getPeakHours(rentals);

            return new StationStatsResponse(
                    station.getName(),
                    revenue,
                    totalVehicles,
                    rentals.size(),
                    usageRate,
                    peakHours
            );
        }).toList();
    }

    private List<Integer> getPeakHours(List<Rental> rentals){
        Map<Integer, Long> hourFrequency = rentals.stream()
                .filter(rental -> (rental.getStatus() == RentalStatus.COMPLETED || rental.getStatus() == RentalStatus.ONGOING))
                .collect(Collectors.groupingBy(rental -> rental.getStartTime().getHour(), Collectors.counting()));
        long maxCount = hourFrequency.values().stream()
                .max(Long::compareTo).orElse(0L);
        return hourFrequency.entrySet().stream()
                .filter(entry -> entry.getValue() == maxCount)
                .map(Map.Entry::getKey)
                .toList();
    }

    // Helper để tính tỷ lệ sử dụng
    private double calculateUsageRate(List<Vehicle> vehicles) {
        if (vehicles.isEmpty()) return 0;
        long inUse = vehicles.stream().filter(v -> v.getStatus() == VehicleStatus.ONGOING || v.getStatus() == VehicleStatus.CHECKING).count();
        return ((double) inUse / vehicles.size()) * 100;
    }

    // Helper để tính tổng doanh thu
    private double calculateRevenue(List<Rental> rentals) {
        return rentals.stream()
                .filter(r -> r.getStatus() == RentalStatus.COMPLETED)
                .mapToDouble(Rental::getRentFee)
                .sum();
    }
}
