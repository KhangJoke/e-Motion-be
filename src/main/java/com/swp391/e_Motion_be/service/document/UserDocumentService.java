package com.swp391.e_Motion_be.service.document;

import com.swp391.e_Motion_be.dto.requests.document.UserDocumentCreationRequest;
import com.swp391.e_Motion_be.dto.requests.document.UserDocumentUpdateRequest;
import com.swp391.e_Motion_be.dto.responses.UserDocumentRespon;
import com.swp391.e_Motion_be.entity.User;
import com.swp391.e_Motion_be.entity.UserDocument;
import com.swp391.e_Motion_be.enums.ErrorCode;
import com.swp391.e_Motion_be.exception.AppException;
import com.swp391.e_Motion_be.mapper.UserDocumentMapper;
import com.swp391.e_Motion_be.repository.UserDocumentRepository;
import com.swp391.e_Motion_be.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserDocumentService {

    UserDocumentRepository userDocumentRepository;
    UserRepository userRepository;
    UserDocumentMapper userDocumentMapper;

    public UserDocumentRespon createDocument(UserDocumentCreationRequest request) {
        if(userDocumentRepository.existsByDocNumber(request.getDocNumber())){
            throw new AppException(ErrorCode.DOCUMENT_NUMBER_EXISTS);
        }
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->new AppException(ErrorCode.USER_NOT_EXISTS));
        UserDocument document = userDocumentMapper.toDocumentEntity(request);
        user.addUserDocument(document);
        userRepository.save(user);
        return userDocumentMapper.toDocumentResponse(document);
    }

    public List<UserDocumentRespon> getAllDocuments(){
        return userDocumentRepository.findAll().stream()
                .map(userDocumentMapper::toDocumentResponse)
                .toList();
    }

    public UserDocumentRespon getDocumentById(long id){
        return userDocumentMapper.toDocumentResponse(userDocumentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DOCUMENT_NOT_FOUND)));
    }

    public List<UserDocumentRespon> getDocumentsByUserId(long userId){
        return userDocumentRepository.findByUserId(userId).stream()
                .map(userDocumentMapper :: toDocumentResponse)
                .toList();
    }

    public UserDocumentRespon updateDocument(long docId, UserDocumentUpdateRequest request){
        UserDocument document = userDocumentRepository.findById(docId)
                .orElseThrow(() -> new AppException(ErrorCode.DOCUMENT_NOT_FOUND));
        userDocumentMapper.updateDocumentFromRequest(document, request);
        return userDocumentMapper.toDocumentResponse(userDocumentRepository.save(document));
    }

    public void deleteDocumentById(long docId){
        if(userDocumentRepository.existsById(docId)){
            userDocumentRepository.deleteById(docId);
        } else {
            throw new RuntimeException("Document not found");
        }
    }
}
