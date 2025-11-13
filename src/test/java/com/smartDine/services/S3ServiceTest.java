package com.smartDine.services;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.InputStreamResource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

/**
 * Test unitario para S3Service.
 * Prueba los métodos uploadFile y getFile con casos de éxito.
 */
@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

    @Mock
    private AmazonS3 amazonS3;

    @InjectMocks
    private S3Service s3Service;

    private static final String BUCKET_NAME = "test-bucket";
    private static final String KEY_NAME = "test-image.jpg";
    private static final String FILE_CONTENT = "Test file content";
    private static final String CONTENT_TYPE = "image/jpeg";
    private static final long FILE_SIZE = FILE_CONTENT.length();

    @BeforeEach
    void setUp() {
        // Inyectar el bucketName usando ReflectionTestUtils
        ReflectionTestUtils.setField(s3Service, "bucketName", BUCKET_NAME);
    }

    /**
     * Test: uploadFile con un archivo válido debe subir el archivo a S3 y retornar la URL.
     */
    @Test
    void testUploadFile_Success() throws IOException {
        // Arrange - Preparar datos de prueba
        MultipartFile mockFile = mock(MultipartFile.class);
        InputStream inputStream = new ByteArrayInputStream(FILE_CONTENT.getBytes());
        
        when(mockFile.getSize()).thenReturn(FILE_SIZE);
        when(mockFile.getContentType()).thenReturn(CONTENT_TYPE);
        when(mockFile.getInputStream()).thenReturn(inputStream);
        
        // Mockear la URL que devuelve S3
        URL expectedUrl = new URL("https://test-bucket.s3.amazonaws.com/test-image.jpg");
        when(amazonS3.getUrl(BUCKET_NAME, KEY_NAME)).thenReturn(expectedUrl);
        
        // Act - Ejecutar el método a probar
        String resultUrl = s3Service.uploadFile(mockFile, KEY_NAME);
        
        // Assert - Verificar resultados
        assertNotNull(resultUrl, "La URL retornada no debe ser nula");
        assertEquals(expectedUrl.toString(), resultUrl, "La URL debe coincidir con la esperada");
        
        // Verificar que se llamó al método putObject con los parámetros correctos
        verify(amazonS3, times(1)).putObject(any(PutObjectRequest.class));
        verify(amazonS3, times(1)).getUrl(BUCKET_NAME, KEY_NAME);
        
        // Verificar que se leyeron las propiedades del archivo
        verify(mockFile, times(1)).getSize();
        verify(mockFile, times(1)).getContentType();
        verify(mockFile, times(1)).getInputStream();
    }

    /**
     * Test: uploadFile con archivo de imagen PNG debe manejar correctamente el content type.
     */
    @Test
    void testUploadFile_WithPngImage_Success() throws IOException {
        // Arrange
        MultipartFile mockFile = mock(MultipartFile.class);
        InputStream inputStream = new ByteArrayInputStream("PNG image data".getBytes());
        String pngContentType = "image/png";
        
        when(mockFile.getSize()).thenReturn(15L);
        when(mockFile.getContentType()).thenReturn(pngContentType);
        when(mockFile.getInputStream()).thenReturn(inputStream);
        
        URL expectedUrl = new URL("https://test-bucket.s3.amazonaws.com/test-image.png");
        when(amazonS3.getUrl(BUCKET_NAME, "test-image.png")).thenReturn(expectedUrl);
        
        // Act
        String resultUrl = s3Service.uploadFile(mockFile, "test-image.png");
        
        // Assert
        assertNotNull(resultUrl);
        assertEquals(expectedUrl.toString(), resultUrl);
        verify(amazonS3, times(1)).putObject(any(PutObjectRequest.class));
    }

    /**
     * Test: uploadFile con archivo grande debe manejar correctamente el tamaño.
     */
    @Test
    void testUploadFile_WithLargeFile_Success() throws IOException {
        // Arrange - Simular un archivo de 5MB
        MultipartFile mockFile = mock(MultipartFile.class);
        long largeFileSize = 5 * 1024 * 1024; // 5MB
        InputStream inputStream = new ByteArrayInputStream(new byte[1024]); // Mock data
        
        when(mockFile.getSize()).thenReturn(largeFileSize);
        when(mockFile.getContentType()).thenReturn(CONTENT_TYPE);
        when(mockFile.getInputStream()).thenReturn(inputStream);
        
        URL expectedUrl = new URL("https://test-bucket.s3.amazonaws.com/large-image.jpg");
        when(amazonS3.getUrl(BUCKET_NAME, "large-image.jpg")).thenReturn(expectedUrl);
        
        // Act
        String resultUrl = s3Service.uploadFile(mockFile, "large-image.jpg");
        
        // Assert
        assertNotNull(resultUrl);
        assertEquals(expectedUrl.toString(), resultUrl);
        verify(mockFile, times(1)).getSize();
    }

    /**
     * Test: getFile debe retornar un InputStreamResource con el contenido del archivo.
     */
    @Test
    void testGetFile_Success() throws IOException {
        // Arrange - Preparar el objeto S3 mock
        S3Object mockS3Object = mock(S3Object.class);
        S3ObjectInputStream mockS3InputStream = mock(S3ObjectInputStream.class);
        
        when(amazonS3.getObject(BUCKET_NAME, KEY_NAME)).thenReturn(mockS3Object);
        when(mockS3Object.getObjectContent()).thenReturn(mockS3InputStream);
        
        // Act - Ejecutar el método a probar
        InputStreamResource result = s3Service.getFile(KEY_NAME);
        
        // Assert - Verificar resultados
        assertNotNull(result, "El InputStreamResource no debe ser nulo");
        
        // Verificar que se llamaron los métodos correctos
        verify(amazonS3, times(1)).getObject(BUCKET_NAME, KEY_NAME);
        verify(mockS3Object, times(1)).getObjectContent();
    }

    /**
     * Test: getFile con diferentes nombres de clave debe funcionar correctamente.
     */
    @Test
    void testGetFile_WithDifferentKeyName_Success() {
        // Arrange
        String customKeyName = "images/menu/dish-123.jpg";
        S3Object mockS3Object = mock(S3Object.class);
        S3ObjectInputStream mockS3InputStream = mock(S3ObjectInputStream.class);
        
        when(amazonS3.getObject(BUCKET_NAME, customKeyName)).thenReturn(mockS3Object);
        when(mockS3Object.getObjectContent()).thenReturn(mockS3InputStream);
        
        // Act
        InputStreamResource result = s3Service.getFile(customKeyName);
        
        // Assert
        assertNotNull(result);
        verify(amazonS3, times(1)).getObject(BUCKET_NAME, customKeyName);
    }

    /**
     * Test: getMetadata debe retornar los metadatos del archivo almacenado.
     */
    @Test
    void testGetMetadata_Success() {
        // Arrange
        ObjectMetadata mockMetadata = new ObjectMetadata();
        mockMetadata.setContentType(CONTENT_TYPE);
        mockMetadata.setContentLength(FILE_SIZE);
        
        when(amazonS3.getObjectMetadata(BUCKET_NAME, KEY_NAME)).thenReturn(mockMetadata);
        
        // Act
        ObjectMetadata result = s3Service.getMetadata(KEY_NAME);
        
        // Assert
        assertNotNull(result, "Los metadatos no deben ser nulos");
        assertEquals(CONTENT_TYPE, result.getContentType());
        assertEquals(FILE_SIZE, result.getContentLength());
        
        verify(amazonS3, times(1)).getObjectMetadata(BUCKET_NAME, KEY_NAME);
    }

    /**
     * Test: uploadFile y luego getFile - flujo completo de éxito.
     */
    @Test
    void testUploadAndGetFile_CompleteFlow_Success() throws IOException {
        // Arrange - Upload
        MultipartFile mockFile = mock(MultipartFile.class);
        InputStream uploadInputStream = new ByteArrayInputStream(FILE_CONTENT.getBytes());
        
        when(mockFile.getSize()).thenReturn(FILE_SIZE);
        when(mockFile.getContentType()).thenReturn(CONTENT_TYPE);
        when(mockFile.getInputStream()).thenReturn(uploadInputStream);
        
        URL expectedUrl = new URL("https://test-bucket.s3.amazonaws.com/test-image.jpg");
        when(amazonS3.getUrl(BUCKET_NAME, KEY_NAME)).thenReturn(expectedUrl);
        
        // Arrange - Get
        S3Object mockS3Object = mock(S3Object.class);
        S3ObjectInputStream mockS3InputStream = mock(S3ObjectInputStream.class);
        when(amazonS3.getObject(BUCKET_NAME, KEY_NAME)).thenReturn(mockS3Object);
        when(mockS3Object.getObjectContent()).thenReturn(mockS3InputStream);
        
        // Act - Subir archivo
        String uploadedUrl = s3Service.uploadFile(mockFile, KEY_NAME);
        
        // Act - Obtener archivo
        InputStreamResource retrievedFile = s3Service.getFile(KEY_NAME);
        
        // Assert
        assertNotNull(uploadedUrl, "URL de subida no debe ser nula");
        assertNotNull(retrievedFile, "Archivo recuperado no debe ser nulo");
        assertEquals(expectedUrl.toString(), uploadedUrl);
        
        verify(amazonS3, times(1)).putObject(any(PutObjectRequest.class));
        verify(amazonS3, times(1)).getObject(BUCKET_NAME, KEY_NAME);
    }
}
