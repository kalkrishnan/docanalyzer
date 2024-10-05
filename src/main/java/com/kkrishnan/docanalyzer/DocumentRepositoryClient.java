package com.kkrishnan.docanalyzer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;
import java.util.UUID;

@Service
public class DocumentRepositoryClient {

    AmazonCredentialsProvider credentialsProvider;
    S3Client s3Client;

    public DocumentRepositoryClient(AmazonCredentialsProvider credentialsProvider) {
        this.credentialsProvider = credentialsProvider;
        s3Client = S3Client.builder()
                .credentialsProvider(credentialsProvider.getCredentials())
                .region(Region.US_WEST_2)
                .build();

    }

    public String getDocumentBucketName(){
        return "docanalyzertest";
    }

    public String getDocumentKey(MultipartFile file) {
        return "documents/" + file.getOriginalFilename();
    }

    public void uploadDocument(MultipartFile file) {

        // Upload file to S3
        File localFile = null;
        try {
            localFile = convertMultiPartToFile(file);

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(getDocumentBucketName())
                .key(getDocumentKey(file)).build();
        s3Client.putObject(request,
                RequestBody.fromBytes(Files.readAllBytes(localFile.toPath())));

        localFile.delete();
        } catch (IOException e) {

            throw new RuntimeException(e);
        }
    }


    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convFile = new File(Objects.requireNonNull(file.getOriginalFilename()));
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        return convFile;
    }
}
