package com.swp391.e_Motion_be.repository;


import com.swp391.e_Motion_be.entity.Document;
import com.swp391.e_Motion_be.enums.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByUser_Email(String email);
    boolean existsByNumber(String docNumber );
    boolean existsByUser_IdAndType(long userId, DocumentType type);
    boolean existsByUser_EmailAndType(String email, DocumentType type);
    Document findByUser_EmailAndType(String email, DocumentType type);
}
