package com.swp391.e_Motion_be.controller;

import com.swp391.e_Motion_be.dto.requests.deposit.DepositCreateRequest;
import com.swp391.e_Motion_be.dto.requests.payment.CreatePaymentUrlRequest;
import com.swp391.e_Motion_be.dto.requests.reservation.CreateReservationRequest;
import com.swp391.e_Motion_be.dto.requests.reservation.UpdateReservationStatusRequest;
import com.swp391.e_Motion_be.dto.responses.ApiResponse;
import com.swp391.e_Motion_be.dto.responses.DepositResponse;
import com.swp391.e_Motion_be.dto.responses.ReservationResponse;
import com.swp391.e_Motion_be.enums.DepositStatus;
import com.swp391.e_Motion_be.enums.ReservationStatus;
import com.swp391.e_Motion_be.service.DepositService;
import com.swp391.e_Motion_be.service.PaymentService;
import com.swp391.e_Motion_be.service.ReservationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final DepositService depositService;
    private final PaymentService paymentService;

    @PostMapping
    public ApiResponse<Map<String, Object>> createReservation(HttpServletRequest httpReq, @RequestBody @Valid CreateReservationRequest request) throws Exception {
        // Create Reservation
        ReservationResponse reservationResponse = reservationService.createReservation(request);
        // Create Reservation Deposit
        DepositCreateRequest depositCreateRequest = new DepositCreateRequest(DepositStatus.PENDING, 500000, reservationResponse.getCode(), null);
        DepositResponse depositResponse = depositService.createDeposit(depositCreateRequest);
        //Create Payment VnPay Url
        CreatePaymentUrlRequest paymentUrlRequest = new CreatePaymentUrlRequest(depositResponse.getAmount(), "Reservation Deposit", reservationResponse.getUserEmail(), depositResponse.getId(), null);
        String url = paymentService.createPaymentUrl(paymentUrlRequest, httpReq.getRemoteAddr());
        //Create ApiResponse
        Map<String, Object> data = new HashMap<>();
        data.put("VnPayUrl", url);
        data.put("Reservation",reservationResponse);
        data.put("Deposit",depositResponse);
        ApiResponse<Map<String, Object>> response = new ApiResponse<>();
        response.setData(data);
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

    @PutMapping("/{code}/cancel")
    public ApiResponse<ReservationResponse> cancelReservation(@PathVariable String code) {
        ApiResponse<ReservationResponse> response = new ApiResponse<>();
        response.setData(reservationService.cancelReservation(code));
        response.setMessage("Cancelled reservation successfully");
        response.setStatus(200);
        return response;
    }
}
