package com.kkrishnan.docanalyzer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;

@Service
public class AmazonCredentialsProvider {

    private final AwsBasicCredentials credentials;

    public AmazonCredentialsProvider( @Value("${s3.access.key.id}") String accessKeyId, @Value("${s3.secret.access.key}") String secretAccessKey){
        credentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
    }


    public StaticCredentialsProvider getCredentials() {
        return StaticCredentialsProvider.create(credentials);
    }
}
