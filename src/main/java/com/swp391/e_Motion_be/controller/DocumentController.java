package com.swp391.e_Motion_be.controller;

import com.swp391.e_Motion_be.dto.requests.document.DocumentCreationRequest;
import com.swp391.e_Motion_be.dto.requests.document.DocumentUpdateRequest;
import com.swp391.e_Motion_be.dto.responses.ApiResponse;
import com.swp391.e_Motion_be.dto.responses.DocumentResponse;
import com.swp391.e_Motion_be.service.document.DocumentService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DocumentController {

    DocumentService documentService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    ApiResponse<DocumentResponse> createDocument(@RequestBody @Valid DocumentCreationRequest request){
        ApiResponse<DocumentResponse> ApiResponse = new ApiResponse<>();
        ApiResponse.setData(documentService.createDocument(request));
        return ApiResponse;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    ApiResponse<List<DocumentResponse>> getAllDocuments(){
        ApiResponse<List<DocumentResponse>> ApiResponse = new ApiResponse<>();
        ApiResponse.setData(documentService.getAllDocuments());
        return ApiResponse;
    }

    @GetMapping("/{docId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    ApiResponse<DocumentResponse> getDocumentById(@PathVariable long docId){
        ApiResponse<DocumentResponse> ApiResponse = new ApiResponse<>();
        ApiResponse.setData(documentService.getDocumentById(docId));
        return ApiResponse;
    }

    @GetMapping("/{email}")
    @PreAuthorize("#email == authentication.principal.email or hasAnyRole('ADMIN', 'STAFF')")
    ApiResponse<List<DocumentResponse>> getDocumentsByUserEmail(@PathVariable String email){
        ApiResponse<List<DocumentResponse>> ApiResponse = new ApiResponse<>();
        ApiResponse.setData(documentService.getDocumentsByUserEmail(email));
        return ApiResponse;
    }

    @PutMapping("/{docId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    ApiResponse<DocumentResponse> updateDocument(@PathVariable long docId, @RequestBody @Valid DocumentUpdateRequest request){
        ApiResponse<DocumentResponse> ApiResponse = new ApiResponse<>();
        ApiResponse.setData(documentService.updateDocument(docId, request));
        return ApiResponse;
    }

    @DeleteMapping("/{docId}")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<String> deleteDocument(@PathVariable long docId){
        documentService.deleteDocumentById(docId);
        ApiResponse<String> ApiResponse = new ApiResponse<>();
        ApiResponse.setMessage("Document deleted successfully");
        return ApiResponse;
    }
}
