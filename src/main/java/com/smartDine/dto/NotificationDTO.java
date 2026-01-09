package com.smartDine.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.smartDine.entity.Notification;
import com.smartDine.entity.Request;
import com.smartDine.entity.RequestType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for Notification entity responses.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {

    private Long id;
    private String message;
    private LocalDateTime date;
    private boolean read;
    private String notificationType;
    private Long requestId; // Only set if this notification is a Request
    private RequestType requestType; // Only set if this notification is a Request
    private Long senderId; // Only set if this notification is a Request
    private String senderName; // Only set if this notification is a Request

    /**
     * Convert a Notification entity to NotificationDTO.
     */
    public static NotificationDTO fromEntity(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setMessage(notification.getMessage());
        dto.setDate(notification.getDate());
        dto.setRead(notification.isRead());
        dto.setNotificationType(notification.getNotificationType());
        
        // If the notification is a Request, set the request-specific fields
        if (notification instanceof Request request) {
            dto.setRequestId(notification.getId());
            dto.setRequestType(request.getRequestType());
            if (request.getSender() != null) {
                dto.setSenderId(request.getSender().getId());
                dto.setSenderName(request.getSender().getName());
            }
        } else {
            dto.setRequestId(null);
            dto.setRequestType(null);
            dto.setSenderId(null);
            dto.setSenderName(null);
        }
        return dto;
    }

    /**
     * Convert a list of Notification entities to NotificationDTOs.
     */
    public static List<NotificationDTO> fromEntity(List<Notification> notifications) {
        return notifications.stream()
                .map(NotificationDTO::fromEntity)
                .collect(Collectors.toList());
    }
}
