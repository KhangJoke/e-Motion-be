package com.swp391.e_Motion_be.service.document;

import com.swp391.e_Motion_be.dto.requests.document.DocumentCreationRequest;
import com.swp391.e_Motion_be.dto.requests.document.DocumentUpdateRequest;
import com.swp391.e_Motion_be.dto.responses.DocumentResponse;
import com.swp391.e_Motion_be.entity.Document;
import com.swp391.e_Motion_be.entity.User;
import com.swp391.e_Motion_be.enums.ErrorCode;
import com.swp391.e_Motion_be.exception.AppException;
import com.swp391.e_Motion_be.mapper.DocumentMapper;
import com.swp391.e_Motion_be.repository.DocumentRepository;
import com.swp391.e_Motion_be.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DocumentService {

    DocumentRepository documentRepository;
    UserRepository userRepository;
    DocumentMapper documentMapper;

    public DocumentResponse createDocument(DocumentCreationRequest request) {
        if(documentRepository.existsByDocNumber(request.getDocNumber())){
            throw new AppException(ErrorCode.DOCUMENT_NUMBER_EXISTS);
        }
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->new AppException(ErrorCode.USER_NOT_EXISTS));
        Document document = documentMapper.toDocumentEntity(request);
        user.addUserDocument(document);
        userRepository.save(user);
        return documentMapper.toDocumentResponse(document);
    }

    public List<DocumentResponse> getAllDocuments(){
        return documentRepository.findAll().stream()
                .map(documentMapper::toDocumentResponse)
                .toList();
    }

    public DocumentResponse getDocumentById(long id){
        return documentMapper.toDocumentResponse(documentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DOCUMENT_NOT_FOUND)));
    }

    public List<DocumentResponse> getDocumentsByEmail(String email){
        return documentRepository.findByUser_Email(email).stream()
                .map(documentMapper:: toDocumentResponse)
                .toList();
    }

    public DocumentResponse updateDocument(long docId, DocumentUpdateRequest request){
        Document document = documentRepository.findById(docId)
                .orElseThrow(() -> new AppException(ErrorCode.DOCUMENT_NOT_FOUND));
        documentMapper.updateDocumentFromRequest(document, request);
        return documentMapper.toDocumentResponse(documentRepository.save(document));
    }

    public void deleteDocumentById(long docId){
        if(documentRepository.existsById(docId)){
            documentRepository.deleteById(docId);
        } else {
            throw new RuntimeException("Document not found");
        }
    }
}
