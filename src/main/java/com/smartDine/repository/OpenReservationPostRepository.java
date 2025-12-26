package com.smartDine.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartDine.entity.Community;
import com.smartDine.entity.Reservation;
import com.smartDine.entity.community.OpenReservationPost;

@Repository
public interface OpenReservationPostRepository extends JpaRepository<OpenReservationPost, Long> {
    
    /**
     * Find all open reservation posts for a specific community.
     */
    List<OpenReservationPost> findByCommunity(Community community);
    
    /**
     * Find all open reservation posts for a specific reservation.
     */
    List<OpenReservationPost> findByReservation(Reservation reservation);
    
    /**
     * Check if an open reservation post already exists for a given reservation in a community.
     */
    Optional<OpenReservationPost> findByReservationAndCommunity(Reservation reservation, Community community);
    
    /**
     * Check if an open reservation post already exists for a given reservation.
     */
    boolean existsByReservation(Reservation reservation);
}
