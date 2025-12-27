package com.smartDine.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smartDine.dto.community.post.CommunityPostResponseDTO;
import com.smartDine.dto.community.post.CommunityPostSummaryDTO;
import com.smartDine.dto.community.post.CreateCommunityPostRequestDTO;
import com.smartDine.dto.community.post.CreateOpenReservationPostDTO;
import com.smartDine.dto.community.post.OpenReservationPostResponseDTO;
import com.smartDine.dto.community.post.UpdateCommunityPostRequestDTO;
import com.smartDine.entity.User;
import com.smartDine.entity.community.CommunityPost;
import com.smartDine.entity.community.OpenReservationPost;
import com.smartDine.services.CommunityPostService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/smartdine/api")
@Validated
public class CommunityPostsController {

    private final CommunityPostService communityPostService;

    public CommunityPostsController(CommunityPostService communityPostService) {
        this.communityPostService = communityPostService;
    }

    @PostMapping("/communities/{id}/posts")
    public ResponseEntity<CommunityPostResponseDTO> createPost(@PathVariable Long id,
            @Valid @RequestBody CreateCommunityPostRequestDTO requestDTO,
            @AuthenticationPrincipal User user) {
        requestDTO.setCommunityId(id);
        CommunityPost post = communityPostService.createPost(user.getId(), requestDTO);
        return ResponseEntity.ok(CommunityPostResponseDTO.fromEntity(post));
    }

    @GetMapping("/communities/{communityId}/posts")
    public ResponseEntity<List<CommunityPostResponseDTO>> getPostsByCommunity(
            @PathVariable Long communityId,
            @RequestParam(required = false) String search,
            @AuthenticationPrincipal User user) {
        List<CommunityPost> posts = communityPostService.getPostsByCommunity(communityId, search,
                user != null ? user.getId() : null);
        return ResponseEntity.ok(CommunityPostResponseDTO.fromEntity(posts));
    }

    @GetMapping("/communities/posts/{postId}")
    public ResponseEntity<CommunityPostResponseDTO> getPost(
            @PathVariable Long postId,
            @AuthenticationPrincipal User user) {
        CommunityPost post = communityPostService.getPostById(postId, user != null ? user.getId() : null);
        return ResponseEntity.ok(CommunityPostResponseDTO.fromEntity(post));
    }

    @GetMapping("/communities/members/{memberId}/posts")
    public ResponseEntity<List<CommunityPostSummaryDTO>> getPostsByMember(
            @PathVariable Long memberId,
            @RequestParam(required = false) String search,
            @AuthenticationPrincipal User user) {
        List<CommunityPost> posts = communityPostService.getPostsByMember(memberId, search,
                user != null ? user.getId() : null);
        return ResponseEntity.ok(CommunityPostSummaryDTO.fromEntity(posts));
    }

    @PutMapping("/communities/posts/{postId}")
    public ResponseEntity<CommunityPostResponseDTO> updatePost(
            @PathVariable Long postId,
            @Valid @RequestBody UpdateCommunityPostRequestDTO requestDTO,
            @AuthenticationPrincipal User user) {
        CommunityPost post = communityPostService.updatePost(postId, user.getId(), requestDTO);
        return ResponseEntity.ok(CommunityPostResponseDTO.fromEntity(post));
    }

    @DeleteMapping("/communities/posts/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal User user) {
        communityPostService.deletePost(postId, user.getId());
        return ResponseEntity.noContent().build();
    }

    /**
     * Creates an open reservation post in a community.
     * Allows community members to share their reservations for others to join.
     */
    @PostMapping("/communities/{communityId}/openreservationposts")
    public ResponseEntity<OpenReservationPostResponseDTO> createOpenReservationPost(
            @PathVariable Long communityId,
            @Valid @RequestBody CreateOpenReservationPostDTO requestDTO,
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        requestDTO.setCommunityId(communityId);
        OpenReservationPost post = communityPostService.createOpenReservationPost(user.getId(), requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(OpenReservationPostResponseDTO.fromEntity(post));
    }

    /**
     * Allows a user to join an open reservation post.
     * The user will be added as a participant to the linked reservation.
     */
    @PutMapping("/communities/{communityId}/openreservationposts/{postId}")
    public ResponseEntity<OpenReservationPostResponseDTO> joinOpenReservationPost(
            @PathVariable Long communityId,
            @PathVariable Long postId,
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        OpenReservationPost post = communityPostService.joinOpenReservationPost(postId, user.getId());
        return ResponseEntity.ok(OpenReservationPostResponseDTO.fromEntity(post));
    }

    /**
     * Gets a specific open reservation post by ID.
     * Returns details about the reservation available for joining.
     */
    @GetMapping("/communities/{communityId}/openreservationposts/{postId}")
    public ResponseEntity<OpenReservationPostResponseDTO> getOpenReservationPost(
            @PathVariable Long communityId,
            @PathVariable Long postId,
            @AuthenticationPrincipal User user) {
        OpenReservationPost post = communityPostService.getOpenReservationPostById(
                postId, 
                user != null ? user.getId() : null);
        return ResponseEntity.ok(OpenReservationPostResponseDTO.fromEntity(post));
    }
}
