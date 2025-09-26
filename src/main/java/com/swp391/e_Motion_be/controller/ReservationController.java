package com.swp391.e_Motion_be.controller;

import com.swp391.e_Motion_be.dto.requests.reservation.CreateReservationRequest;
import com.swp391.e_Motion_be.dto.requests.reservation.UpdateReservationStatusRequest;
import com.swp391.e_Motion_be.dto.responses.ApiResponse;
import com.swp391.e_Motion_be.dto.responses.ReservationResponse;
import com.swp391.e_Motion_be.enums.ReservationStatus;
import com.swp391.e_Motion_be.service.reservation.ReservationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ApiResponse<ReservationResponse> createReservation(@RequestBody @Valid CreateReservationRequest request) {
        ReservationResponse reservationResponse = reservationService.createReservation(request);
        ApiResponse<ReservationResponse> response = new ApiResponse<>();
        response.setData(reservationResponse);
        response.setMessage("Reservation created successfully");
        response.setStatus(201);

        return response;
    }

    @GetMapping
    public ApiResponse<List<ReservationResponse>> getAllReservations() {
        List<ReservationResponse> data = reservationService.getAllReservations();
        ApiResponse<List<ReservationResponse>> response = new ApiResponse<>();
        response.setData(data);
        response.setMessage("Fetched all reservations successfully");
        response.setStatus(200);
        return response;
    }

    @GetMapping("/{code}")
    public ApiResponse<ReservationResponse> getReservationByCode(@PathVariable String code) {
        ReservationResponse reservationResponse = reservationService.getReservationByCode(code);

        ApiResponse<ReservationResponse> response = new ApiResponse<>();
        response.setData(reservationResponse);
        response.setMessage("Fetched reservation successfully");
        response.setStatus(200);

        return response;
    }

    @PatchMapping("/update-status")
    public ApiResponse<ReservationResponse> updateReservationStatus(@RequestBody @Valid UpdateReservationStatusRequest request) {
        ReservationResponse reservationResponse = reservationService.updateReservationStatus(request);

        ApiResponse<ReservationResponse> response = new ApiResponse<>();
        response.setData(reservationResponse);
        response.setMessage("Updated reservation status successfully");
        response.setStatus(200);

        return response;
    }

    @DeleteMapping("/{code}")
    public ApiResponse<Void> deleteReservation(@PathVariable String code) {
        reservationService.deleteReservationByCode(code);

        ApiResponse<Void> response = new ApiResponse<>();
        response.setMessage("Deleted reservation successfully");
        response.setStatus(204);

        return response;
    }

    @GetMapping("/status")
    public ApiResponse<List<ReservationResponse>> getReservationsByStatus(@RequestParam ReservationStatus status) {
        List<ReservationResponse> data = reservationService.getReservationsByStatus(status);
        ApiResponse<List<ReservationResponse>> response = new ApiResponse<>();
        response.setData(data);
        response.setMessage("Fetched reservations by status successfully");
        response.setStatus(200);
        return response;
    }

    @GetMapping("/email")
    public ApiResponse<List<ReservationResponse>> getReservationsByUserEmail(@RequestParam String email) {
        List<ReservationResponse> data = reservationService.getReservationsByUserEmail(email);
        ApiResponse<List<ReservationResponse>> response = new ApiResponse<>();
        response.setData(data);
        response.setMessage("Fetched reservations by user email successfully");
        response.setStatus(200);
        return response;
    }

    @GetMapping("/station")
    public ApiResponse<List<ReservationResponse>> getReservationsByStationName(@RequestParam String stationName) {
        List<ReservationResponse> data = reservationService.getReservationsByStationName(stationName);
        ApiResponse<List<ReservationResponse>> response = new ApiResponse<>();
        response.setData(data);
        response.setMessage("Fetched reservations by station name successfully");
        response.setStatus(200);
        return response;
    }

    @GetMapping("/vehicle")
    public ApiResponse<List<ReservationResponse>> getReservationsByVehicleId(@RequestParam Long vehicleId) {
        List<ReservationResponse> data = reservationService.getReservationsByVehicleId(vehicleId);
        ApiResponse<List<ReservationResponse>> response = new ApiResponse<>();
        response.setData(data);
        response.setMessage("Fetched reservations by vehicle ID successfully");
        response.setStatus(200);
        return response;
    }

    @GetMapping("/time")
    public ApiResponse<List<ReservationResponse>> getExpiredReservations(@RequestParam LocalDateTime time) {
        List<ReservationResponse> data = reservationService.getValidReservations(time);
        ApiResponse<List<ReservationResponse>> response = new ApiResponse<>();
        response.setData(data);
        response.setMessage("Fetched reservations before specified time successfully");
        response.setStatus(200);
        return response;
    }
}
