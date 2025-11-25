package com.smartDine.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.smartDine.entity.Community;
import com.smartDine.entity.CommunityType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CommunityDTO {
    private Long id;
    
    @NotBlank(message = "Name is required")
    private String name;
    
    @NotBlank(message = "Description is required")
    private String description;
    
    private String imageUrl;
    
    private boolean visibility;
    @NotNull(message = "Community type is required")
    private String communityType;
    
    private int memberCount;
    
    private Long ownerId;
    
    private String ownerName;

    // Constructors
    public CommunityDTO() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public boolean isVisibility() { return visibility; }
    public void setVisibility(boolean visibility) { this.visibility = visibility; }

    public String getCommunityType() { return communityType; }
    public void setCommunityType(String communityType) { this.communityType = communityType; }

    public int getMemberCount() { return memberCount; }
    public void setMemberCount(int memberCount) { this.memberCount = memberCount; }

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    // Conversion methods
    public static Community toEntity(CommunityDTO dto) {
        Community community = new Community();
        if (dto.getId() != null) {
            community.setId(dto.getId());
        }
        community.setName(dto.getName());
        community.setDescription(dto.getDescription());
        community.setImageUrl(dto.getImageUrl());
        community.setVisibility(dto.isVisibility());
        if (dto.getCommunityType() != null) {
            community.setCommunityType(CommunityType.valueOf(dto.getCommunityType()));
        }
        return community;
    }

    public static CommunityDTO fromEntity(Community community) {
        CommunityDTO dto = new CommunityDTO();
        dto.setId(community.getId());
        dto.setName(community.getName());
        dto.setDescription(community.getDescription());
        dto.setImageUrl(community.getImageUrl());
        dto.setVisibility(community.isVisibility());
        if (community.getCommunityType() != null) {
            dto.setCommunityType(community.getCommunityType().name());
        }
        
        if (community.getMembers() != null) {
            dto.setMemberCount(community.getMembers().size());
            // Find owner
            community.getMembers().stream()
                .filter(m -> "OWNER".equals(m.getMemberRole().name()))
                .findFirst()
                .ifPresent(owner -> {
                    dto.setOwnerId(owner.getUser().getId());
                    dto.setOwnerName(owner.getUser().getName());
                });
        }
        
        return dto;
    }

    public static List<CommunityDTO> fromEntity(List<Community> communities) {
        return communities.stream()
            .map(CommunityDTO::fromEntity)
            .collect(Collectors.toList());
    }
}
