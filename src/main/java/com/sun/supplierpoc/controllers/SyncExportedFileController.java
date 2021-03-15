package com.sun.supplierpoc.controllers;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.excelExporters.SalesExcelExporter;
import com.sun.supplierpoc.fileDelimiterExporters.GeneralExporterMethods;
import com.sun.supplierpoc.fileDelimiterExporters.SalesFileDelimiterExporter;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.SyncJobData;
import com.sun.supplierpoc.models.SyncJobType;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.repositories.AccountRepo;
import com.sun.supplierpoc.repositories.SyncJobDataRepo;
import com.sun.supplierpoc.repositories.SyncJobRepo;
import com.sun.supplierpoc.repositories.SyncJobTypeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.Principal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
public class SyncExportedFileController {
    @Autowired
    private SyncJobRepo syncJobRepo;
    @Autowired
    private SyncJobTypeRepo syncJobTypeRepo;
    @Autowired
    private SyncJobDataRepo syncJobDataRepo;
    @Autowired
    private AccountRepo accountRepo;

    public Conversions conversions = new Conversions();
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @GetMapping("/export/excel")
    public void exportToExcel(@RequestParam(name = "syncJobId") String syncJobId,
                              HttpServletResponse response) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Sales" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);

        List<SyncJobData> salesList = syncJobDataRepo.findBySyncJobIdAndDeleted(syncJobId,
                false);

        SalesExcelExporter excelExporter = new SalesExcelExporter(salesList);

        excelExporter.export(response);
    }

    @GetMapping("/export/csv")
    public void exportToText(Principal principal,
                             @RequestParam(name = "syncJobId") String syncJobId,
                             @RequestParam(name = "syncTypeName") String syncTypeName,
                             HttpServletResponse response) throws IOException {
        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        Account account = accountOptional.get();
        response.setContentType("application/octet-stream");

        List<SyncJobData> salesList = syncJobDataRepo.findBySyncJobIdAndDeleted(syncJobId, false);

        SyncJobType syncJobType = syncJobTypeRepo.findByNameAndAccountIdAndDeleted(syncTypeName, account.getId(), false);


        String businessDate =  syncJobType.getConfiguration().timePeriod;
        String fromDate =  syncJobType.getConfiguration().fromDate;
        String transactionDate = conversions.getTransactionDate(businessDate, fromDate);

        String fileName = "month/" + transactionDate.substring(4) + transactionDate.substring(2,4) + transactionDate.substring(0,2);

        String headerKey = HttpHeaders.CONTENT_DISPOSITION;
        String headerValue = "attachment; filename=" + fileName + ".ndf";
        response.setHeader(headerKey, headerValue);
        response.setContentType("text/csv");

        SalesFileDelimiterExporter excelExporter = new SalesFileDelimiterExporter(fileName + ".ndf", syncJobType, salesList);
        excelExporter.writeSyncData(response.getWriter());
    }

    @GetMapping("/listSyncFiles")
    public ResponseEntity listSyncFiles(Principal principal) {
        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        Account account = accountOptional.get();

        GeneralExporterMethods exporterMethods = new GeneralExporterMethods();
        return ResponseEntity.status(HttpStatus.OK).body(exporterMethods.ListSyncFiles(account.getName()));
    }

    /*
    * Generate single file of last month data
    * */
    @GetMapping("/generateSingleFile")
    public void generateSingleFileRequest(Principal principal,
                                          @RequestParam(name = "syncJobTypeName") String syncJobTypeName,
                                          HttpServletResponse response) throws IOException {
        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        Account account = accountOptional.get();

        SyncJobType syncJobType = syncJobTypeRepo.findByNameAndAccountIdAndDeleted(syncJobTypeName, account.getId(), false);
        boolean perLocation = syncJobType.getConfiguration().exportFilePerLocation;

        DateFormat dateFormat = new SimpleDateFormat("MMyyy");

        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH) + 1;

        String date = dateFormat.format(calendar.getTime());

        String fileDirectory = account.getName() + "/" + syncJobType.getName();
        String fileName = date + ".ndf";

        String headerKey = HttpHeaders.CONTENT_DISPOSITION;
        String headerValue = "attachment; filename=" + fileName;
        response.setHeader(headerKey, headerValue);
        response.setContentType("application/octet-stream");
        response.setContentType("text/csv");

        GeneralExporterMethods exporterMethods = new GeneralExporterMethods(fileName);
        exporterMethods.generateSingleFile(response.getWriter(), fileDirectory, String.valueOf(month), fileName, perLocation);
    }
}
