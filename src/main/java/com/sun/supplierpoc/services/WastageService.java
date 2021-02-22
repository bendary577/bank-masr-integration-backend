package com.sun.supplierpoc.services;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.controllers.InvoiceController;
import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.models.configurations.*;
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

import javax.security.auth.login.FailedLoginException;
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
    @Autowired
    private SyncJobDataService syncJobDataService;

    private Conversions conversions = new Conversions();
    private SetupEnvironment setupEnvironment = new SetupEnvironment();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public Response getWastageData(SyncJobType syncJobType, ArrayList<Item> items,
                                                  ArrayList<CostCenter> costCenters, ArrayList<OverGroup> overGroups,
                                                  ArrayList<WasteGroup> wasteGroups, Account account) {

        Response response = new Response();

        WebDriver driver = null;
        try{
            driver = setupEnvironment.setupSeleniumEnv(false);
        }
        catch (Exception ex){
            response.setStatus(false);
            response.setMessage("Failed to establish connection with firefox driver.");

            return response;
        }

        ArrayList<HashMap<String, Object>> wastes = new ArrayList<>();
        ArrayList<HashMap<String, Object>> journalEntries = new ArrayList<>();

        try{
            if (!setupEnvironment.loginOHIM(driver, Constants.OHIM_LOGIN_LINK, account)) {
                driver.quit();

                response.setStatus(false);
                response.setMessage("Invalid username and password.");

                return response;
            }

            String bookedWastes = "https://mte03-ohim-prod.hospitality.oracleindustry.com/Webclient/Store/Waste/WstOverviewView.aspx?type=1";
            driver.get(bookedWastes);

            Select select = new Select(driver.findElement(By.id("_ctl5")));
            String timePeriod = syncJobType.getConfiguration().timePeriod;
            String fromDate = syncJobType.getConfiguration().fromDate;
            String toDate = syncJobType.getConfiguration().toDate;

            // Open filter search
            String filterStatus = driver.findElement(By.id("filterPanel_btnToggleFilter")).getAttribute("value");

            if (filterStatus.equals("Show Filter")){
                driver.findElement(By.id("filterPanel_btnToggleFilter")).click();
            }


            HashMap<String, Object> dateResponse = setupEnvironment.selectTimePeriodOHIM(timePeriod, fromDate, toDate, select, driver);

            if (!dateResponse.get("status").equals(Constants.SUCCESS)){
                response.setStatus(false);
                response.setMessage(String.valueOf(dateResponse.get("message")));

                return response;
            }

            driver.findElement(By.name("filterPanel_btnRefresh")).click();

            try{
                WebDriverWait wait = new WebDriverWait(driver, 60);
                wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("tableLoadingBar")));

            } catch (Exception e) {
                driver.quit();
                response.setStatus(false);
                response.setMessage("Oracle Hospitality takes long time to load, Please try again after few minutes.");
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
                    TransferService.checkPagination(driver, "dg_rc_0_0");
                    bodyTable = driver.findElement(By.id("G_dg"));
                    rows = bodyTable.findElements(By.tagName("tr"));
                }
            }

            for (HashMap<String, Object> waste:wastes) {
                getWasteDetails(items, overGroups, waste, driver, journalEntries);
            }

            driver.quit();

            response.setStatus(true);
            response.setWaste(journalEntries);

            return response;


        }catch (Exception e) {
            e.printStackTrace();

            driver.quit();
            response.setStatus(false);
            response.setMessage(e.getMessage());
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

                journalEntry.put("accountingPeriod", ((String)waste.get("waste_date")).substring(2,6));
                journalEntry.put("transactionDate", String.valueOf(waste.get("waste_date")));

                journalEntry.put("totalCr", String.valueOf(conversions.roundUpFloat(journal.getTotalWaste())));
                journalEntry.put("totalDr", String.valueOf(conversions.roundUpFloat(journal.getTotalWaste()) * -1));

                journalEntry.put("fromCostCenter", costCenter.costCenter);
                journalEntry.put("fromAccountCode", costCenter.accountCode);

                journalEntry.put("toCostCenter", costCenter.costCenter);
                journalEntry.put("toAccountCode", costCenter.accountCode);

                journalEntry.put("fromLocation", costCenter.accountCode);
                journalEntry.put("toLocation", costCenter.accountCode);

                String description =  "F " + costCenter.costCenterReference + " - " + journal.getOverGroup();
                if (description.length() > 50){
                    description = description.substring(0, 50);
                }

                journalEntry.put("description", description);

                if (waste.containsKey("reference") && !waste.get("reference").equals("")){
                    String reference = (String)waste.get("reference");
                    if(reference.length() > 30){
                        reference = reference.substring(0, 30);
                    }
                    journalEntry.put("transactionReference", reference);
                }else {
                    journalEntry.put("transactionReference", "Waste");
                }

                journalEntry.put("overGroup", journal.getOverGroup());

                journalEntry.put("inventoryAccount", oldOverGroupData.getInventoryAccount());
                journalEntry.put("expensesAccount", oldOverGroupData.getExpensesAccount());

                journalEntries.add(journalEntry);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void saveWastageSunData(ArrayList<JournalBatch> wasteBatches, SyncJob syncJob) {
        ArrayList<SyncJobData> addedWaste;
        ArrayList<HashMap<String, Object>> wastes;

        for (JournalBatch wasteBatch : wasteBatches) {
            addedWaste = new ArrayList<>();
            wastes = wasteBatch.getWaste();
            for (HashMap<String, Object> waste : wastes) {
                SyncJobData syncJobData = new SyncJobData(waste, Constants.RECEIVED, "", new Date(),
                        syncJob.getId());
                syncJobDataRepo.save(syncJobData);

                addedWaste.add(syncJobData);
            }

            wasteBatch.setWasteData(addedWaste);
        }
    }

    public Response getWastageReportData(SyncJobType syncJobType, GeneralSettings generalSettings, Account account) {

        Response response = new Response();
        String businessDate = syncJobType.getConfiguration().timePeriod;
        String fromDate = syncJobType.getConfiguration().fromDate;
        String toDate = syncJobType.getConfiguration().toDate;

        ArrayList<Item> items = generalSettings.getItems();
        ArrayList<CostCenter> locations = generalSettings.getLocations();
        ArrayList<OverGroup> overGroups = generalSettings.getOverGroups();
        ArrayList<WasteGroup> wasteGroups = syncJobType.getConfiguration().wastageConfiguration.wasteGroups;

        ArrayList<HashMap<String, Object>> wastesStatus;
        JournalBatch journalBatch;
        ArrayList<HashMap<String, Object>> journalEntries;
        ArrayList<JournalBatch> journalBatches = new ArrayList<>();

        WebDriver driver;
        try{
            driver = setupEnvironment.setupSeleniumEnv(false);
        }
        catch (Exception ex){
            response.setStatus(false);
            response.setMessage("Failed to establish connection with firefox driver.");
            return response;
        }

        if (!setupEnvironment.loginOHRA(driver, Constants.OHRA_LINK, account)) {
            driver.quit();
            response.setStatus(false);
            response.setMessage( "Invalid username and password.");

            return response;
        }

        try {
            WebDriverWait wait = new WebDriverWait(driver, 3);
            wait.until(ExpectedConditions.alertIsPresent());
        }
        catch (Exception e) {
            System.out.println("Waiting");
        }

        for (CostCenter costCenter : locations) {
            try {
                journalBatch = new JournalBatch();
                journalEntries = new ArrayList<>();

                if (!costCenter.checked) continue;

                wastesStatus = new ArrayList<>();
                driver.get(Constants.BOOKED_WASTE_REPORT_LINK);

                try {
                    WebDriverWait wait = new WebDriverWait(driver, 60);
                    wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("loadingFrame")));
                }
                catch (Exception ignored){}

                Response dateResponse = new Response();
                if (setupEnvironment.runReport(businessDate, fromDate, toDate, costCenter, new RevenueCenter(), driver, dateResponse)){
                    driver.quit();

                    if(dateResponse.getMessage().equals(Constants.WRONG_BUSINESS_DATE)){
                        response.setStatus(false);
                        response.setMessage(dateResponse.getMessage());
                        return response;
                    } else if (dateResponse.getMessage().equals(Constants.NO_INFO)) {
                        response.setStatus(true);
                        response.setMessage(Constants.NO_INFO);
                        continue;
                    }
                }

                driver.get(Constants.WASTE_GROUPS_CONTENT_LINK);

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

                    WebElement td = cols.get(columns.indexOf("waste_type"));
                    WasteGroup wasteTypeData = conversions.checkWasteTypeExistence(wasteGroups, td.getText().strip());

                    if (!wasteTypeData.getChecked()) {
                        continue;
                    }

                    td = cols.get(columns.indexOf("date"));
                    String deliveryDate = td.getText().strip();

                    SimpleDateFormat formatter1=new SimpleDateFormat("MM/dd/yyyy");
                    Date deliveryDateFormatted =formatter1.parse(deliveryDate);

                    SimpleDateFormat simpleformat = new SimpleDateFormat("ddMMy");
                    String date = simpleformat.format(deliveryDateFormatted);

                    waste.put("waste_date", date);

                    for (int j = 0; j < cols.size(); j++) {
                        td = cols.get(j);
                        if (j == columns.indexOf("date"))
                            continue;
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
                    getWasteReportDetails(items, overGroups, costCenter, waste, syncJobType, driver, journalEntries);
                }

                journalBatch.setCostCenter(costCenter);
                journalBatch.setWaste(journalEntries);
                journalBatches.add(journalBatch);

            } catch (Exception e) {
                driver.quit();
                response.setStatus(false);
                response.setMessage(e.getMessage());

                return response;
            }
        }

        driver.quit();
        response.setStatus(true);
        response.setJournalBatches(journalBatches);
        return response;
    }

    private void getWasteReportDetails(
            ArrayList<Item> items, ArrayList<OverGroup> overGroups, CostCenter costCenter,
            HashMap<String, Object> waste, SyncJobType syncJobType,
            WebDriver driver, ArrayList<HashMap<String, Object>> journalEntries){
        ArrayList<Journal> journals = new ArrayList<>();

        try {
            driver.get(Constants.OHRA_LINK + waste.get("waste_details_link"));

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

                if (!oldOverGroupData.getChecked())
                    continue;
                if(conversions.roundUpFloat(journal.getTotalWaste()) == 0)
                    continue;

                HashMap<String, Object> journalEntry = new HashMap<>();
                syncJobDataService.prepareAnalysis(journalEntry, syncJobType.getConfiguration(),
                        costCenter, null, null);

                if (costCenter.costCenterReference.equals("")){
                    costCenter.costCenterReference = costCenter.costCenter;
                }

                journalEntry.put("totalCr", Float.toString(conversions.roundUpFloat(journal.getTotalWaste())));
                journalEntry.put("totalDr", Float.toString(conversions.roundUpFloat(journal.getTotalWaste()) * -1));

                journalEntry.put("fromCostCenter", costCenter.costCenter);
                journalEntry.put("fromAccountCode", costCenter.accountCode);

                journalEntry.put("toCostCenter", costCenter.costCenter);
                journalEntry.put("toAccountCode", costCenter.accountCode);

                journalEntry.put("fromLocation", costCenter.accountCode);
                journalEntry.put("toLocation", costCenter.accountCode);

                String description = waste.get("waste_type") + " - " + journal.getOverGroup();
                if (description.length() > 50){
                    description = description.substring(0, 50);
                }

                journalEntry.put("description", description);
                journalEntry.put("transactionReference", "Waste" + " - " + costCenter.costCenterReference);

                journalEntry.put("transactionDate", String.valueOf(waste.get("waste_date")));

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
