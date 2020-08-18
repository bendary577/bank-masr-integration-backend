package com.sun.supplierpoc.services;


import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.controllers.InvoiceController;
import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.models.configurations.CostCenter;
import com.sun.supplierpoc.models.configurations.ItemGroup;
import com.sun.supplierpoc.models.configurations.OverGroup;
import com.sun.supplierpoc.repositories.SyncJobDataRepo;
import com.sun.supplierpoc.seleniumMethods.SetupEnvironment;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class JournalService {
    @Autowired
    SyncJobDataRepo syncJobDataRepo;
    @Autowired
    InvoiceController invoiceController;

    private Conversions conversions = new Conversions();
    private SetupEnvironment setupEnvironment = new SetupEnvironment();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public HashMap<String, Object> getJournalData(SyncJobType journalSyncJobType, ArrayList<CostCenter> costCenters,
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

        String timePeriod =  journalSyncJobType.getConfiguration().getTimePeriod();

        try {
            if (!setupEnvironment.loginOHRA(driver, Constants.OHRA_LOGIN_LINK, account)){
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

            Response dateResponse = setupEnvironment.selectTimePeriodOHRA(timePeriod, driver);

            if (!dateResponse.isStatus()){
                response.put("status", Constants.FAILED);
                response.put("message", dateResponse.getMessage());
                response.put("journals", journalsEntries);
                return response;
            }

            driver.findElement(By.id("Run Report")).click();

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

            String baseURL = "https://mte03-ohra-prod.hospitality.oracleindustry.com";

            for (HashMap<String, Object> costCenter : selectedCostCenters) {
                try {
                    journals = new ArrayList<>();

                    driver.get(baseURL + costCenter.get("extensions"));

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

    public ArrayList<SyncJobData> saveJournalData(ArrayList<HashMap<String, Object>> journals, SyncJob syncJob,
                                                  ArrayList<OverGroup> overGroups){
        ArrayList<SyncJobData> addedJournals = new ArrayList<>();

        for (HashMap<String, Object> journal : journals) {
            // check zero entries (not needed)
            CostCenter costCenter = (CostCenter) journal.get("cost_center");
            Journal journalData = (Journal) journal.get("journal");

            if (costCenter.costCenterReference.equals("")){
                costCenter.costCenterReference = costCenter.costCenter;
            }

            if(journalData.getTotalVariance() != 0){
                HashMap<String, String> varianceData = new HashMap<>();

                varianceData.put("transactionDate", "01072020");

                varianceData.put("totalCr", String.valueOf(Math.round(journalData.getTotalVariance())));
                varianceData.put("totalDr", String.valueOf(Math.round(journalData.getTotalVariance()) * -1));

                varianceData.put("from_cost_center", costCenter.costCenter);
                varianceData.put("from_account_code", costCenter.accountCode);

                varianceData.put("to_cost_center", costCenter.costCenter);
                varianceData.put("to_account_code", costCenter.accountCode);

                varianceData.put("location", costCenter.accountCode);

                String description =  "Variance F " + costCenter.costCenterReference + " " + journalData.getOverGroup();
                if (description.length() > 50){
                    description = description.substring(0, 50);
                }

                varianceData.put("description", description);

                varianceData.put("transactionReference", "Variance Transaction Reference");
                varianceData.put("overGroup", journalData.getOverGroup());

                OverGroup oldOverGroupData = conversions.checkOverGroupExistence(overGroups, journalData.getOverGroup());

                varianceData.put("inventoryAccount", oldOverGroupData.getInventoryAccount());
                varianceData.put("expensesAccount", oldOverGroupData.getExpensesAccount());

                SyncJobData syncJobData = new SyncJobData(varianceData, Constants.RECEIVED, "", new Date(),
                        syncJob.getId());
                syncJobDataRepo.save(syncJobData);

                addedJournals.add(syncJobData);
            }

            if (journalData.getTotalCost() != 0){
                HashMap<String, String> costData = new HashMap<>();

                // Example: 01062020
                costData.put("transactionDate", "01072020");

                costData.put("totalCr", String.valueOf(Math.round(journalData.getTotalCost())));
                costData.put("totalDr", String.valueOf(Math.round(journalData.getTotalCost()) * -1));

                costData.put("from_cost_center", costCenter.costCenter);
                costData.put("from_account_code", costCenter.accountCode);

                costData.put("to_cost_center", costCenter.costCenter);
                costData.put("to_account_code", costCenter.accountCode);

                costData.put("location", costCenter.accountCode);

                String description = "Cost Of Sales F " + costCenter.costCenterReference + " " + journalData.getOverGroup();
                if (description.length() > 50){
                    description = description.substring(0, 50);
                }

                costData.put("description", description);

                costData.put("transactionReference", "Cost Transaction Reference");
                costData.put("overGroup", journalData.getOverGroup());

                OverGroup oldOverGroupData = conversions.checkOverGroupExistence(overGroups, journalData.getOverGroup());

                costData.put("inventoryAccount", oldOverGroupData.getInventoryAccount());
                costData.put("expensesAccount", oldOverGroupData.getExpensesAccount());

                SyncJobData syncJobData = new SyncJobData(costData, Constants.RECEIVED, "", new Date(),
                        syncJob.getId());
                syncJobDataRepo.save(syncJobData);

                addedJournals.add(syncJobData);
            }

        }
        return addedJournals;

    }

}
