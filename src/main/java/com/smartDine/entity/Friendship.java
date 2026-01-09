package com.smartDine.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing a bidirectional friendship between two customers.
 * The order of userA and userB is normalized to prevent duplicate entries.
 */
@Entity
@Table(name = "friendships", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_a_id", "user_b_id"})
})
@Getter
@Setter
@NoArgsConstructor
public class Friendship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_a_id", nullable = false)
    private Customer userA;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_b_id", nullable = false)
    private Customer userB;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * Creates a friendship with normalized order (lower ID first).
     * This prevents duplicate entries like (A,B) and (B,A).
     */
    public Friendship(Customer userA, Customer userB) {
        // Normalize order: lower ID goes to userA
        if (userA.getId() < userB.getId()) {
            this.userA = userA;
            this.userB = userB;
        } else {
            this.userA = userB;
            this.userB = userA;
        }
    }

    /**
     * Check if a user is part of this friendship.
     * @param user The user to check
     * @return true if the user is either userA or userB
     */
    public boolean involves(User user) {
        return userA.getId().equals(user.getId()) || userB.getId().equals(user.getId());
    }

    /**
     * Get the other user in this friendship.
     * @param user The current user
     * @return The other user in the friendship
     * @throws IllegalArgumentException if the user is not part of this friendship
     */
    public Customer getOtherUser(User user) {
        if (userA.getId().equals(user.getId())) {
            return userB;
        } else if (userB.getId().equals(user.getId())) {
            return userA;
        }
        throw new IllegalArgumentException("User is not part of this friendship");
    }
}
