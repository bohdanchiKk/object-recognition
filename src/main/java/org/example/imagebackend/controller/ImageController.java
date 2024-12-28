package org.example.imagebackend.controller;

import org.example.imagebackend.service.S3Service;
import org.example.imagebackend.entity.Image;
import org.example.imagebackend.repository.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    private final S3Service s3Service;
    private final ImageRepository imageRepository;

    @Autowired
    public ImageController(S3Service s3Service, ImageRepository imageRepository) {
        this.s3Service = s3Service;
        this.imageRepository = imageRepository;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadImage(@RequestParam("image") MultipartFile file) throws IOException {
        String contentType = file.getContentType();
        long fileSizeInBytes = file.getSize();
        long maxSizeInBytes = 10 * 1024 * 1024; // 10MB in bytes

        if (contentType == null ||
                (!contentType.equalsIgnoreCase("image/png") && !contentType.equalsIgnoreCase("image/jpeg"))) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid file type. Only PNG and JPG are allowed.");
        }
        if (fileSizeInBytes > maxSizeInBytes) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File is too large. Maximum size allowed is 10MB.");
        }
            String imageUrl = s3Service.uploadImage(file);
            return ResponseEntity.ok(imageUrl);
        }


    @GetMapping("/search")
    public List<Image> searchImages(@RequestParam String description) {
        return imageRepository.findByDescription(description);
    }

    @GetMapping("/getAll")
    public List<Image> getAllImages() {
        return imageRepository.findAll();
    }
}

