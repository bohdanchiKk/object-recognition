package org.example.imagebackend.controller;

import org.example.imagebackend.response.UploadResponse;
import org.example.imagebackend.service.S3Service;
import org.example.imagebackend.entity.Image;
import org.example.imagebackend.repository.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    private final S3Service s3Service;
    private final ImageRepository imageRepository;
    private UploadResponse uploadResponse;

    @Autowired
    public ImageController(S3Service s3Service, ImageRepository imageRepository, UploadResponse uploadResponse) {
        this.s3Service = s3Service;
        this.imageRepository = imageRepository;
        this.uploadResponse = uploadResponse;
    }

    @PostMapping("/upload")
    public ResponseEntity<UploadResponse> uploadImage(@RequestParam("images") MultipartFile[] files) throws IOException {
        String contentType;
        long fileSizeInBytes;
        long maxSizeInBytes = 10 * 1024 * 1024;
        List<String> badFile = new ArrayList<>();
        List<String> images = new ArrayList<>();

        for (MultipartFile multipartFile: files){
            contentType = multipartFile.getContentType();
            fileSizeInBytes = multipartFile.getSize();

            if (contentType == null ||
                    (!contentType.equalsIgnoreCase("image/png") && !contentType.equalsIgnoreCase("image/jpeg"))) {
                badFile.add(multipartFile.getOriginalFilename()+" has invalid file type and was not loaded. Only PNG and JPG are allowed.");
                continue;
            }
            if (fileSizeInBytes > maxSizeInBytes) {
                badFile.add(multipartFile.getName()+" file is too large and was not loaded. Maximum size allowed is 10MB.");
                continue;
            }
            images.add(s3Service.uploadImage(multipartFile));
        }
        this.uploadResponse = new UploadResponse(badFile,images);
        return ResponseEntity.ok(uploadResponse);
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

