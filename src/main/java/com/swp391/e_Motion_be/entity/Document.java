package com.swp391.e_Motion_be.entity;

import com.swp391.e_Motion_be.enums.DocType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "user_documents")
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Document {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    long id;
    @Column(name = "img_url")
    String imgUrl;
    @Enumerated(EnumType.STRING)
    @Column(name = "doc_type")
    DocType docType;
    @Column(name = "doc_number", unique = true)
    String docNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
