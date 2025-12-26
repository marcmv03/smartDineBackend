package com.smartDine.services;

import java.util.List;

import com.smartDine.dto.community.post.CreateCommunityPostRequestDTO;
import com.smartDine.dto.community.post.CreateOpenReservationPostDTO;
import com.smartDine.dto.community.post.UpdateCommunityPostRequestDTO;
import com.smartDine.entity.community.CommunityPost;
import com.smartDine.entity.community.OpenReservationPost;

public interface CommunityPostService {
    CommunityPost createPost(Long currentUserId, CreateCommunityPostRequestDTO requestDTO);

    CommunityPost getPostById(Long postId, Long currentUserId);

    List<CommunityPost> getPostsByMember(Long memberId, String search, Long currentUserId);

    List<CommunityPost> getPostsByCommunity(Long communityId, String search, Long currentUserId);

    CommunityPost updatePost(Long postId, Long currentUserId, UpdateCommunityPostRequestDTO requestDTO);

    void deletePost(Long postId, Long currentUserId);

    /**
     * Creates an open reservation post in a community.
     * 
     * @param currentUserId The ID of the user creating the post
     * @param requestDTO The request data including reservation info
     * @return The created OpenReservationPost
     */
    OpenReservationPost createOpenReservationPost(Long currentUserId, CreateOpenReservationPostDTO requestDTO);

    /**
     * Allows a user to join an open reservation post.
     * 
     * @param postId The ID of the post to join
     * @param currentUserId The ID of the user joining
     * @return The updated OpenReservationPost
     */
    OpenReservationPost joinOpenReservationPost(Long postId, Long currentUserId);
}
