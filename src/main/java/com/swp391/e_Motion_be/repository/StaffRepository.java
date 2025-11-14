package com.swp391.e_Motion_be.repository;

import com.swp391.e_Motion_be.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface StaffRepository extends JpaRepository<Staff, Long> {
    Optional<Staff> findByUser_EmailAndIsDeleteFalse(String email);
    boolean existsByUser_Email(String email);
    Long countByIsDeleteFalseAndStation_Id(Long stationId);

}
