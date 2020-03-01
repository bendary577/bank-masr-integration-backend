package com.sun.supplierpoc.services;


import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.controllers.InvoiceController;
import com.sun.supplierpoc.models.Journal;
import com.sun.supplierpoc.models.SyncJob;
import com.sun.supplierpoc.models.SyncJobData;
import com.sun.supplierpoc.models.SyncJobType;
import com.sun.supplierpoc.repositories.SyncJobDataRepo;
import com.sun.supplierpoc.seleniumMethods.SetupEnvironment;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Service
public class JournalService {
    @Autowired
    SyncJobDataRepo syncJobDataRepo;
    @Autowired
    InvoiceController invoiceController;

    private Conversions conversions = new Conversions();
    private SetupEnvironment setupEnvironment = new SetupEnvironment();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public HashMap<String, Object> getJournalData(SyncJobType syncJobType, SyncJobType syncJobTypeApprovedInvoice){
        HashMap<String, Object> data = new HashMap<>();

        WebDriver driver = setupEnvironment.setupSeleniumEnv(false);
        ArrayList<Journal> journals = new ArrayList<>();
        ArrayList<HashMap<String, Object>> journalsEntries = new ArrayList<>();

        ArrayList<HashMap<String, String>> costCenters = (ArrayList<HashMap<String, String>>) syncJobTypeApprovedInvoice.getConfiguration().get("costCenters");
        ArrayList<HashMap<String, String>> itemGroups = (ArrayList<HashMap<String, String>>) syncJobType.getConfiguration().get("itemGroups");


        try {
            String url = "https://mte03-ohra-prod.hospitality.oracleindustry.com/servlet/PortalLogIn/";

            if (!setupEnvironment.loginOHRA(driver, url)){
                driver.quit();

                data.put("status", Constants.FAILED);
                data.put("message", "Invalid username and password.");
                data.put("journals", journals);
                return data;
            }
            // just wait to make sure credentials of user saved to be able to move to another pages.
            try {
                new WebDriverWait(driver, 3)
                        .ignoring(NoAlertPresentException.class)
                        .until(ExpectedConditions.alertIsPresent());

                Alert al = driver.switchTo().alert();
                al.accept();

            } catch (TimeoutException Ex) {
                System.out.println("No alert");
            }

            String journalUrl = "https://mte03-ohra-prod.hospitality.oracleindustry.com/finengine/reportRunAction.do?rptroot=499&method=run&reportID=myInvenCOSByCC";
            driver.get(journalUrl);

            List<WebElement> rows = driver.findElements(By.tagName("tr"));

            ArrayList<String> columns = setupEnvironment.getTableColumns(rows, false, 5);

            ArrayList<HashMap<String, Object>> selectedCostCenters = new ArrayList<>();

            for (int i = 6; i < rows.size(); i++) {
                HashMap<String, Object> journal = new HashMap<>();

                WebElement row = rows.get(i);
                List<WebElement> cols = row.findElements(By.tagName("td"));
                if (cols.size() != columns.size()) {
                    continue;
                }

                WebElement td = cols.get(columns.indexOf("cost_center"));
                HashMap<String, Object> oldCostCenterData = invoiceController.checkCostCenterExistence(costCenters, td.getText().strip(), false);

                if (!(boolean) oldCostCenterData.get("status")) {
                    continue;
                }

                String extensions = cols.get(0).findElement(By.tagName("div")).getAttribute("onclick").substring(7);
                int index = extensions.indexOf('\'');
                extensions = extensions.substring(0, index);

                journal.put("extensions", extensions);
//                journal.put(columns.get(0), cols.get(0).getText());
                journal.put("cost_center", oldCostCenterData.get("costCenter"));

                selectedCostCenters.add(journal);
            }

            String baseURL = "https://mte03-ohra-prod.hospitality.oracleindustry.com";

            for (HashMap<String, Object> costCenter : selectedCostCenters) {
                try {
                    driver.get(baseURL + costCenter.get("extensions"));

                    rows = driver.findElements(By.tagName("tr"));

                    columns = setupEnvironment.getTableColumns(rows, false, 4);

                    for (int i = 6; i < rows.size(); i++) {
                        HashMap<String, Object> transferDetails = new HashMap<>();
                        WebElement row = rows.get(i);
                        List<WebElement> cols = row.findElements(By.tagName("td"));

                        if (cols.size() != columns.size()) {
                            continue;
                        }

                        // check if this item group belong to selected item groups
                        WebElement td = cols.get(columns.indexOf("item_group"));

                        HashMap<String, Object> oldItemData = conversions.checkItemGroupExistence(itemGroups, td.getText().strip());

                        if (!(boolean) oldItemData.get("status")) {
                            continue;
                        }

                        HashMap<String, String> oldItemGroup = (HashMap<String, String>) oldItemData.get("itemGroup");
                        String overGroup = oldItemGroup.get("over_group");


                        for (int j = 0; j < cols.size(); j++) {
                            transferDetails.put(columns.get(j), cols.get(j).getText());
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

            data.put("status", Constants.SUCCESS);
            data.put("message", "");
            data.put("journals", journalsEntries);
            return data;

        } catch (Exception e) {
            e.printStackTrace();
            driver.quit();

            data.put("status", Constants.FAILED);
            data.put("message", e);
            data.put("journals", journalsEntries);
            return data;
        }
    }

    public ArrayList<SyncJobData> saveJournalData(ArrayList<HashMap<String, Object>> journals, SyncJob syncJob){
        ArrayList<SyncJobData> addedJournals = new ArrayList<>();

        for (HashMap<String, Object> journal : journals) {
            // check zero entries (not needed)
            HashMap<String, String> costCenter = (HashMap<String, String>) journal.get("cost_center");
            Journal journalData = (Journal) journal.get("journal");

            if (journalData.getTotalWaste() != 0){
                HashMap<String, String> wasteData = new HashMap<>();

                wasteData.put("total", String.valueOf(journalData.getTotalWaste()));
                wasteData.put("from_cost_center", costCenter.get("costCenter"));
                wasteData.put("from_account_code", costCenter.get("accountCode"));

                wasteData.put("to_cost_center", costCenter.get("costCenter"));
                wasteData.put("to_account_code", costCenter.get("accountCode"));
                wasteData.put("description", "Wastage Entry For " + costCenter.get("costCenter") + " " + journalData.getOverGroup());

                SyncJobData syncJobData = new SyncJobData(wasteData, Constants.RECEIVED, "", new Date(),
                        syncJob.getId());
                syncJobDataRepo.save(syncJobData);

                addedJournals.add(syncJobData);

            }

            if(journalData.getTotalVariance() != 0){
                HashMap<String, String> varianceData = new HashMap<>();

                varianceData.put("total", String.valueOf(journalData.getTotalVariance()));
                varianceData.put("from_cost_center", costCenter.get("costCenter"));
                varianceData.put("from_account_code", costCenter.get("accountCode"));

                varianceData.put("to_cost_center", costCenter.get("costCenter"));
                varianceData.put("to_account_code", costCenter.get("accountCode"));
                varianceData.put("description", "Staff Meal Cost For " + costCenter.get("costCenter") + " " + journalData.getOverGroup());

                SyncJobData syncJobData = new SyncJobData(varianceData, Constants.RECEIVED, "", new Date(),
                        syncJob.getId());
                syncJobDataRepo.save(syncJobData);

                addedJournals.add(syncJobData);
            }

            if (journalData.getTotalCost() != 0){
                HashMap<String, String> costData = new HashMap<>();

                costData.put("total", String.valueOf(journalData.getTotalCost()));
                costData.put("from_cost_center", costCenter.get("costCenter"));
                costData.put("from_account_code", costCenter.get("accountCode"));

                costData.put("to_cost_center", costCenter.get("costCenter"));
                costData.put("to_account_code", costCenter.get("accountCode"));
                costData.put("description", "Cost Of Sales For " + costCenter.get("costCenter") + " " + journalData.getOverGroup());

                SyncJobData syncJobData = new SyncJobData(costData, Constants.RECEIVED, "", new Date(),
                        syncJob.getId());
                syncJobDataRepo.save(syncJobData);

                addedJournals.add(syncJobData);
            }

        }
        return addedJournals;

    }

}
