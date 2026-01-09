package com.smartDine.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.smartDine.entity.Customer;
import com.smartDine.entity.Friendship;
import com.smartDine.entity.User;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for Friend/Friendship responses.
 * Represents the "other" user in a friendship from the current user's perspective.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FriendDTO {

    private Long friendshipId;
    private Long friendId;
    private String friendName;
    private String friendEmail;
    private LocalDateTime friendsSince;

    /**
     * Convert a Friendship entity to FriendDTO from the perspective of the current user.
     * 
     * @param friendship The friendship entity
     * @param currentUser The user requesting the friend list
     * @return FriendDTO representing the other user in the friendship
     */
    public static FriendDTO fromEntity(Friendship friendship, User currentUser) {
        FriendDTO dto = new FriendDTO();
        dto.setFriendshipId(friendship.getId());
        
        Customer friend = friendship.getOtherUser(currentUser);
        dto.setFriendId(friend.getId());
        dto.setFriendName(friend.getName());
        dto.setFriendEmail(friend.getEmail());
        dto.setFriendsSince(friendship.getCreatedAt());
        
        return dto;
    }

    /**
     * Convert a list of Friendship entities to FriendDTOs.
     * 
     * @param friendships List of friendship entities
     * @param currentUser The user requesting the friend list
     * @return List of FriendDTOs
     */
    public static List<FriendDTO> fromEntity(List<Friendship> friendships, User currentUser) {
        return friendships.stream()
                .map(f -> fromEntity(f, currentUser))
                .collect(Collectors.toList());
    }
}
