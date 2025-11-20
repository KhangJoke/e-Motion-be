package com.swp391.e_Motion_be.dto.requests.report;

import com.swp391.e_Motion_be.enums.report.ReportStatus;
import com.swp391.e_Motion_be.enums.report.ReportType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportSearchRequest {
    private String title;
    private ReportType type;
    private ReportStatus status;
}
