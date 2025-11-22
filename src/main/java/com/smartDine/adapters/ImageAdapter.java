package com.smartDine.adapters;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

import com.smartDine.dto.ImageResponseDTO;
import com.smartDine.dto.UploadResponse;

/**
 * Adapter interface for image storage operations.
 * This interface defines the contract for uploading and retrieving images,
 * allowing different storage implementations (S3, local filesystem, etc.)
 */
public interface ImageAdapter {
    
    /**
     * Uploads an image to the storage system.
     * 
     * @param image the image file to upload
     * @param path the full path/key where the image should be stored
     * @return UploadResponse containing upload details (key, url, contentType, size)
     * @throws IOException if the upload fails
     */
    UploadResponse uploadImage(MultipartFile image, String path) throws IOException;
    
    /**
     * Retrieves an image from the storage system with its metadata.
     * 
     * @param key the key/path of the image to retrieve
     * @return ImageResponseDTO containing the image stream and metadata
     * @throws IllegalArgumentException if the image is not found
     */
    ImageResponseDTO getImage(String key);
}
