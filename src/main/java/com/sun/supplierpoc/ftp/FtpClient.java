package com.sun.supplierpoc.ftp;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;

public class FtpClient {
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
}
