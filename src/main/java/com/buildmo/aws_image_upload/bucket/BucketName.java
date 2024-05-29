package com.buildmo.aws_image_upload.bucket;

public enum BucketName {

    PROFILE_IMAGE("buildmo-aws-image-upload");

    private final String bucketName;

    public String getBucketName() {
        return bucketName;
    }

    BucketName(String bucketName) {
        this.bucketName = bucketName;
    }


}
