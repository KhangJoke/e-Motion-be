package com.swp391.e_Motion_be.repository;

import com.swp391.e_Motion_be.entity.Deposit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface DepositRepository extends JpaRepository<Deposit, Long> {
    Optional<Deposit> findByRental_Id(long rentalId);
    Optional<Deposit> findByReservation_Id(long id);
}
