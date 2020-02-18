package com.sun.supplierpoc.controllers;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.models.CostCenter;
import com.sun.supplierpoc.models.SyncJob;
import com.sun.supplierpoc.models.SyncJobData;
import com.sun.supplierpoc.models.SyncJobType;
import com.sun.supplierpoc.repositories.SyncJobDataRepo;
import com.sun.supplierpoc.repositories.SyncJobRepo;
import com.sun.supplierpoc.repositories.SyncJobTypeRepo;
import com.sun.supplierpoc.seleniumMethods.SetupEnvironment;
import com.sun.supplierpoc.services.InvoiceService;
import com.sun.supplierpoc.services.TransferService;
import com.systemsunion.ssc.client.*;
import org.openqa.selenium.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;



@RestController

public class InvoiceController {
    @Autowired
    private SyncJobRepo syncJobRepo;
    @Autowired
    private SyncJobTypeRepo syncJobTypeRepo;
    @Autowired
    private SyncJobDataRepo syncJobDataRepo;
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
    public HashMap<String, Object> getApprovedInvoices() {
        HashMap<String, Object> response = new HashMap<>();

        SyncJobType syncJobType = syncJobTypeRepo.findByNameAndAccountId("Approved Invoices", "1");

        SyncJob syncJob = new SyncJob(Constants.RUNNING, "", new Date(), null, "1", "1",
                syncJobType.getId());

        syncJobRepo.save(syncJob);

        HashMap<String, Object> data = invoiceService.getInvoicesData(false, syncJobType);

        if (data.get("status").equals(Constants.SUCCESS)){
            ArrayList<HashMap<String, String>> invoices = (ArrayList<HashMap<String, String>>) data.get("invoices");

            if (invoices.size() > 0){
                ArrayList<SyncJobData> addedInvoices = invoiceService.saveInvoicesData(invoices, syncJob, false);
                if(addedInvoices.size() != 0){
                    try {
                        for (SyncJobData invoice: addedInvoices ) {
                            boolean addInvoiceFlag = transferService.sendTransferData(invoice, syncJobType);

                            if(addInvoiceFlag){
                                invoice.setStatus(Constants.SUCCESS);
                                syncJobDataRepo.save(invoice);
                            }
                            else {
                                invoice.setStatus(Constants.FAILED);
                                invoice.setReason("");
                                syncJobDataRepo.save(invoice);
                            }
                        }

                    } catch (SoapFaultException | ComponentException e) {
                        e.printStackTrace();
                    }
                }

                syncJob.setStatus(Constants.SUCCESS);
                syncJob.setEndDate(new Date());
                syncJobRepo.save(syncJob);

                return response;
            }
            else {
                syncJob.setStatus(Constants.SUCCESS);
                syncJob.setReason("There is no invoices to get from Oracle Hospitality.");
                syncJobRepo.save(syncJob);

                response.put("message", "There is no invoices to get from Oracle Hospitality.");
                response.put("success", true);
                return response;

            }


        }
        else {
            syncJob.setStatus(Constants.SUCCESS);
            syncJob.setReason("Failed to get invoices from Oracle Hospitality.");
            syncJobRepo.save(syncJob);

            response.put("message", "Failed to sync invoices.");
            response.put("success", false);
            return response;
        }
    }


    @RequestMapping("/getCostCenter")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public HashMap<String, Object> getCostCenter(@RequestParam(name = "syncTypeName") String syncTypeName){
        HashMap<String, Object> data = new HashMap<>();
        WebDriver driver = setupEnvironment.setupSeleniumEnv();
        ArrayList<CostCenter> costCenters = new ArrayList<>();

        SyncJobType syncJobType = syncJobTypeRepo.findByNameAndAccountId(syncTypeName, "1");
        ArrayList<CostCenter> oldCostCenters = (ArrayList<CostCenter>) syncJobType.getConfiguration().get("costCenters");

        try
        {
            String url = "https://mte03-ohim-prod.hospitality.oracleindustry.com/Webclient/FormLogin.aspx";

            if (!setupEnvironment.loginOHIM(driver, url)){
                data.put("status", Constants.FAILED);
                data.put("message", "Invalid username and password.");
                data.put("costCenters", costCenters);
                return data;
            }

            String costCentersURL = "https://mte03-ohim-prod.hospitality.oracleindustry.com/Webclient/MasterData/CostCenters/OverviewCC.aspx";
            driver.get(costCentersURL);

            driver.findElement(By.name("filterPanel_btnRefresh")).click();

            List<WebElement> rows = driver.findElements(By.tagName("tr"));

            ArrayList<String> columns = setupEnvironment.getTableColumns(rows, 13);

            fillCostCenterObject(costCenters, rows, 14, oldCostCenters, columns);

            driver.findElement(By.id("cbxUseAssignedTo")).click();
            driver.findElement(By.id("rbStore")).click();

            fillCostCenterObject(costCenters, rows, 14, oldCostCenters, columns);

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

    public HashMap<String, Object> checkExistence(ArrayList<CostCenter> costCenters, String costCenterName){
        HashMap<String, Object> data = new HashMap<>();
        for (CostCenter costCenter :
                costCenters) {
            if(costCenter.costCenter.equals(costCenterName)){
                data.put("status", true);
                data.put("costCenter", costCenter);
                return data;
            }
        }
        data.put("status", false);
        data.put("costCenter", new CostCenter());
        return data;
    }

    private void fillCostCenterObject(ArrayList<CostCenter> costCenters, List<WebElement> rows, int rowNumber,
                                      ArrayList<CostCenter> oldCostCenters, ArrayList<String> columns){

        for (int i = rowNumber; i < rows.size(); i++) {
            CostCenter costCenter = new CostCenter();

            WebElement row = rows.get(i);
            List<WebElement> cols = row.findElements(By.tagName("td"));
            if (cols.size() < columns.size()){
                continue;
            }

            costCenter.costCenter =  cols.get(1).getText().strip();

            HashMap<String, Object> oldCostCenterData = checkExistence(oldCostCenters, cols.get(1).getText().strip());
            CostCenter oldCostCenter = (CostCenter) oldCostCenterData.get("costCenter");

            if ((boolean)oldCostCenterData.get("status")){
                costCenter.checked = true;
                costCenter.department = oldCostCenter.department;
                costCenter.project = oldCostCenter.project;
                costCenter.future2 = oldCostCenter.future2;
                costCenter.company = oldCostCenter.company;
                costCenter.businessUnit = oldCostCenter.businessUnit;
                costCenter.account = oldCostCenter.account;
                costCenter.product = oldCostCenter.product;
                costCenter.interCompany = oldCostCenter.interCompany;
                costCenter.location = oldCostCenter.location;
                costCenter.currency = oldCostCenter.currency;
            }
            else {
                costCenter.checked = false;
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

            costCenters.add(costCenter);
        }
    }

}
