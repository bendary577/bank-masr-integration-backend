package com.sun.supplierpoc.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.controllers.*;
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

    @Autowired
    private JournalController journalController;

    @Autowired
    private CostOfGoodsController costOfGoodsController;

    @Autowired
    private InvoiceController invoiceController;

    @Autowired
    private CreditNoteController creditNoteController;

    @Autowired
    private WastageController wastageController;

    @Autowired
    private BookedProductionController bookedProductionController;


    public void supportExportedFile(User user, Account account, Date fromDate, Date toDate, List<CostCenter> stores, String email,
                                    List<SyncJobType> modules, Principal principal) {

        Response response = new Response();

        boolean notSuccess = true;
        boolean isFirst = true;
        int count = 0 ;
        while (notSuccess && count != 3 ) {

            FileSystemResource file = getZip(account, fromDate, toDate, stores, modules);

            if (file != null && !isFirst) {
                notSuccess = false;
                try {
                    emailService.sendExportedSyncsMailMail(file, account, user, fromDate, toDate, stores, email, modules);
                    response.setMessage("Your request has been received successfully.");
                    response.setStatus(true);
                } catch (Exception e) {
                    emailService.sendExportedSyncsMailMail(file, account, user, fromDate, toDate, stores, email, modules);
                    response.setMessage(e.getMessage());
                    response.setStatus(false);
                }
            } else {
                isFirst = false;
                count++;
                reSyncModules(account, fromDate, toDate, stores, modules, principal);
            }

        }
    }

    public FileSystemResource getZip(Account account, Date fromDate, Date toDate, List<CostCenter> stores,
                                     List<SyncJobType> modules) {

        List<FileSystemResource> files = new ArrayList<>();

        for (SyncJobType tempSyncJobType : modules) {

            Optional<SyncJobType> syncJobTypeOptional = syncJobTypeRepo.findById(tempSyncJobType.getId());

            if (syncJobTypeOptional.isPresent()) {

            SyncJobType syncJobType = syncJobTypeOptional.get();

            String module = syncJobType.getName();

            for (CostCenter costCenter : stores) {

                String store = costCenter.costCenterReference;
                Date toDateRequest = addDays(toDate, 1);
                Date fromDateRequest = fromDate;

                    while (!checkIfEquivalent(fromDateRequest, toDateRequest)) {

                        String[] daysOfWeek = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
                        String fileName = String.format("%02d", fromDate.getDate()) +
                                String.format("%02d", (fromDate.getMonth() + 1)) + "20" +
                                String.format("%03d", fromDate.getYear()).substring(1, 3) +
                                daysOfWeek[fromDate.getDay()];

                        String path = account.getName() + "/" + module + "/" + (fromDate.getMonth() + 1) + "/" + store + "/" + fileName + " - " + store + ".ndf";

                        fromDateRequest = addDays(fromDateRequest, 1);

                        FileSystemResource file = new FileSystemResource(path);
                        files.add(file);
                    }
                }

            } else {
                return null;
            }
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

    public void reSyncModules(Account account, Date fromDate, Date toDate, List<CostCenter> stores, List<SyncJobType> syncJobTypes, Principal principal){

        Response response = new Response();

        for (SyncJobType tempSyncJobType : syncJobTypes) {

            Optional<SyncJobType> syncJobTypeOptional = syncJobTypeRepo.findById(tempSyncJobType.getId());

            if (syncJobTypeOptional.isPresent()) {
                SyncJobType syncJobType = syncJobTypeOptional.get();


                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    syncJobType.getConfiguration().setFromDate(dateFormat.format(fromDate));
                    syncJobType.getConfiguration().setToDate(dateFormat.format(addDays(toDate, 1)));
                    syncJobTypeRepo.save(syncJobType);
                    GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);
                    ArrayList<CostCenter> locations = generalSettings.getLocations();

                    List<String> storesList  = new ArrayList<>();
                    for(CostCenter store : stores){
                        storesList.add(store.costCenterReference);
                    }
                        for (CostCenter location : locations) {
                            if (storesList.contains(location.costCenterReference)) {
                                location.checked = true;
                            } else {
                                location.checked = false;
                            }
                        }

                    generalSettings.setLocations(locations);
                    generalSettingsRepo.save(generalSettings);
                    try {

                        if (syncJobType.getName().equals(Constants.SALES)) {
                            response = salesController.getPOSSalesRequest(principal).getBody();
                        }else if(syncJobType.getName().equals(Constants.CONSUMPTION)){
                            journalController.getJournalsRequest(principal).getBody();
                        }else if(syncJobType.getName().equals(Constants.APPROVED_INVOICES)){
                            invoiceController.getApprovedInvoicesRequest(principal).getBody();
                        }else if(syncJobType.getName().equals(Constants.BOOKED_PRODUCTION)){
                            bookedProductionController.getBookedProductionRequest(principal);
                        }else if(syncJobType.getName().equals(Constants.COST_OF_GOODS)){
                            costOfGoodsController.getCostOfGoodsRequest(principal);
                        }else if(syncJobType.getName().equals(Constants.WASTAGE)){
                            wastageController.getWastageRequest(principal);
                        }else if (syncJobType.getName().equals(Constants.CREDIT_NOTES)){
                            creditNoteController.getCreditNotesRequest(principal);
                        }

                    } catch (ParseException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            } else {

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
