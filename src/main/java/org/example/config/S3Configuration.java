package org.example.config;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@EnableConfigurationProperties(AwsConfigProperties.class)
public class S3Configuration {

    @Autowired
    private AwsConfigProperties awsConfigProperties;

    public AmazonS3 amazonS3() throws Exception{
        AWSSecurityTokenService awsSecurityTokenService = AWSSecurityTokenServiceClientBuilder
                .standard()
                .withEndpointConfiguration(
                        new AwsClientBuilder.EndpointConfiguration(
                                awsConfigProperties.getArn().getStsEndpoint(),awsConfigProperties.getArn().getRegion()
                        )
                ).build();

        return (AmazonS3) awsSecurityTokenService;
    }
}
