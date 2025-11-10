package com.swp391.e_Motion_be.service;

import com.swp391.e_Motion_be.dto.requests.ImgVehicle.ImgVehicleCreationRequest;
import com.swp391.e_Motion_be.dto.requests.vehicle.*;
import com.swp391.e_Motion_be.dto.responses.ImgVehicleResponse;
import com.swp391.e_Motion_be.dto.responses.rental.PageAndFilterRentalResponse;
import com.swp391.e_Motion_be.dto.responses.rental.RentalListResponse;
import com.swp391.e_Motion_be.dto.responses.user.PageAndFilterUserResponse;
import com.swp391.e_Motion_be.dto.responses.user.UserResponse;
import com.swp391.e_Motion_be.dto.responses.vehicle.*;
import com.swp391.e_Motion_be.entity.*;
import com.swp391.e_Motion_be.enums.ErrorCode;
import com.swp391.e_Motion_be.enums.RentalStatus;
import com.swp391.e_Motion_be.enums.ReservationStatus;
import com.swp391.e_Motion_be.enums.Role;
import com.swp391.e_Motion_be.enums.vehicle.FeeType;
import com.swp391.e_Motion_be.enums.vehicle.VehicleBrand;
import com.swp391.e_Motion_be.enums.vehicle.VehicleCategory;
import com.swp391.e_Motion_be.enums.vehicle.VehicleStatus;
import com.swp391.e_Motion_be.exception.AppException;
import com.swp391.e_Motion_be.mapper.ImgVehicleMapper;
import com.swp391.e_Motion_be.mapper.VehicleMapper;
import com.swp391.e_Motion_be.repository.RentalRepository;
import com.swp391.e_Motion_be.repository.ReservationRepository;
import com.swp391.e_Motion_be.repository.StationRepository;
import com.swp391.e_Motion_be.repository.VehicleRepository;
import com.swp391.e_Motion_be.service.user.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final ImgVehicleService imgVehicleService;
    private final ImgVehicleMapper imgVehicleMapper;
    private final VehicleMapper vehicleMapper;
    private final StationRepository stationRepository;
    private final RentalRepository rentalRepository;
    private final ReservationRepository reservationRepository;

    private final RentalService rentalService;
    private final UserService userService;

    @Value("${vat.percentage}")
    private double vatPercentage;

    @Value("${hold.fee.value}")
    private double holdCarFee;


    // Find by ID
    public VehicleDetailResponse findVehicleById(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_NOT_EXIST));

        List<VehicleListResponse> similarVehicles = vehicleRepository.findByCategory(vehicle.getCategory())
                .stream()
                .filter(v -> !v.getId().equals(vehicle.getId()))
                .map(v -> vehicleMapper.toVehicleListResponse(v, 4))
                .toList();
        VehicleDetailResponse vehicleDetailResponse = vehicleMapper.toVehicleDetailResponse(vehicle);
        vehicleDetailResponse.setSimilarVehicleList(similarVehicles);
        return vehicleDetailResponse;
    }

    // Find by PlateNumber
    public VehicleDetailResponse findVehicleByPlateNumber(String plateNumber) {
        Vehicle vehicle = vehicleRepository.findByPlateNumber(plateNumber)
                .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_NOT_EXIST));
        return vehicleMapper.toVehicleDetailResponse(vehicle);
    }

    // Find By Brand
    public List<VehicleListResponse> findVehicleByBrand(String brand) {
        VehicleBrand vehicleBrand;
        try {
            vehicleBrand = VehicleBrand.valueOf(brand.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.INVALID_VEHICLE_BRAND);
        }

        List<Vehicle> vehicles = vehicleRepository.findByBrandAndStatus(vehicleBrand,VehicleStatus.AVAILABLE );

        if (vehicles == null || vehicles.isEmpty()) {
            throw new AppException(ErrorCode.VEHICLE_NOT_EXIST);
        }

        return vehicles.stream()
                .map(v -> vehicleMapper.toVehicleListResponse(v, 4))
                .collect(Collectors.toList());
    }


    // Find all
    public List<VehicleListResponse> findAllVehicles() {
        List<Vehicle> vehicles = vehicleRepository.findByStatus(VehicleStatus.AVAILABLE);

        if (vehicles.isEmpty()) {
            throw new AppException(ErrorCode.VEHICLE_NOT_EXIST);
        }

        return vehicles.stream()
                .map(v -> vehicleMapper.toVehicleListResponse(v, 4))
                .collect(Collectors.toList());
    }

    // CREATE
    public VehicleDetailResponse createVehicle(VehicleCreationRequest request) {
        //Check Station is FOUNd or NOT
        Station station = stationRepository.findById(request.getStationId())
                .orElseThrow(() -> new AppException(ErrorCode.STATION_NOT_FOUND));

        if (vehicleRepository.findByPlateNumber(request.getPlateNumber()).isPresent()) {
            throw new AppException(ErrorCode.VEHICLE_EXIST);
        }
        Vehicle vehicle = vehicleMapper.toVehicleEntity(request);

        vehicle.setStation(station);

        //SAVE
        vehicleRepository.save(vehicle);

        VehicleDetailResponse vehicleDetailResponse = vehicleMapper.toVehicleDetailResponse(vehicle);
        List<ImgVehicleResponse> imageResponses = imgVehicleService.createMultipleImagesForVehicle(vehicle,request.getImages());

        vehicleDetailResponse.setImages(imageResponses);

        return vehicleDetailResponse;
    }

    private double roundToNearest10(double value) {
        return Math.round(value / 10.0) * 10.0;
    }
    // UPDATE
    @Transactional
    public VehicleDetailResponse updateVehicle(Long id, VehicleUpdateRequest request) {

        Vehicle existing = vehicleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_NOT_EXIST));

        if (!existing.getPlateNumber().equals(request.getPlateNumber())
                && vehicleRepository.findByPlateNumber(request.getPlateNumber()).isPresent()) {
            throw new AppException(ErrorCode.VEHICLE_EXIST);
        }

        vehicleMapper.updateVehicleFromRequest(existing, request);

        if (request.getStationId() != null) {
            Station station = stationRepository.findById(request.getStationId())
                    .orElseThrow(() -> new AppException(ErrorCode.STATION_NOT_FOUND));
            existing.setStation(station);
        }

        // No need to call save() — transaction will automatically flush changes
        return vehicleMapper.toVehicleDetailResponse(existing);
    }

    //DELETE
    public void deleteVehicleById(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_NOT_EXIST));
        vehicle.setDelete(true);
        vehicleRepository.save(vehicle);
    }

    public List<VehicleScheduleResponse> getVehicleSchedule(Long vid){
        List<VehicleScheduleResponse> schedules = new ArrayList<>();
        rentalRepository.findByVehicle_Id(vid).ifPresent(rental ->
                schedules.add(new VehicleScheduleResponse(rental.getStartTime(), rental.getEndTime()))
        );
        List<Reservation> reservations = reservationRepository.findByVehicle_IdAndStatusIn(vid, List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRM));
        schedules.addAll(reservations.stream()
                .map(reservation -> new VehicleScheduleResponse(reservation.getStartTime(), reservation.getEndTime()))
                .toList());
        return schedules;
    }

    public boolean isAvailable(long id) {
        List<Vehicle> availableVehicle = vehicleRepository.findByStatus(VehicleStatus.AVAILABLE);
        return availableVehicle.stream().anyMatch(v -> v.getId() == id);
    }

    public List<FeeResponse> getListFeeBooking(Long vid, String start, String end){
        Vehicle vehicle = vehicleRepository.findById(vid)
                .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_NOT_EXIST));
        List<FeeResponse> fees = new ArrayList<>();

        double bookingFeeValue = rentalService.calculateRentalFee(vehicle, LocalDateTime.parse(start), LocalDateTime.parse(end));

        FeeResponse bookingFee = new FeeResponse("Phí thuê xe", FeeType.BOOKING_FEE, bookingFeeValue);
        FeeResponse deposit = new FeeResponse("Tiền cọc xe", FeeType.DEPOSIT, vehicle.getDepositFee());
        FeeResponse holdCar = new FeeResponse("Tiền giữ chỗ", FeeType.HOLD_CAR, holdCarFee);
        FeeResponse total = new FeeResponse("Tổng tiền phải trả", FeeType.TOTAL_AMOUNT,
                bookingFeeValue + vehicle.getDepositFee());
        fees.add(bookingFee);
        fees.add(deposit);
        fees.add(holdCar);
        fees.add(total);

        return fees;
    }

    public List<VehicleQuantityEachStatusResponse> getVehicleQuantityEachStatusOfStation(Long stationId){
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new AppException(ErrorCode.STATION_NOT_FOUND));

        return Stream.of(VehicleStatus.AVAILABLE, VehicleStatus.ONGOING, VehicleStatus.MAINTAINED)
                .map(status ->
                        new VehicleQuantityEachStatusResponse(
                                vehicleRepository.countByStation_IdAndStatus(station.getId(), status)
                                , status))
                .toList();
    }

    public PageAndFilterVehicleResponse findByPageAndFilterAndSearch(PageAndFilterVehicleRequest request) {
        List<VehicleBrand> brandsList = (request.getBrands() == null || request.getBrands().isEmpty())
                ? Arrays.asList(VehicleBrand.values())
                : request.getBrands();

        List<VehicleCategory> categoryList = (request.getCategories() == null || request.getCategories().isEmpty())
                ? Arrays.asList(VehicleCategory.values())
                : request.getCategories();

        Pageable pageable = PageRequest.of(request.getPage() - 1, request.getLimit(), Sort.by("id").ascending());

        long hours = Duration.between(request.getStartTime(), request.getEndTime()).toHours();

        List<Long> ids = vehicleRepository
                .findByStation_CityAndStatusIn(
                        request.getCity(),
                        List.of(VehicleStatus.AVAILABLE, VehicleStatus.ONGOING)
                ).stream()
                .filter(v -> v.getReservations().stream()
                        .filter(r -> r.getStatus() != ReservationStatus.COMPLETED && r.getStatus() != ReservationStatus.CANCELLED && r.getStatus() != ReservationStatus.FAILED)
                        .noneMatch(r -> r.getStartTime().isBefore(request.getEndTime()) &&
                                r.getEndTime().isAfter(request.getStartTime()))
                        &&
                        v.getRentals().stream()
                                .filter(r -> r.getStatus() != RentalStatus.COMPLETED && r.getStatus() != RentalStatus.CANCELLED)
                                .noneMatch(r -> r.getStartTime().isBefore(request.getEndTime()) &&
                                        r.getEndTime().isAfter(request.getStartTime()))
                )
                .map(Vehicle::getId)
                .toList();

        Page<Vehicle> vehiclePage = vehicleRepository.findByIdInAndBrandInAndCategoryInAndNameContains(ids, brandsList, categoryList, request.getSearch(), pageable);
        List<VehicleListResponse> vehicles = vehiclePage.getContent().stream()
                .map(v -> vehicleMapper.toVehicleListResponse(v, hours))
                .toList();

        return new PageAndFilterVehicleResponse(vehicles, vehiclePage.getTotalPages());
    }

    public PageAndFilterVehicleResponse manageCar(PageAndFilterManageVehicleRequest request) {
        User user = userService.currentUser();

        List<VehicleStatus> statusList = (request.getStatus() == null || request.getStatus().isEmpty())
                ? Arrays.asList(VehicleStatus.values())
                : request.getStatus();

        Pageable pageable = PageRequest.of(request.getPage() - 1, request.getLimit(), Sort.by("id").descending());
        Page<Vehicle> vehiclePage;

        if(user.getRole() == Role.ROLE_STAFF){
            Station station = stationRepository.findById(user.getStaff().getStation().getId())
                    .orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_FOUND));
            vehiclePage = vehicleRepository.findByStatusInAndNameContainsAndStation_Id(statusList, request.getSearch(), station.getId(), pageable);
        }else{
            vehiclePage = vehicleRepository.findByStatusInAndNameContains(statusList, request.getSearch(), pageable);
        }

        List<VehicleListResponse> vehicles = vehiclePage.getContent().stream()
                .map(v -> vehicleMapper.toVehicleListResponse(v, 4))
                .toList();

        return new PageAndFilterVehicleResponse(vehicles, vehiclePage.getTotalPages());
    }
}