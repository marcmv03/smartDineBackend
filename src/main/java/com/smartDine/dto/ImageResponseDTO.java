package com.smartDine.dto;

import java.io.InputStream;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for image retrieval responses.
 * Encapsulates the image content stream along with its metadata.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ImageResponseDTO {
    
    /**
     * The input stream containing the image data
     */
    private InputStream inputStream;
    
    /**
     * The MIME type of the image (e.g., "image/jpeg", "image/png")
     */
    private String contentType;
    
    /**
     * The size of the image in bytes
     */
    private long contentLength;
    
    /**
     * The filename extracted from the storage key
     */
    private String filename;
}
