package com.smartDine.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.smartDine.entity.Customer;
import com.smartDine.entity.Friendship;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    /**
     * Find all friendships where the user is either userA or userB.
     */
    @Query("SELECT f FROM Friendship f WHERE f.userA = :user OR f.userB = :user")
    List<Friendship> findAllByUser(@Param("user") Customer user);

    /**
     * Check if a friendship exists between two users (in any order).
     */
    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM Friendship f " +
           "WHERE (f.userA = :userA AND f.userB = :userB) OR (f.userA = :userB AND f.userB = :userA)")
    boolean existsBetween(@Param("userA") Customer userA, @Param("userB") Customer userB);

    /**
     * Find a specific friendship between two users (in any order).
     */
    @Query("SELECT f FROM Friendship f " +
           "WHERE (f.userA = :userA AND f.userB = :userB) OR (f.userA = :userB AND f.userB = :userA)")
    Optional<Friendship> findByUsers(@Param("userA") Customer userA, @Param("userB") Customer userB);
}
