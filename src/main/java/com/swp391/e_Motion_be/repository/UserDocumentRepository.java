package com.swp391.e_Motion_be.repository;


import com.swp391.e_Motion_be.entity.UserDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserDocumentRepository extends JpaRepository<UserDocument, Long> {
    List<UserDocument> findByUserId(long userId);
    boolean existsByDocNumber(String docNumber );
}
