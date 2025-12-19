package com.smartDine.dto.community.post;

import com.smartDine.entity.Community;
import com.smartDine.entity.Member;
import com.smartDine.entity.community.CommunityPost;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateCommunityPostRequestDTO {
    private Long communityId;

    @NotBlank(message = "Title is required")
    @Size(max = 150, message = "Title cannot exceed 150 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    public Long getCommunityId() {
        return communityId;
    }

    public void setCommunityId(Long communityId) {
        this.communityId = communityId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public static CommunityPost toEntity(CreateCommunityPostRequestDTO dto, Community community, Member author) {
        CommunityPost post = new CommunityPost();
        post.setTitle(dto.getTitle());
        post.setDescription(dto.getDescription());
        post.setCommunity(community);
        post.setAuthor(author);
        return post;
    }
}
