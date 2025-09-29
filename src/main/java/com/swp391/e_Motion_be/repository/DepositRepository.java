package com.swp391.e_Motion_be.repository;

import com.swp391.e_Motion_be.entity.Deposit;
import org.springframework.data.jpa.repository.JpaRepository;


public interface DepositRepository extends JpaRepository<Deposit, Long> {
    Deposit findByReservation_Code(String code);
    Deposit findByRental_Id(long rentalId);
}
