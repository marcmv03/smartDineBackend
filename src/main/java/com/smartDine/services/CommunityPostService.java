package com.smartDine.services;

import java.util.List;

import com.smartDine.dto.community.post.CreateCommunityPostRequestDTO;
import com.smartDine.dto.community.post.UpdateCommunityPostRequestDTO;
import com.smartDine.entity.community.CommunityPost;

public interface CommunityPostService {
    CommunityPost createPost(Long currentUserId, CreateCommunityPostRequestDTO requestDTO);

    CommunityPost getPostById(Long postId, Long currentUserId);

    List<CommunityPost> getPostsByMember(Long memberId, String search, Long currentUserId);

    List<CommunityPost> getPostsByCommunity(Long communityId, String search, Long currentUserId);

    CommunityPost updatePost(Long postId, Long currentUserId, UpdateCommunityPostRequestDTO requestDTO);

    void deletePost(Long postId, Long currentUserId);
}
