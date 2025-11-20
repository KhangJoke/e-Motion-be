package com.swp391.e_Motion_be.repository;

import com.swp391.e_Motion_be.entity.Report;
import com.swp391.e_Motion_be.entity.User;
import com.swp391.e_Motion_be.enums.report.ReportStatus;
import com.swp391.e_Motion_be.enums.report.ReportType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByUser(User user);

    @Query("""
    SELECT r FROM Report r
    WHERE (:type IS NULL OR r.type = :type)
      AND (:status IS NULL OR r.status = :status)
      AND (:title IS NULL OR LOWER(r.title) LIKE LOWER(CONCAT('%', :title, '%')))
      AND r.delete = false
""")
    List<Report> searchReports(
            @Param("type") ReportType type,
            @Param("status") ReportStatus status,
            @Param("title") String title
    );
}
