package com.sun.supplierpoc.ftp;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.configurations.AccountCredential;
import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class FtpClient {
    @Value("${spring.cloud.gcp.credentials.location}")
    private String baseConfigPath;

    private String server;
    private int port;
    private String user;
    private String password;
    private FTPClient ftp;


    public FtpClient() {
    }

    public FtpClient(String server, String user, String password) {
        this.server = server;
        this.user = user;
        this.password = password;
    }

    public boolean open() throws IOException {
        ftp = new FTPClient();

        ftp.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));

        ftp.connect(server);
        int reply = ftp.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            ftp.disconnect();
//            throw new IOException("Exception in connecting to FTP Server");
            return false;
        }

        return ftp.login(user, password);
    }

    public void close() throws IOException {
        ftp.logout();
        ftp.disconnect();
    }

    public boolean putFileToPath(File file, String path) throws IOException {
        ftp.setControlKeepAliveTimeout(120);
        return ftp.storeFile(path, new FileInputStream(file));
    }

    public boolean putFile(String file, String path) throws IOException {
        ftp.setControlKeepAliveTimeout(200);
//        ftp.setConnectTimeout(50000);
//         File files = new URL(file).openStream();

        return ftp.storeFile(path, new URL(file).openStream());
    }

    public FtpClient createFTPClient(Account account) {
        ArrayList<AccountCredential> accountCredentials = account.getAccountCredentials();
        AccountCredential sunCredentials = account.getAccountCredentialByAccount(Constants.SUN, accountCredentials);

        String username = sunCredentials.getUsername();
        String password = sunCredentials.getPassword();
        String host = sunCredentials.getHost();

        if (!username.equals("") && !password.equals("") && !host.equals("")){
            return new FtpClient(host, username, password);
        }
        return null;
    }
}
