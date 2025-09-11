package com.smartDine.repository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartDine.entity.Business;

@Repository
public interface BusinessRepository extends JpaRepository<Business, Long> {
    Optional<Business> findByEmail(String email);
    List<Business> findByNameContainingIgnoreCase(String name);
    Optional<Business> findByPhoneNumber(Long phoneNumber);
    Optional<Business> findByEmailOrPhoneNumber(String email, Long number);
}