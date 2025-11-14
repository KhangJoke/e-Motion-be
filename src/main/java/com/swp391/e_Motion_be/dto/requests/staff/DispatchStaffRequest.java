package com.swp391.e_Motion_be.dto.requests.staff;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DispatchStaffRequest {
    @NotEmpty
    private List<Long> staffIds;
    @NotNull
    private Long stationId;
}
