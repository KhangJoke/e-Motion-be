package com.swp391.e_Motion_be.repository;

import com.swp391.e_Motion_be.entity.Report;
import com.swp391.e_Motion_be.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByUser(User user);
}
