package com.smartDine.dto.community.post;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.smartDine.entity.community.CommunityPost;
import com.smartDine.entity.community.OpenReservationPost;
import com.smartDine.entity.community.PostType;

/**
 * DTO for open reservation post responses.
 * Includes all base post fields plus reservation-specific information.
 */
public class OpenReservationPostResponseDTO {
    
    private Long id;
    private String title;
    private String description;
    private LocalDateTime publishedAt;
    private Long communityId;
    private Long authorId;
    private String authorName;
    private PostType type;
    
    // Reservation-specific fields
    private Long reservationId;
    private int maxParticipants;
    private int currentParticipants;
    private String restaurantName;
    private LocalDate reservationDate;
    private Double timeSlotStart;
    private Double timeSlotEnd;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public Long getCommunityId() {
        return communityId;
    }

    public void setCommunityId(Long communityId) {
        this.communityId = communityId;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public PostType getType() {
        return type;
    }

    public void setType(PostType type) {
        this.type = type;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
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

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public LocalDate getReservationDate() {
        return reservationDate;
    }

    public void setReservationDate(LocalDate reservationDate) {
        this.reservationDate = reservationDate;
    }

    public Double getTimeSlotStart() {
        return timeSlotStart;
    }

    public void setTimeSlotStart(Double timeSlotStart) {
        this.timeSlotStart = timeSlotStart;
    }

    public Double getTimeSlotEnd() {
        return timeSlotEnd;
    }

    public void setTimeSlotEnd(Double timeSlotEnd) {
        this.timeSlotEnd = timeSlotEnd;
    }

    /**
     * Converts an OpenReservationPost entity to this DTO.
     */
    public static OpenReservationPostResponseDTO fromEntity(OpenReservationPost post) {
        OpenReservationPostResponseDTO dto = new OpenReservationPostResponseDTO();
        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setDescription(post.getDescription());
        dto.setPublishedAt(post.getPublishedAt());
        dto.setType(post.getType());
        dto.setMaxParticipants(post.getMaxParticipants());
        dto.setCurrentParticipants(post.getCurrentParticipants());
        
        if (post.getCommunity() != null) {
            dto.setCommunityId(post.getCommunity().getId());
        }
        
        if (post.getAuthor() != null) {
            dto.setAuthorId(post.getAuthor().getId());
            dto.setAuthorName(post.getAuthor().getUser().getName());
        }
        
        if (post.getReservation() != null) {
            dto.setReservationId(post.getReservation().getId());
            dto.setReservationDate(post.getReservation().getDate());
            
            if (post.getReservation().getRestaurant() != null) {
                dto.setRestaurantName(post.getReservation().getRestaurant().getName());
            }
            
            if (post.getReservation().getTimeSlot() != null) {
                dto.setTimeSlotStart(post.getReservation().getTimeSlot().getStartTime());
                dto.setTimeSlotEnd(post.getReservation().getTimeSlot().getEndTime());
            }
        }
        
        return dto;
    }

    /**
     * Converts a list of OpenReservationPost entities to DTOs.
     */
    public static List<OpenReservationPostResponseDTO> fromEntity(List<OpenReservationPost> posts) {
        return posts.stream()
            .map(OpenReservationPostResponseDTO::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Converts a CommunityPost to this DTO if it's an OpenReservationPost, otherwise returns null.
     */
    public static OpenReservationPostResponseDTO fromCommunityPost(CommunityPost post) {
        if (post instanceof OpenReservationPost) {
            return fromEntity((OpenReservationPost) post);
        }
        return null;
    }
}
