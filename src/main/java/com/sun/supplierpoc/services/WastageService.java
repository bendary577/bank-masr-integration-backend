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

import java.text.SimpleDateFormat;
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

    public HashMap<String, Object> getWastageData(SyncJobType syncJobType, ArrayList<Item> items,
                                                  ArrayList<CostCenter> costCenters, ArrayList<OverGroup> overGroups,
                                                  ArrayList<WasteGroup> wasteGroups, Account account) {

        HashMap<String, Object> response = new HashMap<>();

        WebDriver driver = null;
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

        try{
            if (!setupEnvironment.loginOHIM(driver, Constants.OHIM_LOGIN_LINK, account)) {
                driver.quit();

                response.put("status", Constants.FAILED);
                response.put("message", "Invalid username and password.");
                response.put("wastes", wastes);
                return response;
            }

            String bookedWastes = "https://mte03-ohim-prod.hospitality.oracleindustry.com/Webclient/Store/Waste/WstOverviewView.aspx?type=1";
            driver.get(bookedWastes);

            Select select = new Select(driver.findElement(By.id("_ctl5")));
            String timePeriod = syncJobType.getConfiguration().getTimePeriod();

            // Open filter search
            String filterStatus = driver.findElement(By.id("filterPanel_btnToggleFilter")).getAttribute("value");

            if (filterStatus.equals("Show Filter")){
                driver.findElement(By.id("filterPanel_btnToggleFilter")).click();
            }

            response = setupEnvironment.selectTimePeriodOHIM(timePeriod, select, driver);

            if (!response.get("status").equals(Constants.SUCCESS)){
                return response;
            }

            driver.findElement(By.name("filterPanel_btnRefresh")).click();

            try{
                WebDriverWait wait = new WebDriverWait(driver, 60);
                wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("tableLoadingBar")));

            } catch (Exception e) {
                driver.quit();

                response.put("status", Constants.FAILED);
                response.put("message", "Oracle Hospitality takes long time to load, Please try again after few minutes.");
                response.put("invoices", journalEntries);
                return response;
            }

            try{
                // wait until table is ready
                WebDriverWait wait = new WebDriverWait(driver, 60);
                wait.until(ExpectedConditions.presenceOfElementLocated(By.id("G_dg")));

            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

            WebElement bodyTable = driver.findElement(By.id("G_dg"));
            WebElement headerTable = driver.findElement(By.id("dg_main"));

            List<WebElement> rows = bodyTable.findElements(By.tagName("tr"));
            List<WebElement> headerRows = headerTable.findElements(By.tagName("tr"));

            ArrayList<String> columns = setupEnvironment.getTableColumns(headerRows, true, 0);

            while (true){
                for (int i = 1; i < rows.size(); i++) {
                    HashMap<String, Object> waste = new HashMap<>();

                    WebElement row = rows.get(i);
                    List<WebElement> cols = row.findElements(By.tagName("td"));
                    if (cols.size() !=  columns.size()){
                        continue;
                    }

                    // check if cost center chosen
                    WebElement td = cols.get(columns.indexOf("cost_center"));
                    CostCenter oldCostCenterData = conversions.checkCostCenterExistence(costCenters, td.getText().strip(), false);

                    if (!oldCostCenterData.checked) {
                        continue;
                    }

                    waste.put(columns.get(columns.indexOf("cost_center")), oldCostCenterData);

                    // check if waste group chosen
                    td = cols.get(columns.indexOf("waste_group"));
                    WasteGroup oldWasteGroupData = conversions.checkWasteTypeExistence(wasteGroups, td.getText().strip());

                    if (!oldWasteGroupData.getChecked()) {
                        continue;
                    }

                    waste.put(columns.get(columns.indexOf("waste_group")), oldWasteGroupData);

                    String link = cols.get(columns.indexOf("document")).findElement(By.tagName("a")).getAttribute("href");
                    waste.put("waste_details_link", link);

                    td = cols.get(columns.indexOf("waste_date"));
                    String deliveryDate = td.getText().strip();
                    // 7/11/2020 "Hospitality Format"
                    SimpleDateFormat formatter1=new SimpleDateFormat("MM/dd/yyyy");
                    Date deliveryDateFormatted =formatter1.parse(deliveryDate);

                    SimpleDateFormat simpleformat = new SimpleDateFormat("ddMMy");
                    String date = simpleformat.format(deliveryDateFormatted);

                    waste.put("waste_date", date);

                    for (int j = 0; j < cols.size(); j++) {
                        if (j == columns.indexOf("cost_center") || j == columns.indexOf("waste_group")
                                || j == columns.indexOf("waste_date"))
                            continue;
                        waste.put(columns.get(j), cols.get(j).getText().strip());
                    }
                    wastes.add(waste);
                }

                // check if there is other pages
                if (driver.findElements(By.linkText("Next")).size() == 0){
                    break;
                }
                else {
                    TransferService.checkPagination(driver, "dg_rc_0_1");
                    bodyTable = driver.findElement(By.id("G_dg"));
                    rows = bodyTable.findElements(By.tagName("tr"));
                }
            }

            for (HashMap<String, Object> waste:wastes) {
                getWasteDetails(items, overGroups, waste, driver, journalEntries);
            }


            driver.quit();

            response.put("status", Constants.SUCCESS);
            response.put("message", "");
            response.put("wastes", journalEntries);
            return response;

        }catch (Exception e) {
            e.printStackTrace();
            driver.quit();
            response.put("status", Constants.FAILED);
            response.put("message", e.getMessage());
            response.put("wastes", journalEntries);
            return response;
        }
    }

    private void getWasteDetails(
            ArrayList<Item> items, ArrayList<OverGroup> overGroups,
            HashMap<String, Object> waste, WebDriver driver, ArrayList<HashMap<String, Object>> journalEntries){
        ArrayList<Journal> journals = new ArrayList<>();

        try {
            driver.get((String) waste.get("waste_details_link"));

            WebElement bodyTable = driver.findElement(By.id("G_dg"));
            WebElement headerTable = driver.findElement(By.xpath("/html/body/form/table/tbody/tr[5]/td/table/tbody/tr[1]/td/div/table"));

            List<WebElement> headerRows = headerTable.findElements(By.tagName("tr"));
            List<WebElement> rows = bodyTable.findElements(By.tagName("tr"));

            ArrayList<String> columns = setupEnvironment.getTableColumns(headerRows, true, 0);

            for (int i = 1; i < rows.size(); i++) {
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

                transferDetails.put("Item", td.getText().strip());

                td = cols.get(columns.indexOf("total"));
                transferDetails.put("value", td.getText().strip());

                Journal journal = new Journal();
                journals = journal.checkExistence(journals, overGroup, conversions.convertStringToFloat((String) transferDetails.get("value")),
                        0,0, 0);

            }

            CostCenter costCenter = (CostCenter) waste.get("cost_center");
            for (Journal journal : journals) {
                OverGroup oldOverGroupData = conversions.checkOverGroupExistence(overGroups, journal.getOverGroup());

                if (!oldOverGroupData.getChecked()) {
                    continue;
                }

                HashMap<String, Object> journalEntry = new HashMap<>();

                if (costCenter.costCenterReference.equals("")){
                    costCenter.costCenterReference = costCenter.costCenter;
                }

                journalEntry.put("transactionDate", waste.get("waste_date"));

                journalEntry.put("totalCr", conversions.roundUpFloat(journal.getTotalWaste()));
                journalEntry.put("totalDr", conversions.roundUpFloat(journal.getTotalWaste()) * -1);

                journalEntry.put("fromCostCenter", costCenter.costCenter);
                journalEntry.put("fromAccountCode", costCenter.accountCode);

                journalEntry.put("toCostCenter", costCenter.costCenter);
                journalEntry.put("toAccountCode", costCenter.accountCode);

                journalEntry.put("fromLocation", costCenter.accountCode);
                journalEntry.put("toLocation", costCenter.accountCode);

                String description =  "W For " + costCenter.costCenterReference + " - " + journal.getOverGroup();
                if (description.length() > 50){
                    description = description.substring(0, 50);
                }

                journalEntry.put("description", description);

                journalEntry.put("transactionReference", "Wastage Transaction Reference");
                journalEntry.put("overGroup", journal.getOverGroup());

                journalEntry.put("inventoryAccount", oldOverGroupData.getInventoryAccount());
                journalEntry.put("expensesAccount", oldOverGroupData.getExpensesAccount());

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

    @Deprecated
    public HashMap<String, Object> getWastageReportData(SyncJobType syncJobType, GeneralSettings generalSettings, Account account) {

        HashMap<String, Object> response = new HashMap<>();
        ArrayList<Item> items = generalSettings.getItems();
        ArrayList<CostCenter> costCenters = generalSettings.getCostCenterAccountMapping();
        ArrayList<CostCenter> costCenterLocationMapping = generalSettings.getCostCenterLocationMapping();
        ArrayList<OverGroup> overGroups = generalSettings.getOverGroups();
        ArrayList<WasteGroup> wasteGroups = syncJobType.getConfiguration().getWasteGroups();

        WebDriver driver = null;
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

                Select locationData = new Select(driver.findElement(By.id("locationData")));
                try {
                    locationData.selectByVisibleText(locationName);
                } catch (Exception e) {
                    System.out.println("Invalid location");
                    continue;
                }

                String selectedOption = locationData.getFirstSelectedOption().getText().strip();
                while (!selectedOption.equals(locationName)){
                    selectedOption = locationData.getFirstSelectedOption().getText().strip();
                }

                Response dateResponse = setupEnvironment.selectTimePeriodOHRA(timePeriod, "", driver);

                if (!dateResponse.isStatus()){
                    response.put("status", Constants.FAILED);
                    response.put("message", dateResponse.getMessage());
                    response.put("wastes", journalEntries);
                    return response;
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
                        waste.put(columns.get(j), td.getText().strip());
                    }
                    wastesStatus.add(waste);
                }

                for (HashMap<String, Object> waste: wastesStatus) {
                    getWasteReportDetails(items, overGroups, costCenter, waste, driver, journalEntries);
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

    @Deprecated
    private void getWasteReportDetails(
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

                transferDetails.put("Item", td.getText().strip());

                td = cols.get(columns.indexOf("value"));
                transferDetails.put("value", td.getText().strip());

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

                if (costCenter.costCenterReference.equals("")){
                    costCenter.costCenterReference = costCenter.costCenter;
                }

                journalEntry.put("totalCr", conversions.roundUpFloat(journal.getTotalWaste()));
                journalEntry.put("totalDr", conversions.roundUpFloat(journal.getTotalWaste()) * -1);

                journalEntry.put("fromCostCenter", costCenter.costCenter);
                journalEntry.put("fromAccountCode", costCenter.accountCode);

                journalEntry.put("toCostCenter", costCenter.costCenter);
                journalEntry.put("toAccountCode", costCenter.accountCode);

                journalEntry.put("fromLocation", costCenter.accountCode);
                journalEntry.put("toLocation", costCenter.accountCode);

                String description = "W F " + costCenter.costCenterReference + " - " + waste.get("waste_type") + " - " + journal.getOverGroup();
                if (description.length() > 50){
                    description = description.substring(0, 50);
                }

                journalEntry.put("description", description);

                journalEntry.put("transactionReference", "Wastage Transaction Reference");
                journalEntry.put("overGroup", journal.getOverGroup());

                journalEntry.put("inventoryAccount", oldOverGroupData.getInventoryAccount());
                journalEntry.put("expensesAccount", oldOverGroupData.getExpensesAccount());

                journalEntries.add(journalEntry);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
