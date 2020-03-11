package com.sun.supplierpoc.controllers;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.models.configurations.CostCenter;
import com.sun.supplierpoc.repositories.AccountRepo;
import com.sun.supplierpoc.repositories.SyncJobDataRepo;
import com.sun.supplierpoc.repositories.SyncJobRepo;
import com.sun.supplierpoc.repositories.SyncJobTypeRepo;
import com.sun.supplierpoc.seleniumMethods.SetupEnvironment;
import com.sun.supplierpoc.services.InvoiceService;
import com.sun.supplierpoc.services.TransferService;
import com.systemsunion.ssc.client.ComponentException;
import com.systemsunion.ssc.client.SoapFaultException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;


@RestController

public class InvoiceController {
    @Autowired
    private SyncJobRepo syncJobRepo;
    @Autowired
    private SyncJobTypeRepo syncJobTypeRepo;
    @Autowired
    private SyncJobDataRepo syncJobDataRepo;
    @Autowired
    private AccountRepo accountRepo;

    @Autowired
    private InvoiceService invoiceService;
    @Autowired
    private TransferService transferService;

    public Conversions conversions = new Conversions();

    public SetupEnvironment setupEnvironment = new SetupEnvironment();


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @RequestMapping("/getApprovedInvoices")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public HashMap<String, Object> getApprovedInvoicesRequest(Principal principal) {
        User user = (User)((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        Account account = accountOptional.get();

        HashMap<String, Object> response = getApprovedInvoices(user.getId(), account);

        return response;

    }

    public HashMap<String, Object> getApprovedInvoices(String userId, Account account) {

        HashMap<String, Object> response = new HashMap<>();

        SyncJobType syncJobType = syncJobTypeRepo.findByNameAndAccountId(Constants.APPROVED_INVOICES, account.getId());
        SyncJobType syncJobTypeJournal = syncJobTypeRepo.findByNameAndAccountId(Constants.JOURNALS, account.getId());

        SyncJob syncJob = new SyncJob(Constants.RUNNING, "", new Date(), null, userId,
                account.getId(), syncJobType.getId());

        syncJobRepo.save(syncJob);

        HashMap<String, Object> data = invoiceService.getInvoicesData(false, syncJobType, account);

        if (data.get("status").equals(Constants.SUCCESS)){
            ArrayList<HashMap<String, Object>> invoices = (ArrayList<HashMap<String, Object>>) data.get("invoices");
            if (invoices.size() > 0){
                ArrayList<SyncJobData> addedInvoices = invoiceService.saveInvoicesData(invoices, syncJob, false);
                handleSendTransfer(syncJobType, syncJobTypeJournal, syncJob, addedInvoices, transferService,
                        syncJobDataRepo, account);
                syncJob.setReason("");
                syncJob.setEndDate(new Date());
                syncJobRepo.save(syncJob);

                response.put("message", "Sync Invoices Successfully.");
                response.put("success", true);

            }
            else {
                syncJob.setStatus(Constants.SUCCESS);
                syncJob.setReason("There is no invoices to get from Oracle Hospitality.");
                syncJob.setEndDate(new Date());
                syncJobRepo.save(syncJob);

                response.put("message", "There is no invoices to get from Oracle Hospitality.");
                response.put("success", true);

            }
        }
        else {
            syncJob.setStatus(Constants.FAILED);
            syncJob.setReason((String) data.get("message"));
            syncJob.setEndDate(new Date());
            syncJobRepo.save(syncJob);

            response.put("message", "Failed to sync Invoices.");
            response.put("success", false);
        }
        return response;
    }

    static void handleSendTransfer(SyncJobType syncJobType, SyncJobType syncJobTypeJournal, SyncJob syncJob,
                                   ArrayList<SyncJobData> addedInvoices, TransferService transferService,
                                   SyncJobDataRepo syncJobDataRepo, Account account) {
        HashMap<String, Object> data;
        if(addedInvoices.size() != 0){
            for (SyncJobData addedInvoice : addedInvoices) {
                try {
                    data  = transferService.sendTransferData(addedInvoice, syncJobType, syncJobTypeJournal, account);
                    if ((Boolean) data.get("status")){
                        addedInvoice.setStatus(Constants.SUCCESS);
                        addedInvoice.setReason("");
                        syncJobDataRepo.save(addedInvoice);
                    }
                    else {
                        addedInvoice.setStatus(Constants.FAILED);
                        addedInvoice.setReason((String) data.get("message"));
                        syncJobDataRepo.save(addedInvoice);
                    }

                } catch (SoapFaultException | ComponentException e) {
                    e.printStackTrace();
                    continue;
                }
            }
        }

        syncJob.setStatus(Constants.SUCCESS);
    }


    @RequestMapping("/getCostCenter")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public HashMap<String, Object> getCostCenter(@RequestParam(name = "syncTypeName") String syncTypeName,
                                                 Principal principal){
        User user = (User)((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        Account account = accountOptional.get();

        HashMap<String, Object> data = new HashMap<>();
        WebDriver driver = setupEnvironment.setupSeleniumEnv(false);
        if (driver == null){
            data.put("status", Constants.FAILED);
            data.put("message", "Failed to establish connection with firefox driver.");
            data.put("invoices", new ArrayList<>());
            return data;
        }
        ArrayList<CostCenter> costCenters = new ArrayList<>();

        SyncJobType syncJobType = syncJobTypeRepo.findByNameAndAccountId(syncTypeName, user.getAccountId());
        ArrayList<CostCenter> oldCostCenters = syncJobType.getConfiguration().getCostCenters();

        try
        {
            String url = "https://mte03-ohim-prod.hospitality.oracleindustry.com/Webclient/FormLogin.aspx";

            if (!setupEnvironment.loginOHIM(driver, url, account)){
                driver.quit();

                data.put("status", Constants.FAILED);
                data.put("message", "Invalid username and password.");
                data.put("costCenters", costCenters);
                return data;
            }

            String costCentersURL = "https://mte03-ohim-prod.hospitality.oracleindustry.com/Webclient/MasterData/CostCenters/OverviewCC.aspx";
            driver.get(costCentersURL);

            driver.findElement(By.name("filterPanel_btnRefresh")).click();

            List<WebElement> rows = driver.findElements(By.tagName("tr"));

            ArrayList<String> columns = setupEnvironment.getTableColumns(rows, true, 13);

            while (true){
                fillCostCenterObject(costCenters, rows, 14, oldCostCenters, columns, account.getERD());

                // check if there is other pages
                if (driver.findElements(By.linkText("Next")).size() == 0){
                    break;
                }
                else {
                    TransferService.checkPagination(driver);
                    rows = driver.findElements(By.tagName("tr"));
                }
            }
            driver.quit();

            data.put("status", Constants.SUCCESS);
            data.put("message", "Get cost centers successfully");
            data.put("costCenters", costCenters);
            return data;

        }catch (Exception e) {
            e.printStackTrace();

            driver.quit();

            data.put("status", Constants.FAILED);
            data.put("message", e);
            data.put("costCenters", costCenters);
            return data;
        }
    }

    private void fillCostCenterObject(ArrayList<CostCenter> costCenters, List<WebElement> rows, int rowNumber,
                                      ArrayList<CostCenter> oldCostCenters, ArrayList<String> columns,
                                      String accountERD){

        for (int i = rowNumber; i < rows.size(); i++) {
            CostCenter costCenter = new CostCenter();

            WebElement row = rows.get(i);
            List<WebElement> cols = row.findElements(By.tagName("td"));
            if (cols.size() != columns.size()){
                continue;
            }

            costCenter.costCenter =  cols.get(1).getText().strip();

            CostCenter oldCostCenterData = conversions.checkCostCenterExistence(oldCostCenters, cols.get(1).getText().strip(), true);

            if (oldCostCenterData.checked){
                costCenter.checked = true;
                // Fusion Data
                if (accountERD.equals("FUSION")){
                    costCenter = oldCostCenterData;
                }
                // Sun Data
                else if (accountERD.equals("SUN")){
                    costCenter.accountCode = oldCostCenterData.accountCode;
                }
            }
            else {
                costCenter.checked = false;
                // Fusion Data
                if (accountERD.equals("FUSION")){
                    costCenter.department = "000";
                    costCenter.project = "000000";
                    costCenter.future2 = "0000";
                    costCenter.company = "517";
                    costCenter.businessUnit = "";
                    costCenter.account = "411101";
                    costCenter.product = "1200";
                    costCenter.interCompany = "000";
                    costCenter.location = "11101";
                    costCenter.currency = "AED";
                }
                // Sun Data
                else if(accountERD.equals("SUN")){
                    costCenter.accountCode = oldCostCenterData.accountCode;
                }
            }

            costCenters.add(costCenter);
        }
    }

}
