package com.smartDine.dto.community.post;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class CommunityPostBaseDTO {
    private Long id;
    private String title;
    private LocalDateTime publishedAt;
    private Long communityId;
    private Long authorId;
}
