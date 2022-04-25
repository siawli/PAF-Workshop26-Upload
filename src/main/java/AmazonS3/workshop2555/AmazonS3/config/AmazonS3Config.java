package AmazonS3.workshop2555.AmazonS3.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AmazonS3Config {
    
    @Value("${spaces.endpoint}")
    private String endpoint;

    @Value("${spaces.region}")
    private String region;

    @Bean
    public AmazonS3 create() {
        final String accessKey = System.getenv("ACCESS_KEY");
        final String secretKey = System.getenv("SECRET_KEY");

        AWSStaticCredentialsProvider cred = new AWSStaticCredentialsProvider(
            new BasicAWSCredentials(accessKey, secretKey));
        EndpointConfiguration config = new EndpointConfiguration(endpoint, region);

        return AmazonS3ClientBuilder
                .standard()
                .withEndpointConfiguration(config)
                .withCredentials(cred)
                .build();
    
    }
}
