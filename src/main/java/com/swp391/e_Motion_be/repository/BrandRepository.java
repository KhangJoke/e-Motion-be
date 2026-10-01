package com.swp391.e_Motion_be.repository;

import com.swp391.e_Motion_be.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {
    List<Brand> findByActiveTrueOrderByDisplayOrderAsc();
    Optional<Brand> findByCode(String code);
    Optional<Brand> findByCodeIgnoreCase(String code);
    Optional<Brand> findByNameIgnoreCase(String name);
    boolean existsByCode(String code);
    boolean existsByName(String name);
}
