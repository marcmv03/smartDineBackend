package com.smartDine.dto.community.post;

import com.smartDine.entity.Community;
import com.smartDine.entity.Member;
import com.smartDine.entity.Reservation;
import com.smartDine.entity.community.OpenReservationPost;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO for creating an open reservation post.
 * Extends the basic post fields with reservation-specific data.
 */
public class CreateOpenReservationPostDTO {
    
    private Long communityId;

    @NotBlank(message = "Title is required")
    @Size(max = 150, message = "Title cannot exceed 150 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    @NotNull(message = "Reservation ID is required")
    private Long reservationId;

    @NotNull(message = "Max participants is required")
    @Min(value = 1, message = "Max participants must be at least 1")
    private Integer maxParticipants;

    // Getters and Setters
    public Long getCommunityId() {
        return communityId;
    }

    public void setCommunityId(Long communityId) {
        this.communityId = communityId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }

    public Integer getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(Integer maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    /**
     * Converts this DTO to an OpenReservationPost entity.
     * Note: The reservation must be set separately after validation.
     */
    public static OpenReservationPost toEntity(CreateOpenReservationPostDTO dto, 
                                                Community community, 
                                                Member author,
                                                Reservation reservation) {
        OpenReservationPost post = new OpenReservationPost();
        post.setTitle(dto.getTitle());
        post.setDescription(dto.getDescription());
        post.setCommunity(community);
        post.setAuthor(author);
        post.setReservation(reservation);
        post.setMaxParticipants(dto.getMaxParticipants());
        post.setCurrentParticipants(0);
        return post;
    }
}
