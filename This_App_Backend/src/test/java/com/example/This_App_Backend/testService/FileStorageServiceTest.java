package com.example.This_App_Backend.testService;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import com.example.This_App_Backend.service.FileStorageService;

@ExtendWith(MockitoExtension.class)
public class FileStorageServiceTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    @Mock
    private MultipartFile mockFile;

    @InjectMocks
    private FileStorageService fileStorageService;

    @BeforeEach
    void setUp() {
        // Make cloudinary.uploader() return our mock uploader
        when(cloudinary.uploader()).thenReturn(uploader);
    }

    // ---------- storeProfilePhoto ----------
    @Test
    void storeProfilePhoto_success() throws IOException {
        byte[] fileBytes = "test data".getBytes();
        when(mockFile.getBytes()).thenReturn(fileBytes);
        Map<String, Object> expectedResult = Map.of("secure_url", "https://cloudinary.com/profile_photos/user_123.jpg");
        when(uploader.upload(eq(fileBytes), any(Map.class))).thenReturn(expectedResult);

        String result = fileStorageService.storeProfilePhoto(mockFile, 123L);

        assertEquals("https://cloudinary.com/profile_photos/user_123.jpg", result);
        verify(uploader).upload(eq(fileBytes), argThat(params -> "profile_photos".equals(params.get("folder")) &&
                "user_123".equals(params.get("public_id")) &&
                Boolean.TRUE.equals(params.get("overwrite")) &&
                "image".equals(params.get("resource_type"))));
    }

    @Test
    void storeProfilePhoto_throwsIOExceptionWhenUploadFails() throws IOException {
        when(mockFile.getBytes()).thenReturn("data".getBytes());
        when(uploader.upload(any(), any(Map.class))).thenThrow(new RuntimeException("Cloudinary error"));

        IOException exception = assertThrows(IOException.class,
                () -> fileStorageService.storeProfilePhoto(mockFile, 1L));
        assertTrue(exception.getMessage().contains("Cloudinary upload failed"));
    }

    // ---------- uploadImage ----------
    @Test
    void uploadImage_success() throws IOException {
        byte[] fileBytes = "img".getBytes();
        when(mockFile.getBytes()).thenReturn(fileBytes);
        Map<String, Object> resultMap = Map.of("secure_url", "https://cloudinary.com/some/image.jpg");
        when(uploader.upload(eq(fileBytes), any(Map.class))).thenReturn(resultMap);

        String url = fileStorageService.uploadImage(mockFile, "test_folder", "test_id");

        assertEquals("https://cloudinary.com/some/image.jpg", url);
        verify(uploader).upload(eq(fileBytes), argThat(params -> "test_folder".equals(params.get("folder")) &&
                "test_id".equals(params.get("public_id")) &&
                Boolean.TRUE.equals(params.get("overwrite")) &&
                "image".equals(params.get("resource_type"))));
    }

    @Test
    void uploadImage_throwsIOExceptionOnFailure() throws IOException {
        when(mockFile.getBytes()).thenThrow(new IOException("File error"));
        assertThrows(IOException.class,
                () -> fileStorageService.uploadImage(mockFile, "folder", "id"));
    }

    // ---------- deleteImage ----------
    @Test
    void deleteImage_success() throws IOException {
        fileStorageService.deleteImage("some_public_id");
        verify(uploader).destroy("some_public_id", Map.of());
    }

    @Test
    void deleteImage_throwsIOExceptionOnFailure() throws IOException {
        // 1. Arrange: Mock uploader to throw a RuntimeException (simulating Cloudinary
        // failure)
        doThrow(new RuntimeException("Cloudinary delete error"))
                .when(uploader).destroy(anyString(), anyMap());

        // 2. Act & Assert: Verify the service catches that and throws an IOException
        // instead
        IOException exception = assertThrows(IOException.class, () -> {
            fileStorageService.deleteImage("id");
        });

        // 3. Verify the error message matches what you defined in the service
        assertTrue(exception.getMessage().contains("Failed to delete product image"));
    }

    // ---------- storeStoreLogo ----------
    @Test
    void storeStoreLogo_success() throws IOException {
        byte[] data = "logo".getBytes();
        when(mockFile.getBytes()).thenReturn(data);
        Map<String, Object> result = Map.of("secure_url", "https://cloudinary.com/store_logos/store_42.jpg");
        when(uploader.upload(any(), any(Map.class))).thenReturn(result);

        String url = fileStorageService.storeStoreLogo(mockFile, "42");
        assertEquals("https://cloudinary.com/store_logos/store_42.jpg", url);
        verify(uploader).upload(eq(data), argThat(params -> "store_logos".equals(params.get("folder")) &&
                "store_42".equals(params.get("public_id")) &&
                Boolean.TRUE.equals(params.get("overwrite"))));
    }

    @Test
    void storeStoreLogo_throwsIOExceptionOnFailure() throws IOException {
        when(mockFile.getBytes()).thenReturn(new byte[1]);
        when(uploader.upload(any(), any())).thenThrow(new RuntimeException("Upload error"));
        IOException ex = assertThrows(IOException.class,
                () -> fileStorageService.storeStoreLogo(mockFile, "1"));
        assertTrue(ex.getMessage().contains("Failed to upload store logo"));
    }

    // ---------- storeProductImage ----------
    @Test
    void storeProductImage_success() throws IOException {
        byte[] data = "product".getBytes();
        when(mockFile.getBytes()).thenReturn(data);
        Map<String, Object> result = Map.of("secure_url", "https://cloudinary.com/store_1/products/product_99.jpg");
        when(uploader.upload(any(), any(Map.class))).thenReturn(result);

        String url = fileStorageService.storeProductImage(mockFile, 1L, 99L);
        assertEquals("https://cloudinary.com/store_1/products/product_99.jpg", url);
        verify(uploader).upload(eq(data), argThat(params -> "store_1/products".equals(params.get("folder")) &&
                "product_99".equals(params.get("public_id")) &&
                Boolean.TRUE.equals(params.get("overwrite"))));
    }

    @Test
    void storeProductImage_throwsIOExceptionOnFailure() throws IOException {
        when(mockFile.getBytes()).thenReturn(new byte[1]);
        when(uploader.upload(any(), any())).thenThrow(new RuntimeException("Cloud failure"));
        IOException ex = assertThrows(IOException.class,
                () -> fileStorageService.storeProductImage(mockFile, 1L, 1L));
        assertTrue(ex.getMessage().contains("Failed to upload product image"));
    }

    // ---------- deleteStoreImage ----------
    @Test
    void deleteStoreImage_success() throws IOException {
        fileStorageService.deleteStoreImage(5L);
        verify(uploader).destroy("store_logos/store_5", Map.of());
    }

    @Test
    void deleteStoreImage_throwsIOExceptionWhenDestroyFails() throws IOException {
        doThrow(new RuntimeException("Destroy error")).when(uploader).destroy(any(), any());
        IOException ex = assertThrows(IOException.class,
                () -> fileStorageService.deleteStoreImage(10L));
        assertTrue(ex.getMessage().contains("Failed to delete store image"));
    }

    // ---------- deleteProductImage ----------
    @Test
    void deleteProductImage_success() throws IOException {
        fileStorageService.deleteProductImage(3L, 7L);
        verify(uploader).destroy("store_3/products/product_7", Map.of());
    }

    @Test
    void deleteProductImage_throwsIOExceptionOnFailure() throws IOException {
        doThrow(new RuntimeException("Destroy error")).when(uploader).destroy(any(), any());
        IOException ex = assertThrows(IOException.class,
                () -> fileStorageService.deleteProductImage(2L, 8L));
        assertTrue(ex.getMessage().contains("Failed to delete product image"));
    }

    // ---------- deleteAllStoreImages ----------
    @Test
    void deleteAllStoreImages_callsDeleteStoreImage() throws IOException {
        // deleteAllStoreImages currently only calls deleteStoreImage(storeId)
        // We'll spy on the service to verify internal call, or just verify
        // uploader.destroy
        fileStorageService.deleteAllStoreImages(42L);
        verify(uploader).destroy("store_logos/store_42", Map.of());
        // No other calls because the method only deletes the logo in current
        // implementation
    }

    @Test
    void deleteAllStoreImages_throwsIOExceptionIfDeleteFails() throws IOException {
        doThrow(new RuntimeException("Destroy error")).when(uploader).destroy(any(), any());
        IOException ex = assertThrows(IOException.class,
                () -> fileStorageService.deleteAllStoreImages(99L));
        assertTrue(ex.getMessage().contains("Failed to delete store images"));
    }
}
