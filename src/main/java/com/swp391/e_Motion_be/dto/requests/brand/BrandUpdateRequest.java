package com.swp391.e_Motion_be.dto.requests.brand;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandUpdateRequest {
    @NotBlank(message = "Tên thương hiệu không được để trống")
    private String name;

    @NotBlank(message = "Mã thương hiệu không được để trống")
    private String code;

    @NotBlank(message = "URL logo không được để trống")
    private String logoUrl;

    private int displayOrder;

    private Boolean active;
}
