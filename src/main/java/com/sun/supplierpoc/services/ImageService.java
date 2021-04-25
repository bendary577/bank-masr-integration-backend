package com.sun.supplierpoc.services;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.*;
import com.sun.supplierpoc.Constants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class ImageService {

    @Value("${spring.cloud.gcp.credentials.location}")
    private String baseConfigPath;

    public String store(MultipartFile image) {

        File  fileImage = null;
        try {
            fileImage = multipartToFile(image);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(fileImage.toPath());
        Random random = new Random();
        int rand = random.nextInt();
        String projectId = "oracle-symphony-integrator";
        String bucketName = "oracle-integrator-bucket";

        String objectName = "AccourImage/" + rand + image.getOriginalFilename();

        StorageOptions storageOptions = null;
        try {
            storageOptions = StorageOptions.newBuilder()
                    .setProjectId(projectId)
                    .setCredentials(GoogleCredentials.fromStream(new
                            FileInputStream(baseConfigPath))).build();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Storage storage = storageOptions.getService();

        BlobId blobId = BlobId.of(bucketName, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
        try {
            storage.create(blobInfo, Files.readAllBytes(Paths.get(fileImage.toPath().toString())));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return downloadFile(storage, objectName);
    }

    public File multipartToFile(MultipartFile multipart) throws IllegalStateException, IOException {
        File convFile = new File(System.getProperty("java.io.tmpdir")+"/"+multipart.getOriginalFilename());
        multipart.transferTo(convFile);
        return convFile;
    }

    public String downloadFile(Storage storage, String fileName) {
        Blob blob = storage.get("oracle-integrator-bucket", fileName);
        String PATH_TO_JSON_KEY = baseConfigPath;
        URL signedUrl = null;
        try {
            signedUrl = storage.signUrl(BlobInfo.newBuilder("oracle-integrator-bucket", fileName).build(),
                    1, TimeUnit.DAYS, Storage.SignUrlOption.signWith(ServiceAccountCredentials.fromStream(
                            new FileInputStream(PATH_TO_JSON_KEY))));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return signedUrl.toString();
    }


    public String storeFile(File file){
        String projectId = "oracle-symphony-integrator";
        String bucketName = "oracle-integrator-bucket";

        String objectName = file.getName();

        StorageOptions storageOptions = null;
        try {
            storageOptions = StorageOptions.newBuilder()
                    .setProjectId(projectId)
                    .setCredentials(GoogleCredentials.fromStream(new
                            FileInputStream(baseConfigPath))).build();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Storage storage = storageOptions.getService();

        BlobId blobId = BlobId.of(bucketName, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
        try {
            storage.create(blobInfo, Files.readAllBytes(Paths.get(file.toPath().toString())));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return downloadFile(storage, objectName);
    }
}

