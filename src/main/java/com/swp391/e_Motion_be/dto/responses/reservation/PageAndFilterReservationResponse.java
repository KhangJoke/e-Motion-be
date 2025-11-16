package com.swp391.e_Motion_be.dto.responses.reservation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageAndFilterReservationResponse {
    private List<ReservationListResponse> content;
    private int totalPages;
}
