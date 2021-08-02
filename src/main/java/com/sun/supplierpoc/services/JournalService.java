package com.sun.supplierpoc.services;


import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.controllers.InvoiceController;
import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.models.configurations.*;
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

    private final Conversions conversions = new Conversions();
    private final SetupEnvironment setupEnvironment = new SetupEnvironment();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /*
     * Get consumptions entries based on cost center
     * */
    public Response getJournalDataByCostCenter(SyncJobType journalSyncJobType, ArrayList<CostCenter> costCenters,
                                               ArrayList<ItemGroup> itemGroups, Account account) {
        Response response = new Response();

        WebDriver driver;
        try {
            driver = setupEnvironment.setupSeleniumEnv(false);
        } catch (Exception ex) {
            response.setStatus(false);
            response.setMessage("Failed to establish connection with firefox driver.");
            return response;
        }

        ArrayList<Journal> journals;
        JournalBatch journalBatch;
        ArrayList<JournalBatch> journalBatches = new ArrayList<>();

        String businessDate = journalSyncJobType.getConfiguration().timePeriod;
        String fromDate = journalSyncJobType.getConfiguration().fromDate;
        String toDate = journalSyncJobType.getConfiguration().toDate;

        try {
            if (!setupEnvironment.loginOHRA(driver, Constants.OHRA_LINK, account)) {
                driver.quit();

                response.setStatus(false);
                response.setMessage("Invalid username and password.");
                return response;
            }
            // just wait to make sure credentials of user saved to be able to move to another pages.
            try {
                WebDriverWait wait = new WebDriverWait(driver, 5);
                wait.until(ExpectedConditions.alertIsPresent());
            } catch (Exception e) {
                System.out.println("Waiting");
            }

            String journalUrl = "https://mte03-ohra-prod.hospitality.oracleindustry.com/finengine/reportAction.do?method=run&reportID=499";
            driver.get(journalUrl);

            try {
                WebDriverWait wait = new WebDriverWait(driver, 60);
                wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("loadingFrame")));
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }

            Response dateResponse = new Response();
            if (setupEnvironment.runReport(businessDate, fromDate, toDate, new CostCenter(), new RevenueCenter(), driver, dateResponse)) {
                driver.quit();

                if (dateResponse.getMessage().equals(Constants.WRONG_BUSINESS_DATE)) {
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

            if (rows.size() < 4) {
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

                if (rows.size() <= 3) {
                    continue;
                }

                columns = setupEnvironment.getTableColumns(rows, false, 3);

                String group;
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

                    if (journalSyncJobType.getConfiguration().syncPerGroup.equals("OverGroups"))
                        group = itemGroup.getOverGroup();
                    else
                        group = itemGroup.getItemGroup();

                    for (int j = 0; j < cols.size(); j++) {
                        transferDetails.put(columns.get(j), cols.get(j).getText().strip());
                    }

                    Journal journal = new Journal();
                    float cost = conversions.convertStringToFloat((String) transferDetails.get("actual_usage"));
                    journals = journal.checkExistence(journals, group, 0, cost, 0);
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
                                   ArrayList<ItemGroup> itemGroups, List<CostCenter> costCenters, Account account) {
        Response response = new Response();

        WebDriver driver;
        try {
            driver = setupEnvironment.setupSeleniumEnv(false);
        } catch (Exception ex) {
            response.setStatus(false);
            response.setMessage("Failed to establish connection with firefox driver.");
            return response;
        }

        ArrayList<Journal> journals;
        JournalBatch journalBatch;
        ArrayList<JournalBatch> journalBatches = new ArrayList<>();

        String businessDate = journalSyncJobType.getConfiguration().timePeriod;
        String fromDate = journalSyncJobType.getConfiguration().fromDate;
        String toDate = journalSyncJobType.getConfiguration().toDate;

        try {
            if (!setupEnvironment.loginOHRA(driver, Constants.OHRA_LINK, account)) {
                driver.quit();
                response.setStatus(false);
                response.setMessage("Invalid username and password.");
                return response;
            }
            try {
                WebDriverWait wait = new WebDriverWait(driver, 5);
                wait.until(ExpectedConditions.alertIsPresent());
            } catch (Exception ignored) {
            }

            for (CostCenter costCenter : costCentersLocation) {
                journalBatch = new JournalBatch();
                journals = new ArrayList<>();

                if (!costCenter.checked)
                    continue;

                if (!driver.getCurrentUrl().equals(Constants.CONSUMPTION_REPORT_LINK)) {
                    driver.get(Constants.CONSUMPTION_REPORT_LINK);

                    try {
                        WebDriverWait wait = new WebDriverWait(driver, 60);
                        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("loadingFrame")));
                    } catch (Exception ex) {
                        System.out.println(ex.getMessage());
                    }
                }

                Response dateResponse = new Response();
                if (setupEnvironment.runReport(businessDate, fromDate, toDate, costCenter, new RevenueCenter(), driver, dateResponse)) {
                    if (dateResponse.getMessage().equals(Constants.WRONG_BUSINESS_DATE)) {
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

                ArrayList<HashMap<String, String>> costExtensions = new ArrayList<>();

                for (int i = 6; i < rows.size(); i++) {

                    WebElement row = rows.get(i);
                    List<WebElement> cols = row.findElements(By.tagName("td"));
                    if (cols.size() != columns.size()) {
                        continue;
                    }

                    String extension = cols.get(0).findElement(By.tagName("div")).getAttribute("onclick").substring(7);
                    String costCenterName = cols.get(0).findElement(By.tagName("div")).getText();
                    int index = extension.indexOf('\'');
                    extension = extension.substring(0, index);
                    HashMap<String, String> map = new HashMap<>();
                    map.put("extension", extension);
                    map.put("costCenterName", costCenterName);
                    costExtensions.add(map);
                }

                String group;
                for (HashMap<String, String> extension : costExtensions) {
                    try {
                        driver.get(Constants.OHRA_LINK + extension.get("extension"));

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

                            if (journalSyncJobType.getConfiguration().syncPerGroup.equals("OverGroups"))
                                group = itemGroup.getOverGroup();
                            else
                                group = itemGroup.getItemGroup();

                            for (int j = 0; j < cols.size(); j++) {
                                transferDetails.put(columns.get(j), cols.get(j).getText().strip());
                            }

                            Journal journal = new Journal();

                            for (CostCenter tempCostCenter : costCenters) {
                                if (tempCostCenter.costCenter.equals(extension.get("costCenterName"))) {
                                    journal.setCostCenter(tempCostCenter);
                                    break;
                                }
                            }
                            float cost = conversions.convertStringToFloat((String) transferDetails.get("actual_usage"));

                            journals = journal.checkExistence(journals, group, 0, cost, 0);
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

    /*
     * Get consumptions entries based on location and item group
     * */

    public Response getJournalDataByItemGroup(SyncJobType journalSyncJobType,
                                              ArrayList<ConsumptionLocation> costCentersLocation,
                                              ArrayList<ConsumptionLocation> consumptionCostCenters, Account account) {
        Response response = new Response();

        WebDriver driver;
        try {
            driver = setupEnvironment.setupSeleniumEnv(false);
        } catch (Exception ex) {
            response.setStatus(false);
            response.setMessage("Failed to establish connection with firefox driver.");
            return response;
        }

        ArrayList<ConsumptionJournal> journals;
        ArrayList<ConsumptionJournal> costCenterJournals;
        JournalBatch journalBatch;
        ArrayList<JournalBatch> journalBatches = new ArrayList<>();

        String businessDate = journalSyncJobType.getConfiguration().timePeriod;
        String fromDate = journalSyncJobType.getConfiguration().fromDate;
        String toDate = journalSyncJobType.getConfiguration().toDate;

        try {
            if (!setupEnvironment.loginOHRA(driver, Constants.OHRA_LINK, account)) {
                driver.quit();
                response.setStatus(false);
                response.setMessage("Invalid username and password.");
                return response;
            }
            try {
                WebDriverWait wait = new WebDriverWait(driver, 5);
                wait.until(ExpectedConditions.alertIsPresent());
            } catch (Exception ignored) {
            }

            for (ConsumptionLocation consumptionLocation : costCentersLocation) {
                if(!consumptionLocation.check)
                    continue;

                journalBatch = new JournalBatch();
                journals = new ArrayList<>();
                costCenterJournals = new ArrayList<>();

                if (!driver.getCurrentUrl().equals(Constants.CONSUMPTION_REPORT_LINK)) {
                    driver.get(Constants.CONSUMPTION_REPORT_LINK);

                    try {
                        WebDriverWait wait = new WebDriverWait(driver, 60);
                        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("loadingFrame")));
                    } catch (Exception ex) {
                        System.out.println(ex.getMessage());
                    }
                }

                Response dateResponse = new Response();
                if (setupEnvironment.runReport(businessDate, fromDate, toDate, consumptionLocation.costCenter, new RevenueCenter(), driver, dateResponse)) {
                    if (dateResponse.getMessage().equals(Constants.WRONG_BUSINESS_DATE)) {
                        driver.quit();
                        response.setStatus(false);
                        response.setMessage(dateResponse.getMessage());
                        return response;
                    } else if (dateResponse.getMessage().equals(Constants.NO_INFO)) {
                        continue;
                    }
                }

                driver.get(Constants.CONSUMPTION_TABLE_LINK);
                WebElement table = driver.findElement(By.id("tableContainer"));

                List<WebElement> rows = table.findElements(By.tagName("tr"));

                if (rows.size() < 2)
                    continue;

                ArrayList<String> columns = setupEnvironment.getTableColumns(rows, false, 0);

                ArrayList<HashMap<String, String>> costExtensions = new ArrayList<>();

                for (int i = 3; i < rows.size(); i++) {

                    WebElement row = rows.get(i);
                    List<WebElement> cols = row.findElements(By.tagName("td"));
                    if (cols.size() != columns.size()) {
                        continue;
                    }

                    String extension = cols.get(0).findElement(By.tagName("div")).getAttribute("onclick").substring(7);
                    String costCenterName = cols.get(0).findElement(By.tagName("div")).getText();
                    if(!costCenterName.equals("")){
                        int index = extension.indexOf('\'');
                        extension = extension.substring(0, index);
                        HashMap<String, String> map = new HashMap<>();
                        map.put("extension", extension);
                        map.put("costCenterName", costCenterName);
                        costExtensions.add(map);
                    }
                }

                String group;
                float totalCost = 0;
                ConsumptionJournal journal = new ConsumptionJournal();

                // Check exception cost centers
                for (HashMap<String, String> extension : costExtensions) {
                    try {
                        driver.get(Constants.OHRA_LINK + extension.get("extension"));

                        float costCenterTotalCost = 0;
                        String costCenterName = extension.get("costCenterName");

                        boolean costCenterExist = false;
                        ConsumptionLocation consumptionCostCenter = conversions.checkConCostCenterExistence(consumptionCostCenters, costCenterName);
                        if(!consumptionCostCenter.accountCode.equals("")){
                            costCenterExist = true;
                        }

                        table = driver.findElement(By.id("tableContainer"));

                        rows = table.findElements(By.tagName("tr"));

                        if (rows.size() <= 3)
                            continue;
                        columns = setupEnvironment.getTableColumns(rows, false, 0);

                        for (int i = 3; i < rows.size(); i++) {
                            WebElement row = rows.get(i);
                            List<WebElement> cols = row.findElements(By.tagName("td"));

                            if (cols.size() != columns.size()) {
                                continue;
                            }

                            WebElement td = cols.get(columns.indexOf("item_group"));

                            ItemGroup itemGroup;
                            if(costCenterExist)
                                itemGroup = conversions.checkItemGroupExistence(consumptionCostCenter.itemGroups, td.getText().strip());
                            else
                                itemGroup = conversions.checkItemGroupExistence(consumptionLocation.itemGroups, td.getText().strip());

                            if(itemGroup.getItemGroup().equals(""))
                                continue;

                            group = itemGroup.getItemGroup();

                            journal.costCenter = consumptionCostCenter.costCenter;
                            float cost = conversions.convertStringToFloat(cols.get(columns.indexOf("actual_usage")).getText());
                            if(cost == 0)
                                continue;

                            if(costCenterExist){
                                costCenterTotalCost += cost;
                            }else{
                                totalCost += cost;
                            }

                            // Debit line
                            if(costCenterExist){
                                costCenterJournals = journal.checkJournalExistence(costCenterJournals, group, cost, itemGroup.getExpensesAccount(),
                                        consumptionCostCenter.costCenter, "D");
                            }else{
                                journals = journal.checkJournalExistence(journals, group, cost, itemGroup.getExpensesAccount(),
                                        consumptionLocation.costCenter, "D");
                            }
                        }

                        if(costCenterExist && costCenterTotalCost != 0){
                            costCenterJournals = journal.checkJournalExistence(costCenterJournals, "", costCenterTotalCost, consumptionCostCenter.accountCode,
                                    consumptionCostCenter.costCenter, "C");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                // Credit line
                if(totalCost != 0)
                    journals = journal.checkJournalExistence(journals, "", totalCost, consumptionLocation.accountCode,
                            consumptionLocation.costCenter, "C");

                journalBatch.setCostCenter(consumptionLocation.costCenter);
                journals.addAll(costCenterJournals);
                journalBatch.setConsumptionJournals(journals);
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

    public ArrayList<JournalBatch> saveJournalDataByItemGroup(ArrayList<JournalBatch> journalBatches, SyncJobType syncJobType, SyncJob syncJob,
                                                   String businessDate, String fromDate) {
        ArrayList<SyncJobData> addedJournals;
        ArrayList<JournalBatch> addedJournalBatches = new ArrayList<>();
        ArrayList<ConsumptionJournal> journals;
        CostCenter costCenter;

        for (JournalBatch batch : journalBatches) {
            addedJournals = new ArrayList<>();
            journals = batch.getConsumptionJournals();

            for (ConsumptionJournal journal : journals) {
                costCenter = journal.costCenter;

                if (costCenter.costCenterReference.equals("")) {
                    costCenter.costCenterReference = journal.costCenter.costCenter;
                }

                // check zero entries (not needed)
                if (journal.totalCost != 0) {
                    HashMap<String, Object> costData = new HashMap<>();

                    syncJobDataService.prepareConsumptionJournalAnalysis(costData, syncJobType.getConfiguration(), costCenter, journal.DCMarker);

                    String transactionDate = conversions.getTransactionDate(businessDate, fromDate);
                    costData.put("accountingPeriod", transactionDate.substring(2, 6));
                    costData.put("transactionDate", transactionDate);

                    if(journal.DCMarker.equals("C")){
                        costData.put("totalCr", String.valueOf(conversions.roundUpFloat(journal.totalCost)));
                        costData.put("inventoryAccount", journal.accountCode);
                        costData.put("fromLocation", costCenter.accountCode);
                    }else {
                        costData.put("totalDr", String.valueOf(conversions.roundUpFloat(journal.totalCost * -1)));
                        costData.put("expensesAccount", journal.accountCode);
                        costData.put("toLocation", journal.accountCode);
                    }

                    String description = "";
                    if(journal.overGroup.equals(""))
                        description = costCenter.costCenterReference;
                    else
                        description = journal.overGroup;

                    if (description.length() > 50) {
                        description = description.substring(0, 50);
                    }

                    costData.put("description", description);

                    if (costCenter.costCenterReference.equals(""))
                        costData.put("transactionReference", "CON");
                    else {
                        if(costCenter.location.locationName.equals(""))
                            costData.put("transactionReference", costCenter.costCenterReference);
                        else
                            costData.put("transactionReference", costCenter.location.locationName);
                    }

                    SyncJobData syncJobData = new SyncJobData(costData, Constants.RECEIVED, "", new Date(),
                            syncJob.getId());
                    syncJobDataRepo.save(syncJobData);

                    addedJournals.add(syncJobData);
                }
            }

            if (addedJournals.size() > 0) {
                batch.setConsumptionData(addedJournals);
                addedJournalBatches.add(batch);
            }
        }
        return addedJournalBatches;
    }


    public ArrayList<JournalBatch> saveJournalData(ArrayList<JournalBatch> journalBatches, SyncJobType syncJobType, SyncJob syncJob,
                                                   String businessDate, String fromDate) {
        ArrayList<SyncJobData> addedJournals;
        ArrayList<JournalBatch> addedJournalBatches = new ArrayList<>();
        ArrayList<Journal> journals;
        CostCenter costCenter;

        for (JournalBatch batch : journalBatches) {
            addedJournals = new ArrayList<>();
            journals = batch.getConsumption();

            for (Journal journal : journals) {
                if(journal.getCostCenter() != null){
                    costCenter = journal.getCostCenter();
                }else{
                    costCenter = batch.getCostCenter();
                }

                if (costCenter.costCenterReference.equals("")) {
                    costCenter.costCenterReference = costCenter.costCenter;
                }

                // check zero entries (not needed)
                if (journal.getTotalCost() != 0) {
                    HashMap<String, Object> costData = new HashMap<>();

                    costData.put("inventoryAccount", costCenter.accountCode);
                    costData.put("expensesAccount", costCenter.accountCode);

                    if (costCenter.location != null && !costCenter.location.locationName.equals("")) {
                        syncJobDataService.prepareAnalysis(costData, syncJobType.getConfiguration(), costCenter, null, null);
                    }

                    String transactionDate = conversions.getTransactionDate(businessDate, fromDate);
                    costData.put("accountingPeriod", transactionDate.substring(2, 6));
                    costData.put("transactionDate", transactionDate);

                    costData.put("totalCr", String.valueOf(conversions.roundUpFloat(journal.getTotalCost())));
                    costData.put("totalDr", String.valueOf(conversions.roundUpFloat(journal.getTotalCost() * -1)));

                    costData.put("fromCostCenter", batch.getCostCenter().costCenter);
                    costData.put("fromAccountCode", batch.getCostCenter().accountCode);

                    costData.put("toCostCenter", costCenter.costCenter);
                    costData.put("toAccountCode", costCenter.accountCode);

                    costData.put("fromLocation", costCenter.accountCode);
                    costData.put("toLocation", costCenter.accountCode);

                    String description = "";


                    description = batch.getLocation().costCenterReference;


                    if (description.length() > 50) {
                        description = description.substring(0, 50);
                    }

                    costData.put("description", description);

                    if (costCenter.costCenterReference.equals(""))
                        costData.put("transactionReference", "CON");
                    else {
                        costData.put("transactionReference", batch.getLocation().costCenterReference);
                    }

                    SyncJobData syncJobData = new SyncJobData(costData, Constants.RECEIVED, "", new Date(),
                            syncJob.getId());
                    syncJobDataRepo.save(syncJobData);

                    addedJournals.add(syncJobData);
                }
            }

            if (addedJournals.size() > 0) {
                batch.setConsumptionData(addedJournals);
                addedJournalBatches.add(batch);
            }
        }
        return addedJournalBatches;
    }

}
