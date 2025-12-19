package com.smartDine.dto.community.post;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;

import com.smartDine.entity.community.CommunityPost;

public class CommunityPostSummaryDTO extends CommunityPostBaseDTO {

    public static CommunityPostSummaryDTO fromEntity(CommunityPost post) {
        CommunityPostSummaryDTO dto = new CommunityPostSummaryDTO();
        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setPublishedAt(post.getPublishedAt());
        if (post.getCommunity() != null) {
            dto.setCommunityId(post.getCommunity().getId());
        }
        if (post.getAuthor() != null) {
            dto.setAuthorId(post.getAuthor().getId());
        }
        return dto;
    }

    public static List<CommunityPostSummaryDTO> fromEntity(List<CommunityPost> posts) {
        return posts.stream()
            .map(CommunityPostSummaryDTO::fromEntity)
            .collect(Collectors.toList());
    }

    public static Page<CommunityPostSummaryDTO> fromEntity(Page<CommunityPost> posts) {
        return posts.map(CommunityPostSummaryDTO::fromEntity);
    }
}
