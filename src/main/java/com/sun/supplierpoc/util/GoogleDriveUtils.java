package com.sun.supplierpoc.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.io.IOException;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.Permission;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.sun.supplierpoc.components.GoogleDriveClient;
import com.sun.supplierpoc.models.Account;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class GoogleDriveUtils {

    private static final String baseConfigPath = "/oracle-symphony-integrator-e10d2db033fa.json";

    private static final String APPLICATION_NAME = "osii-google-drive";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);
    private static final String CREDENTIALS_FILE_PATH = "/client_secret.json";

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
//        InputStream in = new FileInputStream(CREDENTIALS_FILE_PATH);
        InputStream in = GoogleDriveClient.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();


        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setHost("127.0.0.1").setPort(8089).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }


    public static Drive getDriveService() throws IOException {
        final NetHttpTransport HTTP_TRANSPORT;
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

            return new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME)
                    .build();
        }catch(Exception e){
            return null;
        }
    }

    // PRIVATE!
    public Boolean uploadFileTODrive( Account account, String module, java.io.File uploadFile){

        File fileMetadata = new File();
        fileMetadata.setName(uploadFile.getName());

        String parentModule = getAllParentPath(account, module, uploadFile);
        List<String> parents = Arrays.asList(parentModule);

        fileMetadata.setParents(parents);
        File file = null;
        try {
            Drive driveService = GoogleDriveUtils.getDriveService();
            AbstractInputStreamContent uploadStreamContent = new FileContent("text/plain", uploadFile);
            file = driveService.files().create(fileMetadata, uploadStreamContent)
                    .setFields("id, webContentLink, webViewLink, parents").execute();
        }catch(Exception e){
            LoggerFactory.getLogger(GoogleDriveUtils.class).info(e.getMessage());
        }
         if(file != null){
             return true;
         }else{
             return false;
         }
    }

    private String getAllParentPath(Account account, String module, java.io.File uploadFile) {

        String parentId = "";
        String parentAccount = "";
        List<File> parentFile = getGoogleFoldersByName(account.getName());
        if( parentFile.size() > 0){
            parentAccount = parentFile.get(0).getId();
        }else{
            File parenModuleFile = createGoogleFolder(null, account.getName());
            if(parenModuleFile == null ){
                return "";
            }
            parentAccount = parenModuleFile.getId();
        }

        List<File> moduleFiles = getGoogleFoldersByName(module);

        String parentModule = "";
        if( moduleFiles.size() > 0){
            for(File file : moduleFiles){
                if(file.getParents().contains(parentFile.get(0).getId())){
                    parentModule = moduleFiles.get(0).getId();
                }
            }
        }else{
            File parenModuleFile = createGoogleFolder(parentAccount, module);
            if(parenModuleFile == null ){
                return "";
            }
            parentModule = parenModuleFile.getId();
        }
        return parentModule;
    }

    public File createGoogleFolder(String folderIdParent, String folderName) {
        try {
            File fileMetadata = new File();
            fileMetadata.setName(folderName);
            fileMetadata.setMimeType("application/vnd.google-apps.folder");
            if (folderIdParent != null) {
                List<String> parents = Arrays.asList(folderIdParent);

                fileMetadata.setParents(parents);
            }
            Drive driveService = GoogleDriveUtils.getDriveService();
            // Create a Folder.// Returns File object with id & name fields will be assigned values
            File file = driveService.files().create(fileMetadata).setFields("id, name").execute();
//        System.out.println("Created folder with id= "+ file.getId());
//        System.out.println("                    name= "+ file.getName());
            return file;
        }catch (Exception e){
            return null;
        }
    }

    // com.google.api.services.drive.model.File
    public List<File> getGoogleSubFolders(String googleFolderIdParent) throws IOException, GeneralSecurityException {

        Drive driveService = GoogleDriveUtils.getDriveService();
        String pageToken = null;
        List<File> list = new ArrayList<File>();
        String query = null;
        if (googleFolderIdParent == null) {
            query = " mimeType = 'application/vnd.google-apps.folder' " //
                    + " and 'root' in parents";
        } else {
            query = " mimeType = 'application/vnd.google-apps.folder' " //
                    + " and '" + googleFolderIdParent + "' in parents";
        }
        do {
            FileList result = driveService.files().list().setQ(query).setSpaces("drive") //
                    // Fields will be assigned values: id, name, createdTime
                    .setFields("nextPageToken, files(id, name, createdTime)")//
                    .setPageToken(pageToken).execute();
            for (File file : result.getFiles()) {
                list.add(file);
            }
            pageToken = result.getNextPageToken();
        } while (pageToken != null);
        return list;
    }

    // com.google.api.services.drive.model.File
    public List<File> getGoogleFilesByName(String fileNameLike){

        try {
            Drive driveService = GoogleDriveUtils.getDriveService();
            String pageToken = null;
            List<File> list = new ArrayList<File>();
            String query = " name contains '" + fileNameLike + "' " //
                    + " and mimeType != 'application/vnd.google-apps.folder' ";
            do {
                FileList result = driveService.files().list().setQ(query).setSpaces("drive") //
                        // Fields will be assigned values: id, name, createdTime, mimeType
                        .setFields("nextPageToken, files(id, name, createdTime, mimeType)")//
                        .setPageToken(pageToken).execute();
                for (File file : result.getFiles()) {
                    list.add(file);
                }
                pageToken = result.getNextPageToken();
            } while (pageToken != null);
            //
            for (File folder : list) {
                System.out.println("Mime Type: " + folder.getMimeType() + " --- Name: " + folder.getName());
            }
            System.out.println("Done!");
            return list;
        }catch(Exception e){
            return new ArrayList<>();
        }
    }


    // com.google.api.services.drive.model.File
    public List<File> getGoogleFoldersByName(String fileNameLike){

        try {
            Drive driveService = GoogleDriveUtils.getDriveService();
            String pageToken = null;
            List<File> list = new ArrayList<>();
            String query = " name = '" + fileNameLike + "' " //
                    + " and mimeType = 'application/vnd.google-apps.folder' ";
            do {
                FileList result = driveService.files().list().setQ(query).setSpaces("drive") //
                        .setFields("nextPageToken, files(id, name, createdTime, mimeType)")//
                        .setPageToken(pageToken).execute();
                for (File file : result.getFiles()) {
                    list.add(file);
                }
                pageToken = result.getNextPageToken();
            } while (pageToken != null);
            for (File folder : list) {
                System.out.println("Mime Type: " + folder.getMimeType() + " --- Name: " + folder.getName());
            }System.out.println("Done!");
            return list;
        }catch(Exception e){
            return new ArrayList<>();
        }
    }
    // Public a Google File/Folder.
    public static Permission createPublicPermission(String googleFileId) throws IOException {
        // All values: user - group - domain - anyone
        String permissionType = "anyone";
        // All values: organizer - owner - writer - commenter - reader
        String permissionRole = "reader";

        Permission newPermission = new Permission();
        newPermission.setType(permissionType);
        newPermission.setRole(permissionRole);

        Drive driveService = GoogleDriveUtils.getDriveService();


        return driveService.permissions().create(googleFileId, newPermission).execute();
    }

    public static Permission createPermissionForEmail(String googleFileId, String googleEmail) throws IOException {
        // All values: user - group - domain - anyone
        String permissionType = "user"; // Valid: user, group
        // organizer - owner - writer - commenter - reader
        String permissionRole = "reader";

        Permission newPermission = new Permission();
        newPermission.setType(permissionType);
        newPermission.setRole(permissionRole);

        newPermission.setEmailAddress(googleEmail);

        Drive driveService = GoogleDriveUtils.getDriveService();
        return driveService.permissions().create(googleFileId, newPermission).execute();
    }

    // com.google.api.services.drive.model.File
    public List<File> getGoogleRootFolders() throws IOException, GeneralSecurityException {
        return getGoogleSubFolders(null);
    }

    public void main() throws IOException, GeneralSecurityException {
        List<File> googleRootFolders = getGoogleRootFolders();
        for (File folder : googleRootFolders) {
            System.out.println("Folder ID: " + folder.getId() + " --- Name: " + folder.getName());
        }
    }


}