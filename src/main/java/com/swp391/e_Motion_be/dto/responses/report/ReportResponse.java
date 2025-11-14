package com.swp391.e_Motion_be.dto.responses.report;

import com.swp391.e_Motion_be.dto.responses.StaffResponse;
import com.swp391.e_Motion_be.dto.responses.user.UserResponse;
import com.swp391.e_Motion_be.enums.report.ReportStatus;
import com.swp391.e_Motion_be.enums.report.ReportType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponse {
    private Long id;
    private String title;
    private String description;
    private ReportStatus status;
    private ReportType type;
    private LocalDateTime createdAt;
    private UserResponse user;
    private StaffResponse staff;
}
