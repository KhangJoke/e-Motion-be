package com.swp391.e_Motion_be.dto.requests.checklist;

import com.swp391.e_Motion_be.enums.CheckType;
import lombok.Data;

import java.util.List;

@Data
public class PageAndFilterCheckListRequest {
    private List<CheckType> type;
    private Integer page;
    private Integer limit;
    private String search;
}
