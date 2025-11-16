package com.swp391.e_Motion_be.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.swp391.e_Motion_be.enums.ErrorCode;
import com.swp391.e_Motion_be.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    // Xóa ảnh trên Cloudinary theo public_id
    public void delete(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("invalidate", true));
        } catch (IOException e) {
            throw new AppException(ErrorCode.DELETE_IMG_FAIL);
        }
    }

    // Trả về public_id từ URL (giả sử URL dạng .../v1234567890/publicId.png)
    public String getPublicIdFromUrl(String url) {
        try {
            // Lấy phần sau "/upload/"
            String[] parts = url.split("/upload/");
            if (parts.length < 2) return url;

            // Bỏ version (v123456) và extension (.jpg, .png, ...)
            String path = parts[1];
            int versionIdx = path.indexOf('/');
            if (versionIdx >= 0) path = path.substring(versionIdx + 1);
            int dotIdx = path.lastIndexOf('.');
            return dotIdx > 0 ? path.substring(0, dotIdx) : path;
        } catch (Exception e) {
            return url; // fallback nếu parse lỗi
        }
    }

}


