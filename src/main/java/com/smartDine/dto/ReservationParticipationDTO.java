package com.smartDine.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.smartDine.entity.ReservationParticipation;

/**
 * DTO for reservation participation responses.
 */
public class ReservationParticipationDTO {

    private Long id;
    private Long reservationId;
    private Long participantId;
    private String participantName;
    private LocalDateTime addedAt;

    public ReservationParticipationDTO() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }

    public Long getParticipantId() {
        return participantId;
    }

    public void setParticipantId(Long participantId) {
        this.participantId = participantId;
    }

    public String getParticipantName() {
        return participantName;
    }

    public void setParticipantName(String participantName) {
        this.participantName = participantName;
    }

    public LocalDateTime getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(LocalDateTime addedAt) {
        this.addedAt = addedAt;
    }

    /**
     * Converts a ReservationParticipation entity to DTO.
     * 
     * @param entity The entity to convert
     * @return The DTO
     */
    public static ReservationParticipationDTO fromEntity(ReservationParticipation entity) {
        ReservationParticipationDTO dto = new ReservationParticipationDTO();
        dto.setId(entity.getId());
        dto.setReservationId(entity.getReservation().getId());
        dto.setParticipantId(entity.getCustomer().getId());
        dto.setParticipantName(entity.getCustomer().getName());
        dto.setAddedAt(entity.getJoinedAt());
        return dto;
    }

    /**
     * Converts a list of ReservationParticipation entities to DTOs.
     * 
     * @param entities The entities to convert
     * @return List of DTOs
     */
    public static List<ReservationParticipationDTO> fromEntity(List<ReservationParticipation> entities) {
        return entities.stream()
                .map(ReservationParticipationDTO::fromEntity)
                .collect(Collectors.toList());
    }
}
