package com.swp391.e_Motion_be.dto.requests.report;

import com.swp391.e_Motion_be.enums.ReportStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportUpdateStatusRequest {
    @NotNull(message = "Report ID is required")
    private   Long id;
    @NotNull(message = "Report Status is required")
    private ReportStatus status;
}
