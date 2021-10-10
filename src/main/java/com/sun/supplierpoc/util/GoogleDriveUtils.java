package com.sun.supplierpoc.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

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
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.sun.supplierpoc.components.GoogleDriveClient;
import com.sun.supplierpoc.models.Account;
import org.slf4j.LoggerFactory;

public class GoogleDriveUtils {

    private static final String baseConfigPath = "/oracle-symphony-integrator-e10d2db033fa.json";

    private static final String APPLICATION_NAME = "osii-google-drive";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);
    private static final String CREDENTIALS_FILE_PATH = "/client_secret.json";

    private Drive drive;

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public GoogleDriveUtils() {
        this.drive = getDriveService();
    }

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


    public Drive getDriveService()   {
        final NetHttpTransport HTTP_TRANSPORT;
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

            Drive drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            return drive;
        } catch (Exception e) {
            return null;
        }
    }

    // PRIVATE!
    public Boolean uploadFileTODrive(Account account, String module, java.io.File uploadFile) {

        File fileMetadata = new File();
        fileMetadata.setName(uploadFile.getName());

        String parentFolder = getAllParentPath(account, module, uploadFile);
        List<String> parents = Arrays.asList(parentFolder);

        fileMetadata.setParents(parents);

        List<File> files = getGoogleFilesByNameAndParent(uploadFile.getName(), parentFolder);
        if (files.size() != 0) {
            for (File file : files) {
                deleteFile(file.getId());
            }
        }
        File file = null;
        try {
            AbstractInputStreamContent uploadStreamContent = new FileContent("text/plain", uploadFile);
            file = this.drive.files().create(fileMetadata, uploadStreamContent)
                    .setFields("id, webContentLink, webViewLink, parents").execute();
        } catch (Exception e) {
            LoggerFactory.getLogger(GoogleDriveUtils.class).info(e.getMessage());
        }
        if (file != null) {
            return true;
        } else {
            return false;
        }
    }

    private String getAllParentPath(Account account, String module, java.io.File uploadFile)  {

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

        List<File> moduleFiles = getGoogleSubFolders(parentAccount, module);
        String parentModule = "";
        if( moduleFiles.size() > 0){
            parentModule = moduleFiles.get(0).getId();
        }else{
            File parenModuleFile = createGoogleFolder(parentAccount, module);
            if(parenModuleFile == null ){
                return "";
            }
            parentModule = parenModuleFile.getId();
        }

        HashMap<String, String> dateValues = getDateDirectoryAndLocation(uploadFile);

        List<File> yearFiles = getGoogleSubFolders(parentModule, dateValues.get("year"));
        String parentYear = "";
        if( yearFiles.size() > 0){
            parentYear = yearFiles.get(0).getId();
        }else{
            File parenYearFile = createGoogleFolder(parentModule, dateValues.get("year"));
            if(parenYearFile == null ){
                return "";
            }
            parentYear = parenYearFile.getId();
        }

        List<File> monthFiles = getGoogleSubFolders(parentYear, dateValues.get("month"));
        String parentMonth = "";
        if( monthFiles.size() > 0){
            parentMonth = monthFiles.get(0).getId();
        }else{
            File parenMonthFile = createGoogleFolder(parentYear, dateValues.get("month"));
            if(parenMonthFile == null ){
                return "";
            }
            parentMonth = parenMonthFile.getId();
        }

        List<File> locationFiles = getGoogleSubFolders(parentMonth, dateValues.get("location"));
        String parentLocation = "";
        if( locationFiles.size() > 0){
            parentLocation = locationFiles.get(0).getId();
        }else{
            File parenLocationFile = createGoogleFolder(parentMonth, dateValues.get("location"));
            if(parenLocationFile == null ){
                return "";
            }
            parentLocation = parenLocationFile.getId();
        }

        return parentLocation;
    }

    private HashMap<String, String> getDateDirectoryAndLocation(java.io.File uploadFile) {

        HashMap<String, String> date = new HashMap<>();
        String name = uploadFile.getName();

        String month = name.substring(2, 4);
        if(month.substring(0, 1).equals("0")){
            date.put("month", month.substring(1,2));
        }else{
            date.put("month", month);
        }

        String year = name.substring(4, 8);
        date.put("year", year);

        int length = name.length();
        String location = name.substring(14, (length - 4));
        date.put("location", location);

        return date;
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
            // Create a Folder.// Returns File object with id & name fields will be assigned values
            File file = this.drive.files().create(fileMetadata).setFields("id, name").execute();
//        System.out.println("Created folder with id= "+ file.getId());
//        System.out.println("                    name= "+ file.getName());
            return file;
        }catch (Exception e){
            return null;
        }
    }

    // com.google.api.services.drive.model.File
    public List<File> getGoogleSubFolders(String googleFolderIdParent, String fileNameLike) {

        String pageToken = null;
        List<File> list = new ArrayList<File>();
        String query = null;
        if (googleFolderIdParent == null) {
            query = " name = '" + fileNameLike + "' " //
                    + " and  mimeType = 'application/vnd.google-apps.folder' " //
                    + " and 'root' in parents";
        } else {
            query = " name = '" + fileNameLike + "' " //
                    + " and mimeType = 'application/vnd.google-apps.folder' " //
                    + " and '" + googleFolderIdParent + "' in parents";
        }
        do {
            FileList result = null;
            try {
                result = this.drive.files().list().setQ(query).setSpaces("drive") //
                        // Fields will be assigned values: id, name, createdTime
                        .setFields("nextPageToken, files(id, name, createdTime)")//
                        .setPageToken(pageToken).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
            String pageToken = null;
            List<File> list = new ArrayList<File>();
            String query = " name contains '" + fileNameLike + "' " //
                    + " and mimeType != 'application/vnd.google-apps.folder' ";
            do {
                FileList result = this.drive.files().list().setQ(query).setSpaces("drive") //
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
    public List<File> getGoogleFilesByNameAndParent(String fileNameLike, String googleFolderIdParent){

        try {
            String pageToken = null;
            List<File> list = new ArrayList<File>();
            String query = " name contains '" + fileNameLike + "' " //
                    + " and mimeType != 'application/vnd.google-apps.folder' "//
                    + " and '" + googleFolderIdParent + "' in parents";
            do {
                FileList result = this.drive.files().list().setQ(query).setSpaces("drive") //
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
            String pageToken = null;
            List<File> list = new ArrayList<>();
            String query = " name = '" + fileNameLike + "' " //
                    + " and mimeType = 'application/vnd.google-apps.folder' ";
            do {
                FileList result = this.drive.files().list().setQ(query).setSpaces("drive") //
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

    public boolean deleteFile(String fileId) {
        boolean deleted;

        try {
            this.drive.files().delete(fileId).execute();
            deleted = true;
        }catch(Exception e){
            deleted = false;
        }
        return deleted;
    }


    // Public a Google File/Folder.
    public Permission createPublicPermission(String googleFileId) {
        // All values: user - group - domain - anyone
        String permissionType = "anyone";
        // All values: organizer - owner - writer - commenter - reader
        String permissionRole = "reader";

        Permission newPermission = new Permission();
        newPermission.setType(permissionType);
        newPermission.setRole(permissionRole);

        Permission permission = null;
        try {
            permission = this.drive.permissions().create(googleFileId, newPermission).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return permission;
    }

    public Permission createPermissionForEmail(String googleFileId, String googleEmail)  {
        // All values: user - group - domain - anyone
        String permissionType = "user"; // Valid: user, group
        // organizer - owner - writer - commenter - reader
        String permissionRole = "reader";

        Permission newPermission = new Permission();
        newPermission.setType(permissionType);
        newPermission.setRole(permissionRole);

        newPermission.setEmailAddress(googleEmail);

        Permission permission = null;
        try {
            permission = this.drive.permissions().create(googleFileId, newPermission).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return permission;
    }

}
