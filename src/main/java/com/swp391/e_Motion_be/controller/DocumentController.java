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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DocumentController {

    DocumentService documentService;

    @PostMapping
    ApiResponse<DocumentResponse> createDocument(@RequestBody @Valid DocumentCreationRequest request){
        ApiResponse<DocumentResponse> ApiResponse = new ApiResponse<>();
        ApiResponse.setData(documentService.createDocument(request));
        return ApiResponse;
    }

    @GetMapping
    ApiResponse<List<DocumentResponse>> getAllDocuments(){
        ApiResponse<List<DocumentResponse>> ApiResponse = new ApiResponse<>();
        ApiResponse.setData(documentService.getAllDocuments());
        return ApiResponse;
    }

    @GetMapping("/{docId}")
    ApiResponse<DocumentResponse> getDocumentById(@PathVariable long docId){
        ApiResponse<DocumentResponse> ApiResponse = new ApiResponse<>();
        ApiResponse.setData(documentService.getDocumentById(docId));
        return ApiResponse;
    }

    @GetMapping("/{email}")
    ApiResponse<List<DocumentResponse>> getDocumentsByUserId(@PathVariable String email){
        ApiResponse<List<DocumentResponse>> ApiResponse = new ApiResponse<>();
        ApiResponse.setData(documentService.getDocumentsByEmail(email));
        return ApiResponse;
    }

    @PutMapping("/{docId}")
    ApiResponse<DocumentResponse> updateDocument(@PathVariable long docId, @RequestBody @Valid DocumentUpdateRequest request){
        ApiResponse<DocumentResponse> ApiResponse = new ApiResponse<>();
        ApiResponse.setData(documentService.updateDocument(docId, request));
        return ApiResponse;
    }

    @DeleteMapping("/{docId}")
    ApiResponse<String> deleteDocument(@PathVariable long docId){
        documentService.deleteDocumentById(docId);
        ApiResponse<String> ApiResponse = new ApiResponse<>();
        ApiResponse.setMessage("Document deleted successfully");
        return ApiResponse;
    }
}
