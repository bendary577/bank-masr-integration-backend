package com.sun.supplierpoc.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.controllers.SalesController;
import com.sun.supplierpoc.controllers.SyncJobController;
import com.sun.supplierpoc.controllers.SyncJobTypeController;
import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.models.configurations.CostCenter;
import com.sun.supplierpoc.models.configurations.Location;
import com.sun.supplierpoc.repositories.GeneralSettingsRepo;
import com.sun.supplierpoc.repositories.SyncJobRepo;
import com.sun.supplierpoc.repositories.SyncJobTypeRepo;
import org.apache.commons.httpclient.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Paths;
import java.security.Principal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class SupportService {

    @Autowired
    private SyncJobTypeRepo syncJobTypeRepo;

    @Autowired
    private SyncJobRepo syncJobRepo;

    @Autowired
    private SendEmailService emailService;

    @Autowired
    SyncJobTypeController syncJobTypeController;

    @Autowired
    private SalesController salesController;

    @Autowired
    private GeneralSettingsRepo generalSettingsRepo;

    public void supportExportedFile(User user, Account account, Date fromDate, Date toDate, String store, String email, String moduleId, Principal principal) {

        Response response = new Response();

        Optional<SyncJobType> syncJobTypeOptional = syncJobTypeRepo.findById(moduleId);
        if (syncJobTypeOptional.isPresent()) {
            boolean notSuccess = true;
            while (notSuccess) {
                SyncJobType syncJobType = syncJobTypeOptional.get();
                FileSystemResource file = getZip(account, fromDate, toDate, store, syncJobType.getName());
                if (file != null) {
                    notSuccess = false;
                    try {
                        emailService.sendExportedSyncsMailMail(file, account, user, fromDate, toDate, store, email, syncJobType);
                        response.setMessage("Your request has been received successfully.");
                        response.setStatus(true);
                    } catch (Exception e) {
                        emailService.sendExportedSyncsMailMail(file, account, user, fromDate, toDate, store, email, syncJobType);
                        response.setMessage(e.getMessage());
                        response.setStatus(false);
                    }
                } else {
                    reSyncModules(account, fromDate, toDate, store, syncJobType, principal);
                }
            }
        } else {
            response.setMessage("Wrong sync job type.");
            response.setStatus(false);
        }
    }

    public FileSystemResource getZip(Account account, Date fromDate, Date toDate, String store, String module) {

        List<FileSystemResource> files = new ArrayList<>();
        while (!checkIfEquivalent(fromDate, toDate)) {
            String[] daysOfWeek = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
            String fileName = String.format("%02d", fromDate.getDate()) +
                    String.format("%02d", (fromDate.getMonth() + 1)) + "20" +
                    String.format("%03d", fromDate.getYear()).substring(1, 3) +
                    daysOfWeek[fromDate.getDay()];
            String path = account.getName() + "/" + module + "/" +(fromDate.getMonth() + 1) + "/" + store + "/" + fileName + " - " + store + ".ndf";
            fromDate = addDays(fromDate, 1);
            FileSystemResource file = new FileSystemResource(path);
            files.add(file);
        }
        FileSystemResource fileSystemResource = zip(files, "exported_files.zip");
        return fileSystemResource;
    }

    public Date addDays(Date date, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, days); //minus number would decrement the days
        return cal.getTime();
    }

    public FileSystemResource zip(List<FileSystemResource> files, String filename) {
        try {
            String zipFile = Constants.ZIP_PATH;

            byte[] buffer = new byte[1024];

            FileOutputStream out = new FileOutputStream(zipFile);
            ZipOutputStream zipOut = new ZipOutputStream(out);

            for (int i = 0; i < files.size(); i++) {
                FileSystemResource file = files.get(i);
                FileInputStream fin = new FileInputStream(file.getPath());
                zipOut.putNextEntry(new ZipEntry(file.getPath()));
                int length;
                while ((length = fin.read(buffer)) > 0) {
                    zipOut.write(buffer, 0, length);
                }
                zipOut.closeEntry();
                fin.close();
            }
            zipOut.close();
            System.out.println("Zip file has been created!");

            FileSystemResource fileSystemResource = new FileSystemResource(Constants.ZIP_PATH);
            return fileSystemResource;

        } catch (IOException ioe) {
            System.out.println("IOException :" + ioe);
            return null;
        }
    }

    public void reSyncModules(Account account, Date fromDate, Date toDate, String store, SyncJobType syncJobType, Principal principal){

        Response response = new Response();

        if(syncJobType.getName().equals(Constants.SALES)){

            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

            syncJobType.getConfiguration().setFromDate( dateFormat.format(fromDate));
            syncJobType.getConfiguration().setToDate(dateFormat.format(addDays(toDate, 1)));

            syncJobTypeRepo.save(syncJobType);
            GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);
            ArrayList<CostCenter> locations = generalSettings.getLocations();
            for(CostCenter location : locations){
                if(location.costCenterReference.equals(store)) {
                    location.checked = true;
                }else{
                    location.checked = false;
                }
            }

            generalSettings.setLocations(locations);
            generalSettingsRepo.save(generalSettings);

            try {
                response = salesController.getPOSSalesRequest(principal).getBody();
            } catch (ParseException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    public boolean checkIfEquivalent(Date fromDate, Date toDate) {

        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(fromDate);
        cal2.setTime(toDate);
        boolean sameDay = cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);

        return sameDay;
    }
}
