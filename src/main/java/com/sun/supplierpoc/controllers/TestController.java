package com.sun.supplierpoc.controllers;

import com.google.api.services.drive.model.FileList;
import com.sun.supplierpoc.components.GoogleDriveClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private GoogleDriveClient driveClient;

//    public List<File> listingEverything() throws IOException, GeneralSecurityException {
//        // Print the names and IDs for up to 10 files.
//        FileList result = driveClient.getInstance().files().list()
//                .setPageSize(10)
//                .setFields("nextPageToken, files(id, name)")
//                .execute();
//        return result.getFiles();
//    }
//    // MainController.java
//    @GetMapping({"/"})
//    public ResponseEntity<List<File>> listEverything() throws IOException, GeneralSecurityException {
//        List<File> files = fileManager.listEverything();
//        return ResponseEntity.ok(files);
//    }

//    @GetMapping("/firstTest")
//    public void main(){
//        try {
//            java.io.File file = new java.io.File("fcm/client_secret_465767792052-m03ec4m2krcivcgfpr4sp3rh7m6jqe39.apps.googleusercontent.com.json");
//            GoogleDriveClient googleDriveClient = new GoogleDriveClient();
////                    googleDriveClient.main(file);
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (GeneralSecurityException e) {
//            e.printStackTrace();
//        }
//    }
}
