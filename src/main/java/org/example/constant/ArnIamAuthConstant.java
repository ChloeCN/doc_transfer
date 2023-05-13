package org.example.constant;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@Accessors(chain = true)
@ConfigurationProperties(prefix = "aws")
public class ArnIamAuthConstant {
    private Arn arn;

    private IamAuth iamAuth;

    @Data
    public static class Arn {
        private String region;
        private String roleSessionName;
        private String roleArn;
        private String stsEndpoint;
    }

    @Data
    public static class IamAuth {
        private String hostname;
        private String port;
        private String userName;
    }
}
