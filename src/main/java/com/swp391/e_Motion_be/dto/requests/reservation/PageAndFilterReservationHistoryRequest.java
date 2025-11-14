package com.swp391.e_Motion_be.dto.requests.reservation;

import com.swp391.e_Motion_be.enums.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageAndFilterReservationHistoryRequest {
    private List<ReservationStatus> status;
    private Integer page;
    private Integer limit;
    private String search;
    private String email;
}
