package com.example.This_App_Backend.testService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.example.This_App_Backend.service.productsImageUpload.ProductsImageUpload;

@ExtendWith(MockitoExtension.class)
public class ProductsImageUploadTest {

    @Mock
    private Cloudinary cloudinary;

    @InjectMocks
    private ProductsImageUpload productsImageUpload;

    @Test
    void storeProductImage_whenUploadSuccessful_shouldReturnSecureUrl() throws Exception {
        // Arrange
        MultipartFile file = mock(MultipartFile.class);
        Long storeId = 1L;
        Long productId = 100L;
        byte[] fileBytes = "test image content".getBytes();
        String expectedUrl = "https://res.cloudinary.com/test/image/upload/v123/store_1/products/product_100.jpg";

        when(file.getBytes()).thenReturn(fileBytes);

        Uploader uploader = mock(Uploader.class);
        when(cloudinary.uploader()).thenReturn(uploader);

        Map<String, Object> uploadResult = Map.of("secure_url", expectedUrl);
        when(uploader.upload(any(byte[].class), any(Map.class))).thenReturn(uploadResult);

        // Act
        String result = productsImageUpload.storeProductImage(file, storeId, productId);

        // Assert
        assertEquals(expectedUrl, result);
    }

    @Test
    void storeProductImage_whenUploadFails_shouldThrowException() throws Exception {
        // Arrange
        MultipartFile file = mock(MultipartFile.class);
        Long storeId = 2L;
        Long productId = 200L;
        byte[] fileBytes = "test image content".getBytes();

        when(file.getBytes()).thenReturn(fileBytes);

        Uploader uploader = mock(Uploader.class);
        when(cloudinary.uploader()).thenReturn(uploader);

        when(uploader.upload(any(byte[].class), any(Map.class)))
                .thenThrow(new RuntimeException("Upload failed"));

        // Act & Assert
        Exception exception = assertThrows(Exception.class,
                () -> productsImageUpload.storeProductImage(file, storeId, productId));

        assertEquals("Cloudinary upload failedUpload failed", exception.getMessage());
    }
}