package com.swp391.e_Motion_be.service;

import com.swp391.e_Motion_be.dto.requests.ImgVehicle.ImgVehicleCreationRequest;
import com.swp391.e_Motion_be.dto.requests.vehicle.*;
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
import com.swp391.e_Motion_be.repository.*;
import com.swp391.e_Motion_be.service.user.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final ImgVehicleService imgVehicleService;
    private final StationRepository stationRepository;
    private final RentalRepository rentalRepository;
    private final ReservationRepository reservationRepository;
    private final ImgVehicleRepository imgVehicleRepository;

    private final RentalService rentalService;
    private final UserService userService;
    private final CloudinaryService cloudinaryService;

    private final VehicleMapper vehicleMapper;

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
                .filter( v ->!v.getId().equals(vehicle.getId()) &&
                        !v.isDelete() &&
                        v.getStation().getId().equals(vehicle.getStation().getId()))
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



    public List<VehicleBrandResponse> findAllVehicleBrands() {
        return Arrays.stream(VehicleBrand.values())
                .map(VehicleBrandResponse::new)
                .toList();
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
    @Transactional
    public VehicleListResponse createVehicle(VehicleCreationRequest request) {
        //Check Station is FOUNd or NOT
        Station station = stationRepository.findById(request.getStationId())
                .orElseThrow(() -> new AppException(ErrorCode.STATION_NOT_FOUND));

        if (vehicleRepository.findByPlateNumber(request.getPlateNumber()).isPresent()) {
            throw new AppException(ErrorCode.VEHICLE_EXIST);
        }
        Vehicle vehicle = vehicleMapper.toVehicleEntity(request);
        vehicle.setLastMaintenance(LocalDateTime.now());

        vehicleRepository.save(vehicle);
        imgVehicleService.createMultipleImagesForVehicle(vehicle,request.getImages());

        //Reload vehicle to get images and staion for reponse
        vehicle = vehicleRepository.findById(vehicle.getId())
                .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_NOT_EXIST));
        vehicle.setStation(station);
        vehicle.setImages(imgVehicleRepository.findByVehicle_Id(vehicle.getId()));

        return vehicleMapper.toVehicleListResponse(vehicle, 4);
    }

    // UPDATE
    @Transactional
    public void updateVehicle(VehicleUpdateRequest request) {

        Vehicle vehicle = vehicleRepository.findById(request.getId())
                .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_NOT_EXIST));
        Station station = stationRepository.findById(request.getStationId())
                .orElseThrow(() -> new AppException(ErrorCode.STATION_NOT_FOUND));
        vehicle.setStation(station);

        if (!vehicle.getPlateNumber().equals(request.getPlateNumber())
                && vehicleRepository.findByPlateNumber(request.getPlateNumber()).isPresent()) {
            throw new AppException(ErrorCode.VEHICLE_EXIST);
        }

        // Xóa ảnh đã up trên cloud mà request gửi update ko còn url
        List<ImgVehicle> oldImages = imgVehicleRepository.findByVehicle_Id(vehicle.getId());
        Set<String> newUrls = request.getImages().stream()
                .map(ImgVehicleCreationRequest::getUrl)
                .collect(Collectors.toSet());

        oldImages.stream()
                .filter(img -> !newUrls.contains(img.getUrl()))
                .forEach(img -> {
                    // Lấy public_id từ URL
                    String publicId = cloudinaryService.getPublicIdFromUrl(img.getUrl());
                    cloudinaryService.delete(publicId);  // xóa trên Cloudinary
                    imgVehicleRepository.delete(img);    // xóa record DB
                });

        request.getImages().forEach(imgReq -> {
            ImgVehicle imgVehicle = imgVehicleRepository.findByVehicle_IdAndUrl(vehicle.getId(), imgReq.getUrl())
                    .orElseGet(() -> {
                        ImgVehicle imgVehicleEntity = new ImgVehicle();
                        imgVehicleEntity.setVehicle(vehicle);
                        imgVehicleEntity.setUrl(imgReq.getUrl());
                        return imgVehicleEntity;
                    });
            imgVehicle.setMain(imgReq.isMain());
            imgVehicleRepository.save(imgVehicle);
        });

        vehicleMapper.updateVehicleFromRequest(vehicle, request);
        vehicleRepository.save(vehicle);
    }

    //DELETE
    public void deleteVehicleById(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_NOT_EXIST));
        vehicle.setDelete(true);
        vehicleRepository.save(vehicle);
    }


    public List<FeeResponse> getListFeeBooking(FeeRequest request){
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_NOT_EXIST));
        List<FeeResponse> fees = new ArrayList<>();

        double bookingFeeValue = rentalService.calculateRentalFee(vehicle, request.getStartTime(), request.getEndTime());

        FeeResponse bookingFee = new FeeResponse("Phí thuê xe", FeeType.BOOKING_FEE, bookingFeeValue);
        FeeResponse deposit = new FeeResponse("Tiền cọc xe", FeeType.DEPOSIT, vehicle.getDepositFee());
        FeeResponse holdCar;
        if(request.isRental()){
            holdCar = new FeeResponse("Tiền giữ chỗ", FeeType.HOLD_CAR, 0.0);
        }else{
            holdCar = new FeeResponse("Tiền giữ chỗ", FeeType.HOLD_CAR, holdCarFee);
        }
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

    // --- Lọc những xe còn trống trong khung giờ ---
    private List<Vehicle> vehiclesAvailableInRange(List<Vehicle> vehicles, LocalDateTime start, LocalDateTime end) {
        return vehicles
                .stream()
                .filter(v -> v.getReservations().stream()
                        .filter(r -> r.getStatus() != ReservationStatus.COMPLETED
                                && r.getStatus() != ReservationStatus.CANCELLED
                                && r.getStatus() != ReservationStatus.FAILED)
                        .noneMatch(r -> r.getStartTime().isBefore(end)
                                && r.getEndTime().isAfter(start))
                        &&
                        v.getRentals().stream()
                                .filter(r -> r.getStatus() != RentalStatus.COMPLETED
                                        && r.getStatus() != RentalStatus.CANCELLED)
                                .noneMatch(r -> r.getStartTime().isBefore(end)
                                        && r.getEndTime().isAfter(start))
                ).toList();
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
                    List.of(VehicleStatus.AVAILABLE, VehicleStatus.ONGOING))
                    .stream()
                    .filter(v -> !v.isDelete())
                    .toList();
        }else{
            vehicles = vehicleRepository.findByStation_CityAndStatusIn(
                    request.getCity(),
                    List.of(VehicleStatus.AVAILABLE, VehicleStatus.ONGOING))
                    .stream()
                    .filter(v -> !v.isDelete())
                    .toList();
        }
        // --- Lọc những xe còn trống trong khung giờ ---
        List<Long> availableIdList  = vehiclesAvailableInRange(vehicles, request.getStartTime(), request.getEndTime())
                .stream()
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
                    List.of(VehicleStatus.AVAILABLE, VehicleStatus.ONGOING))
                    .stream()
                    .filter(v -> !v.isDelete())
                    .toList();
        }else{
            vehicles = vehicleRepository.findByStation_CityAndStatusIn(
                    request.getCity(),
                    List.of(VehicleStatus.AVAILABLE, VehicleStatus.ONGOING))
                    .stream()
                    .filter(v -> !v.isDelete())
                    .toList();
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

        List<Vehicle> vehicles = vehicleRepository.findByIsDeleteFalse();

        List<Long> vehicleId = vehicles
                .stream()
                .map(Vehicle::getId)
                .toList();

        if(request.getStartTime() != null && request.getEndTime() != null){
            vehicleId = vehiclesAvailableInRange(vehicles, request.getStartTime(), request.getEndTime())
                    .stream()
                    .map(Vehicle::getId)
                    .toList();
        }

        Pageable pageable = PageRequest.of(request.getPage() - 1, request.getLimit(), Sort.by("id").descending());
        Page<Vehicle> vehiclePage;

        if(request.getStationId() != null) {
            Station station = stationRepository.findById(request.getStationId())
                    .orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_FOUND));
            vehiclePage = vehicleRepository.findByIdInAndStatusInAndNameContainsAndStation_Id(vehicleId, statusList, request.getSearch(), station.getId(), pageable);
        }else{
            if(user.getRole() == Role.ROLE_STAFF){
                Station station = stationRepository.findById(user.getStaff().getStation().getId())
                        .orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_FOUND));
                vehiclePage = vehicleRepository.findByIdInAndStatusInAndNameContainsAndStation_Id(vehicleId,statusList, request.getSearch(), station.getId(), pageable);
            }else{
                vehiclePage = vehicleRepository.findByIdInAndStatusInAndNameContains(vehicleId, statusList, request.getSearch(), pageable);
            }
        }

        List<VehicleListResponse> vehiclesResponse = vehiclePage.getContent().stream()
                .map(v -> vehicleMapper.toVehicleListResponse(v, 4))
                .toList();

        return new PageAndFilterVehicleResponse(vehiclesResponse, vehiclePage.getTotalPages());
    }

    public List<VehicleScheduleResponse> getVehicleFullSchedule(Long id) {

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

    @Transactional
    public void dispatchVehicle(VehicleDispatchRequest request) {
        Station station = stationRepository.findById(request.getStationId())
                .orElseThrow(() -> new AppException(ErrorCode.STATION_NOT_FOUND));
        for(Long vehicleId : request.getVehicleIds()) {
            Vehicle vehicle = vehicleRepository.findById(vehicleId)
                    .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_NOT_EXIST));

            if(vehicle.getStatus() != VehicleStatus.AVAILABLE || !isVehicleAvailableToDispatch(vehicle)) {
                throw new AppException(ErrorCode.VEHICLE_NOT_READY);
            }
            vehicle.setStatus(VehicleStatus.TRANSFERRING);
            vehicle.setStation(station);
            vehicleRepository.save(vehicle);
        }
    }

    private boolean isVehicleAvailableToDispatch(Vehicle vehicle) {
        Reservation reservedVehicle = reservationRepository.findByVehicle_IdAndStartTimeAfter(vehicle.getId(), LocalDateTime.now())
                .orElse(null);
        return reservedVehicle == null;
    }

    public VehicleCheckAvailableResponse vehicleCheckAvailable(VehicleCheckAvailableRequest request) {
        boolean isAvailable = vehicleRepository.doesConflictExistForVehicle(
                request.getVehicleId(),
                request.getStartTime(),
                request.getEndTime(),
                List.of("PENDING", "CONFIRM"),
                List.of("COMPLETED", "CANCELLED", "OVERDUE"),
                null,
                null
        ) == 0;
        return new VehicleCheckAvailableResponse(isAvailable);
    }
}