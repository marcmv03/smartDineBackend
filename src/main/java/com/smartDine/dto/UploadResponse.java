package com.smartDine.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class UploadResponse {
    private String key;
    private String url;
    private String contentType;
    private long size;
}
