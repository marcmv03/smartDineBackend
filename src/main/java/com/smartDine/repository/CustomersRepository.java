package com.smartDine.repository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartDine.entity.Customer;

@Repository
public interface  CustomersRepository extends  JpaRepository<Customer,Integer> {
    Optional<Customer> findById(Long id);
    Optional<Customer> findByNumber(Long number);
     List<Customer> findByNameContainingIgnoreCase(String name);
    Optional<Customer> findByEmailOrNumber(String email, Long number);
    Optional<Customer> findByEmail(String mail );

}
