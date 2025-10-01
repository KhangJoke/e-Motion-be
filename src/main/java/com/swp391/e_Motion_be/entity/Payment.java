package com.swp391.e_Motion_be.entity;

import com.swp391.e_Motion_be.enums.payment.PaymentMethod;
import com.swp391.e_Motion_be.enums.payment.PaymentStatus;
import com.swp391.e_Motion_be.enums.payment.PaymentType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private long id;
    @Column(name = "total_amount", nullable = false)
    private long amount;
    @Column(name = "payment_method", nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentMethod method;
    @Column(name = "payment_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentType type;
    @Column(name = "payment_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(nullable = false, unique = true)
    private String txnRef;
    @Column(name = "payment_description")
    private String description;

    // Các tham số VNPay trả về
    private String responseCode;  // vnp_ResponseCode
    private String transactionNo; // vnp_TransactionNo
    private String bankCode;      // vnp_BankCode
    private LocalDateTime payDate;// vnp_PayDate

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rental_id")
    private Rental rental;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deposit_id")
    private Deposit deposit;
}
