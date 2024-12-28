package org.example.imagebackend;

import org.example.imagebackend.controller.ImageController;
import org.example.imagebackend.service.S3Service;
import org.example.imagebackend.repository.ImageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class ImageControllerTest {

    @Mock
    private S3Service s3Service;

    @Mock
    private ImageRepository imageRepository;

    @InjectMocks
    private ImageController imageController;

    @Mock
    private MultipartFile file;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testUploadImage_ValidFile() throws IOException {
        String fileName = "test.png";
        when(file.getContentType()).thenReturn("image/png");
        when(file.getSize()).thenReturn(1024L);
        when(s3Service.uploadImage(file)).thenReturn("https://s3.amazonaws.com/test-image-url");

        ResponseEntity<String> response = imageController.uploadImage(file);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("https://s3.amazonaws.com/test-image-url", response.getBody());
        verify(s3Service, times(1)).uploadImage(file);
    }

    @Test
    void testUploadImage_InvalidFileType() throws IOException {
        String fileName = "test.mp4";
        when(file.getContentType()).thenReturn("video/mp4");
        when(file.getSize()).thenReturn(1024L);

        ResponseEntity<String> response = imageController.uploadImage(file);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid file type. Only PNG and JPG are allowed.", response.getBody());
        verify(s3Service, never()).uploadImage(file);
    }

    @Test
    void testUploadImage_FileTooLarge() throws IOException {
        String fileName = "test.png";
        when(file.getContentType()).thenReturn("image/png");
        when(file.getSize()).thenReturn(15 * 1024 * 1024L); // 15MB

        ResponseEntity<String> response = imageController.uploadImage(file);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("File is too large. Maximum size allowed is 10MB.", response.getBody());
        verify(s3Service, never()).uploadImage(file);
    }
}

