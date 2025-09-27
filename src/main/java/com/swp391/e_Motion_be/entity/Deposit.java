package com.swp391.e_Motion_be.entity;

import com.swp391.e_Motion_be.enums.DepositStatus;
import com.swp391.e_Motion_be.enums.DepositType;
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
    @Column(name = "deposit_type")
    @Enumerated(EnumType.STRING)
    DepositType type;
    @Column(name = "deposit_amoount")
    long amount;
    @Column(name = "create_at", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    LocalDateTime createAt;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    User user;
}
