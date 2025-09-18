package com.swp391.e_Motion_be.dto.requests.document;

import com.swp391.e_Motion_be.enums.DocType;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserDocumentUpdateRequest {
    String imgUrl;
    DocType docType;
    String docNumber;
}
