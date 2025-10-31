package com.swp391.e_Motion_be.repository;

import com.swp391.e_Motion_be.entity.Staff;
import com.swp391.e_Motion_be.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface StaffRepository extends JpaRepository<Staff, Long> {
    Optional<Staff> findByUser_Email(String email);
    boolean existsByUser(User user);
    Long countByStation_Id(Long stationId);
    Optional<Staff> findByUser_Id(Long id);
}
