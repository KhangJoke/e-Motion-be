package com.swp391.e_Motion_be.service;

import com.swp391.e_Motion_be.dto.requests.ImgVehicle.ImgVehicleCreationRequest;
import com.swp391.e_Motion_be.dto.requests.ImgVehicle.ImgVehicleUpdateRequest;
import com.swp391.e_Motion_be.dto.responses.ImgVehicleResponse;
import com.swp391.e_Motion_be.entity.ImgVehicle;
import com.swp391.e_Motion_be.entity.Vehicle;
import com.swp391.e_Motion_be.enums.ErrorCode;
import com.swp391.e_Motion_be.exception.AppException;
import com.swp391.e_Motion_be.mapper.ImgVehicleMapper;
import com.swp391.e_Motion_be.repository.ImgVehicleRepository;
import com.swp391.e_Motion_be.repository.VehicleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ImgVehicleService {
    private final ImgVehicleMapper imgVehicleMapper;
    private final ImgVehicleRepository imgVehicleRepository;
    private final VehicleRepository vehicleRepository;
    private final CloudinaryService cloudinaryService;

    public ImgVehicleResponse create(ImgVehicleCreationRequest request) {
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_NOT_EXIST));

        ImgVehicle entity = imgVehicleMapper.toEntity(request);
        entity.setVehicle(vehicle);

        ImgVehicleResponse response = imgVehicleMapper.toResponse(imgVehicleRepository.save(entity));
        return response;
    }

    public List<ImgVehicleResponse> createMultipleImagesForVehicle(Vehicle vehicle,List<ImgVehicleCreationRequest> request) {
        List<ImgVehicleResponse> images = new ArrayList<>();
        for (ImgVehicleCreationRequest imgVehicleCreationRequest : request) {
            ImgVehicle entity = imgVehicleMapper.toEntity(imgVehicleCreationRequest);
            entity.setVehicle(vehicle);
            imgVehicleRepository.save(entity);
            images.add(imgVehicleMapper.toResponse(entity));
        }
        return images;
    }


    // Find all images
    public List<ImgVehicleResponse> findAll() {
        return imgVehicleRepository.findAll()
                .stream()
                .map(entity -> {
                    ImgVehicleResponse response = imgVehicleMapper.toResponse(entity);
                    return response;
                })
                .toList();
    }

    // Find images by Vehicle ID
    public List<ImgVehicleResponse> findByVehicleId(Long vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_NOT_EXIST));

        return imgVehicleRepository.findByVehicle(vehicle)
                .stream()
                .map(entity -> {
                    ImgVehicleResponse response = imgVehicleMapper.toResponse(entity);
                    return response;
                })
                .toList();
    }

    // Update image
    @Transactional
    public ImgVehicleResponse update(Long imgId, ImgVehicleUpdateRequest request) {
        ImgVehicle entity = imgVehicleRepository.findById(imgId)
                .orElseThrow(() -> new AppException(ErrorCode.IMG_VEHICLE_NOT_FOUND));

        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_NOT_EXIST));

        // Update fields
        imgVehicleMapper.updateEntityFromRequest(entity, request);
        entity.setVehicle(vehicle);

        ImgVehicle updated = imgVehicleRepository.save(entity);
        return imgVehicleMapper.toResponse(updated);
    }

    @Transactional
    public void deleteImgVehicle(Long id) {
        ImgVehicle entity = imgVehicleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Image not found"));
        cloudinaryService.delete(cloudinaryService.getPublicIdFromUrl(entity.getUrl()));
        imgVehicleRepository.delete(entity);
    }
}
