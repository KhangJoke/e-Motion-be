package com.swp391.e_Motion_be.repository;

import com.swp391.e_Motion_be.entity.RentalCheckList;
import com.swp391.e_Motion_be.enums.CheckType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RentalCheckListRepository extends JpaRepository<RentalCheckList, Long> {
    List<RentalCheckList> findByRental_Id(Long rentalId);
    List<RentalCheckList> findByRental_IdAndTypeIn(Long rentalId,List<CheckType> type);
    List<RentalCheckList> findByStaff_User_EmailContainsAndTypeIn(String email,List<CheckType> type);
    List<RentalCheckList> findByTypeIn(List<CheckType> type);
    int countByStaff_IdAndType(Long staffId, CheckType type);
}
