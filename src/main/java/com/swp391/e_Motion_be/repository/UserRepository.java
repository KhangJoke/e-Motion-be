package com.swp391.e_Motion_be.repository;

import com.swp391.e_Motion_be.entity.User;
import com.swp391.e_Motion_be.enums.Role;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailAndRole(String email, Role role);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    long countByRole(Role role);
    List<User> findByEmailContains(String email);
    Page<User> findByBlockedInAndRoleInAndEmailContains(List<Boolean> blockedIn, List<Role> roles, String search, Pageable pageable);
    Page<User> findByBlockedInAndRoleInAndEmailContainsAndStaff_Station_Id(List<Boolean> blockedIn, List<Role> roles, String search, Long stationId, Pageable pageable);
    @Query("SELECT u FROM User u LEFT JOIN u.staff s " +
            "WHERE u.blocked IN :blockedList " +
            "AND u.role IN :roles " +
            "AND u.email LIKE %:search% " +
            "AND (u.role = 'ROLE_USER' OR (u.role = 'ROLE_STAFF' AND s.station.id = :stationId))")
    Page<User> findUsersByStaff(
            @Param("blockedList") List<Boolean> blockedList,
            @Param("roles") List<Role> roles,
            @Param("search") String search,
            @Param("stationId") Long stationId,
            Pageable pageable
    );
}
