package com.sun.supplierpoc.services;


import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.controllers.InvoiceController;
import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.models.configurations.CostCenter;
import com.sun.supplierpoc.models.configurations.ItemGroup;
import com.sun.supplierpoc.models.configurations.OverGroup;
import com.sun.supplierpoc.models.configurations.RevenueCenter;
import com.sun.supplierpoc.repositories.SyncJobDataRepo;
import com.sun.supplierpoc.seleniumMethods.SetupEnvironment;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import java.util.*;

@Service
public class JournalService {
    @Autowired
    SyncJobDataRepo syncJobDataRepo;
    @Autowired
    InvoiceController invoiceController;
    @Autowired
    private SyncJobDataService syncJobDataService;

    private Conversions conversions = new Conversions();
    private SetupEnvironment setupEnvironment = new SetupEnvironment();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /*
    * Get consumptions entries based on cost center
    * */
    public HashMap<String, Object> getJournalDataByCostCenter(SyncJobType journalSyncJobType, ArrayList<CostCenter> costCenters,
                                                  ArrayList<ItemGroup> itemGroups, Account account){
        HashMap<String, Object> response = new HashMap<>();

        WebDriver driver;
        try{
            driver = setupEnvironment.setupSeleniumEnv(false);
        }
        catch (Exception ex){
            response.put("status", Constants.FAILED);
            response.put("message", "Failed to establish connection with firefox driver.");
            response.put("invoices", new ArrayList<>());
            return response;
        }

        ArrayList<Journal> journals;
        ArrayList<HashMap<String, Object>> journalsEntries = new ArrayList<>();

        String businessDate =  journalSyncJobType.getConfiguration().timePeriod;
        String fromDate = journalSyncJobType.getConfiguration().fromDate;
        String toDate = journalSyncJobType.getConfiguration().toDate;

        try {
            if (!setupEnvironment.loginOHRA(driver, Constants.OHRA_LINK, account)){
                driver.quit();

                response.put("status", Constants.FAILED);
                response.put("message", "Invalid username and password.");
                response.put("journals", journalsEntries);
                return response;
            }
            // just wait to make sure credentials of user saved to be able to move to another pages.
            try {
                WebDriverWait wait = new WebDriverWait(driver, 5);
                wait.until(ExpectedConditions.alertIsPresent());
            }
            catch (Exception e) {
                System.out.println("Waiting");
            }

            String journalUrl = "https://mte03-ohra-prod.hospitality.oracleindustry.com/finengine/reportAction.do?method=run&reportID=499";
            driver.get(journalUrl);

            try {
                WebDriverWait wait = new WebDriverWait(driver, 60);
                wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("loadingFrame")));
            }
            catch (Exception ex){
                System.out.println(ex.getMessage());
            }

            Response dateResponse = new Response();
            if (setupEnvironment.runReport(businessDate, fromDate, toDate, new CostCenter(), new RevenueCenter(), driver, dateResponse)){
                driver.quit();

                if(dateResponse.getMessage().equals(Constants.WRONG_BUSINESS_DATE)){
                    response.put("status", Constants.FAILED);
                    response.put("message", dateResponse.getMessage());
                    response.put("journals", journalsEntries);
                    return response;
                } else if (dateResponse.getMessage().equals(Constants.NO_INFO)) {
                    response.put("status", Constants.SUCCESS);
                    response.put("message", "");
                    response.put("journals", journalsEntries);
                    return response;
                }
            }

            journalUrl = "https://mte03-ohra-prod.hospitality.oracleindustry.com/finengine/reportRunAction.do?rptroot=499&method=run&reportID=myInvenCOSByCC";
            driver.get(journalUrl);

            List<WebElement> rows = driver.findElements(By.tagName("tr"));

            if (rows.size() < 4){
                driver.quit();

                response.put("status", Constants.SUCCESS);
                response.put("message", "There is no journals in selected range");
                response.put("journals", journalsEntries);
                return response;
            }

            ArrayList<String> columns = setupEnvironment.getTableColumns(rows, false, 4);

            ArrayList<HashMap<String, Object>> selectedCostCenters = new ArrayList<>();

            for (int i = 6; i < rows.size(); i++) {
                HashMap<String, Object> journal = new HashMap<>();

                WebElement row = rows.get(i);
                List<WebElement> cols = row.findElements(By.tagName("td"));
                if (cols.size() != columns.size()) {
                    continue;
                }

                WebElement td = cols.get(columns.indexOf("cost_center"));
                CostCenter oldCostCenterData = conversions.checkCostCenterExistence(costCenters, td.getText().strip(), false);

                if (!oldCostCenterData.checked) {
                    continue;
                }

                String extensions = cols.get(0).findElement(By.tagName("div")).getAttribute("onclick").substring(7);
                int index = extensions.indexOf('\'');
                extensions = extensions.substring(0, index);

                journal.put("extensions", extensions);
                journal.put("cost_center", oldCostCenterData);

                selectedCostCenters.add(journal);
            }

            for (HashMap<String, Object> costCenter : selectedCostCenters) {
                try {
                    journals = new ArrayList<>();

                    driver.get(Constants.OHRA_LINK + costCenter.get("extensions"));

                    rows = driver.findElements(By.tagName("tr"));

                    if (rows.size() <= 3){
                        continue;
                    }

                    columns = setupEnvironment.getTableColumns(rows, false, 3);

                    for (int i = 6; i < rows.size(); i++) {
                        HashMap<String, Object> transferDetails = new HashMap<>();
                        WebElement row = rows.get(i);
                        List<WebElement> cols = row.findElements(By.tagName("td"));

                        if (cols.size() != columns.size()) {
                            continue;
                        }

                        // check if this Item group belong to selected Item groups
                        WebElement td = cols.get(columns.indexOf("item_group"));

                        ItemGroup oldItemData = conversions.checkItemGroupExistence(itemGroups, td.getText().strip());

                        if (!oldItemData.getChecked()) {
                            continue;
                        }

                        String overGroup = oldItemData.getOverGroup();


                        for (int j = 0; j < cols.size(); j++) {
                            transferDetails.put(columns.get(j), cols.get(j).getText().strip());
                        }

                        Journal journal = new Journal();
                        float waste = conversions.convertStringToFloat((String) transferDetails.get("waste"));
                        float cost = conversions.convertStringToFloat((String) transferDetails.get("actual_usage"));
                        float variance = conversions.convertStringToFloat((String) transferDetails.get("variance"));
                        float transfer = conversions.convertStringToFloat((String) transferDetails.get("net_transfers"));

                        journals = journal.checkExistence(journals, overGroup, waste,cost, variance, transfer);

                    }
                    for (Journal journal : journals) {
                        HashMap<String, Object> journalsEntry = new HashMap<>();

                        journalsEntry.put("cost_center", costCenter.get("cost_center"));
                        journalsEntry.put("journal", journal);

                        journalsEntries.add(journalsEntry);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            driver.quit();

            response.put("status", Constants.SUCCESS);
            response.put("message", "");
            response.put("journals", journalsEntries);
            return response;

        } catch (Exception e) {
            e.printStackTrace();
            driver.quit();

            response.put("status", Constants.FAILED);
            response.put("message", e.getMessage());
            response.put("journals", journalsEntries);
            return response;
        }
    }


    /*
     * Get consumptions entries based on location
     * */
    public HashMap<String, Object> getJournalData(SyncJobType journalSyncJobType,
                                                  ArrayList<CostCenter> costCentersLocation,
                                                  ArrayList<ItemGroup> itemGroups, Account account){
        HashMap<String, Object> response = new HashMap<>();

        WebDriver driver;
        try{
            driver = setupEnvironment.setupSeleniumEnv(false);
        }
        catch (Exception ex){
            response.put("status", Constants.FAILED);
            response.put("message", "Failed to establish connection with firefox driver.");
            response.put("invoices", new ArrayList<>());
            return response;
        }

        ArrayList<Journal> journals;
        ArrayList<HashMap<String, Object>> journalsEntries = new ArrayList<>();

        String businessDate =  journalSyncJobType.getConfiguration().timePeriod;
        String fromDate = journalSyncJobType.getConfiguration().fromDate;
        String toDate = journalSyncJobType.getConfiguration().toDate;

        try {
            if (!setupEnvironment.loginOHRA(driver, Constants.OHRA_LINK, account)){
                driver.quit();

                response.put("status", Constants.FAILED);
                response.put("message", "Invalid username and password.");
                response.put("journals", journalsEntries);
                return response;
            }
            try {
                WebDriverWait wait = new WebDriverWait(driver, 5);
                wait.until(ExpectedConditions.alertIsPresent());
            }
            catch (Exception e) {
                System.out.println("Waiting");
            }


            for (CostCenter costCenter : costCentersLocation) {
                if (!costCenter.checked)
                    continue;

                if (!driver.getCurrentUrl().equals(Constants.CONSUMPTION_REPORT_LINK)){
                    driver.get(Constants.CONSUMPTION_REPORT_LINK);

                    try {
                        WebDriverWait wait = new WebDriverWait(driver, 60);
                        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("loadingFrame")));
                    }
                    catch (Exception ex){
                        System.out.println(ex.getMessage());
                    }
                }

                Response dateResponse = new Response();
                if (setupEnvironment.runReport(businessDate, fromDate, toDate, costCenter, new RevenueCenter(), driver, dateResponse)){
                    if(dateResponse.getMessage().equals(Constants.WRONG_BUSINESS_DATE)){
                        driver.quit();

                        response.put("status", Constants.FAILED);
                        response.put("message", dateResponse.getMessage());
                        response.put("journals", journalsEntries);
                        return response;
                    } else if (dateResponse.getMessage().equals(Constants.NO_INFO)) {
                        continue;
                    }
                }

                driver.get(Constants.CONSUMPTION_TABLE_LINK);

                List<WebElement> rows = driver.findElements(By.tagName("tr"));

                if (rows.size() < 4)
                    continue;

                ArrayList<String> columns = setupEnvironment.getTableColumns(rows, false, 4);

                ArrayList<String> extensions  = new ArrayList<>();

                for (int i = 6; i < rows.size(); i++) {

                    WebElement row = rows.get(i);
                    List<WebElement> cols = row.findElements(By.tagName("td"));
                    if (cols.size() != columns.size()) {
                        continue;
                    }

                    String extension = cols.get(0).findElement(By.tagName("div")).getAttribute("onclick").substring(7);
                    int index = extension.indexOf('\'');
                    extension = extension.substring(0, index);
                    extensions.add(extension);
                }

                journals = new ArrayList<>();

                for (String extension : extensions) {
                    try {
                        driver.get(Constants.OHRA_LINK + extension);

                        rows = driver.findElements(By.tagName("tr"));

                        if (rows.size() <= 3)
                            continue;

                        columns = setupEnvironment.getTableColumns(rows, false, 3);

                        for (int i = 6; i < rows.size(); i++) {
                            HashMap<String, Object> transferDetails = new HashMap<>();
                            WebElement row = rows.get(i);
                            List<WebElement> cols = row.findElements(By.tagName("td"));

                            if (cols.size() != columns.size()) {
                                continue;
                            }

                            // check if this Item group belong to selected Item groups
                            WebElement td = cols.get(columns.indexOf("item_group"));

                            ItemGroup oldItemData = conversions.checkItemGroupExistence(itemGroups, td.getText().strip());

                            if (!oldItemData.getChecked()) {
                                continue;
                            }

                            String overGroup = oldItemData.getOverGroup();


                            for (int j = 0; j < cols.size(); j++) {
                                transferDetails.put(columns.get(j), cols.get(j).getText().strip());
                            }

                            Journal journal = new Journal();
                            float cost = conversions.convertStringToFloat((String) transferDetails.get("actual_usage"));

                            journals = journal.checkExistence(journals, overGroup, 0,cost, 0, 0);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                for (Journal journal : journals) {
                    HashMap<String, Object> journalsEntry = new HashMap<>();

                    journalsEntry.put("cost_center", costCenter);
                    journalsEntry.put("journal", journal);

                    journalsEntries.add(journalsEntry);
                }
            }

            driver.quit();

            response.put("status", Constants.SUCCESS);
            response.put("message", "");
            response.put("journals", journalsEntries);
            return response;

        } catch (Exception e) {
            e.printStackTrace();
            driver.quit();

            response.put("status", Constants.FAILED);
            response.put("message", "Failed to get consumption entries from Oracle Hospitality.");
            response.put("journals", journalsEntries);
            return response;
        }
    }


    public ArrayList<SyncJobData> saveJournalData(ArrayList<HashMap<String, Object>> journals,
                                                  SyncJobType syncJobType, SyncJob syncJob,
                                                  String businessDate, String fromDate,
                                                  ArrayList<OverGroup> overGroups){
        ArrayList<SyncJobData> addedJournals = new ArrayList<>();

        for (HashMap<String, Object> journal : journals) {
            // check zero entries (not needed)
            CostCenter costCenter = (CostCenter) journal.get("cost_center");
            Journal journalData = (Journal) journal.get("journal");

            if (costCenter.costCenterReference.equals("")){
                costCenter.costCenterReference = costCenter.costCenter;
            }

            if (journalData.getTotalCost() != 0){
                HashMap<String, String> costData = new HashMap<>();
                if(syncJobType.getConfiguration().consumptionConfiguration.consumptionBasedOnType.equals("Cost Center")
                        && costCenter.location != null)
                    syncJobDataService.prepareAnalysis(costData, syncJobType.getConfiguration(), costCenter.location, null, null);
                else
                    syncJobDataService.prepareAnalysis(costData, syncJobType.getConfiguration(), costCenter, null, null);

                String transactionDate = conversions.getTransactionDate(businessDate, fromDate);
                costData.put("accountingPeriod", transactionDate.substring(2,6));
                costData.put("transactionDate", transactionDate);

                costData.put("totalCr", String.valueOf(conversions.roundUpFloat(journalData.getTotalCost())));
                costData.put("totalDr", String.valueOf(conversions.roundUpFloat(journalData.getTotalCost()) * -1));

                costData.put("fromCostCenter", costCenter.costCenter);
                costData.put("fromAccountCode", costCenter.accountCode);

                costData.put("toCostCenter", costCenter.costCenter);
                costData.put("toAccountCode", costCenter.accountCode);

                costData.put("fromLocation", costCenter.accountCode);
                costData.put("toLocation", costCenter.accountCode);

                String description = "F " + costCenter.costCenterReference + " " + journalData.getOverGroup();
                if (description.length() > 50){
                    description = description.substring(0, 50);
                }

                costData.put("description", description);

                costData.put("transactionReference", "Consumption");
                costData.put("overGroup", journalData.getOverGroup());

                OverGroup oldOverGroupData = conversions.checkOverGroupExistence(overGroups, journalData.getOverGroup());

                if (oldOverGroupData.getChecked() && !oldOverGroupData.getInventoryAccount().equals("")
                    && !oldOverGroupData.getExpensesAccount().equals("")){
                    costData.put("inventoryAccount", oldOverGroupData.getInventoryAccount());
                    costData.put("expensesAccount", oldOverGroupData.getExpensesAccount());

                    SyncJobData syncJobData = new SyncJobData(costData, Constants.RECEIVED, "", new Date(),
                            syncJob.getId());
                    syncJobDataRepo.save(syncJobData);

                    addedJournals.add(syncJobData);
                }
            }
        }
        return addedJournals;

    }

}
