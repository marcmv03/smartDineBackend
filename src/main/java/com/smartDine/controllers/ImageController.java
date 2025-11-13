package com.smartDine.controllers;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smartDine.services.S3Service;

@RestController
@RequestMapping("smartdine/api")
public class ImageController {

    private final S3Service s3;

    public ImageController(S3Service s3) { this.s3 = s3; }


    @GetMapping("/images")
    public ResponseEntity<InputStreamResource> getImage(@RequestParam ("key") String key) {
        var meta = s3.getMetadata(key);
        InputStreamResource resource = s3.getFile(key);

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        String ct = meta.getContentType();
        if (ct != null && !ct.isBlank()) {
            try { 
                mediaType = MediaType.parseMediaType(ct); 
            } catch (org.springframework.http.InvalidMediaTypeException ignored) {}
        }

        String filename = key.contains("/") ? key.substring(key.lastIndexOf('/') + 1) : key;

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename(filename).toString())
                .contentType(mediaType)
                .contentLength(meta.getContentLength())
                .body(resource);
    }
}
