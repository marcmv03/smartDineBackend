package com.smartDine.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartDine.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByPhoneNumber(Long number);
    Boolean existsByEmail(String email);
    Boolean existsByPhoneNumber(Long number);
    Optional<User> findByEmailOrPhoneNumber(String email, Long number);
}