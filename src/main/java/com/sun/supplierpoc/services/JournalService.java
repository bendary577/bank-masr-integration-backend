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
    public Response getJournalDataByCostCenter(SyncJobType journalSyncJobType, ArrayList<CostCenter> costCenters,
                                                  ArrayList<ItemGroup> itemGroups, Account account){
        Response response = new Response();

        WebDriver driver;
        try{
            driver = setupEnvironment.setupSeleniumEnv(false);
        }
        catch (Exception ex){
            response.setStatus(false);
            response.setMessage("Failed to establish connection with firefox driver.");
            return response;
        }

        String group;
        ArrayList<Journal> journals;
        JournalBatch journalBatch;
        ArrayList<JournalBatch> journalBatches = new ArrayList<>();

        String businessDate =  journalSyncJobType.getConfiguration().timePeriod;
        String fromDate = journalSyncJobType.getConfiguration().fromDate;
        String toDate = journalSyncJobType.getConfiguration().toDate;

        try {
            if (!setupEnvironment.loginOHRA(driver, Constants.OHRA_LINK, account)){
                driver.quit();

                response.setStatus(false);
                response.setMessage("Invalid username and password.");
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
                    response.setStatus(false);
                    response.setMessage(dateResponse.getMessage());
                    return response;
                } else if (dateResponse.getMessage().equals(Constants.NO_INFO)) {
                    response.setStatus(true);
                    response.setMessage(dateResponse.getMessage());
                    return response;
                }
            }

            journalUrl = "https://mte03-ohra-prod.hospitality.oracleindustry.com/finengine/reportRunAction.do?rptroot=499&method=run&reportID=myInvenCOSByCC";
            driver.get(journalUrl);

            List<WebElement> rows = driver.findElements(By.tagName("tr"));

            if (rows.size() < 4){
                driver.quit();

                response.setStatus(true);
                response.setMessage(Constants.NO_INFO);
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
                journalBatch = new JournalBatch();
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

                    WebElement td = cols.get(columns.indexOf("item_group"));

                    ItemGroup itemGroup = conversions.checkItemGroupExistence(itemGroups, td.getText().strip());

                    if (!itemGroup.getChecked()) {
                        continue;
                    }

                    if(journalSyncJobType.getConfiguration().consumptionConfiguration.consumptionPerGroup.equals("OverGroups"))
                        group = itemGroup.getOverGroup();
                    else
                        group = itemGroup.getItemGroup();

                    for (int j = 0; j < cols.size(); j++) {
                        transferDetails.put(columns.get(j), cols.get(j).getText().strip());
                    }

                    Journal journal = new Journal();
                    float cost = conversions.convertStringToFloat((String) transferDetails.get("actual_usage"));
                    journals = journal.checkExistence(journals, group, 0,cost, 0, 0);
                }

                journalBatch.setCostCenter((CostCenter) costCenter.get("cost_center"));
                journalBatch.setConsumption(journals);
                journalBatches.add(journalBatch);
            }

            driver.quit();

            response.setStatus(true);
            response.setJournalBatches(journalBatches);
            return response;

        } catch (Exception e) {
            e.printStackTrace();
            driver.quit();

            response.setStatus(false);
            response.setMessage("Failed to get consumption entries from Oracle Hospitality.");
            return response;
        }
    }


    /*
     * Get consumptions entries based on location
     * */
    public Response getJournalData(SyncJobType journalSyncJobType,
                                                  ArrayList<CostCenter> costCentersLocation,
                                                  ArrayList<ItemGroup> itemGroups, Account account){
        Response response = new Response();

        WebDriver driver;
        try{
            driver = setupEnvironment.setupSeleniumEnv(false);
        }
        catch (Exception ex){
            response.setStatus(false);
            response.setMessage("Failed to establish connection with firefox driver.");
            return response;
        }

        String group;
        ArrayList<Journal> journals;
        JournalBatch journalBatch;
        ArrayList<JournalBatch> journalBatches = new ArrayList<>();

        String businessDate =  journalSyncJobType.getConfiguration().timePeriod;
        String fromDate = journalSyncJobType.getConfiguration().fromDate;
        String toDate = journalSyncJobType.getConfiguration().toDate;

        try {
            if (!setupEnvironment.loginOHRA(driver, Constants.OHRA_LINK, account)){
                driver.quit();
                response.setStatus(false);
                response.setMessage("Invalid username and password.");
                return response;
            }
            try {
                WebDriverWait wait = new WebDriverWait(driver, 5);
                wait.until(ExpectedConditions.alertIsPresent());
            }
            catch (Exception ignored) { }


            for (CostCenter costCenter : costCentersLocation) {
                journalBatch = new JournalBatch();
                journals = new ArrayList<>();

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

                        response.setStatus(false);
                        response.setMessage(dateResponse.getMessage());
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

                            ItemGroup itemGroup = conversions.checkItemGroupExistence(itemGroups, td.getText().strip());

                            if (!itemGroup.getChecked()) {
                                continue;
                            }

                            if(journalSyncJobType.getConfiguration().consumptionConfiguration.consumptionPerGroup.equals("OverGroups"))
                                group = itemGroup.getOverGroup();
                            else
                                group = itemGroup.getItemGroup();

                            for (int j = 0; j < cols.size(); j++) {
                                transferDetails.put(columns.get(j), cols.get(j).getText().strip());
                            }

                            Journal journal = new Journal();
                            float cost = conversions.convertStringToFloat((String) transferDetails.get("actual_usage"));

                            journals = journal.checkExistence(journals, group, 0,cost, 0, 0);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                journalBatch.setCostCenter(costCenter);
                journalBatch.setConsumption(journals);
                journalBatches.add(journalBatch);
            }

            driver.quit();

            response.setStatus(true);
            response.setJournalBatches(journalBatches);
            return response;

        } catch (Exception e) {
            e.printStackTrace();
            driver.quit();
            response.setStatus(false);

            response.setMessage("Failed to get consumption entries from Oracle Hospitality.");
            return response;
        }
    }


    public ArrayList<JournalBatch> saveJournalData(ArrayList<JournalBatch> journalBatches, SyncJobType syncJobType, SyncJob syncJob,
                                                  String businessDate, String fromDate, ArrayList<OverGroup> overGroups,
                                                   ArrayList<ItemGroup> itemGroups){
        ArrayList<SyncJobData> addedJournals;
        ArrayList<JournalBatch> addedJournalBatches = new ArrayList<>();
        ArrayList<Journal> journals;
        CostCenter costCenter;

        for (JournalBatch batch : journalBatches) {
            addedJournals = new ArrayList<>();
            journals = batch.getConsumption();
            costCenter = batch.getCostCenter();

            for (Journal journal : journals) {
                if (costCenter.costCenterReference.equals("")){
                    costCenter.costCenterReference = costCenter.costCenter;
                }

                // check zero entries (not needed)
                if (journal.getTotalCost() != 0){
                    HashMap<String, String> costData = new HashMap<>();

                    if(!syncJobType.getConfiguration().consumptionConfiguration.consumptionPerGroup.equals("OverGroups")){
                        ItemGroup itemGroup = conversions.checkItemGroupExistence(itemGroups, journal.getOverGroup());

                        costData.put("inventoryAccount", itemGroup.getInventoryAccount());
                        costData.put("expensesAccount", itemGroup.getExpensesAccount());
                    }else {
                        OverGroup oldOverGroupData = conversions.checkOverGroupExistence(overGroups, journal.getOverGroup());

                        if (oldOverGroupData.getChecked() && !oldOverGroupData.getInventoryAccount().equals("")
                                && !oldOverGroupData.getExpensesAccount().equals("")){
                            costData.put("inventoryAccount", oldOverGroupData.getInventoryAccount());
                            costData.put("expensesAccount", oldOverGroupData.getExpensesAccount());
                        }else
                            continue;
                    }

                    syncJobDataService.prepareAnalysis(costData, syncJobType.getConfiguration(), costCenter, null, null);

                    String transactionDate = conversions.getTransactionDate(businessDate, fromDate);
                    costData.put("accountingPeriod", transactionDate.substring(2,6));
                    costData.put("transactionDate", transactionDate);

                    costData.put("totalCr", String.valueOf(conversions.roundUpFloat(journal.getTotalCost())));
                    costData.put("totalDr", String.valueOf(conversions.roundUpFloat(journal.getTotalCost()) * -1));

                    costData.put("fromCostCenter", costCenter.costCenter);
                    costData.put("fromAccountCode", costCenter.accountCode);

                    costData.put("toCostCenter", costCenter.costCenter);
                    costData.put("toAccountCode", costCenter.accountCode);

                    costData.put("fromLocation", costCenter.accountCode);
                    costData.put("toLocation", costCenter.accountCode);

                    String description = "F " + costCenter.costCenterReference + " " + journal.getOverGroup();
                    if (description.length() > 50){
                        description = description.substring(0, 50);
                    }

                    costData.put("description", description);

                    if(costCenter.costCenterReference.equals(""))
                        costData.put("transactionReference", "Consumption");
                    else
                        costData.put("transactionReference", costCenter.costCenterReference);

                    costData.put("overGroup", journal.getOverGroup());

                    SyncJobData syncJobData = new SyncJobData(costData, Constants.RECEIVED, "", new Date(),
                            syncJob.getId());
                    syncJobDataRepo.save(syncJobData);

                    addedJournals.add(syncJobData);
                }
            }

            if(addedJournals.size() >0){
                batch.setConsumptionData(addedJournals);
                addedJournalBatches.add(batch);
            }
        }
        return addedJournalBatches;
    }
}
