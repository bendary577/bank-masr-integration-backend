package com.sun.supplierpoc.services;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.controllers.InvoiceController;
import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.repositories.SyncJobDataRepo;
import com.sun.supplierpoc.seleniumMethods.SetupEnvironment;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Service

public class WastageService {
    @Autowired
    SyncJobDataRepo syncJobDataRepo;
    @Autowired
    InvoiceController invoiceController;

    private Conversions conversions = new Conversions();
    private SetupEnvironment setupEnvironment = new SetupEnvironment();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public HashMap<String, Object> getWastageData(SyncJobType syncJobType, SyncJobType syncJobTypeJournal,
                                                   Account account) {

        HashMap<String, Object> data = new HashMap<>();

        ArrayList<HashMap<String,String>> items = (ArrayList<HashMap<String, String>>) syncJobTypeJournal.getConfiguration().get("items");
        ArrayList<HashMap<String,String>> costCenters = (ArrayList<HashMap<String,String>>) syncJobTypeJournal.getConfiguration().get("costCenters");
        ArrayList<HashMap<String,String>> costCenterLocationMapping = (ArrayList<HashMap<String,String>>) syncJobTypeJournal.getConfiguration().get("costCenterLocationMapping");
        ArrayList<HashMap<String,String>> overGroups = (ArrayList<HashMap<String, String>>) syncJobTypeJournal.getConfiguration().get("overGroups");

        ArrayList<HashMap<String,String>> wasteGroups = (ArrayList<HashMap<String, String>>) syncJobType.getConfiguration().get("wasteGroups");

        WebDriver driver = setupEnvironment.setupSeleniumEnv(false);
        ArrayList<HashMap<String, Object>> wastes = new ArrayList<>();
        ArrayList<HashMap<String, Object>> wastesStatus = new ArrayList<>();
        ArrayList<HashMap<String, Object>> journalEntries = new ArrayList<>();

        String url = "https://mte03-ohim-prod.hospitality.oracleindustry.com/Webclient/FormLogin.aspx";

        if (!setupEnvironment.loginOHIM(driver, url, account)) {
            driver.quit();

            data.put("status", Constants.FAILED);
            data.put("message", "Invalid username and password.");
            data.put("wastes", wastes);
            return data;
        }

        for (HashMap<String,String> costCenter : costCenters) {
            try {
                HashMap<String, Object> CostCenterData = invoiceController.checkCostCenterExistence(costCenterLocationMapping, costCenter.get("cost_center"), false);
                HashMap<String, String> CostCenter = (HashMap<String, String>) CostCenterData.get("costCenter");

                if (!(boolean)CostCenterData.get("status")) continue;

                String locationName = CostCenter.get("locationName");

                String bookedWasteUrl = "https://mte03-ohra-prod.hospitality.oracleindustry.com/finengine/reportAction.do?method=run&reportID=497";
                driver.get(bookedWasteUrl);

                WebDriverWait wait = new WebDriverWait(driver, 20);
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("loadingFrame")));

                wait.until(ExpectedConditions.elementToBeClickable(By.id("calendarBtn")));
                driver.findElement(By.id("calendarBtn")).click();

                Select location = new Select(driver.findElement(By.id("locationData")));
                location.selectByVisibleText(locationName);

                wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.id("calendarFrame")));
                wait.until(ExpectedConditions.presenceOfElementLocated(By.id("selectQuick")));

                Select date = new Select(driver.findElement(By.id("'selectQuick'")));
                date.selectByVisibleText("Last Month");

                driver.switchTo().defaultContent();
                driver.findElement(By.id("Run Report")).click();

                String baseURL = "https://mte03-ohra-prod.hospitality.oracleindustry.com/finengine/reportRunAction.do?rptroot=497&reportID=myInvenItemWasteSummary&method=run";

                driver.get(baseURL);

                List<WebElement> rows = driver.findElements(By.tagName("tr"));

                ArrayList<String> columns = setupEnvironment.getTableColumns(rows, true, 6);

                for (int i = 7; i < rows.size(); i++) {
                    HashMap<String, Object> waste = new HashMap<>();

                    WebElement row = rows.get(i);
                    List<WebElement> cols = row.findElements(By.tagName("td"));
                    if (cols.size() != columns.size()) {
                        continue;
                    }

                    // check if this row in selected waste type
                    WebElement td = cols.get(columns.indexOf("waste_type"));
                    HashMap<String, Object> wasteTypeData = conversions.checkWasteTypeExistence(wasteGroups, td.getText().strip());

                    if (!(boolean) wasteTypeData.get("status")) {
                        continue;
                    }

                    for (int j = 0; j < cols.size(); j++) {
                        td = cols.get(j);
                        if (j == columns.indexOf("document_name")){
                            String extension = td.findElement(By.tagName("div")).getAttribute("onclick");
                            int index = extension.indexOf('\'');
                            extension = extension.substring(0, index);
                            waste.put("waste_details_link", extension);
                            continue;
                        }
                        waste.put(columns.get(j), td.getText());
                    }
                    wastesStatus.add(waste);
                }

                for (HashMap<String, Object> waste: wastesStatus) {
                    getWasteDetails(items, overGroups, waste, costCenter, driver, journalEntries);
                }

            } catch (Exception e) {
                e.printStackTrace();
                driver.quit();

                data.put("status", Constants.FAILED);
                data.put("message", e);
                data.put("wastes", journalEntries);
                return data;
            }
        }
        driver.quit();

        data.put("status", Constants.SUCCESS);
        data.put("message", "");
        data.put("wastes", journalEntries);
        return data;
    }

    private void getWasteDetails(
            ArrayList<HashMap<String, String>> items, ArrayList<HashMap<String, String>> overGroups,
            HashMap<String, Object> waste, HashMap<String, String> costCenter, WebDriver driver
            , ArrayList<HashMap<String, Object>> journalEntries){
        ArrayList<Journal> journals = new ArrayList<>();

        try {
            String baseURL = "https://mte03-ohra-prod.hospitality.oracleindustry.com";
            driver.get(baseURL + waste.get("waste_details_link"));

            List<WebElement> rows = driver.findElements(By.tagName("tr"));
            ArrayList<String> columns = setupEnvironment.getTableColumns(rows, true, 6);

            for (int i = 7; i < rows.size(); i++) {
                HashMap<String, Object> transferDetails = new HashMap<>();
                WebElement row = rows.get(i);
                List<WebElement> cols = row.findElements(By.tagName("td"));

                if (cols.size() != columns.size()) {
                    continue;
                }

                // check if this item belong to selected items
                WebElement td = cols.get(columns.indexOf("item"));

                HashMap<String, Object> oldItemData = conversions.checkItemExistence(items, td.getText().strip());

                if (!(boolean) oldItemData.get("status")) {
                    continue;
                }

                HashMap<String, String> oldItem = (HashMap<String, String>) oldItemData.get("item");
                String overGroup = oldItem.get("over_group");

                transferDetails.put("item", td.getText());

                td = cols.get(columns.indexOf("value"));
                transferDetails.put("value", td.getText());

                Journal journal = new Journal();
                journals = journal.checkExistence(journals, overGroup, conversions.convertStringToFloat((String) transferDetails.get("value")),
                        0,0, 0);

            }

            for (Journal journal : journals) {
                HashMap<String, Object> oldOverGroupData = conversions.checkOverGroupExistence(overGroups, journal.getOverGroup());
                HashMap<String, String> oldOverGroup = (HashMap<String, String>)oldOverGroupData.get("overGroup");

                if (!(boolean) oldOverGroupData.get("status")) {
                    continue;
                }

                HashMap<String, Object> journalEntry = new HashMap<>();

                journalEntry.put("total", journal.getTotalTransfer());
                journalEntry.put("from_cost_center", costCenter.get("costCenter"));
                journalEntry.put("from_account_code", oldOverGroup.get("wasteAccountCredit"));

                journalEntry.put("to_cost_center", costCenter.get("costCenter"));
                journalEntry.put("to_account_code", oldOverGroup.get("wasteAccountDebit"));

                journalEntry.put("description", "Wastage Entry For " + costCenter.get("costCenter") + " " + waste.get("waste_type") + " - " + " - " + journal.getOverGroup());

                journalEntry.put("transactionReference", "");
                journalEntry.put("overGroup", journal.getOverGroup());

                journalEntries.add(journalEntry);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public ArrayList<SyncJobData> saveWastageSunData(ArrayList<HashMap<String, String>> wastes, SyncJob syncJob) {
        ArrayList<SyncJobData> addedTransfers = new ArrayList<>();

        for (HashMap<String, String> waste : wastes) {

            SyncJobData syncJobData = new SyncJobData(waste, Constants.RECEIVED, "", new Date(),
                    syncJob.getId());
            syncJobDataRepo.save(syncJobData);

            addedTransfers.add(syncJobData);
        }
        return addedTransfers;

    }



}
