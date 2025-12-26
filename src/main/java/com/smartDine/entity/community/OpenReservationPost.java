package com.smartDine.entity.community;

import com.smartDine.entity.Reservation;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * A community post that links to an existing reservation, allowing other users to join.
 * The currentParticipants count includes only those who joined via this post,
 * not the original creator of the reservation.
 */
@Entity
@Table(name = "open_reservation_posts")
@DiscriminatorValue("OPEN_RESERVATION")
public class OpenReservationPost extends CommunityPost {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @Column(name = "max_participants", nullable = false)
    private int maxParticipants;

    @Column(name = "current_participants", nullable = false)
    private int currentParticipants = 0;

    public OpenReservationPost() {
        setType(PostType.OPEN_RESERVATION);
    }

    public Reservation getReservation() {
        return reservation;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    public int getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(int maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public int getCurrentParticipants() {
        return currentParticipants;
    }

    public void setCurrentParticipants(int currentParticipants) {
        this.currentParticipants = currentParticipants;
    }

    /**
     * Increments the current participants count by one.
     */
    public void incrementParticipants() {
        this.currentParticipants++;
    }

    /**
     * Checks if there are available slots for new participants.
     * @return true if slots are available, false otherwise
     */
    public boolean hasAvailableSlots() {
        return currentParticipants < maxParticipants;
    }
}
