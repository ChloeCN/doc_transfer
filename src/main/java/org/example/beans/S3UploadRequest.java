package org.example.beans;

import lombok.Data;

@Data
public class S3UploadRequest {
    String file;

    String path;
}
