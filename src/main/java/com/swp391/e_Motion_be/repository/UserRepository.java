package com.swp391.e_Motion_be.repository;

import com.swp391.e_Motion_be.entity.User;
import com.swp391.e_Motion_be.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    long countByRole(Role role);
    List<User> findByEmailContains(String email);
    Page<User> findByBlockedInAndRoleInAndEmailContains(List<Boolean> blockedIn, List<Role> roles, String search, Pageable pageable);
    Page<User> findByBlockedInAndRoleInAndEmailContainsAndStaff_Station_Id(List<Boolean> blockedIn, List<Role> roles, String search, Long stationId, Pageable pageable);
}
