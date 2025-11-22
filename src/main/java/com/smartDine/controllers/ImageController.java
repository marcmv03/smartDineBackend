package com.smartDine.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smartDine.adapters.ImageAdapter;
import com.smartDine.dto.ImageResponseDTO;

@RestController
@RequestMapping("smartdine/api")
public class ImageController {

    @Autowired
    private ImageAdapter imageAdapter;

    @GetMapping("/images")
    public ResponseEntity<InputStreamResource> getImage(@RequestParam ("key") String key) {
        // Get image with metadata from adapter
        ImageResponseDTO imageResponse = imageAdapter.getImage(key);
        
        // Create InputStreamResource from the InputStream
        InputStreamResource resource = new InputStreamResource(imageResponse.getInputStream());

        // Parse content type, fallback to octet-stream if invalid
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        String contentType = imageResponse.getContentType();
        if (contentType != null && !contentType.isBlank()) {
            try { 
                mediaType = MediaType.parseMediaType(contentType); 
            } catch (org.springframework.http.InvalidMediaTypeException ignored) {}
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename(imageResponse.getFilename()).toString())
                .contentType(mediaType)
                .contentLength(imageResponse.getContentLength())
                .body(resource);
    }
}
