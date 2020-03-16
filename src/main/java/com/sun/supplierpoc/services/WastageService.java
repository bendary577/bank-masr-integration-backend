package com.sun.supplierpoc.services;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.controllers.InvoiceController;
import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.models.configurations.CostCenter;
import com.sun.supplierpoc.models.configurations.Item;
import com.sun.supplierpoc.models.configurations.OverGroup;
import com.sun.supplierpoc.models.configurations.WasteGroup;
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
                                                   SyncJobType invoiceSyncJobType, Account account) {

        HashMap<String, Object> response = new HashMap<>();
        ArrayList<Item> items = syncJobTypeJournal.getConfiguration().getItems();
        ArrayList<CostCenter> costCenters = invoiceSyncJobType.getConfiguration().getCostCenters();
        ArrayList<CostCenter> costCenterLocationMapping = syncJobTypeJournal.getConfiguration().getCostCenterLocationMapping();
        ArrayList<OverGroup> overGroups = syncJobTypeJournal.getConfiguration().getOverGroups();
        ArrayList<WasteGroup> wasteGroups = syncJobType.getConfiguration().getWasteGroups();

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

        ArrayList<HashMap<String, Object>> wastes = new ArrayList<>();
        ArrayList<HashMap<String, Object>> wastesStatus;
        ArrayList<HashMap<String, Object>> journalEntries = new ArrayList<>();

        String url = "https://mte03-ohra-prod.hospitality.oracleindustry.com/servlet/PortalLogIn/";

        if (!setupEnvironment.loginOHRA(driver, url, account)) {
            driver.quit();

            response.put("status", Constants.FAILED);
            response.put("message", "Invalid username and password.");
            response.put("wastes", wastes);
            return response;
        }

        try {
            WebDriverWait wait = new WebDriverWait(driver, 3);
            wait.until(ExpectedConditions.alertIsPresent());
        }
        catch (Exception e) {
            System.out.println("Waiting");
        }

        for (CostCenter costCenter : costCenters) {
            try {
                CostCenter CostCenterData = conversions.checkCostCenterExistence(costCenterLocationMapping, costCenter.costCenter, false);

                if (!CostCenterData.checked) continue;

                wastesStatus = new ArrayList<>();

                String locationName = CostCenterData.locationName;

                String bookedWasteUrl = "https://mte03-ohra-prod.hospitality.oracleindustry.com/finengine/reportAction.do?method=run&reportID=497";
                driver.get(bookedWasteUrl);

                try {
                    WebDriverWait wait = new WebDriverWait(driver, 60);
                    wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("loadingFrame")));
                }
                catch (Exception ex){ }

                String timePeriod = syncJobType.getConfiguration().getTimePeriod();

                WebDriverWait wait = new WebDriverWait(driver, 20);
                if (timePeriod.equals("Last Month")){
                    wait.until(ExpectedConditions.elementToBeClickable(By.id("calendarBtn")));
                    driver.findElement(By.id("calendarBtn")).click();

                    Select locationDate = new Select(driver.findElement(By.id("locationData")));
                    locationDate.selectByVisibleText(locationName);

                    String selectedOption = locationDate.getFirstSelectedOption().getText().strip();
                    while (!selectedOption.equals(locationName)){}

                    wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.id("calendarFrame")));
                    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("selectQuick")));

                    Select businessDate = new Select(driver.findElement(By.id("selectQuick")));
                    businessDate.selectByVisibleText(timePeriod);

                    selectedOption = businessDate.getFirstSelectedOption().getText().strip();
                    while (!selectedOption.equals(timePeriod)){}

                    driver.switchTo().defaultContent();
                }
                else {
                    wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("loadingFrame")));

                    Select businessDate = new Select(driver.findElement(By.id("calendarData")));
                    businessDate.selectByVisibleText(timePeriod);

                    String selectedOption = businessDate.getFirstSelectedOption().getText().strip();
                    while (!selectedOption.equals(timePeriod)){}

                    Select locationDate= new Select(driver.findElement(By.id("locationData")));
                    locationDate.selectByVisibleText(locationName);

                    selectedOption = locationDate.getFirstSelectedOption().getText().strip();
                    while (!selectedOption.equals(locationName)){}

                }
                driver.findElement(By.id("Run Report")).click();

                String baseURL = "https://mte03-ohra-prod.hospitality.oracleindustry.com/finengine/reportRunAction.do?rptroot=497&reportID=myInvenItemWasteSummary&method=run";

                driver.get(baseURL);

                WebElement table = driver.findElement(By.id("tableContainer"));
                List<WebElement> rows = table.findElements(By.tagName("tr"));

                ArrayList<String> columns = setupEnvironment.getTableColumns(rows, false, 0);

                for (int i = 1; i < rows.size(); i++) {
                    HashMap<String, Object> waste = new HashMap<>();

                    WebElement row = rows.get(i);
                    List<WebElement> cols = row.findElements(By.tagName("td"));
                    if (cols.size() != columns.size()) {
                        continue;
                    }

                    // check if this row in selected waste type
                    WebElement td = cols.get(columns.indexOf("waste_type"));
                    WasteGroup wasteTypeData = conversions.checkWasteTypeExistence(wasteGroups, td.getText().strip());

                    if (!wasteTypeData.getChecked()) {
                        continue;
                    }

                    for (int j = 0; j < cols.size(); j++) {
                        td = cols.get(j);
                        if (j == columns.indexOf("document_name")){
                            String extension = td.findElement(By.tagName("div")).getAttribute("onclick");
                            int index = extension.indexOf('\'');
                            int index2 = extension.indexOf(',');
                            extension = extension.substring(index+1 , index2-1);
                            waste.put("waste_details_link", extension);
                            continue;
                        }
                        waste.put(columns.get(j), td.getText());
                    }
                    wastesStatus.add(waste);
                }

                for (HashMap<String, Object> waste: wastesStatus) {
                    getWasteDetails(items, overGroups, costCenter, waste, driver, journalEntries);
                }

            } catch (Exception e) {
                driver.quit();

                response.put("status", Constants.FAILED);
                response.put("message", e.getMessage());
                response.put("wastes", journalEntries);
                return response;
            }
        }
        driver.quit();

        response.put("status", Constants.SUCCESS);
        response.put("message", "");
        response.put("wastes", journalEntries);
        return response;
    }

    private void getWasteDetails(
            ArrayList<Item> items, ArrayList<OverGroup> overGroups, CostCenter costCenter,
            HashMap<String, Object> waste, WebDriver driver, ArrayList<HashMap<String, Object>> journalEntries){
        ArrayList<Journal> journals = new ArrayList<>();

        try {
            String baseURL = "https://mte03-ohra-prod.hospitality.oracleindustry.com";
            driver.get(baseURL + waste.get("waste_details_link"));

            List<WebElement> rows = driver.findElements(By.tagName("tr"));
            ArrayList<String> columns = setupEnvironment.getTableColumns(rows, false, 4);

            for (int i = 7; i < rows.size(); i++) {
                HashMap<String, Object> transferDetails = new HashMap<>();
                WebElement row = rows.get(i);
                List<WebElement> cols = row.findElements(By.tagName("td"));

                if (cols.size() != columns.size()) {
                    continue;
                }

                // check if this Item belong to selected items
                WebElement td = cols.get(columns.indexOf("item"));

                Item oldItemData = conversions.checkItemExistence(items, td.getText().strip());

                if (!oldItemData.isChecked()) {
                    continue;
                }

                String overGroup = oldItemData.getOverGroup();

                transferDetails.put("Item", td.getText());

                td = cols.get(columns.indexOf("value"));
                transferDetails.put("value", td.getText());

                Journal journal = new Journal();
                journals = journal.checkExistence(journals, overGroup, conversions.convertStringToFloat((String) transferDetails.get("value")),
                        0,0, 0);

            }

            for (Journal journal : journals) {
                OverGroup oldOverGroupData = conversions.checkOverGroupExistence(overGroups, journal.getOverGroup());

                if (!oldOverGroupData.getChecked()) {
                    continue;
                }

                HashMap<String, Object> journalEntry = new HashMap<>();

                journalEntry.put("total", journal.getTotalTransfer());
                journalEntry.put("from_cost_center", costCenter.costCenter);
                journalEntry.put("from_account_code", oldOverGroupData.getWasteAccountCredit());

                journalEntry.put("to_cost_center", costCenter.costCenter);
                journalEntry.put("to_account_code", oldOverGroupData.getWasteAccountDebit());

                journalEntry.put("description", "Wastage Entry For " + costCenter.costCenter + " - " + waste.get("waste_type") + " - " + journal.getOverGroup());

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
