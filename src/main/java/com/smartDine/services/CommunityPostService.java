package com.smartDine.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.smartDine.dto.community.post.CreateCommunityPostRequestDTO;
import com.smartDine.dto.community.post.UpdateCommunityPostRequestDTO;
import com.smartDine.entity.community.CommunityPost;

public interface CommunityPostService {
    CommunityPost createPost(Long currentUserId, CreateCommunityPostRequestDTO requestDTO);

    CommunityPost getPostById(Long postId, Long currentUserId);

    Page<CommunityPost> getPostsByMember(Long memberId, String search, Pageable pageable, Long currentUserId);

    Page<CommunityPost> getPostsByCommunity(Long communityId, String search, Pageable pageable, Long currentUserId);

    CommunityPost updatePost(Long postId, Long currentUserId, UpdateCommunityPostRequestDTO requestDTO);

    void deletePost(Long postId, Long currentUserId);
}
