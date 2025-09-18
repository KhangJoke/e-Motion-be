package com.swp391.e_Motion_be.mapper;


import com.swp391.e_Motion_be.dto.requests.document.UserDocumentCreationRequest;
import com.swp391.e_Motion_be.dto.requests.document.UserDocumentUpdateRequest;
import com.swp391.e_Motion_be.dto.responses.UserDocumentRespon;
import com.swp391.e_Motion_be.entity.UserDocument;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserDocumentMapper {
    UserDocument toDocumentEntity(UserDocumentCreationRequest request);
    UserDocumentRespon toDocumentResponse(UserDocument document);
    void updateDocumentFromRequest(@MappingTarget UserDocument document, UserDocumentUpdateRequest request);
}
