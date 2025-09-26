package com.swp391.e_Motion_be.repository;

import com.swp391.e_Motion_be.entity.Staff;
import com.swp391.e_Motion_be.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StaffRepository extends JpaRepository<Staff, Long> {
    boolean existsByUser(User user);
}
