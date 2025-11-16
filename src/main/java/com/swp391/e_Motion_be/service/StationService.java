package com.swp391.e_Motion_be.service;

import com.swp391.e_Motion_be.dto.requests.station.StationCreationRequest;
import com.swp391.e_Motion_be.dto.requests.station.StationUpdateRequest;
import com.swp391.e_Motion_be.dto.responses.rental.RentalResponse;
import com.swp391.e_Motion_be.dto.responses.station.ManageStationResponse;
import com.swp391.e_Motion_be.dto.responses.station.StationDetailResponse;
import com.swp391.e_Motion_be.dto.responses.station.StationResponse;
import com.swp391.e_Motion_be.dto.responses.stats.RevenueResponse;
import com.swp391.e_Motion_be.entity.Rental;
import com.swp391.e_Motion_be.entity.Station;
import com.swp391.e_Motion_be.enums.ErrorCode;
import com.swp391.e_Motion_be.enums.RentalStatus;
import com.swp391.e_Motion_be.enums.station.StationCity;
import com.swp391.e_Motion_be.enums.vehicle.VehicleStatus;
import com.swp391.e_Motion_be.exception.AppException;
import com.swp391.e_Motion_be.mapper.RentalMapper;
import com.swp391.e_Motion_be.mapper.StationMapper;
import com.swp391.e_Motion_be.repository.RentalRepository;
import com.swp391.e_Motion_be.repository.StaffRepository;
import com.swp391.e_Motion_be.repository.StationRepository;
import com.swp391.e_Motion_be.repository.VehicleRepository;
import com.swp391.e_Motion_be.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StationService {
    private final StationRepository stationRepository;
    private final VehicleRepository vehicleRepository;
    private final StaffRepository staffRepository;
    private final RentalRepository rentalRepository;

    private final StationMapper stationMapper;
    private final RentalMapper rentalMapper;
    private final UserService userService;

    public StationResponse createStation(StationCreationRequest request) {
        if (stationRepository.existsByNameIgnoreCase(request.getName())) {
            throw new AppException(ErrorCode.STATION_NAME_EXISTS);
        }
        Station station = stationMapper.toStationEntity(request);
        Station savedStation = stationRepository.save(station);
        return stationMapper.toStationResponse(savedStation);
    }

    public StationResponse updateStation(String currentName, StationUpdateRequest request) {
        Station station = stationRepository.findByNameIgnoreCase(currentName.trim()).orElseThrow(() -> new AppException(ErrorCode.STATION_NOT_FOUND));
        if (request.getName() != null) {
            String newName = request.getName().trim();
            if (!station.getName().equalsIgnoreCase(newName)
                    && stationRepository.existsByNameIgnoreCase(newName)) {
                throw new AppException(ErrorCode.STATION_NAME_EXISTS);
            }
            station.setName(newName);
        }
        stationMapper.updateStation(request, station);
        return stationMapper.toStationResponse(stationRepository.save(station));
    }

    public void deleteStation(String name) {
        Station existing = stationRepository.findByNameIgnoreCase(name.trim()).orElseThrow(() -> new AppException(ErrorCode.STATION_NOT_FOUND));
        existing.setDelete(true);
        stationRepository.save(existing);
    }

    public List<StationResponse> findAllStations() {
        return stationRepository.findAll().stream()
                .map(stationMapper::toStationResponse)
                .toList();
    }

    public List<String> findAllCity() {
        return stationRepository.findAllCityNames();
    }

    public StationResponse getStationByName(String name) {
        return stationMapper.toStationResponse(stationRepository.findByNameIgnoreCase(name.trim()).orElseThrow(() -> new AppException(ErrorCode.STATION_NOT_FOUND)));
    }

    public StationDetailResponse getStationById(Long id) {
        Station station = stationRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.STATION_NOT_FOUND));

        StationDetailResponse stationDetail = new StationDetailResponse();
        stationDetail.setId(station.getId());
        stationDetail.setName(station.getName());
        stationDetail.setAddress(station.getAddress());
        stationDetail.setCity(station.getCity());
        stationDetail.setStatus(station.getStatus());
        stationDetail.setQuantityCar(getCarOfStation(station));
        stationDetail.setQuantityStaff(getStaffOfStation(station));
        stationDetail.setCreatedAt(station.getCreatedAt().toLocalDate());
        stationDetail.setCarRental(getCarRentingOfStation(station));

        List<Rental> rentals = rentalRepository.findByStation_Id(station.getId());
        List<Integer> peakHours = userService.getPeakHours(rentals);
        stationDetail.setPeakHours(peakHours);
        stationDetail.setRentals(
                rentals.stream()
                        .sorted(Comparator.comparing(Rental::getCreatedAt).reversed())
                        .map(rentalMapper::toRentalListResponse)
                        .limit(5)
                        .toList()
        );

        return stationDetail;
    }

    public List<StationResponse> getStationsByAddress(String address) {
        return stationRepository.findByAddressIgnoreCase(address.trim()).stream()
                .map(stationMapper::toStationResponse)
                .toList();
    }

    public List<StationResponse> getStationsByCity(StationCity city) {
        return stationRepository.findByCity(city).stream()
                .map(stationMapper::toStationResponse)
                .toList();
    }

    private Long getCarOfStation(Station station){
        return vehicleRepository.countByStation_Id(station.getId());
    }

    private Long getCarRentingOfStation(Station station){
        return vehicleRepository.countByStation_IdAndStatus(station.getId(), VehicleStatus.ONGOING);
    }

    private Long getStaffOfStation(Station station){
        return staffRepository.countByIsDeleteFalseAndStation_Id(station.getId());
    }

    public List<ManageStationResponse> getDataManageStation(){
        List<Station> stations = stationRepository.findAll();
        List<ManageStationResponse> manageStationResponseList = new ArrayList<>();
        for(Station station : stations){

            ManageStationResponse manageStationResponse = new ManageStationResponse();
            manageStationResponse.setId(station.getId());
            manageStationResponse.setName(station.getName());
            manageStationResponse.setAddress(station.getAddress());
            manageStationResponse.setCity(station.getCity());
            manageStationResponse.setStatus(station.getStatus());
            manageStationResponse.setQuantityCar(getCarOfStation(station));
            manageStationResponse.setQuantityStaff(getStaffOfStation(station));

            manageStationResponseList.add(manageStationResponse);
        }
        return manageStationResponseList;
    }

    private List<RevenueResponse> getRevenueCommon(List<Rental> rentals){
        Map<Long, Double> revenueByStation = rentals.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getStation().getId(),
                        Collectors.summingDouble(Rental::getRentFee)
                ));

        List<Station> stations = stationRepository.findAll();

        return stations.stream()
                .map(station -> new RevenueResponse(
                        station.getName(),
                        revenueByStation.getOrDefault(station.getId(), 0.0)
                ))
                .toList();
    }

    private List<RevenueResponse> getMonthlyRevenue(int month, int year) {
        List<Rental> rentals = rentalRepository.findAll().stream()
                .filter(r -> r.getStatus() == RentalStatus.COMPLETED)
                .filter(r -> r.getEndTime().getYear() == year)
                .filter(r -> r.getEndTime().getMonthValue() == month)
                .toList();
        return getRevenueCommon(rentals);
    }

    private List<RevenueResponse> getDayRevenue(int day, int month, int year) {
        List<Rental> rentals = rentalRepository.findAll().stream()
                .filter(r -> r.getStatus() == RentalStatus.COMPLETED)
                .filter(r -> r.getEndTime().getYear() == year)
                .filter(r -> r.getEndTime().getMonthValue() == month)
                .filter(r -> r.getEndTime().getDayOfMonth() == day)
                .toList();
        return getRevenueCommon(rentals);
    }

    public List<RevenueResponse> getWeeklyRevenueOfStation(Long stationId) {
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new AppException(ErrorCode.STATION_NOT_FOUND));

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(6);

        List<Rental> rentals = rentalRepository.findAll().stream()
                .filter(r -> r.getStatus() == RentalStatus.COMPLETED)
                .filter(r -> r.getStation().getId().equals(station.getId()))
                .filter(r -> {
                    LocalDate endTime = r.getEndTime().toLocalDate();
                    return !endTime.isBefore(startDate) && !endTime.isAfter(endDate);
                })
                .toList();

        // Gom doanh thu theo ngày
        Map<LocalDate, Double> revenuePerDay = rentals.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getEndTime().toLocalDate(),
                        Collectors.summingDouble(Rental::getRentFee)
                ));

        List<RevenueResponse> result = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = endDate.minusDays(i);
            double revenue = revenuePerDay.getOrDefault(date, 0.0);
            String dayName = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, new Locale("vi", "VN"));
            result.add(new RevenueResponse(dayName, revenue));
        }

        return result;
    }


    private List<RevenueResponse> getYearlyRevenue(int year) {
        List<Rental> rentals = rentalRepository.findAll().stream()
                .filter(r -> r.getStatus() == RentalStatus.COMPLETED)
                .filter(r -> r.getEndTime().getYear() == year)
                .toList();
        return getRevenueCommon(rentals);
    }

    private List<RevenueResponse> getTotalRevenue() {
        List<Rental> rentals = rentalRepository.findAll().stream()
                .filter(r -> r.getStatus() == RentalStatus.COMPLETED)
                .toList();
        return getRevenueCommon(rentals);
    }


    public List<RevenueResponse> getRevenueStation(String type, int day, int month, int year) {
        switch (type) {
            case "day" -> {
                return getDayRevenue(day, month, year);
            }
            case "month" -> {
                return getMonthlyRevenue(month, year);
            }
            case "year" -> {
                return getYearlyRevenue(year);
            }
            case "total" -> {
                return getTotalRevenue();
            }
            default -> throw new AppException(ErrorCode.STATION_GET_REVENUE_FAILED);
        }
    }

}
