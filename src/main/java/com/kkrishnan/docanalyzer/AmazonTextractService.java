package com.kkrishnan.docanalyzer;


import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.regions.*;
import software.amazon.awssdk.services.textract.*;
import software.amazon.awssdk.services.textract.model.*;
import org.springframework.stereotype.Service;


import java.util.UUID;

@Service
public class AmazonTextractService {


    AmazonCredentialsProvider credentialsProvider;

    DocumentRepositoryClient docRepositoryClient;
    private final TextractClient textractClient;

    public AmazonTextractService(AmazonCredentialsProvider credentialsProvider, DocumentRepositoryClient docRepositoryClient){
        this.credentialsProvider = credentialsProvider;
        this.docRepositoryClient = docRepositoryClient;
        textractClient = TextractClient.builder()
                .credentialsProvider(credentialsProvider.getCredentials())
                .region(Region.US_WEST_2)
                .build();
    }


    public StartDocumentAnalysisResponse analyzeDocument(MultipartFile file) {
        try {


            // Start async document analysis
            StartDocumentAnalysisRequest  request = StartDocumentAnalysisRequest.builder()
                    .documentLocation(DocumentLocation.builder()
                            .s3Object(S3Object.builder()
                                    .bucket(docRepositoryClient.getDocumentBucketName())
                                    .name(docRepositoryClient.getDocumentKey(file)).build()).build())
                    .featureTypes(FeatureType.TABLES)
                    .featureTypes(FeatureType.FORMS)
                    .clientRequestToken(UUID.randomUUID().toString()).build();

            StartDocumentAnalysisResponse result = textractClient.startDocumentAnalysis(request);

            return result;
        } catch (TextractException e) {
            System.err.println("Error processing the document with Textract: " + e.getMessage());
        }
        return null;
    }

    public GetDocumentAnalysisResponse getDocumentAnalysisResults(String jobId, String nextToken){
        GetDocumentAnalysisRequest request = GetDocumentAnalysisRequest.builder()
                .jobId(jobId)
                .nextToken(nextToken).build();
        return textractClient.getDocumentAnalysis(request);
    }
}
