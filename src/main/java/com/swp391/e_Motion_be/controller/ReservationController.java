package com.swp391.e_Motion_be.controller;

import com.swp391.e_Motion_be.dto.requests.reservation.CreateReservationRequest;
import com.swp391.e_Motion_be.dto.requests.reservation.PageAndFilterReservationRequest;
import com.swp391.e_Motion_be.dto.requests.reservation.UpdateReservationStatusRequest;
import com.swp391.e_Motion_be.dto.responses.ApiResponse;
import com.swp391.e_Motion_be.dto.responses.reservation.PageAndFilterReservationResponse;
import com.swp391.e_Motion_be.dto.responses.reservation.ReservationListResponse;
import com.swp391.e_Motion_be.dto.responses.reservation.ReservationResponse;
import com.swp391.e_Motion_be.service.ReservationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Map<String, Object>> createReservation(HttpServletRequest httpReq, @RequestBody @Valid CreateReservationRequest request) throws Exception {
        ApiResponse<Map<String, Object>> response = new ApiResponse<>();
        response.setData(reservationService.createReservation(request, httpReq));
        response.setMessage("Reservation created successfully");
        response.setStatus(201);

        return response;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<List<ReservationListResponse>> getAllReservations() {
        List<ReservationListResponse> data = reservationService.getAllReservations();
        ApiResponse<List<ReservationListResponse>> response = new ApiResponse<>();
        response.setData(data);
        response.setMessage("Fetched all reservations successfully");
        response.setStatus(200);
        return response;
    }

    @GetMapping("/{code}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<ReservationResponse> getReservationByCode(@PathVariable String code) {
        ReservationResponse reservationResponse = reservationService.getReservationByCode(code);

        ApiResponse<ReservationResponse> response = new ApiResponse<>();
        response.setData(reservationResponse);
        response.setMessage("Fetched reservation successfully");
        response.setStatus(200);

        return response;
    }

    @PatchMapping("/update-status")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<ReservationResponse> updateReservationStatus(@RequestBody @Valid UpdateReservationStatusRequest request) {
        ReservationResponse reservationResponse = reservationService.updateReservationStatus(request);

        ApiResponse<ReservationResponse> response = new ApiResponse<>();
        response.setData(reservationResponse);
        response.setMessage("Updated reservation status successfully");
        response.setStatus(200);

        return response;
    }

    @DeleteMapping("/{code}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<Void> deleteReservation(@PathVariable String code) {
        reservationService.deleteReservationByCode(code);

        ApiResponse<Void> response = new ApiResponse<>();
        response.setMessage("Deleted reservation successfully");
        response.setStatus(204);

        return response;
    }

    @GetMapping("/email/{email}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<ReservationResponse>> getReservationsByUserEmail(@PathVariable String email) {
        List<ReservationResponse> data = reservationService.getReservationsByUserEmail(email);
        ApiResponse<List<ReservationResponse>> response = new ApiResponse<>();
        response.setData(data);
        response.setMessage("Fetched reservations by user email successfully");
        response.setStatus(200);
        return response;
    }

    @GetMapping("/station/{stationName}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<List<ReservationResponse>> getReservationsByStationName(@PathVariable String stationName) {
        List<ReservationResponse> data = reservationService.getReservationsByStationName(stationName);
        ApiResponse<List<ReservationResponse>> response = new ApiResponse<>();
        response.setData(data);
        response.setMessage("Fetched reservations by station name successfully");
        response.setStatus(200);
        return response;
    }

    @GetMapping("/vehicle/{vehicleId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<List<ReservationResponse>> getReservationsByVehicleId(@PathVariable Long vehicleId) {
        List<ReservationResponse> data = reservationService.getReservationsByVehicleId(vehicleId);
        ApiResponse<List<ReservationResponse>> response = new ApiResponse<>();
        response.setData(data);
        response.setMessage("Fetched reservations by vehicle ID successfully");
        response.setStatus(200);
        return response;
    }

    @GetMapping("/time/{time}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<List<ReservationResponse>> getExpiredReservations(@PathVariable LocalDateTime time) {
        List<ReservationResponse> data = reservationService.getValidReservations(time);
        ApiResponse<List<ReservationResponse>> response = new ApiResponse<>();
        response.setData(data);
        response.setMessage("Fetched reservations before specified time successfully");
        response.setStatus(200);
        return response;
    }

    @PostMapping("/{code}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<Boolean> cancelReservation(@PathVariable String code, HttpServletRequest httpReq) {
        ApiResponse<Boolean> response = new ApiResponse<>();
        response.setData(reservationService.cancelReservation(code, httpReq));
        response.setMessage("Cancelled reservation successfully");
        response.setStatus(200);
        return response;
    }

    //Chua lam phan quyen
    @PostMapping("/{code}/extend/")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<ReservationResponse> extendReservationReturnTime(@PathVariable String code, @RequestParam LocalDateTime newReturnTime)
    {
        ApiResponse<ReservationResponse> response = new ApiResponse<>();
        response.setData(reservationService.extendReservationReturnTime(code, newReturnTime));
        response.setMessage("Extended reservation return time successfully");
        response.setStatus(200);
        return response;
    }

    @PostMapping("/filter")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<PageAndFilterReservationResponse>> findByPageAndFilterAndSearch(@RequestBody PageAndFilterReservationRequest request){
        ApiResponse<PageAndFilterReservationResponse> apiResponse = new ApiResponse<>();
        apiResponse.setData(reservationService.findByPageAndFilterAndSearch(request));
        if(apiResponse.getData().getContent().isEmpty()) {
            apiResponse.setMessage("No reservations found");
        }else {
            apiResponse.setMessage("Get reservations successfully");
        }
        return ResponseEntity.ok(apiResponse);
    }
}
