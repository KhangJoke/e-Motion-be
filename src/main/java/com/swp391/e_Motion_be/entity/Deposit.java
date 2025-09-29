package com.swp391.e_Motion_be.entity;

import com.swp391.e_Motion_be.enums.DepositStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;

@Entity
@Table(name = "deposits")
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Deposit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(name = "deposit_status")
    @Enumerated(EnumType.STRING)
    DepositStatus status;
    @Column(name = "deposit_amoount")
    long amount;
    @Column(name = "create_at", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    LocalDateTime createAt;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id")
    Reservation reservation;
}
