package com.smartDine.controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import com.smartDine.dto.community.post.UpdateCommunityPostRequestDTO;
import com.smartDine.entity.User;
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

    @PostMapping("/community/posts")
    public ResponseEntity<CommunityPostResponseDTO> createPost(
            @Valid @RequestBody CreateCommunityPostRequestDTO requestDTO,
            @AuthenticationPrincipal User user) {
        CommunityPostResponseDTO response = communityPostService.createPost(user.getId(), requestDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/community/posts/{postId}")
    public ResponseEntity<CommunityPostResponseDTO> getPost(
            @PathVariable Long postId,
            @AuthenticationPrincipal User user) {
        CommunityPostResponseDTO response = communityPostService.getPostById(postId, user != null ? user.getId() : null);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/community/members/{memberId}/posts")
    public ResponseEntity<Page<CommunityPostSummaryDTO>> getPostsByMember(
            @PathVariable Long memberId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @AuthenticationPrincipal User user) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CommunityPostSummaryDTO> response = communityPostService.getPostsByMember(memberId, search, pageable,
                user != null ? user.getId() : null);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/community/posts/{postId}")
    public ResponseEntity<CommunityPostResponseDTO> updatePost(
            @PathVariable Long postId,
            @Valid @RequestBody UpdateCommunityPostRequestDTO requestDTO,
            @AuthenticationPrincipal User user) {
        CommunityPostResponseDTO response = communityPostService.updatePost(postId, user.getId(), requestDTO);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/community/posts/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal User user) {
        communityPostService.deletePost(postId, user.getId());
        return ResponseEntity.noContent().build();
    }
}
