package com.swp391.e_Motion_be.service;

import com.swp391.e_Motion_be.dto.requests.vehicle.PageAndFilterManageVehicleRequest;
import com.swp391.e_Motion_be.dto.requests.vehicle.PageAndFilterVehicleRequest;
import com.swp391.e_Motion_be.dto.requests.vehicle.VehicleCreationRequest;
import com.swp391.e_Motion_be.dto.requests.vehicle.VehicleUpdateRequest;
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

    @Value("${price.8h.rate}")
    private double price8hRate;

    @Value("${price.12h.rate}")
    private double price12hRate;

    @Value("${price.day.rate}")
    private double priceDayRate;



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

    public VehicleUpdateResponse getUpdateCarById(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_NOT_EXIST));

        return vehicleMapper.toVehicleUpdateResponse(vehicle);
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


    //Find 16 for home page
    public List<VehicleListResponse> findVehiclesForHomePage() {
        List<Vehicle> vehicles = vehicleRepository.findTop16ByStatusOrderByIdDesc(VehicleStatus.AVAILABLE);
        if (vehicles.isEmpty()) {
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
    public VehicleListResponse createVehicle(VehicleCreationRequest request) {
        //Check Station is FOUNd or NOT
        Station station = stationRepository.findById(request.getStationId())
                .orElseThrow(() -> new AppException(ErrorCode.STATION_NOT_FOUND));

        if (vehicleRepository.findByPlateNumber(request.getPlateNumber()).isPresent()) {
            throw new AppException(ErrorCode.VEHICLE_EXIST);
        }
        Vehicle vehicle = vehicleMapper.toVehicleEntity(request);
        vehicle.setPoint(request.getPoint());
        vehicle.setStation(station);
        //SAVE
        vehicleRepository.save(vehicle);

        VehicleListResponse vehicleListResponse = vehicleMapper.toVehicleListResponse(vehicle, 4);
        imgVehicleService.createMultipleImagesForVehicle(vehicle,request.getImages());

        return vehicleListResponse;
    }

    // UPDATE
    @Transactional
    public VehicleDetailResponse updateVehicle(VehicleUpdateRequest request) {

        Vehicle vehicle = vehicleRepository.findById(request.getId())
                .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_NOT_EXIST));

        if (!vehicle.getPlateNumber().equals(request.getPlateNumber())
                && vehicleRepository.findByPlateNumber(request.getPlateNumber()).isPresent()) {
            throw new AppException(ErrorCode.VEHICLE_EXIST);
        }

        vehicleMapper.updateVehicleFromRequest(vehicle, request);

        if (request.getStationId() != null) {
            Station station = stationRepository.findById(request.getStationId())
                    .orElseThrow(() -> new AppException(ErrorCode.STATION_NOT_FOUND));
            vehicle.setStation(station);
        }

        // No need to call save() — transaction will automatically flush changes
        return vehicleMapper.toVehicleDetailResponse(vehicle);
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


    public List<FeeResponse> getListFeeBooking(Long vid, String start, String end){
        Vehicle vehicle = vehicleRepository.findById(vid)
                .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_NOT_EXIST));
        List<FeeResponse> fees = new ArrayList<>();

        long hours = Duration.between( LocalDateTime.parse(start), LocalDateTime.parse(end)).toHours();
        double feePoint = vehicle.getPoint() * ((double) hours /4);

        double bookingFeeValue = rentalService.calculateRentalFee(vehicle, LocalDateTime.parse(start), LocalDateTime.parse(end));

        FeeResponse bookingFee = new FeeResponse("Phí thuê xe", FeeType.BOOKING_FEE, bookingFeeValue);
//        FeeResponse pointFee = new FeeResponse("Giảm giá", FeeType.BOOKING_FEE, feePoint * 1000);
        FeeResponse deposit = new FeeResponse("Tiền cọc xe", FeeType.DEPOSIT, vehicle.getDepositFee());
        FeeResponse holdCar = new FeeResponse("Tiền giữ chỗ", FeeType.HOLD_CAR, holdCarFee);
        FeeResponse total = new FeeResponse("Tổng tiền phải trả", FeeType.TOTAL_AMOUNT,
                bookingFeeValue + vehicle.getDepositFee());

        fees.add(bookingFee);
//        fees.add(pointFee);
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

    public PageAndFilterVehicleResponse findAvailableVehicles(PageAndFilterVehicleRequest request) {
        Integer seats = request.getSeats();
        List<VehicleBrand> brandsList = (request.getBrands() == null || request.getBrands().isEmpty())
                ? Arrays.asList(VehicleBrand.values())
                : request.getBrands();

        List<VehicleCategory> categoryList = (request.getCategories() == null || request.getCategories().isEmpty())
                ? Arrays.asList(VehicleCategory.values())
                : request.getCategories();

        Pageable pageable = PageRequest.of(request.getPage() - 1, request.getLimit(), Sort.by("id").ascending());

        long hours = Duration.between(request.getStartTime(), request.getEndTime()).toHours();

        // --- Lấy list xe theo city hoặc station ---
        List<Vehicle> vehicles;
        if(request.getStationId() != null){
            Station station = stationRepository.findById(request.getStationId())
                    .orElseThrow(() -> new AppException(ErrorCode.STATION_NOT_FOUND));

            if (!station.getCity().equals(request.getCity())) {
                throw new AppException(ErrorCode.STATION_NOT_IN_CITY);
            }
            vehicles = vehicleRepository.findByStation_IdAndStatusIn(
                    request.getStationId(),
                    List.of(VehicleStatus.AVAILABLE, VehicleStatus.ONGOING));
        }else{
            vehicles = vehicleRepository.findByStation_CityAndStatusIn(
                    request.getCity(),
                    List.of(VehicleStatus.AVAILABLE, VehicleStatus.ONGOING)
            );
        }
        // --- Lọc những xe còn trống trong khung giờ ---
        List<Long> availableIdList = vehicles
                .stream()
                .filter(v -> v.getReservations().stream()
                        .filter(r -> r.getStatus() != ReservationStatus.COMPLETED
                                && r.getStatus() != ReservationStatus.CANCELLED
                                && r.getStatus() != ReservationStatus.FAILED)
                        .noneMatch(r -> r.getStartTime().isBefore(request.getEndTime())
                                && r.getEndTime().isAfter(request.getStartTime()))
                        &&
                        v.getRentals().stream()
                                .filter(r -> r.getStatus() != RentalStatus.COMPLETED
                                        && r.getStatus() != RentalStatus.CANCELLED)
                                .noneMatch(r -> r.getStartTime().isBefore(request.getEndTime())
                                        && r.getEndTime().isAfter(request.getStartTime()))
                )
                // --- Lọc theo giá (theo giờ thực tế user chọn) ---
                .filter(v -> {
                    double priceValue;
                    if(hours < 8){
                        priceValue = v.getPricePer4Hours();
                    }else if(hours < 12){
                        priceValue = v.getPricePer4Hours() * price8hRate;
                    }else if(hours < 24){
                        priceValue = v.getPricePer4Hours() * price12hRate;
                    }else{
                        priceValue = v.getPricePer4Hours() * priceDayRate;
                    }
                    if(request.getMinPrice() != null && request.getMaxPrice() != null){
                        return priceValue >= request.getMinPrice()
                                && priceValue <= request.getMaxPrice();
                    }else if(request.getMinPrice() != null){
                        return priceValue >= request.getMinPrice();
                    }else if(request.getMaxPrice() != null){
                        return priceValue <= request.getMaxPrice();
                    }
                    return true;
                })
                .map(Vehicle::getId)
                .toList();

        Page<Vehicle> vehiclePage;
        if(seats != null){
            vehiclePage = vehicleRepository.findByIdInAndBrandInAndCategoryInAndSeatsAndNameContainingIgnoreCase(availableIdList, brandsList, categoryList, seats, request.getSearch(), pageable);
        }else{
            vehiclePage = vehicleRepository.findByIdInAndBrandInAndCategoryInAndNameContains(availableIdList, brandsList, categoryList, request.getSearch(), pageable);
        }

        List<VehicleListResponse> vehicleResponse = vehiclePage.getContent().stream()
                .map(v -> vehicleMapper.toVehicleListResponse(v, hours))
                .toList();
        return new PageAndFilterVehicleResponse(vehicleResponse, vehiclePage.getTotalPages());
    }

    public PageAndFilterVehicleResponse findUnavailableVehicles(PageAndFilterVehicleRequest request) {
        Integer seats = request.getSeats();
        List<VehicleBrand> brandsList = (request.getBrands() == null || request.getBrands().isEmpty())
                ? Arrays.asList(VehicleBrand.values())
                : request.getBrands();

        List<VehicleCategory> categoryList = (request.getCategories() == null || request.getCategories().isEmpty())
                ? Arrays.asList(VehicleCategory.values())
                : request.getCategories();

        Pageable pageable = PageRequest.of(request.getPage() - 1, request.getLimit(), Sort.by("id").ascending());

        long hours = Duration.between(request.getStartTime(), request.getEndTime()).toHours();

        List<Vehicle> vehicles;
        if(request.getStationId() != null){
            Station station = stationRepository.findById(request.getStationId())
                    .orElseThrow(() -> new AppException(ErrorCode.STATION_NOT_FOUND));

            if (!station.getCity().equals(request.getCity())) {
                throw new AppException(ErrorCode.STATION_NOT_IN_CITY);
            }
            vehicles = vehicleRepository.findByStation_IdAndStatusIn(
                    request.getStationId(),
                    List.of(VehicleStatus.AVAILABLE, VehicleStatus.ONGOING));
        }else{
            vehicles = vehicleRepository.findByStation_CityAndStatusIn(
                    request.getCity(),
                    List.of(VehicleStatus.AVAILABLE, VehicleStatus.ONGOING)
            );
        }

        List<Long> unavailableIdList  = vehicles
                .stream()
                .filter(v -> v.getReservations().stream()
                        .filter(r -> r.getStatus() != ReservationStatus.COMPLETED
                                && r.getStatus() != ReservationStatus.CANCELLED
                                && r.getStatus() != ReservationStatus.FAILED)
                        .anyMatch(r -> r.getStartTime().isBefore(request.getEndTime())
                                && r.getEndTime().isAfter(request.getStartTime()))
                        ||
                        v.getRentals().stream()
                                .filter(r -> r.getStatus() != RentalStatus.COMPLETED
                                        && r.getStatus() != RentalStatus.CANCELLED)
                                .anyMatch(r -> r.getStartTime().isBefore(request.getEndTime())
                                        && r.getEndTime().isAfter(request.getStartTime()))
                )
                // --- Lọc theo giá ---
                .filter(v -> {
                    double priceValue;
                    if(hours < 8){
                        priceValue = v.getPricePer4Hours();
                    }else if(hours < 12){
                        priceValue = v.getPricePer4Hours() * price8hRate;
                    }else if(hours < 24){
                        priceValue = v.getPricePer4Hours() * price12hRate;
                    }else{
                        priceValue = v.getPricePer4Hours() * priceDayRate;
                    }
                    if(request.getMinPrice() != null && request.getMaxPrice() != null){
                        return priceValue >= request.getMinPrice()
                                && priceValue <= request.getMaxPrice();
                    }else if(request.getMinPrice() != null){
                        return priceValue >= request.getMinPrice();
                    }else if(request.getMaxPrice() != null){
                        return priceValue <= request.getMaxPrice();
                    }
                    return true;
                })
                .map(Vehicle::getId)
                .toList();

        Page<Vehicle> vehiclePage;
        if(seats != null){
            vehiclePage = vehicleRepository.findByIdInAndBrandInAndCategoryInAndSeatsAndNameContainingIgnoreCase(unavailableIdList, brandsList, categoryList, seats, request.getSearch(), pageable);
        }else{
            vehiclePage = vehicleRepository.findByIdInAndBrandInAndCategoryInAndNameContains(unavailableIdList, brandsList, categoryList, request.getSearch(), pageable);
        }

        List<VehicleListResponse> vehicleResponse = vehiclePage.getContent().stream()
                .map(v -> vehicleMapper.toVehicleListResponse(v, hours))
                .toList();

        return new PageAndFilterVehicleResponse(vehicleResponse , vehiclePage.getTotalPages());
    }

    public PageAndFilterVehicleResponse manageCar(PageAndFilterManageVehicleRequest request) {
        User user = userService.currentUser();

        List<VehicleStatus> statusList = (request.getStatus() == null || request.getStatus().isEmpty())
                ? Arrays.asList(VehicleStatus.values())
                : request.getStatus();

        Pageable pageable = PageRequest.of(request.getPage() - 1, request.getLimit(), Sort.by("id").descending());
        Page<Vehicle> vehiclePage;

        if(request.getStationId() != null) {
            Station station = stationRepository.findById(request.getStationId())
                    .orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_FOUND));
            vehiclePage = vehicleRepository.findByStatusInAndNameContainsAndStation_Id(statusList, request.getSearch(), station.getId(), pageable);
        }else{
            if(user.getRole() == Role.ROLE_STAFF){
                Station station = stationRepository.findById(user.getStaff().getStation().getId())
                        .orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_FOUND));
                vehiclePage = vehicleRepository.findByStatusInAndNameContainsAndStation_Id(statusList, request.getSearch(), station.getId(), pageable);
            }else{
                vehiclePage = vehicleRepository.findByStatusInAndNameContains(statusList, request.getSearch(), pageable);
            }
        }

        List<VehicleListResponse> vehicles = vehiclePage.getContent().stream()
                .map(v -> vehicleMapper.toVehicleListResponse(v, 4))
                .toList();

        return new PageAndFilterVehicleResponse(vehicles, vehiclePage.getTotalPages());
    }

    public List<VehicleScheduleResponse> getVehicleFullSchedule(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_NOT_EXIST));

        List<VehicleScheduleResponse> schedules = new ArrayList<>();
        List<Rental> rentals = rentalRepository.findByVehicle_IdAndStatusNotInAndStartTimeAfter(id, List.of(RentalStatus.COMPLETED, RentalStatus.CANCELLED), LocalDateTime.now());
        schedules.addAll(rentals.stream()
                .map(rental -> new VehicleScheduleResponse(rental.getStartTime(), rental.getEndTime()))
                .toList());
        List<Reservation> reservations = reservationRepository.findByVehicle_IdAndStatusInAndStartTimeAfter(id, List.of(ReservationStatus.OVERDUE, ReservationStatus.CONFIRM), LocalDateTime.now());
        schedules.addAll(reservations.stream()
                .map(reservation -> new VehicleScheduleResponse(reservation.getStartTime(), reservation.getEndTime()))
                .toList());

        return schedules;
    }
}