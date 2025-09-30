package com.swp391.e_Motion_be.entity;

import com.swp391.e_Motion_be.enums.DepositStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "deposits")
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Deposit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "deposit_id")
    Long id;
    @Column(name = "deposit_status")
    @Enumerated(EnumType.STRING)
    DepositStatus status;
    @Column(name = "deposit_amount")
    long amount;
    @Column(name = "create_at", updatable = false)
    @CreationTimestamp
    LocalDateTime createdAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id")
    Reservation reservation;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rental_id")
    Rental rental;

    @OneToMany(mappedBy = "deposit", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    List<Payment> payments;
}
