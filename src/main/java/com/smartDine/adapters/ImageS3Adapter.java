package com.smartDine.adapters;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.smartDine.dto.ImageResponseDTO;
import com.smartDine.dto.UploadResponse;
import com.smartDine.services.S3Service;

/**
 * S3 implementation of the ImageAdapter interface.
 * This adapter encapsulates the S3Service and adapts it to the ImageAdapter interface,
 * following the Adapter design pattern.
 */
@Service
public class ImageS3Adapter implements ImageAdapter {
    
    @Autowired
    private S3Service adaptee;  // The adapted S3 service
    
    /**
     * Uploads an image to AWS S3.
     * 
     * @param image the image file to upload
     * @param path the S3 key where the image should be stored
     * @return UploadResponse with key, url, contentType, and size
     * @throws IOException if the upload fails
     */
    @Override
    public UploadResponse uploadImage(MultipartFile image, String path) throws IOException {
        // Validate input
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("Image file cannot be null or empty");
        }
        
        // Upload to S3 and get the public URL
        String url = adaptee.uploadFile(image, path);
        
        // Create and return the response
        return new UploadResponse(path, url, image.getContentType(), image.getSize());
    }
    
    /**
     * Retrieves an image from AWS S3 with its metadata.
     * 
     * @param key the S3 key of the image to retrieve
     * @return ImageResponseDTO containing the image stream and metadata
     * @throws IllegalArgumentException if the image is not found
     */
    @Override
    public ImageResponseDTO getImage(String key) {
        try {
            // Get the InputStreamResource from S3
            InputStreamResource resource = adaptee.getFile(key);
            
            // Get metadata from S3
            com.amazonaws.services.s3.model.ObjectMetadata metadata = adaptee.getMetadata(key);
            
            // Extract the underlying InputStream
            InputStream inputStream = resource.getInputStream();
            
            // Extract content type from metadata
            String contentType = metadata.getContentType();
            if (contentType == null || contentType.isBlank()) {
                contentType = "application/octet-stream";
            }
            
            // Extract content length from metadata
            long contentLength = metadata.getContentLength();
            
            // Extract filename from key (part after last slash)
            String filename = key.contains("/") ? key.substring(key.lastIndexOf('/') + 1) : key;
            
            // Create and return the response DTO
            return new ImageResponseDTO(inputStream, contentType, contentLength, filename);
            
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to retrieve image with key: " + key, e);
        }
    }
    @Override
    public void deleteImage(String key) {
        adaptee.deleteFile(key);
    }
}
