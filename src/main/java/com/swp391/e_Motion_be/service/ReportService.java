package com.swp391.e_Motion_be.service;

import com.swp391.e_Motion_be.dto.requests.report.ReportCreationRequest;
import com.swp391.e_Motion_be.dto.requests.report.ReportUpdateStatusRequest;
import com.swp391.e_Motion_be.dto.responses.report.ReportResponse;
import com.swp391.e_Motion_be.entity.Report;
import com.swp391.e_Motion_be.entity.User;
import com.swp391.e_Motion_be.enums.ErrorCode;
import com.swp391.e_Motion_be.enums.report.ReportStatus;
import com.swp391.e_Motion_be.enums.report.ReportType;
import com.swp391.e_Motion_be.exception.AppException;
import com.swp391.e_Motion_be.mapper.ReportMapper;
import com.swp391.e_Motion_be.repository.ReportRepository;
import com.swp391.e_Motion_be.repository.UserRepository;
import com.swp391.e_Motion_be.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class ReportService {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private ReportMapper reportMapper;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;

    public ReportResponse createReport(ReportCreationRequest report){
        Report newReport = reportMapper.toReportEntity(report);
        if(report.getType().equals(ReportType.REPORT_USER)){
            User user = userRepository.findByEmail(report.getUserEmail())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTS));
            newReport.setUser(user);
        }

        newReport.setStaff(userService.currentUser().getStaff());
        reportRepository.save(newReport);
        return  reportMapper.toResponse(newReport);
    }

    public ReportResponse updateReportStatus(ReportUpdateStatusRequest request){
        Report report = reportRepository.findById(request.getId())
                .orElseThrow(() -> new AppException(ErrorCode.REPORT_NOT_FOUND));
        report.setStatus(request.getStatus());

        if(request.getStatus() == ReportStatus.APPROVED){
            if(report.getType().equals(ReportType.REPORT_USER)){
                User user = report.getUser();
                user.setBlocked(true);
                userRepository.save(user);
            }
        }
        return reportMapper.toResponse(reportRepository.save(report));
    }


    public List<ReportResponse> searchReports(ReportType type, ReportStatus status, String title){
        List<Report> reports = reportRepository.searchReports(type, status, title);
        return reports.stream()
                .filter(report -> !report.isDelete())
                .sorted(Comparator.comparing(Report::getCreatedAt).reversed())
                .map(reportMapper::toResponse)
                .toList();
    }

    public ReportResponse findReportById(Long id){
        return reportMapper.toResponse(reportRepository.findById(id).orElse(null));
    }

    public List<ReportResponse> findAllReports(){
        List<Report> reports = reportRepository.findAll()
                .stream()
                .filter(report -> !report.isDelete())
                .sorted(Comparator.comparing(Report::getCreatedAt).reversed())
                .toList();
        return reports.stream().map(reportMapper::toResponse).toList();
    }

    public List<ReportResponse> findAllReportsByUser(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTS));;

        List<Report> reports = reportRepository.findByUser(user)
                .stream()
                .filter(report -> !report.isDelete())
                .sorted(Comparator.comparing(Report::getCreatedAt).reversed())
                .toList();;
        return reports.stream().map(reportMapper::toResponse).toList();
    }

    public List<ReportType> findAllReportTypes(){
        return List.of(ReportType.values());
    }

    public void deleteReport(Long reportId){
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new AppException(ErrorCode.REPORT_NOT_FOUND));
        report.setDelete(true);
        reportRepository.save(report);
    }
}
