package com.swp391.e_Motion_be.repository;

import com.swp391.e_Motion_be.entity.RentalCheckList;
import com.swp391.e_Motion_be.enums.CheckType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RentalCheckListRepository extends JpaRepository<RentalCheckList, Long> {
    List<RentalCheckList> findByRental_Id(Long rentalId);
    List<RentalCheckList> findByRental_IdAndTypeIn(Long rentalId,List<CheckType> type);
    List<RentalCheckList> findByStaff_User_EmailContainsAndTypeIn(String email,List<CheckType> type);
    List<RentalCheckList> findByTypeIn(List<CheckType> type);
    int countByStaff_IdAndType(Long staffId, CheckType type);
    @Query(value = """
    SELECT rc.*
    FROM rental_checklists rc
    JOIN (
        SELECT rental_id,
               MAX(CASE WHEN check_type = 'CHECK_OUT' THEN 2 ELSE 1 END) AS priority
        FROM rental_checklists
        GROUP BY rental_id
    ) p ON rc.rental_id = p.rental_id
    WHERE (p.priority = 2 AND rc.check_type = 'CHECK_OUT')
       OR (p.priority = 1 AND rc.check_type = 'CHECK_IN')
    """, nativeQuery = true)
    List<RentalCheckList> findLatestChecklistPerRental();
    Page<RentalCheckList> findByTypeInAndStaff_User_EmailContaining(List<CheckType> typeList, String search, Pageable pageable);
}
