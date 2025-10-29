package com.swp391.e_Motion_be.dto.requests.reservation;

import com.swp391.e_Motion_be.enums.ReservationStatus;
import lombok.Data;

import java.util.List;

@Data
public class PageAndFilterReservationRequest {
    private List<ReservationStatus> status;
    private Integer page;
    private Integer limit;
    private String search;
}
