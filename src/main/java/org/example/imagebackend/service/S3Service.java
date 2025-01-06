package org.example.imagebackend.service;

import org.example.imagebackend.entity.Image;
import org.example.imagebackend.repository.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.DetectLabelsRequest;
import software.amazon.awssdk.services.rekognition.model.DetectLabelsResponse;
import software.amazon.awssdk.services.rekognition.model.Label;
import software.amazon.awssdk.services.rekognition.model.S3Object;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class S3Service {
    private final String bucketName;
    private final String region;
    private final S3Client s3Client;
    private final RekognitionClient rekognitionClient;
    private final ImageRepository imageRepository;

    @Autowired
    public S3Service(@Value("${aws.access.key.id}") String accessKey,
                     @Value("${aws.secret.access.key}") String secretKey,
                     @Value("${aws.s3.bucket.name}") String bucketName,
                     @Value("${aws.region}") String region,
                     ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
        this.bucketName = bucketName;
        this.region = region;

        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);
        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();
        this.rekognitionClient = RekognitionClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();

    }

    public String uploadImage(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        try (InputStream inputStream = file.getInputStream()) {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType(file.getContentType())
                    .contentDisposition("inline")
                    .acl(ObjectCannedACL.PUBLIC_READ)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, file.getSize()));
        } catch (S3Exception e) {
            throw new RuntimeException("Error uploading file to S3", e);
        }

        List<String> labels = detectDescription(fileName);

        Image image = new Image();
        image.setUrl(getImageUrl(fileName));
        System.out.println(labels);
        image.setDescription(String.join(",", labels));
        imageRepository.save(image);
        return getImageUrl(fileName);
    }

    private List<String> detectDescription(String fileName) {
        DetectLabelsRequest detectLabelsRequest = DetectLabelsRequest.builder()
                .image(software.amazon.awssdk.services.rekognition.model.Image.builder().s3Object(S3Object.builder().bucket(bucketName).name(fileName).build()).build())
                .maxLabels(20)
                .minConfidence(70F)
                .build();

        DetectLabelsResponse detectLabelsResponse = rekognitionClient.detectLabels(detectLabelsRequest);

        return detectLabelsResponse.labels().stream()
                .map(Label::name)
                .collect(Collectors.toList());
    }

    private String getImageUrl(String fileName) {
        return "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + fileName;
    }
}
