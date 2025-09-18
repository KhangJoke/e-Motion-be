package com.swp391.e_Motion_be.dto.requests.document;

import com.swp391.e_Motion_be.enums.DocType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserDocumentUpdateRequest {
    @NotBlank(message = "Image URL must not be blank")
    @Size(min =10,max = 255, message = "Image URL must not exceed 255 characters")
    String imgUrl;

    @NotBlank(message = "Document type is required")
    DocType docType;

    @NotBlank(message = "Document number must not be blank")
    @Size(max = 20, message = "Document number must not exceed 20 characters")
    @Pattern(regexp = "^[0-9]{9,12}$", message = "Document number must be 9–12 digits (for CCCD/CMND)")
    String docNumber;
}
