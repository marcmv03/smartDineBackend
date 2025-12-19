package com.smartDine.dto.community.post;

import java.util.List;
import java.util.stream.Collectors;

import com.smartDine.entity.community.CommunityPost;

public class CommunityPostResponseDTO extends CommunityPostBaseDTO {
    private String description;
    private String authorName;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public static CommunityPostResponseDTO fromEntity(CommunityPost post) {
        CommunityPostResponseDTO dto = new CommunityPostResponseDTO();
        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setDescription(post.getDescription());
        dto.setPublishedAt(post.getPublishedAt());
        if (post.getCommunity() != null) {
            dto.setCommunityId(post.getCommunity().getId());
        }
        if (post.getAuthor() != null) {
            dto.setAuthorId(post.getAuthor().getId());
            dto.setAuthorName(post.getAuthor().getUser().getName());
        }
        return dto;
    }

    public static List<CommunityPostResponseDTO> fromEntity(List<CommunityPost> posts) {
        return posts.stream()
            .map(CommunityPostResponseDTO::fromEntity)
            .collect(Collectors.toList());
    }
}
