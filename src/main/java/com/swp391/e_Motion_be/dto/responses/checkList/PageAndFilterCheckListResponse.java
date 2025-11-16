package com.swp391.e_Motion_be.dto.responses.checkList;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageAndFilterCheckListResponse {
    private List<RentalCheckListListResponse> content;
    private int totalPages;
}
