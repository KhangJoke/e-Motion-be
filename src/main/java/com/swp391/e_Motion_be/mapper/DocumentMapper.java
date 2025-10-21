package com.swp391.e_Motion_be.mapper;


import com.swp391.e_Motion_be.dto.requests.document.DocumentCreationRequest;
import com.swp391.e_Motion_be.dto.requests.document.DocumentUpdateRequest;
import com.swp391.e_Motion_be.dto.responses.DocumentResponse;
import com.swp391.e_Motion_be.entity.Document;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface DocumentMapper {
    Document toDocumentEntity(DocumentCreationRequest request);

    @Mapping(source = "user.email", target = "email")
    DocumentResponse toDocumentResponse(Document document);
    void updateDocumentFromRequest(@MappingTarget Document document, DocumentUpdateRequest request);
}
