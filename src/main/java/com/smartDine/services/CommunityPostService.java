package com.smartDine.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.smartDine.dto.community.post.CommunityPostResponseDTO;
import com.smartDine.dto.community.post.CommunityPostSummaryDTO;
import com.smartDine.dto.community.post.CreateCommunityPostRequestDTO;
import com.smartDine.dto.community.post.UpdateCommunityPostRequestDTO;

public interface CommunityPostService {
    CommunityPostResponseDTO createPost(Long currentUserId, CreateCommunityPostRequestDTO requestDTO);

    CommunityPostResponseDTO getPostById(Long postId, Long currentUserId);

    Page<CommunityPostSummaryDTO> getPostsByMember(Long memberId, String search, Pageable pageable, Long currentUserId);

    Page<CommunityPostSummaryDTO> getPostsByCommunity(Long communityId, String search, Pageable pageable, Long currentUserId);

    CommunityPostResponseDTO updatePost(Long postId, Long currentUserId, UpdateCommunityPostRequestDTO requestDTO);

    void deletePost(Long postId, Long currentUserId);
}
