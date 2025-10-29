package com.swp391.e_Motion_be.dto.requests.rental;

import com.swp391.e_Motion_be.enums.RentalStatus;
import lombok.Data;

import java.util.List;

@Data
public class PageAndFilterRentalRequest {
    private List<RentalStatus> status;
    private Integer page;
    private Integer limit;
    private String search;
}
