package com.smartDine.services ;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

@Service
public class S3Service {

    @Autowired
    private AmazonS3 amazonS3; // inyectado por campo, no constructor

    @Value("${aws.s3.bucket}")
    private String bucketName;

    /**
     * Sube el archivo a S3 y devuelve la URL de acceso.
     * Si no desea objetos públicos, quite el withCannedAcl y genere URL presignadas en el controlador.
     */
    public String uploadFile(MultipartFile file, String keyName) throws IOException {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        PutObjectRequest put = new PutObjectRequest(
                bucketName,
                keyName,
                file.getInputStream(),
                metadata
        ).withCannedAcl(CannedAccessControlList.PublicRead);

        amazonS3.putObject(put);
        return amazonS3.getUrl(bucketName, keyName).toString();
    }

    /**
     * Obtiene el archivo desde S3 como InputStreamResource para retornarlo en el controlador.
     * El stream se cerrará cuando Spring termine de escribir la respuesta.
     */
    public InputStreamResource getFile(String keyName) {
        try {
        S3Object s3Object = amazonS3.getObject(bucketName, keyName);
        S3ObjectInputStream s3is = s3Object.getObjectContent();
        return new InputStreamResource(s3is);
        }
        catch(com.amazonaws.services.s3.model.AmazonS3Exception e) {
            throw new IllegalArgumentException("No se encontró el archivo con la clave: " + keyName, e);
        }
    }

    /**
     * (Opcional) Metadatos útiles para cabeceras HTTP (Content-Type, Content-Length, etc.).
     */
    public ObjectMetadata getMetadata(String keyName) {
        return amazonS3.getObjectMetadata(bucketName, keyName);
    }
}
