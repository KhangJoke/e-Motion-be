package com.swp391.e_Motion_be.repository;

import com.swp391.e_Motion_be.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Integer> {
}
