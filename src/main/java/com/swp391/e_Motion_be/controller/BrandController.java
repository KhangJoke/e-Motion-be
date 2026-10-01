package com.swp391.e_Motion_be.controller;

import com.swp391.e_Motion_be.dto.requests.brand.BrandCreationRequest;
import com.swp391.e_Motion_be.dto.requests.brand.BrandUpdateRequest;
import com.swp391.e_Motion_be.dto.responses.ApiResponse;
import com.swp391.e_Motion_be.dto.responses.brand.BrandResponse;
import com.swp391.e_Motion_be.service.BrandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/brands")
@RequiredArgsConstructor
public class BrandController {

    private final BrandService brandService;

    // Public: Khách hàng / Mobile app lấy danh sách thương hiệu đang hoạt động
    @GetMapping
    @PreAuthorize("permitAll()")
    public ApiResponse<List<BrandResponse>> getActiveBrands() {
        ApiResponse<List<BrandResponse>> response = new ApiResponse<>();
        response.setData(brandService.findAllActiveBrands());
        response.setMessage("Get active brands successfully");
        return response;
    }

    // Public / Authenticated: Lấy chi tiết thương hiệu
    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    public ApiResponse<BrandResponse> getBrandById(@PathVariable Long id) {
        ApiResponse<BrandResponse> response = new ApiResponse<>();
        response.setData(brandService.findBrandById(id));
        response.setMessage("Get brand by id successfully");
        return response;
    }

    // Admin: Lấy toàn bộ thương hiệu (kể cả đã ẩn)
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<BrandResponse>> getAllBrandsForAdmin() {
        ApiResponse<List<BrandResponse>> response = new ApiResponse<>();
        response.setData(brandService.findAllBrandsForAdmin());
        response.setMessage("Get all brands for admin successfully");
        return response;
    }

    // Admin: Thêm thương hiệu mới (nhận logoUrl đã upload lên Cloudinary)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<BrandResponse> createBrand(@RequestBody @Valid BrandCreationRequest request) {
        ApiResponse<BrandResponse> response = new ApiResponse<>();
        response.setData(brandService.createBrand(request));
        response.setMessage("Create brand successfully");
        return response;
    }

    // Admin: Cập nhật thông tin thương hiệu
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<BrandResponse> updateBrand(@PathVariable Long id, @RequestBody @Valid BrandUpdateRequest request) {
        ApiResponse<BrandResponse> response = new ApiResponse<>();
        response.setData(brandService.updateBrand(id, request));
        response.setMessage("Update brand successfully");
        return response;
    }

    // Admin: Bật / Tắt trạng thái hoạt động của thương hiệu
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<BrandResponse> toggleBrandStatus(@PathVariable Long id) {
        ApiResponse<BrandResponse> response = new ApiResponse<>();
        response.setData(brandService.toggleBrandStatus(id));
        response.setMessage("Toggle brand status successfully");
        return response;
    }

    // Admin: Xoá thương hiệu
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> deleteBrand(@PathVariable Long id) {
        brandService.deleteBrand(id);
        ApiResponse<String> response = new ApiResponse<>();
        response.setMessage("Delete brand successfully");
        return response;
    }
}
