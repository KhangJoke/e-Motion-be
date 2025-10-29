package com.swp391.e_Motion_be.service.user;

import com.swp391.e_Motion_be.dto.requests.user.*;
import com.swp391.e_Motion_be.dto.responses.reservation.ReservationResponse;
import com.swp391.e_Motion_be.dto.responses.rental.RentalResponse;
import com.swp391.e_Motion_be.dto.responses.stats.*;
import com.swp391.e_Motion_be.dto.responses.user.PageAndFilterUserResponse;
import com.swp391.e_Motion_be.dto.responses.user.UserResponse;
import com.swp391.e_Motion_be.entity.Rental;
import com.swp391.e_Motion_be.entity.Station;
import com.swp391.e_Motion_be.entity.User;
import com.swp391.e_Motion_be.entity.Vehicle;
import com.swp391.e_Motion_be.enums.ErrorCode;
import com.swp391.e_Motion_be.enums.RentalStatus;
import com.swp391.e_Motion_be.enums.Role;
import com.swp391.e_Motion_be.enums.vehicle.VehicleStatus;
import com.swp391.e_Motion_be.exception.AppException;
import com.swp391.e_Motion_be.mapper.RentalMapper;
import com.swp391.e_Motion_be.mapper.ReservationMapper;
import com.swp391.e_Motion_be.mapper.UserMapper;
import com.swp391.e_Motion_be.repository.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
    private final ReservationMapper reservationMapper;
    private final RentalMapper rentalMapper;

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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = ((User) authentication.getPrincipal()).getEmail();
        if(user.getEmail().equals(currentUserEmail)) {
            throw new AppException(ErrorCode.CANNOT_DELETE_OWN_ACCOUNT);
        }
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

        if(input.getOldPassword().equals(input.getNewPassword())) {
            throw new AppException(ErrorCode.NEW_PASSWORD_SAME_AS_OLD);
        }

        // Save encoded new password
        user.setPassword(passwordEncoder.encode(input.getNewPassword()));
        userRepository.save(user);
    }


    public List<UserResponse> getUserByEmailContains(String email) {
        return userRepository.findByEmailContains(email).stream()
                .map(userMapper::toUserResponse)
                .toList();
    }

    public UserResponse updateProfile(UpdateProfileRequest input) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = ((User) authentication.getPrincipal()).getEmail();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTS));
        // Check phone mới có tồn tại chưa
        if(!user.getPhone().equals(input.getPhone()) && userRepository.existsByPhone(input.getPhone())) {
            throw new AppException(ErrorCode.PHONE_EXITS);
        }

        user.setFullName(input.getFullName());
        user.setPhone(input.getPhone());

        userRepository.save(user);

        return userMapper.toUserResponse(user);
    }

    public PageAndFilterUserResponse findByPageAndFilterAndSearch(PageAndFilterUserRequest request) {
        List<Role> roleList = (request.getRoleList() == null || request.getRoleList().isEmpty())
                ? List.of(Role.ROLE_USER, Role.ROLE_ADMIN, Role.ROLE_STAFF)
                : request.getRoleList();

        List<Boolean> blockedList = (request.getBlockedList() == null || request.getBlockedList().isEmpty())
                ? List.of(true, false)
                : request.getBlockedList();

        Pageable pageable = PageRequest.of(request.getPage() - 1, request.getLimit(), Sort.by("id").ascending());
        Page<User> userPage = userRepository.findByBlockedInAndRoleInAndEmailContains(blockedList, roleList, request.getSearch(), pageable);
        List<UserResponse> users = userPage.getContent().stream()
                .map(userMapper::toUserResponseWithoutDocument)
                .toList();

        return new PageAndFilterUserResponse(users, userPage.getTotalPages());
    }

    public TotalStatsResponse getTotalStatsDashboard(){
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

    public List<RevenueInYearResponse> getRevenueInYearDashboard(){
        List<Rental> rentals = rentalRepository.findAll().stream()
                .filter(r -> r.getStatus() == RentalStatus.COMPLETED)
                .filter(r -> r.getEndTime().getYear() == LocalDateTime.now().getYear())
                .toList();

        Map<Integer, Double> revenueByMonth = rentals.stream()
                .collect(Collectors.groupingBy(r -> r.getEndTime().getMonth().getValue(),
                        Collectors.summingDouble(Rental::getRentFee)));

        return IntStream.rangeClosed(1, 12)
                .mapToObj(month -> new RevenueInYearResponse(
                        Month.of(month).getValue(), // tên tháng (JANUARY, FEBRUARY,...)
                        revenueByMonth.getOrDefault(month, 0.0)
                ))
                .toList();
    }

    public List<PeakHourResponse> getPeakHoursInDayDashboard(){
        Map<Integer, Long> hourFrequency = rentalRepository.findAll().stream()
                .filter(rental -> (rental.getStatus() == RentalStatus.COMPLETED || rental.getStatus() == RentalStatus.ONGOING))
                .filter(r -> r.getStartTime().toLocalDate().isEqual(LocalDate.now()))
                .collect(Collectors.groupingBy(rental -> rental.getStartTime().getHour(), Collectors.counting()));

        return IntStream.rangeClosed(1, 23)
                .mapToObj(hour -> new PeakHourResponse(
                        hour,
                        hourFrequency.getOrDefault(hour, 0L)
                ))
                .toList();
    }

    private List<Integer> getPeakHours(List<Rental> rentals){
        Map<Integer, Long> hourFrequency = rentals.stream()
                .filter(rental -> (rental.getStatus() == RentalStatus.COMPLETED || rental.getStatus() == RentalStatus.ONGOING))
                .filter(r -> r.getEndTime().getYear() == LocalDateTime.now().getYear())
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
                .filter(r -> r.getEndTime().getYear() == LocalDateTime.now().getYear())
                .mapToDouble(Rental::getRentFee)
                .sum();
    }

    public DataAdminDashboard getDataAdminDashboard(){
        TotalStatsResponse totalStats = getTotalStatsDashboard();
        List<StationStatsResponse> stationDetails = getStationDetailDashboard();
        List<RevenueInYearResponse> revenueInYear = getRevenueInYearDashboard();
        List<PeakHourResponse> peakHours = getPeakHoursInDayDashboard();

        return new DataAdminDashboard(totalStats, stationDetails, revenueInYear, peakHours);
    }

    public UserResponse createUserByAdmin(@Valid CreateUserRequest request) {
        if(userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        if(userRepository.existsByPhone(request.getPhone())) {
            throw new AppException(ErrorCode.PHONE_EXITS);
        }

        User newUser = new User();
        newUser.setFullName(request.getFullName());
        newUser.setEmail(request.getEmail());
        newUser.setPhone(request.getPhone());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setRole(request.getRole());
        newUser.setEnabled(true);
        newUser.setBlocked(false);

        userRepository.save(newUser);

        return userMapper.toUserResponse(newUser);
    }

    public void toggleStatusUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTS));
        user.setBlocked(!user.isBlocked());
        userRepository.save(user);
    }

    public List<ReservationResponse> getReservationHistory() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = ((User) authentication.getPrincipal()).getEmail();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTS));

        return reservationRepository.findByUser_Id(user.getId()).stream()
                .map(reservationMapper::toReservationResponse)
                .toList();
    }

    public List<RentalResponse> getRentalHistory() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = ((User) authentication.getPrincipal()).getEmail();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTS));

        return rentalRepository.findByUser_Id(user.getId()).stream()
                .map(rentalMapper::toRentalResponse)
                .toList();
    }

    public UserResponse updateUserByAdmin(UpdateUserRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTS));

        if(!user.getPhone().equals(request.getPhone()) && userRepository.existsByPhone(request.getPhone())) {
            throw new AppException(ErrorCode.PHONE_EXITS);
        }
        if(request.getFullName() != null && !request.getFullName().isEmpty()) {
            user.setFullName(request.getFullName());
        }
        if(request.getPhone() != null && !request.getPhone().isEmpty()) {
            user.setPhone(request.getPhone());
        }
        if(request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if(request.getRole() != null) {
            user.setRole(request.getRole());
        }

        userRepository.save(user);

        return userMapper.toUserResponseWithoutDocument(user);
    }
}
