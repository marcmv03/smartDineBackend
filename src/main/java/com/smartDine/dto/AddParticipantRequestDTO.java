package com.smartDine.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO for adding a friend as a participant to a reservation.
 */
public class AddParticipantRequestDTO {

    @NotNull(message = "Friend ID is required")
    private Long friendId;

    public AddParticipantRequestDTO() {
    }

    public AddParticipantRequestDTO(Long friendId) {
        this.friendId = friendId;
    }

    public Long getFriendId() {
        return friendId;
    }

    public void setFriendId(Long friendId) {
        this.friendId = friendId;
    }
}
