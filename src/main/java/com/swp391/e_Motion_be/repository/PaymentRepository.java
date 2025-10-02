package com.swp391.e_Motion_be.repository;

import com.swp391.e_Motion_be.entity.Payment;
import com.swp391.e_Motion_be.enums.payment.PaymentMethod;
import com.swp391.e_Motion_be.enums.payment.PaymentStatus;
import com.swp391.e_Motion_be.enums.payment.PaymentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByTxnRef(String txnRef);
    Optional<Payment> findByDepositIdAndType(long depositId, PaymentType type);
    List<Payment> getPaymentsByStatus(PaymentStatus status);
    List<Payment> getPaymentsByType(PaymentType type);
    List<Payment> getPaymentsByMethod(PaymentMethod method);
}
