package com.swp391.e_Motion_be.dto.responses;

import com.swp391.e_Motion_be.enums.DocType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserDocumentRespon {
    long id;
    String imgUrl;
    DocType docType;
    String docNumber;
    long userId;
}
