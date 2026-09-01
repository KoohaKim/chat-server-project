package com.example.gooha.miniproject.service.s3;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3UploaderService {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    public String upload(MultipartFile file, String dirName) throws IOException {
        String originalFileName = file.getOriginalFilename();
        String fileName = dirName + "/" + UUID.randomUUID() + "_" + originalFileName;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType(file.getContentType())
                .build();
        s3Client.putObject(putObjectRequest,
                RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        String fileUrl = "https://" + bucketName + ".s3.amazonaws.com/" +fileName;

        log.info("Uploaded fileName = {}", fileName);
        log.info("Uploaded file URL = {}", fileUrl);

        return fileUrl;
    }


}
