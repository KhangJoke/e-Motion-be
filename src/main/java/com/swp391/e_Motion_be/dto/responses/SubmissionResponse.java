package com.swp391.e_Motion_be.dto.responses;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class SubmissionResponse {
    private Long id;
    private String name;
    private String source;
    private String submitters_order;
    private String slug;
    private String audit_log_url;
    private String combined_document_url;
    private String completed_at;
    private String created_at;
    private String updated_at;
    private String archived_at;
    private String status;
    private Map<String,Object> template;
    private Map<String,Object> created_by_user;
    private List<Map<String,Object>> submitters;
    private List<Map<String,Object>> documents;
    private List<Map<String,Object>> submission_events;
}
