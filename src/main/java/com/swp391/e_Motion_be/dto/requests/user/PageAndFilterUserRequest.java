package com.swp391.e_Motion_be.dto.requests.user;

import com.swp391.e_Motion_be.enums.Role;
import lombok.Data;

import java.util.List;

@Data
public class PageAndFilterUserRequest {
    private List<Boolean> blockedList;
    private List<Role> roleList;
    private Integer page;
    private Integer limit;
    private String search;
    private Long stationId;
}
