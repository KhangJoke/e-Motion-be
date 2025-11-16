package com.swp391.e_Motion_be.dto.requests.rental;

import com.swp391.e_Motion_be.enums.RentalStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageAndFilterRentalHistoryRequest {
    private List<RentalStatus> status;
    private Integer page;
    private Integer limit;
    private String search;
    private String email;
}
