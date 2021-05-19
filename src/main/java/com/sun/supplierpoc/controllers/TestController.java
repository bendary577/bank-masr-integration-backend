package com.sun.supplierpoc.controllers;

import com.sun.supplierpoc.components.GoogleDriveClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.GeneralSecurityException;

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/firstTest")
    public void main(){
        try {
            java.io.File file = new java.io.File("fcm/client_secret_465767792052-m03ec4m2krcivcgfpr4sp3rh7m6jqe39.apps.googleusercontent.com.json");
            GoogleDriveClient googleDriveClient = new GoogleDriveClient();
                    googleDriveClient.main(file);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
    }
}
