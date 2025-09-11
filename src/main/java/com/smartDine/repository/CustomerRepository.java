package com.smartDine.repository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartDine.entity.Customer;

@Repository
public interface  CustomerRepository extends  JpaRepository<Customer,Integer> {
    Optional<Customer> findById(Long id);
    Optional<Customer> findByPhoneNumber(Long number);
     List<Customer> findByNameContainingIgnoreCase(String name);
    Optional<Customer> findByEmailOrPhoneNumber(String email, Long number);
    Optional<Customer> findByEmail(String mail );

}
