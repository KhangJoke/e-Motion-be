package com.swp391.e_Motion_be.controller;

import com.swp391.e_Motion_be.dto.requests.report.ReportCreationRequest;
import com.swp391.e_Motion_be.dto.requests.report.ReportSearchRequest;
import com.swp391.e_Motion_be.dto.requests.report.ReportUpdateStatusRequest;
import com.swp391.e_Motion_be.dto.responses.ApiResponse;
import com.swp391.e_Motion_be.dto.responses.report.ReportResponse;
import com.swp391.e_Motion_be.enums.report.ReportType;
import com.swp391.e_Motion_be.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    @Autowired
    private ReportService reportService;


    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ApiResponse<List<ReportResponse>> getAllReports() {
        ApiResponse<List<ReportResponse>> response = new ApiResponse<>();
        response.setData(reportService.findAllReports());
        return response;
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ApiResponse<ReportResponse> getReportsById(@PathVariable Long id) {
        ApiResponse<ReportResponse> response = new ApiResponse<>();
        response.setData(reportService.findReportById(id));
        return response;
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ApiResponse<List<ReportResponse>> getReportsByUser(@PathVariable Long userId) {
        ApiResponse<List<ReportResponse>> response = new ApiResponse<>();
        response.setData(reportService.findAllReportsByUser(userId));
        return response;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ApiResponse<ReportResponse> createReport(@RequestBody @Valid ReportCreationRequest request) {
        ApiResponse<ReportResponse> response = new ApiResponse<>();
        response.setData(reportService.createReport(request));
        return response;
    }

    @PostMapping("/update-status")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<ReportResponse> updateReportStatus(@RequestBody @Valid ReportUpdateStatusRequest request) {
        ApiResponse<ReportResponse> response = new ApiResponse<>();
        response.setData(reportService.updateReportStatus(request));
        return response;
    }


    @GetMapping("/types")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ApiResponse<List<ReportType>> findAllReportTypes() {
        ApiResponse<List<ReportType>> response = new ApiResponse<>();
        response.setData(reportService.findAllReportTypes());
        return response;
    }



    @PostMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ApiResponse<List<ReportResponse>> searchReports(@RequestBody ReportSearchRequest request) {
        ApiResponse<List<ReportResponse>> response = new ApiResponse<>();
        response.setData(reportService.searchReports(request.getType(), request.getStatus(), request.getTitle()));
        return response;
    }


    @DeleteMapping("/id")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<ReportResponse> deleteReport(@PathVariable Long id) {
        ApiResponse<ReportResponse> response = new ApiResponse<>();
        reportService.deleteReport(id);
        response.setMessage("Delete report successfully");
        return response;
    }
}
