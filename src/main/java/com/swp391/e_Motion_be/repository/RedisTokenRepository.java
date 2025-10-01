package com.swp391.e_Motion_be.repository;

import com.swp391.e_Motion_be.entity.RedisToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RedisTokenRepository extends CrudRepository<RedisToken, String> {
    RedisToken findByJwtId(String jwtId);
}
