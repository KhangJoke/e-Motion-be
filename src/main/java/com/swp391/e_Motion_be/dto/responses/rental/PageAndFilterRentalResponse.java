package com.swp391.e_Motion_be.dto.responses.rental;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageAndFilterRentalResponse {
    private List<RentalResponse> content;
    private int totalPages;
}
