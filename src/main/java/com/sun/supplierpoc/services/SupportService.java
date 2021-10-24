package com.sun.supplierpoc.services;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.controllers.*;
import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.models.configurations.CostCenter;
import com.sun.supplierpoc.repositories.GeneralSettingsRepo;
import com.sun.supplierpoc.repositories.SyncJobRepo;
import com.sun.supplierpoc.repositories.SyncJobTypeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import java.io.*;
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
    @Autowired
    private FeatureService featureService;

    public void supportExportedFile(User user, Account account, Date fromDate, Date toDate, List<CostCenter> stores, String email,
                                    List<SyncJobType> modules, Principal principal) {

        Response response = new Response();
        boolean status;
        int count = 0;

        do {
            status = reSyncModules(account, fromDate, toDate, stores, modules, principal);
            count++;
        }while (!status && count < 3);

        FileSystemResource file = null;
        if(status){
            /* Check if user has custom report feature */
            if (featureService.hasFeature(account, Constants.CUSTOM_REPORT)){
                file = getReportsZip(account, fromDate, toDate,modules);
            }else{
                file = getZip(account, fromDate, toDate, stores, modules);
            }
        }

        if (file != null) {
            try {
                emailService.sendExportedSyncsMailMail(file, account, user, fromDate, toDate, stores, email, modules);
                response.setMessage("Your request has been received successfully.");
                response.setStatus(true);
            } catch (Exception e) {
                if(featureService.hasFeature(account, Constants.CUSTOM_REPORT)){
                    emailService.sendFailureMail(user, email, modules);
                }else{
                    emailService.sendExportedSyncsMailMail(file, account, user, fromDate, toDate, stores, email, modules);
                }
                response.setMessage(e.getMessage());
                response.setStatus(false);
            }
        } else {
            if(featureService.hasFeature(account, Constants.CUSTOM_REPORT)){
                emailService.sendFailureMail(user, email, modules);
            }else{
                emailService.sendExportedSyncsMailMail(file, account, user, fromDate, toDate, stores, email, modules);
            }
            response.setMessage("Failed to send mail.");
            response.setStatus(false);
        }
    }

    private FileSystemResource getZip(Account account, Date fromDate, Date toDate, List<CostCenter> stores,
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
                            String fileName = String.format("%02d", fromDateRequest.getDate()) +
                                    String.format("%02d", (fromDateRequest.getMonth() + 1)) + "20" +
                                    String.format("%03d", fromDateRequest.getYear()).substring(1, 3) +
                                    daysOfWeek[fromDateRequest.getDay()];

                            String path = account.getName() + "/" + module + "/" + "20" + String.format("%03d", fromDateRequest.getYear()).substring(1, 3) + "/" +
                                    (fromDateRequest.getMonth() + 1) + "/" + store + "/" + fileName + " - " + store + ".ndf";
                            fromDateRequest = addDays(fromDateRequest, 1);


                            FileSystemResource file = new FileSystemResource(path);
                            if (file.exists()){
                                files.add(file);
                            }
                        }
                    }
            }
        }
        if(files.size() > 0){
            return zip(files, "exported_files.zip");
        }else{
            return null;
        }
    }

    public FileSystemResource getReportsZip(Account account, Date fromDate, Date toDate, List<SyncJobType> modules) {

        List<FileSystemResource> files = new ArrayList<>();
        for (SyncJobType tempSyncJobType : modules) {
            Optional<SyncJobType> syncJobTypeOptional = syncJobTypeRepo.findById(tempSyncJobType.getId());
            if (syncJobTypeOptional.isPresent()) {
                SyncJobType syncJobType = syncJobTypeOptional.get();
                String module = syncJobType.getName();

                DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
                String dateFormatted = dateFormat.format(fromDate) + dateFormat.format(toDate);

                String fileDirectory = account.getName() + "/" + syncJobType.getName() + "/CustomReports/";
                String fileName = fileDirectory + module + dateFormatted + ".xlsx";

                FileSystemResource file = new FileSystemResource(fileName);
                if (file.exists()){
                    files.add(file);
                }
            }
        }
        if(files.size() > 0){
            return zip(files, "exported_reports.zip");
        }else{
            return null;
        }
    }

    public Date addDays(Date date, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, days); //minus number would decrement the days
        return cal.getTime();
    }

    public FileSystemResource zip(List<FileSystemResource> files, String filename) {
        try {
            String zipFile = Constants.BASE_ZIP_PATH + filename;

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

            return new FileSystemResource(zipFile);

        } catch (IOException ioe) {
            System.out.println("IOException :" + ioe);
            return null;
        }
    }

    private boolean reSyncModules(Account account, Date fromDate, Date toDate, List<CostCenter> stores, List<SyncJobType> syncJobTypes, Principal principal) {
        boolean status = true;
        HashMap<String, Object> responseHash = new HashMap<>();

        GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);
        ArrayList<CostCenter> locations = generalSettings.getLocations();

        List<String> storesList = new ArrayList<>();
        for (CostCenter store : stores) {
            storesList.add(store.costCenterReference);
        }
        for (CostCenter location : locations) {
            location.checked = storesList.contains(location.costCenterReference);
        }

        generalSettings.setLocations(locations);
        generalSettingsRepo.save(generalSettings);

        for (SyncJobType tempSyncJobType : syncJobTypes) {
            Optional<SyncJobType> syncJobTypeOptional = syncJobTypeRepo.findById(tempSyncJobType.getId());

            if (syncJobTypeOptional.isPresent()) {
                SyncJobType syncJobType = syncJobTypeOptional.get();

                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                syncJobType.getConfiguration().setFromDate(dateFormat.format(fromDate));
                syncJobType.getConfiguration().setToDate(dateFormat.format(toDate));
                syncJobTypeRepo.save(syncJobType);

                try {
                    if (syncJobType.getName().equals(Constants.SALES)) {
                         salesController.getPOSSalesRequest(principal).getBody();
                    } else if (syncJobType.getName().equals(Constants.CONSUMPTION)) {
                        responseHash = journalController.getJournalsRequest(principal).getBody();
                    } else if (syncJobType.getName().equals(Constants.APPROVED_INVOICES)) {
                        responseHash = invoiceController.getApprovedInvoicesRequest(principal).getBody();
                    } else if (syncJobType.getName().equals(Constants.BOOKED_PRODUCTION)) {
                        responseHash = bookedProductionController.getBookedProductionRequest(principal);
                    } else if (syncJobType.getName().equals(Constants.COST_OF_GOODS)) {
                        costOfGoodsController.getCostOfGoodsRequest(principal);
                    } else if (syncJobType.getName().equals(Constants.WASTAGE)) {
                        responseHash = wastageController.getWastageRequest(principal).getBody();
                    } else if (syncJobType.getName().equals(Constants.CREDIT_NOTES)) {
                        responseHash = creditNoteController.getCreditNotesRequest(principal).getBody();
                    }

                    if(responseHash != null && responseHash.containsKey("success")
                            && responseHash.get("success").equals(false)){
                        status = false;
                        break;
                    }

                } catch (ParseException | IOException e) {
                    e.printStackTrace();
                    status = false;
                    break;
                }
            }
        }
        return status;
    }

    private boolean checkIfEquivalent(Date fromDate, Date toDate) {

        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(fromDate);
        cal2.setTime(toDate);
        boolean sameDay = cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);

        return sameDay;
    }
}
