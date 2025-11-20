package com.swp391.e_Motion_be.dto.requests.report;

import com.swp391.e_Motion_be.enums.report.ReportType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportCreationRequest {
    @NotNull(message = "Title is required")
    private String title;
    @NotNull(message = "Description is required")
    private String description;
    @NotNull(message = "Report Type is required")
    private ReportType type;

    private String userEmail;
}
