package com.swp391.e_Motion_be.controller;

import com.swp391.e_Motion_be.dto.requests.document.UserDocumentCreationRequest;
import com.swp391.e_Motion_be.dto.requests.document.UserDocumentUpdateRequest;
import com.swp391.e_Motion_be.dto.responses.ApiResponse;
import com.swp391.e_Motion_be.dto.responses.UserDocumentRespon;
import com.swp391.e_Motion_be.service.document.UserDocumentService;
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
public class UserDocumentController {

    UserDocumentService userDocumentService;

    @PostMapping
    ApiResponse<UserDocumentRespon> createDocument(@RequestBody @Valid UserDocumentCreationRequest request){
        ApiResponse<UserDocumentRespon> ApiResponse = new ApiResponse<>();
        ApiResponse.setData(userDocumentService.createDocument(request));
        return ApiResponse;
    }

    @GetMapping
    ApiResponse<List<UserDocumentRespon>> getAllDocuments(){
        ApiResponse<List<UserDocumentRespon>> ApiResponse = new ApiResponse<>();
        ApiResponse.setData(userDocumentService.getAllDocuments());
        return ApiResponse;
    }

    @GetMapping("/{docId}")
    ApiResponse<UserDocumentRespon> getDocumentById(@PathVariable long docId){
        ApiResponse<UserDocumentRespon> ApiResponse = new ApiResponse<>();
        ApiResponse.setData(userDocumentService.getDocumentById(docId));
        return ApiResponse;
    }

    @GetMapping("/user/{userId}")
    ApiResponse<List<UserDocumentRespon>> getDocumentsByUserId(@PathVariable long userId){
        ApiResponse<List<UserDocumentRespon>> ApiResponse = new ApiResponse<>();
        ApiResponse.setData(userDocumentService.getDocumentsByUserId(userId));
        return ApiResponse;
    }

    @PutMapping("/{docId}")
    ApiResponse<UserDocumentRespon> updateDocument(@PathVariable long docId, @RequestBody @Valid UserDocumentUpdateRequest request){
        ApiResponse<UserDocumentRespon> ApiResponse = new ApiResponse<>();
        ApiResponse.setData(userDocumentService.updateDocument(docId, request));
        return ApiResponse;
    }

    @DeleteMapping("/{docId}")
    ApiResponse<String> deleteDocument(@PathVariable long docId){
        userDocumentService.deleteDocumentById(docId);
        ApiResponse<String> ApiResponse = new ApiResponse<>();
        ApiResponse.setMessage("Document deleted successfully");
        return ApiResponse;
    }
}
