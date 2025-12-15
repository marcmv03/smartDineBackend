package com.smartDine.dto.community.post;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommunityPostResponseDTO extends CommunityPostBaseDTO {
    private String description;
    private String authorName;
}
