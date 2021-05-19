package com.sun.supplierpoc.services;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.configurations.AccountCredential;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

@Service
public class FtpService {

    public AccountCredential getAccountCredential(Account account) {

        ArrayList<AccountCredential> accountCredentials = account.getAccountCredentials();
        AccountCredential sunCredentials = account.getAccountCredentialByAccount(Constants.SUN, accountCredentials);

        return sunCredentials;
    }

    public boolean sendFile(AccountCredential accountCredential, String file, String Path){

        FTPClient ftpClient = new FTPClient();

        boolean response = false;
        try {

            ftpClient.connect(accountCredential.getHost(), 21);
            ftpClient.login(accountCredential.getUsername(), accountCredential.getPassword());

            ftpClient.enterLocalPassiveMode();

            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            InputStream inputStream = new URL(file).openStream();

            System.out.println("Start uploading first file");
            response = ftpClient.storeFile(Path, inputStream);

            inputStream.close();
            if (response) {
                System.out.println("The first file is uploaded successfully.");
            }else {
                System.out.println("Can't upload the file.");
            }

        } catch (IOException ex) {
            System.out.println("Error: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        return response;
    }

}
