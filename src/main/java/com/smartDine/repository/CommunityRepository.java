package com.smartDine.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartDine.entity.Community;

@Repository
public interface CommunityRepository extends JpaRepository<Community, Long> {
    List<Community> findByNameContainingIgnoreCase(String name);
    Optional<Community> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
}
