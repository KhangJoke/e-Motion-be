package com.swp391.e_Motion_be.entity;

import com.swp391.e_Motion_be.enums.DocumentType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "documents")
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Document {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "doc_id")
    Long id;
    @Column(name = "doc_img_url")
    String imgUrl;
    @Enumerated(EnumType.STRING)
    @Column(name = "doc_type", nullable = false)
    DocumentType type;
    @Column(name = "doc_number", unique = true, nullable = false)
    String number;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
