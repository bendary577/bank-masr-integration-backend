package com.sun.supplierpoc.services;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.*;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.apache.commons.compress.utils.IOUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
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

        String url = "https://storage.googleapis.com/" + bucketName + "/" + objectName;
        return url;
//        return downloadFile(storage, objectName);
    }

    public String storeFile(MultipartFile file, String bucketPath, String fileName) {

        File  fileImage = null;
        try {
            fileImage = multipartToFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String projectId = "oracle-symphony-integrator";
        String bucketName = "oracle-integrator-bucket";

        String objectName = bucketPath + fileName;

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

        return "https://storage.googleapis.com/" + bucketName + "/" + objectName;
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

        String objectName = file.getPath();
        objectName = objectName.replaceAll("\\\\","/");

        StorageOptions storageOptions = null;
        try {
            storageOptions = StorageOptions.newBuilder()
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

    @Service
    public static class QRCodeGenerator {

        @Autowired
        private ImageService imageService;

        public String generateQRCodeImage(String text, int width, int height, String filePath)
                throws WriterException, IOException {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
            Path path = FileSystems.getDefault().getPath(filePath);
            MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);
            return path.toString();
        }

        public String getQRCodeImage(String text, int width, int height, String filePath) throws WriterException, IOException {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);

            File file = new File(filePath);

            Path path = Paths.get(file.getAbsolutePath());
            try {
                Files.write(path, pngOutputStream.toByteArray());
            } catch (IOException ex) {
                LoggerFactory.getLogger(QRCodeGenerator.class).info(ex.getMessage());
            }

            FileInputStream input = new FileInputStream(file);
            MultipartFile multipartFile = new MockMultipartFile("file",
                    file.getName(), "PNG", IOUtils.toByteArray(input));

            String QRPath = imageService.store(multipartFile);

            new File(filePath).delete();

            return QRPath;
        }
    }
}

