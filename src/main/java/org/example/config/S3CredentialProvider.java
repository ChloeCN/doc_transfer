package org.example.config;

import com.amazonaws.auth.AWSSessionCredentials;
import com.amazonaws.auth.AWSSessionCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleWithWebIdentityRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleWithWebIdentityResult;
import com.amazonaws.services.securitytoken.model.Credentials;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;

@Slf4j
public class S3CredentialProvider implements AWSSessionCredentialsProvider {

    private int sessionDuration = 3600;

    private int refreshThreshold = 500;

    private Date sessionCredentialsExpiration;

    private String subjectFromWIF;
    private AWSSessionCredentials awsSessionCredentials;
    private final AWSSecurityTokenService securityTokenService;

    final AwsConfigProperties awsConfigProperties;

    private void startSession() throws IOException{
        File file = new File(System.getenv("AWS_WEB_IDENTITY_FILE"));
        String webIdentityToken = null;
        try(BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            webIdentityToken = bufferedReader.readLine();
        } catch (IOException e) {
            log.error("aws get s3 session error:",e);
        }
        AssumeRoleWithWebIdentityResult assumeRoleWithWebIdentityResult = securityTokenService
                .assumeRoleWithWebIdentity(new AssumeRoleWithWebIdentityRequest()
                        .withWebIdentityToken(webIdentityToken)
                        .withRoleArn(System.getenv("AWS_ROLE_ARN"))
                        .withRoleSessionName(awsConfigProperties.getArn().getRoleSessionName())
                        .withDurationSeconds(this.sessionDuration)
                );
        Credentials stsCredentials = assumeRoleWithWebIdentityResult.getCredentials();
        subjectFromWIF = assumeRoleWithWebIdentityResult.getSubjectFromWebIdentityToken();
        BasicSessionCredentials basicSessionCredentials = new BasicSessionCredentials(
                stsCredentials.getAccessKeyId(),
                stsCredentials.getSecretAccessKey(),
                stsCredentials.getSessionToken()
        );
        AssumeRoleRequest assumeRoleRequest = new AssumeRoleRequest()
                .withRoleArn(awsConfigProperties.getArn().getRoleArn())
                .withRoleSessionName(awsConfigProperties.getArn().getRoleSessionName())
                .withDurationSeconds(this.sessionDuration)
                .withRequestCredentialsProvider(new AWSStaticCredentialsProvider(basicSessionCredentials));
        Credentials s3Credentials = securityTokenService.assumeRole(assumeRoleRequest).getCredentials();
        awsSessionCredentials = new BasicSessionCredentials(
                s3Credentials.getAccessKeyId(),
                s3Credentials.getSecretAccessKey(),
                s3Credentials.getSessionToken()
        );
        sessionCredentialsExpiration = s3Credentials.getExpiration();
    }

    public S3CredentialProvider(AwsConfigProperties awsConfigProperties,AWSSecurityTokenService awsSecurityTokenService){
        this.awsConfigProperties = awsConfigProperties;
        this.securityTokenService = awsSecurityTokenService;
    }

    private boolean needNewSession(){
        if(awsSessionCredentials == null){
            return true;
        }
        long timeRemaining = sessionCredentialsExpiration.getTime() - System.currentTimeMillis();
        return timeRemaining < (this.refreshThreshold * 1000L);
    }
    @Override
    public AWSSessionCredentials getCredentials() {
        if(needNewSession()){
            try {
                startSession();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return awsSessionCredentials;
    }

    @Override
    public void refresh() {
        try {
            startSession();
        } catch (IOException e) {
            log.error("refresh session failure" + e);
        }
    }
}
