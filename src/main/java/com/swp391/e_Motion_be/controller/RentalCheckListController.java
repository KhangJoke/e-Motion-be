package com.swp391.e_Motion_be.controller;

import com.swp391.e_Motion_be.dto.requests.checklist.PageAndFilterCheckListRequest;
import com.swp391.e_Motion_be.dto.requests.checklist.RentalCheckListCreateRequest;
import com.swp391.e_Motion_be.dto.responses.ApiResponse;
import com.swp391.e_Motion_be.dto.responses.checkList.PageAndFilterCheckListResponse;
import com.swp391.e_Motion_be.dto.responses.checkList.RentalCheckListListResponse;
import com.swp391.e_Motion_be.dto.responses.checkList.RentalCheckListResponse;
import com.swp391.e_Motion_be.enums.CheckType;
import com.swp391.e_Motion_be.service.RentalCheckListService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rental-checklists")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','STAFF')")
public class RentalCheckListController {

    private final RentalCheckListService rentalCheckListService;

    @PostMapping
    public ApiResponse<RentalCheckListResponse> createCheckList(
            @RequestBody @Valid RentalCheckListCreateRequest request) {
        ApiResponse<RentalCheckListResponse> response = new ApiResponse<>();
        response.setData(rentalCheckListService.createCheckList(request));
        return response;
    }

    @GetMapping
    public ApiResponse<List<RentalCheckListListResponse>> getAllCheckList() {
        ApiResponse<List<RentalCheckListListResponse>> response = new ApiResponse<>();
        response.setData(rentalCheckListService.getListCheckLists());
        response.setMessage("Get all rental checklists successfully");
        return response;
    }

    @GetMapping("/rentalId")
    public ApiResponse<List<RentalCheckListResponse>> getCheckListByRentalId(@RequestParam Long rentalId) {
        ApiResponse<List<RentalCheckListResponse>> response = new ApiResponse<>();
        response.setData(rentalCheckListService.getCheckListByRentalId(rentalId));
        response.setMessage("Get rental checklists successfully");
        return response;
    }


    @GetMapping("/search")
    public ApiResponse<List<RentalCheckListResponse>> getCheckListByFilter(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) List<CheckType> type) {
        ApiResponse<List<RentalCheckListResponse>> response = new ApiResponse<>();
        response.setData(rentalCheckListService.getCheckListByFilter(keyword,type));
        if(response.getData().isEmpty()){
            response.setMessage("No rental checklists found");
        }else{
            response.setMessage("Get all rental checklists successfully");
        }
        return response;
    }

    @PostMapping("/filter")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<PageAndFilterCheckListResponse>> findByPageAndFilterAndSearch(@RequestBody PageAndFilterCheckListRequest request){
        ApiResponse<PageAndFilterCheckListResponse> apiResponse = new ApiResponse<>();
        apiResponse.setData(rentalCheckListService.findByPageAndFilterAndSearch(request));
        if(apiResponse.getData().getContent().isEmpty()) {
            apiResponse.setMessage("No check lists found");
        }else {
            apiResponse.setMessage("Get check lists successfully");
        }
        return ResponseEntity.ok(apiResponse);
    }
}

