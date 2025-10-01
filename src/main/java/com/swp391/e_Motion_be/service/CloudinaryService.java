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
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException e) {
            throw new AppException(ErrorCode.DELETE_IMG_FAIL);
        }
    }

    // Trả về public_id từ URL (giả sử URL dạng .../v1234567890/publicId.png)
    public String getPublicIdFromUrl(String url) {
        int idx = url.lastIndexOf("/");
        if (idx < 0) return url;
        String filename = url.substring(idx + 1);
        int dotIdx = filename.lastIndexOf(".");
        return dotIdx > 0 ? filename.substring(0, dotIdx) : filename;
    }
}


