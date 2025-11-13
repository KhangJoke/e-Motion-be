package com.swp391.e_Motion_be.service.document;

import com.swp391.e_Motion_be.dto.requests.document.DocumentCreationRequest;
import com.swp391.e_Motion_be.dto.requests.document.DocumentUpdateRequest;
import com.swp391.e_Motion_be.dto.responses.DocumentResponse;
import com.swp391.e_Motion_be.entity.Document;
import com.swp391.e_Motion_be.entity.User;
import com.swp391.e_Motion_be.enums.DocumentType;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
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
        // lấy ra user
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->new AppException(ErrorCode.USER_NOT_EXISTS));
        // Check user đã có document type này chưa
        if (documentRepository.existsByUser_EmailAndType(request.getEmail(), request.getType())) {
            cloudinaryService.delete(cloudinaryService.getPublicIdFromUrl(request.getImgUrl()));
            throw new AppException(ErrorCode.USER_ALREADY_HAS_DOCUMENT_OF_TYPE);
        }
        String text = ocrService.extractTextFromUrl(request.getImgUrl());
        // 1. Kiểm tra định dạng ảnh có đúng không
        if((request.getType() == DocumentType.CCCD && !ocrService.isCCCD(text))
                || (request.getType() == DocumentType.LICENSE && !ocrService.isGPLX(text))){
            cloudinaryService.delete(cloudinaryService.getPublicIdFromUrl(request.getImgUrl()));
            throw new AppException(ErrorCode.DOCUMENT_INVALID);
        }
        // 2. Kiểm tra số CCCD/GPLX có hợp lệ không
        if(!ocrService.hasValidIdNumber(text)){
            cloudinaryService.delete(cloudinaryService.getPublicIdFromUrl(request.getImgUrl()));
            throw new AppException(ErrorCode.DOCUMENT_NUMBER_INVALID);
        }
        // 3. Kiểm tra số CCCD/GPLX có khớp với số trong ảnh ko
        if(!ocrService.extractIdNumber(text).equals(request.getNumber())){
            cloudinaryService.delete(cloudinaryService.getPublicIdFromUrl(request.getImgUrl()));
            throw new AppException(ErrorCode.DOCUMENT_NUMBER_MISMATCH);
        }
        // 4. Kiểm tra số CCCD đã tồn tại chưa
        if(documentRepository.existsByNumber(request.getNumber())){
            cloudinaryService.delete(cloudinaryService.getPublicIdFromUrl(request.getImgUrl()));
            throw new AppException(ErrorCode.DOCUMENT_NUMBER_EXISTS);
        }
        // 5. Kiểm tra giấy tờ có hết hạn không
        if(ocrService.isExpired(text)){
            cloudinaryService.delete(cloudinaryService.getPublicIdFromUrl(request.getImgUrl()));
            throw new AppException(ErrorCode.DOCUMENT_EXPIRED);
        }
        // 6. Nếu là giấy phép lái xe, kiểm tra có được phép thuê xe không
        if(request.getType() == DocumentType.LICENSE && !ocrService.isAllowedToRentCar(text)){
            cloudinaryService.delete(cloudinaryService.getPublicIdFromUrl(request.getImgUrl()));
            throw new AppException(ErrorCode.LICENSE_NOT_VALID_FOR_VEHICLE);
        }

        Document document = documentMapper.toDocumentEntity(request);
        document.setUser(user);
        documentRepository.save(document);
        return documentMapper.toDocumentResponse(document);
    }

    public boolean checkExpiredDocumentByUserEmail(String email){
        List<Document> documents = documentRepository.findByUser_Email(email);
        for(Document document : documents){
            String text = ocrService.extractTextFromUrl(document.getImgUrl());
            if(ocrService.isExpired(text)){
                return true;
            }
        }
        return false;
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
        User loginUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!document.getUser().getId().equals(loginUser.getId())){
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        documentRepository.deleteById(docId);
        cloudinaryService.delete(cloudinaryService.getPublicIdFromUrl(document.getImgUrl()));
    }
}
