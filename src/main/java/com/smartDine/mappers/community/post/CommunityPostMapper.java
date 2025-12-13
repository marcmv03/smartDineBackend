package com.smartDine.mappers.community.post;

import com.smartDine.dto.community.post.CommunityPostResponseDTO;
import com.smartDine.dto.community.post.CommunityPostSummaryDTO;
import com.smartDine.dto.community.post.CreateCommunityPostRequestDTO;
import com.smartDine.dto.community.post.UpdateCommunityPostRequestDTO;
import com.smartDine.entity.Community;
import com.smartDine.entity.Member;
import com.smartDine.entity.community.CommunityPost;

public class CommunityPostMapper {

    public CommunityPost toEntity(CreateCommunityPostRequestDTO dto, Community community, Member author) {
        CommunityPost post = new CommunityPost();
        post.setTitle(dto.getTitle());
        post.setDescription(dto.getDescription());
        post.setCommunity(community);
        post.setAuthor(author);
        return post;
    }

    public void updateEntity(CommunityPost post, UpdateCommunityPostRequestDTO dto) {
        if (dto.getTitle() != null) {
            post.setTitle(dto.getTitle());
        }
        if (dto.getDescription() != null) {
            post.setDescription(dto.getDescription());
        }
    }

    public CommunityPostResponseDTO toResponseDTO(CommunityPost post) {
        CommunityPostResponseDTO dto = new CommunityPostResponseDTO();
        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setDescription(post.getDescription());
        dto.setPublishedAt(post.getPublishedAt());
        if (post.getCommunity() != null) {
            dto.setCommunityId(post.getCommunity().getId());
        }
        if (post.getAuthor() != null) {
            dto.setAuthorId(post.getAuthor().getUser().getId());
            dto.setAuthorName(post.getAuthor().getUser().getName());
        }
        return dto;
    }

    public CommunityPostSummaryDTO toSummaryDTO(CommunityPost post) {
        CommunityPostSummaryDTO dto = new CommunityPostSummaryDTO();
        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setPublishedAt(post.getPublishedAt());
        if (post.getCommunity() != null) {
            dto.setCommunityId(post.getCommunity().getId());
        }
        if (post.getAuthor() != null) {
            dto.setAuthorId(post.getAuthor().getUser().getId());
        }
        return dto;
    }
}
