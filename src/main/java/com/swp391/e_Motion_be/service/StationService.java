package com.swp391.e_Motion_be.service;

import com.swp391.e_Motion_be.dto.requests.station.StationCreationRequest;
import com.swp391.e_Motion_be.dto.requests.station.StationUpdateRequest;
import com.swp391.e_Motion_be.dto.responses.rental.RentalResponse;
import com.swp391.e_Motion_be.dto.responses.station.ManageStationResponse;
import com.swp391.e_Motion_be.dto.responses.station.RevenueStationResponse;
import com.swp391.e_Motion_be.dto.responses.station.StationResponse;
import com.swp391.e_Motion_be.entity.Rental;
import com.swp391.e_Motion_be.entity.Station;
import com.swp391.e_Motion_be.enums.ErrorCode;
import com.swp391.e_Motion_be.enums.RentalStatus;
import com.swp391.e_Motion_be.enums.station.StationCity;
import com.swp391.e_Motion_be.exception.AppException;
import com.swp391.e_Motion_be.mapper.RentalMapper;
import com.swp391.e_Motion_be.mapper.StationMapper;
import com.swp391.e_Motion_be.repository.RentalRepository;
import com.swp391.e_Motion_be.repository.StaffRepository;
import com.swp391.e_Motion_be.repository.StationRepository;
import com.swp391.e_Motion_be.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

@Service
@RequiredArgsConstructor
public class StationService {
    private final StationRepository stationRepository;
    private final VehicleRepository vehicleRepository;
    private final StaffRepository staffRepository;
    private final RentalRepository rentalRepository;

    private final StationMapper stationMapper;
    private final RentalMapper rentalMapper;

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
        stationRepository.delete(existing);
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

    private Long getStaffOfStation(Station station){
        return staffRepository.countByStation_Id(station.getId());
    }

    private List<RentalResponse> getRentalOfStation(Station station){
        return rentalRepository.findByStation_Id(station.getId()).stream()
                .map(rentalMapper::toRentalResponse)
                .toList();
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
            manageStationResponse.setRental(getRentalOfStation(station));

            manageStationResponseList.add(manageStationResponse);
        }
        return manageStationResponseList;
    }

    private List<RevenueStationResponse> getRevenueCommon(List<Rental> rentals){
        Map<Long, Double> revenueByStation = rentals.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getStation().getId(),
                        Collectors.summingDouble(Rental::getRentFee)
                ));

        List<Station> stations = stationRepository.findAll();

        return stations.stream()
                .map(station -> new RevenueStationResponse(
                        station.getName(),
                        revenueByStation.getOrDefault(station.getId(), 0.0)
                ))
                .toList();
    }

    private List<RevenueStationResponse> getMonthlyRevenue(int month, int year) {
        List<Rental> rentals = rentalRepository.findAll().stream()
                .filter(r -> r.getStatus() == RentalStatus.COMPLETED)
                .filter(r -> r.getEndTime().getYear() == year)
                .filter(r -> r.getStartTime().getMonthValue() == month)
                .toList();
        return getRevenueCommon(rentals);
    }

    private List<RevenueStationResponse> getYearlyRevenue(int year) {
        List<Rental> rentals = rentalRepository.findAll().stream()
                .filter(r -> r.getStatus() == RentalStatus.COMPLETED)
                .filter(r -> r.getEndTime().getYear() == year)
                .toList();
        return getRevenueCommon(rentals);
    }

    private List<RevenueStationResponse> getTotalRevenue() {
        List<Rental> rentals = rentalRepository.findAll().stream()
                .filter(r -> r.getStatus() == RentalStatus.COMPLETED)
                .toList();
        return getRevenueCommon(rentals);
    }


    public List<RevenueStationResponse> getRevenueStation(String type, Integer month, Integer year) {
        switch (type) {
            case "month" -> {
                return getMonthlyRevenue(month != null ? month : LocalDate.now().getMonthValue(), year != null ? year : LocalDate.now().getYear());
            }
            case "year" -> {
                return getYearlyRevenue(year != null ? year : LocalDate.now().getYear());
            }
            case "total" -> {
                return getTotalRevenue();
            }
            default -> throw new AppException(ErrorCode.STATION_GET_REVENUE_FAILED);
        }
    }

}
