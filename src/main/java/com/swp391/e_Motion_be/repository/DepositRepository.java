package com.swp391.e_Motion_be.repository;

import com.swp391.e_Motion_be.entity.Deposit;
import com.swp391.e_Motion_be.entity.Document;
import com.swp391.e_Motion_be.enums.DepositStatus;
import com.swp391.e_Motion_be.enums.DepositType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DepositRepository extends JpaRepository<Deposit, Long> {
    List<Deposit> findByUser_Email(String email);
    Optional<Deposit> findByUser_EmailAndTypeAndStatus(String email, DepositType type, DepositStatus status);
}
