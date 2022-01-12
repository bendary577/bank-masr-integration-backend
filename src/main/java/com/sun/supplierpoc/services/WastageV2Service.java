package com.sun.supplierpoc.services;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.controllers.InvoiceController;
import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.models.configurations.*;
import com.sun.supplierpoc.repositories.SyncJobDataRepo;
import com.sun.supplierpoc.repositories.SyncJobTypeRepo;
import com.sun.supplierpoc.seleniumMethods.MicrosFeatures;
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
public class WastageV2Service {
    @Autowired
    SyncJobDataRepo syncJobDataRepo;
    @Autowired
    SyncJobTypeRepo syncJobTypeRepo;
    @Autowired
    private SyncJobDataService syncJobDataService;

    private Conversions conversions = new Conversions();
    private SetupEnvironment setupEnvironment = new SetupEnvironment();
    private MicrosFeatures microsFeatures = new MicrosFeatures();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public Response  getWastageReportData(SyncJobType syncJobType, GeneralSettings generalSettings, Account account) {

        Response response = new Response();
        String businessDate = syncJobType.getConfiguration().timePeriod;
        String fromDate = syncJobType.getConfiguration().fromDate;
        String toDate = syncJobType.getConfiguration().toDate;

        ArrayList<Item> items = generalSettings.getItems();
        ArrayList<CostCenter> locations = generalSettings.getLocations();
        ArrayList<CostCenter> costCenters = generalSettings.getCostCenterAccountMapping();
        ArrayList<ItemGroup> itemGroups = generalSettings.getItemGroups();
        ArrayList<WasteGroup> wasteGroups = syncJobType.getConfiguration().wastageConfiguration.wasteGroups;

        ArrayList<OverGroup> overGroups;
        if (!syncJobType.getConfiguration().uniqueOverGroupMapping){
            overGroups =  generalSettings.getOverGroups();
        }else{
            overGroups =  syncJobType.getConfiguration().overGroups;
        }

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

        if (!microsFeatures.loginMicrosOHRA(driver, Constants.MICROS_V2_LINK, account)) {
            driver.quit();

            response.setStatus(false);
            response.setMessage("Invalid username and password.");
            response.setEntries(new ArrayList<>());
            return response;
        }

        try {
            WebDriverWait wait = new WebDriverWait(driver, 3);
            wait.until(ExpectedConditions.alertIsPresent());
        }
        catch (Exception e) {
            System.out.println("Waiting");
        }

        WebDriverWait wait = new WebDriverWait(driver, 30);

        for (CostCenter location : locations) {
            try {
                journalBatch = new JournalBatch();
                journalEntries = new ArrayList<>();

                if (!location.checked) continue;

                wastesStatus = new ArrayList<>();
                driver.get(Constants.MICROS_WATE_REPORT_LINK);

                try {
                    wait = new WebDriverWait(driver, 5);
                    wait.until(ExpectedConditions.alertIsPresent());
                    System.out.println("No Alert");
                } catch (Exception e) {
                    System.out.println("Waiting");
                }


                // Filter Report
                Response dateResponse = microsFeatures.selectDateRangeMicros(businessDate, fromDate, toDate, location.locationName,
                        null,"", driver);

                if (!dateResponse.isStatus()){
                    response.setStatus(false);
                    response.setMessage(dateResponse.getMessage());
                    return response;
                }

                try {
                    wait = new WebDriverWait(driver, 5);
                    wait.until(ExpectedConditions.alertIsPresent());
                    System.out.println("No Alert");
                } catch (Exception e) {
                    System.out.println("Waiting");
                }

                driver.findElement(By.xpath("//*[@id=\"save-close-button\"]/button")).click();
                /* Wait until table loaded */
                try {
                    WebDriverWait newWait = new WebDriverWait(driver, 10);
                    newWait.until(ExpectedConditions.visibilityOfElementLocated(By.id("tableContainer_")));
                } catch (Exception e) {
                    System.out.println("Waiting");
                }

                WebElement table = driver.findElement(By.id("tableContainer"));
                List<WebElement> rows = table.findElements(By.tagName("tr"));

                if(rows.size() <= 1)
                    continue;

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
                            td = cols.get(columns.indexOf("document_name"));
                            String extension = td.findElement(By.tagName("a")).getText();
                            waste.put("waste_details_element", extension);
                            continue;
                        }
                        waste.put(columns.get(j), td.getText().strip());
                    }
                    wastesStatus.add(waste);
                }

                for (HashMap<String, Object> waste: wastesStatus) {
                    getWasteReportDetails(items, itemGroups, overGroups, location, costCenters,
                            waste, syncJobType, driver, journalEntries);
                }

                journalBatch.setCostCenter(location);
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
            ArrayList<Item> items, ArrayList<ItemGroup> itemGroups, ArrayList<OverGroup> overGroups,
            CostCenter location, List<CostCenter> costCenters, HashMap<String, Object> waste, SyncJobType syncJobType,
            WebDriver driver, ArrayList<HashMap<String, Object>> journalEntries){
        ArrayList<Journal> journals = new ArrayList<>();

        try {

            /* Click on specific document */
            WebElement wastageDoc = driver.findElement(By.partialLinkText(String.valueOf(waste.get("waste_details_element"))));
            wastageDoc.click();

            /* Wait until table loaded */
            WebDriverWait wait = new WebDriverWait(driver, 30);
            try {
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("tableContainer")));
            } catch (Exception e) {
                System.out.println("Waiting");
                return;
            }
            WebElement tableContainer = driver.findElement(By.id("tableContainer"));
            List<WebElement> rows = tableContainer.findElements(By.tagName("tr"));
            ArrayList<String> columns = setupEnvironment.getTableColumns(rows, false, 0);

            String group;
            for (int i = 2; i < rows.size(); i++) {
                HashMap<String, Object> wasteDetails = new HashMap<>();
                WebElement row = rows.get(i);
                List<WebElement> cols = row.findElements(By.tagName("td"));

                if (cols.size() != columns.size()) {
                    continue;
                }

                WebElement td = cols.get(columns.indexOf("item"));

                Item item = conversions.checkItemExistence(items, td.getText().strip());

                if (!item.isChecked()) {
                    continue;
                }


                if(syncJobType.getConfiguration().syncPerGroup.equals("OverGroups"))
                    group = item.getOverGroup();
                else if(syncJobType.getConfiguration().syncPerGroup.equals("ItemGroups"))
                    group = item.getItemGroup();
                else
                    group = item.getItem();

                wasteDetails.put("Item", td.getText().strip());

                td = cols.get(columns.indexOf("value"));
                wasteDetails.put("value", td.getText().strip());

                td = cols.get(columns.indexOf("unit"));
                wasteDetails.put("unit", td.getText().strip());

                td = cols.get(columns.indexOf("quantity"));
                wasteDetails.put("quantity", td.getText().strip());

                Journal journal = new Journal();
                journals = journal.checkExistence(journals, group, conversions.convertStringToFloat((String) wasteDetails.get("value")),
                        0, 0, (String) wasteDetails.get("unit"), conversions.convertStringToFloat((String)wasteDetails.get("quantity")));

            }

            for (Journal journal : journals) {
                HashMap<String, Object> journalEntry = new HashMap<>();

                if(syncJobType.getConfiguration().syncPerGroup.equals("OverGroups")){
                    OverGroup oldOverGroupData = conversions.checkOverGroupExistence(overGroups, journal.getOverGroup());

                    if (!oldOverGroupData.getChecked())
                        continue;

                    journalEntry.put("inventoryAccount", oldOverGroupData.getInventoryAccount());
                    journalEntry.put("expensesAccount", oldOverGroupData.getExpensesAccount());
                }
                else if(syncJobType.getConfiguration().syncPerGroup.equals("ItemGroups")){
                    ItemGroup itemGroup = conversions.checkItemGroupExistence(itemGroups, journal.getOverGroup());

                    if (!itemGroup.getChecked())
                        continue;

                    journalEntry.put("inventoryAccount", itemGroup.getInventoryAccount());
                    journalEntry.put("expensesAccount", itemGroup.getExpensesAccount());
                }else{
                    Item item = conversions.checkItemExistence(items, journal.getOverGroup());

                    OverGroup oldOverGroupData = conversions.checkOverGroupExistence(overGroups, item.getOverGroup());

                    if (!oldOverGroupData.getChecked())
                        continue;

                    journalEntry.put("inventoryAccount", oldOverGroupData.getInventoryAccount());
                    journalEntry.put("expensesAccount", oldOverGroupData.getExpensesAccount());
                }

                if(location.location != null && !location.location.locationName.equals("")){
                    syncJobDataService.prepareAnalysis(journalEntry, syncJobType.getConfiguration(), location.location, null, null);
                }else {
                    syncJobDataService.prepareAnalysis(journalEntry, syncJobType.getConfiguration(), location, null, null);
                }

                if (location.costCenterReference.equals("")){
                    location.costCenterReference = location.costCenter;
                }

                journalEntry.put("totalCr", Float.toString(conversions.roundUpFloat(journal.getTotalWaste())));
                journalEntry.put("totalDr", Float.toString(conversions.roundUpFloat(journal.getTotalWaste()) * -1));

                CostCenter costCenter = new CostCenter();

                for(CostCenter tempCostCenter : costCenters){
                    if(tempCostCenter.costCenter.equals(waste.get("location"))){
                        costCenter = tempCostCenter;
                        break;
                    }
                }

                journalEntry.put("fromCostCenter", costCenter.costCenter);
                journalEntry.put("fromAccountCode", costCenter.accountCode);

                journalEntry.put("toCostCenter", "Waste");
                journalEntry.put("toAccountCode", "");

                journalEntry.put("fromLocation", costCenter.accountCode);
                journalEntry.put("toLocation", "");

                String description =  journal.getOverGroup();
                if (description.length() > 50){
                    description = description.substring(0, 50);
                }

                journalEntry.put("description", description);
                journalEntry.put("transactionReference", location.costCenterReference);

                journalEntry.put("transactionDate", String.valueOf(waste.get("waste_date")));

                journalEntry.put("overGroup", journal.getOverGroup());
                journalEntry.put("unit", journal.getUnit());
                journalEntry.put("quantity", journal.getQuantity());

                /* Check if this item already exists */
                boolean addCheck = true;
                for (HashMap<String, Object> entry : journalEntries) {
                    if(entry.get("overGroup").equals(journalEntry.get("overGroup"))){
                        entry.put("quantity", (Float)entry.get("quantity") + journal.getQuantity());
                        entry.put("totalCr", Float.toString(Float.valueOf((String) entry.get("totalCr")) + journal.getTotalWaste()));
                        entry.put("totalDr", Float.toString(Float.valueOf((String) entry.get("totalCr")) * -1));
                        addCheck = false;
                        break;
                    }
                }
                if(addCheck)
                    journalEntries.add(journalEntry);
            }

            /* Back to Waste Summary */
            driver.findElement(By.id("100267")).click();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
