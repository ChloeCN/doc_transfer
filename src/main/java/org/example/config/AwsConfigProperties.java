package org.example.config;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@Accessors(chain = true)
@ConfigurationProperties(prefix = "aws")
public class AwsConfigProperties {
    private Arn arn;

    @Data
    public static class Arn{
        private String region;
        private String roleSessionName;
        private String roleArn;
        private String stsEndpoint;
    }
}
