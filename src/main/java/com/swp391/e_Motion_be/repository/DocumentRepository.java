package com.swp391.e_Motion_be.repository;


import com.swp391.e_Motion_be.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByUser_Email(String email);
    boolean existsByNumber(String docNumber );
}
