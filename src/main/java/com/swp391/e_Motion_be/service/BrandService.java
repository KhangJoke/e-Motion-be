package com.swp391.e_Motion_be.service;

import com.swp391.e_Motion_be.dto.requests.brand.BrandCreationRequest;
import com.swp391.e_Motion_be.dto.requests.brand.BrandUpdateRequest;
import com.swp391.e_Motion_be.dto.responses.brand.BrandResponse;
import com.swp391.e_Motion_be.entity.Brand;
import com.swp391.e_Motion_be.enums.ErrorCode;
import com.swp391.e_Motion_be.exception.AppException;
import com.swp391.e_Motion_be.mapper.BrandMapper;
import com.swp391.e_Motion_be.repository.BrandRepository;
import com.swp391.e_Motion_be.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BrandService {

    private final BrandRepository brandRepository;
    private final BrandMapper brandMapper;
    private final CloudinaryService cloudinaryService;
    private final VehicleRepository vehicleRepository;

    // Lấy danh sách thương hiệu đang kích hoạt (cho User / Mobile App)
    public List<BrandResponse> findAllActiveBrands() {
        return brandRepository.findByActiveTrueOrderByDisplayOrderAsc()
                .stream()
                .map(brandMapper::toBrandResponse)
                .toList();
    }

    // Lấy toàn bộ thương hiệu (kể cả đã ẩn) cho Admin
    public List<BrandResponse> findAllBrandsForAdmin() {
        return brandRepository.findAll(Sort.by(Sort.Direction.ASC, "displayOrder"))
                .stream()
                .map(brandMapper::toBrandResponse)
                .toList();
    }

    // Lấy chi tiết 1 thương hiệu
    public BrandResponse findBrandById(Long id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_FOUND));
        return brandMapper.toBrandResponse(brand);
    }

    // Admin tạo mới thương hiệu
    @Transactional
    public BrandResponse createBrand(BrandCreationRequest request) {
        String normalizedCode = request.getCode().trim().toUpperCase();
        if (brandRepository.existsByCode(normalizedCode)) {
            throw new AppException(ErrorCode.BRAND_CODE_EXISTED);
        }
        if (brandRepository.existsByName(request.getName().trim())) {
            throw new AppException(ErrorCode.BRAND_NAME_EXISTED);
        }

        Brand brand = brandMapper.toBrandEntity(request);
        brand.setCode(normalizedCode);
        brand.setName(request.getName().trim());
        brand.setActive(true);

        return brandMapper.toBrandResponse(brandRepository.save(brand));
    }

    // Admin cập nhật thương hiệu
    @Transactional
    public BrandResponse updateBrand(Long id, BrandUpdateRequest request) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_FOUND));

        String normalizedCode = request.getCode().trim().toUpperCase();
        if (!brand.getCode().equalsIgnoreCase(normalizedCode) && brandRepository.existsByCode(normalizedCode)) {
            throw new AppException(ErrorCode.BRAND_CODE_EXISTED);
        }
        if (!brand.getName().equalsIgnoreCase(request.getName().trim()) && brandRepository.existsByName(request.getName().trim())) {
            throw new AppException(ErrorCode.BRAND_NAME_EXISTED);
        }

        // Nếu logo thay đổi và logo cũ là từ Cloudinary -> xoá ảnh cũ trên Cloudinary để dọn dẹp
        if (request.getLogoUrl() != null && !request.getLogoUrl().equals(brand.getLogoUrl())) {
            if (brand.getLogoUrl() != null && brand.getLogoUrl().contains("cloudinary.com")) {
                try {
                    String publicId = cloudinaryService.getPublicIdFromUrl(brand.getLogoUrl());
                    cloudinaryService.delete(publicId);
                } catch (Exception ignored) {
                }
            }
        }

        brandMapper.updateBrandFromRequest(request, brand);
        brand.setCode(normalizedCode);
        brand.setName(request.getName().trim());
        if (request.getActive() != null) {
            brand.setActive(request.getActive());
        }

        return brandMapper.toBrandResponse(brandRepository.save(brand));
    }

    // Admin xoá thương hiệu
    @Transactional
    public void deleteBrand(Long id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_FOUND));

        if (vehicleRepository.existsByBrand_Id(id)) {
            throw new AppException(ErrorCode.BRAND_IN_USE);
        }

        if (brand.getLogoUrl() != null && brand.getLogoUrl().contains("cloudinary.com")) {
            try {
                String publicId = cloudinaryService.getPublicIdFromUrl(brand.getLogoUrl());
                cloudinaryService.delete(publicId);
            } catch (Exception ignored) {
            }
        }

        brandRepository.delete(brand);
    }

    // Admin toggle bật/tắt hiển thị thương hiệu
    @Transactional
    public BrandResponse toggleBrandStatus(Long id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_FOUND));
        brand.setActive(!brand.isActive());
        return brandMapper.toBrandResponse(brandRepository.save(brand));
    }
}
