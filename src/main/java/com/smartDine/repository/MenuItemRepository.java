package com.smartDine.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartDine.entity.MenuItem;
@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem,Long> 
{
    Optional<MenuItem> findById(Long id);
}
