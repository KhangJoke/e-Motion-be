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
import com.swp391.e_Motion_be.service.CloudinaryService;
import jakarta.transaction.Transactional;
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
    OcrService ocrService;
    CloudinaryService cloudinaryService;

    public DocumentResponse createDocument(DocumentCreationRequest request) {
        // 1. lấy ra user
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->new AppException(ErrorCode.USER_NOT_EXISTS));
        // 2. Check user đã có document type này chưa
        if (documentRepository.existsByUser_EmailAndType(request.getEmail(), request.getType())) {
            cloudinaryService.delete(cloudinaryService.getPublicIdFromUrl(request.getImgUrl()));
            throw new AppException(ErrorCode.USER_ALREADY_HAS_DOCUMENT_OF_TYPE);
        }
        String extractedCccd = ocrService.extractCccdFromUrl(request.getImgUrl());
        // 3. Kiểm tra số CCCD có khớp với số trong ảnh ko
        if(!extractedCccd.equals(request.getNumber())){
            cloudinaryService.delete(cloudinaryService.getPublicIdFromUrl(request.getImgUrl()));
            throw new AppException(ErrorCode.DOCUMENT_NUMBER_MISMATCH);
        }

        // 4. Kiểm tra số CCCD đã tồn tại chưa
        if(documentRepository.existsByNumber(request.getNumber())){
            cloudinaryService.delete(cloudinaryService.getPublicIdFromUrl(request.getImgUrl()));
            throw new AppException(ErrorCode.DOCUMENT_NUMBER_EXISTS);
        }

        Document document = documentMapper.toDocumentEntity(request);
        document.setUser(user);
        documentRepository.save(document);
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

    public List<DocumentResponse> getDocumentsByUserEmail(String email){
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

    @Transactional
    public void deleteDocumentById(long docId){
        Document document = documentRepository.findById(docId)
                .orElseThrow(() -> new AppException(ErrorCode.DOCUMENT_NOT_FOUND));
        documentRepository.deleteById(docId);
        cloudinaryService.delete(cloudinaryService.getPublicIdFromUrl(document.getImgUrl()));
    }
}
