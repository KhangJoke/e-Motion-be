package com.swp391.e_Motion_be.mapper;

import com.swp391.e_Motion_be.dto.requests.report.ReportCreationRequest;
import com.swp391.e_Motion_be.dto.responses.report.ReportResponse;
import com.swp391.e_Motion_be.entity.Report;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface ReportMapper {
    Report toReportEntity(ReportCreationRequest request);


    ReportResponse toResponse(Report report);
}
