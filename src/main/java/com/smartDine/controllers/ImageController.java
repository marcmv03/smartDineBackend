package com.smartDine.controllers;

import java.net.URI;
import java.util.Optional;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.smartDine.dto.UploadResponse;
import com.smartDine.services.S3Service;

@RestController
@RequestMapping("smartdine/api")
public class ImageController {

    private final S3Service s3;

    public ImageController(S3Service s3) { this.s3 = s3; }

    @PostMapping(
        value = "/restaurants/{id}/images",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<UploadResponse> upload(
            @PathVariable Long id,
            @RequestPart("file") MultipartFile file) throws java.io.IOException {

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        String ext = Optional.ofNullable(file.getOriginalFilename())
                .filter(n -> n.contains("."))
                .map(n -> n.substring(n.lastIndexOf('.') + 1))
                .orElse("jpg");

        String keyName = "restaurants/%d/images/%s.%s"
                .formatted(id, java.util.UUID.randomUUID(), ext);

        String url = s3.uploadFile(file, keyName); // errores gestionados por GlobalExceptionHandler

        UploadResponse body = new UploadResponse(keyName, url, file.getContentType(), file.getSize());
        URI location = URI.create("/api/images/" + keyName);

        return ResponseEntity.created(location).body(body);
    }

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
