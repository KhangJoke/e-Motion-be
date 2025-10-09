package com.swp391.e_Motion_be.dto.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.swp391.e_Motion_be.enums.DocumentType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocumentResponse {
    long id;
    String imgUrl;
    DocumentType documentType;
    String docNumber;
    String email;
}
