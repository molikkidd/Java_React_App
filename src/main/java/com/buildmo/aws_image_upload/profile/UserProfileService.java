package com.buildmo.aws_image_upload.profile;

import com.buildmo.aws_image_upload.bucket.BucketName;
import com.buildmo.aws_image_upload.filestore.FileStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;

import static org.springframework.http.MediaType.*;

@Service
public class UserProfileService {
    private final UserProfileDataAccessService userProfileDataAccessService;
    private final FileStore fileStore;

    @Autowired
    public UserProfileService(UserProfileDataAccessService userProfileDataAccessService, FileStore fileStore) {
        this.userProfileDataAccessService = userProfileDataAccessService;
        this.fileStore = fileStore;
    }

    List<UserProfile> getUserProfiles() {
        return userProfileDataAccessService.getUserProfiles();
    }

    public void uploadUserProfileImage(UUID userProfileId, MultipartFile file) {
//        check if image is not empty
        if(file.isEmpty()){
            throw  new IllegalStateException("Cannot upload empty file [ " + file.getSize() + "]");
        }
//        If file is an image
        if(!Arrays.asList(IMAGE_JPEG, IMAGE_PNG, IMAGE_GIF).contains(file.getContentType())){
            throw new IllegalStateException("File must be an image");
        }
//        The user exists in our database
        UserProfile user = userProfileDataAccessService
                .getUserProfiles()
                .stream()
                .filter(userProfile -> userProfile.getUserProfileId().equals(userProfileId))
                .findFirst()
                .orElseThrow(()-> new IllegalStateException(String.format("User Profile %s not found", userProfileId)));
//        Grab some metadata form file if any
        Map<String, String> metadata = new HashMap<>();
        metadata.put("Content-Type",  file.getContentType());
        metadata.put("Content-Length", String.valueOf(file.getSize()));
//        Store the image in s# and update database with s3 image link
        String path = String.format("%s/%s", BucketName.PROFILE_IMAGE.getBucketName(), user.getUserProfileId());
        String filename = String.format("%s/%s", file.getName(), UUID.randomUUID());
        try {
            fileStore.save(path, filename,Optional.of(metadata), file.getInputStream());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
