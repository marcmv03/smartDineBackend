package com.smartDine.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.smartDine.entity.Request;
import com.smartDine.entity.RequestStatus;
import com.smartDine.entity.RequestType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for Request entity responses.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RequestDTO {

    private Long id;
    private Long senderId;
    private String senderName;
    private String senderEmail;
    private Long receiverId;
    private String receiverName;
    private RequestType requestType;
    private RequestStatus status;
    private LocalDateTime createdAt;

    /**
     * Convert a Request entity to RequestDTO.
     */
    public static RequestDTO fromEntity(Request request) {
        RequestDTO dto = new RequestDTO();
        dto.setId(request.getId());
        dto.setSenderId(request.getSender().getId());
        dto.setSenderName(request.getSender().getName());
        dto.setSenderEmail(request.getSender().getEmail());
        dto.setReceiverId(request.getReceiver().getId());
        dto.setReceiverName(request.getReceiver().getName());
        dto.setRequestType(request.getRequestType());
        dto.setStatus(request.getStatus());
        dto.setCreatedAt(request.getDate()); // Use getDate() from Notification parent
        return dto;
    }

    /**
     * Convert a list of Request entities to RequestDTOs.
     */
    public static List<RequestDTO> fromEntity(List<Request> requests) {
        return requests.stream()
                .map(RequestDTO::fromEntity)
                .collect(Collectors.toList());
    }
}
