package com.sun.supplierpoc.services;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.controllers.InvoiceController;
import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.models.configurations.*;
import com.sun.supplierpoc.repositories.SyncJobDataRepo;
import com.sun.supplierpoc.seleniumMethods.MicrosFeatures;
import com.sun.supplierpoc.seleniumMethods.SetupEnvironment;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Service
public class CostOfGoodsService {
    @Autowired
    SyncJobDataRepo syncJobDataRepo;
    @Autowired
    InvoiceController invoiceController;
    @Autowired
    private SyncJobDataService syncJobDataService;

    @Autowired
    private MicrosFeatures microsFeatures;

    private final Conversions conversions = new Conversions();
    private final SetupEnvironment setupEnvironment = new SetupEnvironment();

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public Response getJournalDataByRevenueCenter(SyncJobType journalSyncJobType, ArrayList<CostCenter> costCentersLocation,
                                                  ArrayList<MajorGroup> majorGroups, List<RevenueCenter> revenueCenters,
                                                  Account account) {
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

            for (CostCenter location : costCentersLocation) {
                journals = new ArrayList<>();
                journalBatch = new JournalBatch();
                if (!location.checked)
                    continue;

                for (RevenueCenter revenueCenter : revenueCenters) {
                    Response dateResponse = new Response();

                    if (!revenueCenter.isChecked()) {
                        continue;
                    }

                    if (!driver.getCurrentUrl().equals(Constants.CONSUMPTION_COSTOFGOODS_REPORT_LINK)) {
                        driver.get(Constants.CONSUMPTION_COSTOFGOODS_REPORT_LINK);
                        try {
                            WebDriverWait wait = new WebDriverWait(driver, 60);
                            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("loadingFrame")));
                        } catch (Exception ex) {
                            System.out.println(ex.getMessage());
                        }
                    }

                    // Check if this revenue center has order types
                    List<OrderType> orderTypes = revenueCenter.getOrderTypes();

                    if (orderTypes.size() != 0) {
                        for (OrderType orderType : orderTypes) {
                            if (!driver.getCurrentUrl().equals(Constants.CONSUMPTION_COSTOFGOODS_REPORT_LINK)) {
                                driver.get(Constants.CONSUMPTION_COSTOFGOODS_REPORT_LINK);
                                try {
                                    WebDriverWait wait = new WebDriverWait(driver, 60);
                                    wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("loadingFrame")));
                                } catch (Exception ex) {
                                    System.out.println(ex.getMessage());
                                }
                            }

                            if (setupEnvironment.runReportPerOrderType(businessDate, fromDate, toDate, location, revenueCenter, driver, dateResponse, orderType)) {
                                if (dateResponse.getMessage().equals(Constants.WRONG_BUSINESS_DATE)) {
                                    driver.quit();
                                    response.setStatus(false);
                                    response.setMessage(dateResponse.getMessage());
                                    return response;
                                } else {
                                    continue;
                                }
                            }

                            fetchCostOfGoodsRows(majorGroups, location, revenueCenter, orderType, journals, driver);
                        }
                    } else {
                        if (setupEnvironment.runReport(businessDate, fromDate, toDate, location, revenueCenter, driver, dateResponse)) {
                            if (dateResponse.getMessage().equals(Constants.WRONG_BUSINESS_DATE)) {
                                driver.quit();
                                response.setStatus(false);
                                response.setMessage(dateResponse.getMessage());
                                return response;
                            } else {
                                continue;
                            }
                        }

                        fetchCostOfGoodsRows(majorGroups, location, revenueCenter, null, journals, driver);
                    }
                }

                journalBatch.setLocation(location);
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
            response.setMessage("Failed to get cost of goods entries from Oracle Hospitality.");
            return response;
        }
    }

    private void fetchCostOfGoodsRows(ArrayList<MajorGroup> majorGroups, CostCenter location,
                                      RevenueCenter revenueCenter, OrderType orderType,
                                      ArrayList<Journal> journals, WebDriver driver) throws CloneNotSupportedException {
        try {
            Journal journal = new Journal();

            driver.get(Constants.CONSUMPTION_COSTOFGOODS_TABLE_LINK);

            WebElement table = driver.findElement(By.xpath("/html/body/div[3]/table"));
            List<WebElement> rows = table.findElements(By.tagName("tr"));

            if (rows.size() < 4)
                return;

            ArrayList<String> columns = setupEnvironment.getTableColumns(rows, false, 0);

            MajorGroup majorGroup;
            RevenueCenter MGRevenueCenter;
            String majorGroupName = "";
            float majorGroupAmountTotal = 0;

            for (int i = 2; i < rows.size(); i++) {

                WebElement row = rows.get(i);
                List<WebElement> cols = row.findElements(By.tagName("td"));

                if (cols.size() != columns.size())
                    continue;

                WebElement col;

                col = cols.get(columns.indexOf("item_group"));

                float majorGroupAmount = 0;

                if (col.getAttribute("class").equals("header_1")) {
                    majorGroupAmountTotal = 0;

                    majorGroupName = col.getText().strip().toLowerCase();
                    majorGroup = conversions.checkMajorGroupExistence(majorGroups, majorGroupName);

                    if (!majorGroup.getChecked()) {
                        continue;
                    }

                    MGRevenueCenter = conversions.checkRevenueCenterExistence(majorGroup.getRevenueCenters(), revenueCenter.getRevenueCenter());

//                if(orderType != null)
//                    MGRevenueCenter.setAccountCode(orderType.getAccount());

                    CostCenter costCenter = new CostCenter();
                    List<CostCenter> costCenters = majorGroup.getCostCenters();
                    for (CostCenter costCenter1 : costCenters) {
                        if (location.locationName.equals(costCenter1.locationName)) {
                            costCenter = costCenter1;
                        }
                    }

                    // Debit lines
                    for (int j = i + 1; j < rows.size(); j++) {
                        WebElement FGRow = rows.get(j);
                        List<WebElement> FGCols = FGRow.findElements(By.tagName("td"));
                        WebElement FGCol;

                        FGCol = FGCols.get(columns.indexOf("item_group"));
                        if (FGCol.getAttribute("class").equals("header_1")) {
                            i = j - 1;
                            break;
                        }

                        if (FGCols.size() != columns.size()) {
                            continue;
                        }

                        FamilyGroup familyGroup;

                        // Check if family group exists
                        String familyGroupName = FGCol.getText().strip().toLowerCase();
                        familyGroup = conversions.checkFamilyGroupExistence(majorGroup.getFamilyGroups()
                                , familyGroupName);

                        if (familyGroup.familyGroup.equals(""))
                            continue;

                        majorGroupAmount = Math.abs(conversions.convertStringToFloat(FGCols.get(columns.indexOf("cogs")).getText()));
                        majorGroupAmountTotal += majorGroupAmount;

                        journals = journal.checkFGExistence(journals, majorGroup, familyGroup, majorGroupAmount
                                , location, MGRevenueCenter, orderType, familyGroup.departmentCode);
                    }

                    // Credit line
                    journals = journal.checkExistence(journals, majorGroup, majorGroupAmountTotal,
                            costCenter, MGRevenueCenter, orderType, "", "C");
                }
            }
        } catch (Exception e) {
            e.getMessage();
        }
    }

    public ArrayList<JournalBatch> saveCostOfGoodsData(ArrayList<JournalBatch> journalBatches, SyncJobType syncJobType, SyncJob syncJob,
                                                       String businessDate, String fromDate) {
        ArrayList<SyncJobData> addedJournals;
        ArrayList<JournalBatch> addedJournalBatches = new ArrayList<>();
        ArrayList<Journal> journals;
        CostCenter costCenter;
        String syncPer = syncJobType.getConfiguration().syncPerGroup;

        for (JournalBatch batch : journalBatches) {
            addedJournals = new ArrayList<>();
            journals = batch.getConsumption();

            for (Journal journal : journals) {
                costCenter = journal.getCostCenter();
                FamilyGroup familyGroup = journal.getFamilyGroup();
                RevenueCenter revenueCenter = journal.getRevenueCenter();

                if (costCenter.costCenterReference.equals("")) {
                    costCenter.costCenterReference = journal.getCostCenter().costCenter;
                }

                // check zero entries (not needed)
                if (journal.getTotalCost() != 0) {
                    HashMap<String, Object> costData = new HashMap<>();

                    String transactionDate = conversions.getTransactionDate(businessDate, fromDate);
                    costData.put("accountingPeriod", transactionDate.substring(2, 6));
                    costData.put("transactionDate", transactionDate);

                    if (syncJobType.getConfiguration().exportFilePerLocation) {
                        syncJobDataService.prepareConsumptionAnalysis(revenueCenter, costData,
                                syncJobType.getConfiguration(), batch.getLocation(), familyGroup, null);
                    } else {
                        syncJobDataService.prepareConsumptionAnalysis(revenueCenter, costData,
                                syncJobType.getConfiguration(), costCenter, familyGroup, null);
                    }

                    if (journal.getDCMarker().equals("C")) { // Per cost center account
                        costData.put("totalCr", String.valueOf(conversions.roundUpFloat(journal.getTotalCost())));
                        costData.put("inventoryAccount", costCenter.accountCode);
                    } else { // Per over group or FG per revenue center
                        costData.put("totalDr", String.valueOf(conversions.roundUpFloat(journal.getTotalCost() * -1)));
                        if (journal.getOrderType() != null && !journal.getOrderType().getOrderType().equals("")) {
                            costData.put("expensesAccount", journal.getOrderType().getAccount());
                        } else if(syncPer.equals(Constants.MAJOR_GROUP)){
                            costData.put("expensesAccount", journal.getMajorGroup().getAccount());
                        }else{
                            costData.put("expensesAccount", revenueCenter.getAccountCode());
                        }
                    }

                    costData.put("fromLocation", batch.getLocation().accountCode);
                    costData.put("toLocation", batch.getLocation().accountCode);

                    if (costCenter.costCenterReference.equals(""))
                        costData.put("transactionReference", "COG");
                    else {
                        costData.put("transactionReference", batch.getLocation().costCenterReference);
                    }

                    String description = "";

                    if (familyGroup != null && !familyGroup.familyGroup.equals("")) {
                        description = journal.getMajorGroup().getMajorGroup() + " Cost-" + revenueCenter.getRevenueCenter();
                        if (journal.getOrderType() != null && !journal.getOrderType().getOrderType().equals(""))
                            description += " " + journal.getOrderType().getOrderType();
                    } else {
                        description = batch.getLocation().costCenterReference + " " + journal.getMajorGroup().getMajorGroup();
                    }

                    if (description.length() > 50) {
                        description = description.substring(0, 50);
                    }
                    costData.put("description", description);

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


    public Response getJournalDataByRevenueCenterVersion2(SyncJobType journalSyncJobType, ArrayList<CostCenter> costCentersLocation,
                                                          ArrayList<MajorGroup> majorGroups, List<RevenueCenter> revenueCenters,
                                                          Account account) {
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

            if (!microsFeatures.loginMicrosOHRA(driver, Constants.MICROS_V2_LINK, account)) {
                driver.quit();

                response.setStatus(false);
                response.setMessage("Invalid username and password.");
                response.setEntries(new ArrayList<>());
                return response;
            }

            for (CostCenter location : costCentersLocation) {
                journals = new ArrayList<>();
                journalBatch = new JournalBatch();
                if (!location.checked)
                    continue;

                for (RevenueCenter revenueCenter : revenueCenters) {
                    Response dateResponse = new Response();

                    if (!revenueCenter.isChecked()) {
                        continue;
                    }

                    if (!driver.getCurrentUrl().equals(Constants.CONSUMPTION_COSTOFGOODS_TABLE_LINK_MICROS)) {
                        driver.get(Constants.CONSUMPTION_COSTOFGOODS_TABLE_LINK_MICROS);
                        try {
                            WebDriverWait wait = new WebDriverWait(driver, 60);
                            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("loadingFrame")));
                        } catch (Exception ex) {
                            System.out.println(ex.getMessage());
                        }
                    }

                    // Check if this revenue center has order types
                    List<OrderType> orderTypes = revenueCenter.getOrderTypes();

                    if (orderTypes.size() != 0) {
                        for (OrderType orderType : orderTypes) {
                            if (!driver.getCurrentUrl().equals(Constants.CONSUMPTION_COSTOFGOODS_TABLE_LINK_MICROS)) {
                                driver.get(Constants.CONSUMPTION_COSTOFGOODS_TABLE_LINK_MICROS);
                                try {
                                    WebDriverWait wait = new WebDriverWait(driver, 60);
                                    wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("loadingFrame")));
                                } catch (Exception ex) {
                                    System.out.println(ex.getMessage());
                                }
                            }

                            dateResponse = microsFeatures.selectDateRangeMicros(businessDate, fromDate, toDate, location.locationName, revenueCenter.getRevenueCenter(), orderType.getOrderType(), driver);

                            if (!dateResponse.isStatus()) {
                                response.setStatus(false);
                                response.setMessage(dateResponse.getMessage());
                                return response;
                            }

                            try {
                                WebDriverWait wait = new WebDriverWait(driver, 3);
                                wait.until(ExpectedConditions.alertIsPresent());
                            } catch (Exception ex) {
                                System.out.println(ex.getMessage());
                            }

                            //RUN
                            driver.findElement(By.xpath("//*[@id=\"save-close-button\"]/button")).click();


                            // Validate Report Parameters
                            Response validateParameters = microsFeatures.checkReportParameters(driver, fromDate, toDate, businessDate, location.locationName);

                            if (!validateParameters.isStatus()) {
                                response.setStatus(false);
                                response.setMessage(validateParameters.getMessage());
                                return response;
                            }

                            fetchCostOfGoodsRowsVersion2(majorGroups, location, revenueCenter, orderType, journals, driver);
                        }
                    } else if(journalSyncJobType.getConfiguration().syncPerGroup.equals(Constants.MAJOR_GROUP)) {

                        if (!(microsFeatures.selectDateRangeMicros(businessDate, fromDate, toDate, location.locationName,
                                "", "", driver)).isStatus()) {
                            response.setStatus(false);
                            response.setMessage(dateResponse.getMessage());
                            return response;
                        }

                        try {
                            WebDriverWait wait = new WebDriverWait(driver, 3);
                            wait.until(ExpectedConditions.alertIsPresent());
                        } catch (Exception ex) {
                            System.out.println(ex.getMessage());
                        }

                        driver.findElement(By.xpath("//*[@id=\"save-close-button\"]/button")).click();


                        // Validate Report Parameters
                        Response validateParameters = microsFeatures.checkReportParameters(driver, fromDate, toDate, businessDate, location.locationName);

                        if (!validateParameters.isStatus()) {
                            response.setStatus(false);
                            response.setMessage(validateParameters.getMessage());
                            return response;
                        }

                        fetchCostOfGoodsRowsVersionTwoPerMajor(majorGroups, location, revenueCenter, journals, driver);
                        break;
                    }else{

                        if (!(microsFeatures.selectDateRangeMicros(businessDate, fromDate, toDate, location.locationName,
                                revenueCenter.getRevenueCenter(), "", driver)).isStatus()) {
                            response.setStatus(false);
                            response.setMessage(dateResponse.getMessage());
                            return response;
                        }

                        try {
                            WebDriverWait wait = new WebDriverWait(driver, 3);
                            wait.until(ExpectedConditions.alertIsPresent());
                        } catch (Exception ex) {
                            System.out.println(ex.getMessage());
                        }

                        driver.findElement(By.xpath("//*[@id=\"save-close-button\"]/button")).click();


                        // Validate Report Parameters
                        Response validateParameters = microsFeatures.checkReportParameters(driver, fromDate, toDate, businessDate, location.locationName);

                        if (!validateParameters.isStatus()) {
                            response.setStatus(false);
                            response.setMessage(validateParameters.getMessage());
                            return response;
                        }

                        fetchCostOfGoodsRowsVersion2(majorGroups, location, revenueCenter, null, journals, driver);
                        break;
                    }
                }

                journalBatch.setLocation(location);
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
            response.setMessage("Failed to get cost of goods entries from Oracle Hospitality.");
            return response;
        }
    }

    private void fetchCostOfGoodsRowsVersionTwoPerMajor(ArrayList<MajorGroup> majorGroups, CostCenter location,
                                                        RevenueCenter revenueCenter, ArrayList<Journal> journals, WebDriver driver) {
        try {
            Journal journal = new Journal();

//        driver.get(Constants.CONSUMPTION_COSTOFGOODS_TABLE_LINK);

            WebElement table = driver.findElement(By.xpath("//*[@id=\"standard_table_6520_0\"]/table"));
            List<WebElement> rows = table.findElements(By.tagName("tr"));

            if (rows.size() < 4)
                return;

            ArrayList<String> columns = setupEnvironment.getTableColumns(rows, true, 0);

            MajorGroup majorGroup;
            RevenueCenter MGRevenueCenter;
            String majorGroupName = "";
            float majorGroupAmountTotal = 0;

            for (int i = 2; i < rows.size(); i++) {

                WebElement row = rows.get(i);
                List<WebElement> cols = row.findElements(By.tagName("td"));

                if (cols.size() != columns.size())
                    continue;

                WebElement col;

                col = cols.get(columns.indexOf("name"));

                float majorGroupAmount = 0;

                if (col.getAttribute("class").
                        equals("oj-helper-text-align-left oj-table-data-cell oj-form-control-inherit")) {

                    majorGroupName = col.getText().strip().toLowerCase();
                    majorGroup = conversions.checkMajorGroupExistence(majorGroups, majorGroupName);

                    if (!majorGroup.getChecked()) {
                        continue;
                    }

//                    MGRevenueCenter = conversions.checkRevenueCenterExistence(majorGroup.getRevenueCenters(), revenueCenter.getRevenueCenter());
//
//                    CostCenter costCenter = new CostCenter();
//                    List<CostCenter> costCenters = majorGroup.getCostCenters();
//                    for (CostCenter costCenter1 : costCenters) {
//                        if (location.locationName.equals(costCenter1.locationName)) {
//                            costCenter = costCenter1;
//                        }
//                    }

//                    WebElement link = col.findElement(By.tagName("a"));
//                    if (link.getAttribute("class").
//                            equals("oj-component-icon oj-clickable-icon-nocontext oj-rowexpander-expand-icon")) {
//                        link.click();
//                    }
//
//                    table = driver.findElement(By.xpath("//*[@id=\"standard_table_6520_0\"]/table"));
//                    rows = table.findElements(By.tagName("tr"));

                    // Debit lines
//                    for (int j = i + 1; j < rows.size(); j++) {
//                        WebElement FGRow = rows.get(j);
//                        List<WebElement> FGCols = FGRow.findElements(By.tagName("td"));
//                        WebElement FGCol;
//
//                        FGCol = FGCols.get(columns.indexOf("name"));
//
//                        if (!FGCol.getAttribute("class").
//                                equals("oj-helper-text-align-left indent_3 oj-table-data-cell oj-form-control-inherit")) {
//                            i = j - 1;
//                            break;
//                        }
//
//                        if (FGCols.size() != columns.size()) {
//                            continue;
//                        }
//
//                        FamilyGroup familyGroup;
//
//                        // Check if family group exists
//                        String familyGroupName = FGCol.getText().strip().toLowerCase();
//                        familyGroup = conversions.checkFamilyGroupExistence(majorGroup.getFamilyGroups()
//                                , familyGroupName);
//
//                        if (familyGroup.familyGroup.equals(""))
//                            continue;

                        majorGroupAmount = Math.abs(conversions.convertStringToFloat(cols.get(columns.indexOf("margin_less_item_discounts")).getText()));
                        majorGroupAmountTotal += majorGroupAmount;

                        journals = journal.checkMGExistence(journals, majorGroup, majorGroupAmount, location);

//                        link = FGCol.findElement(By.tagName("a"));
//                        if (link.getAttribute("class").
//                                equals("oj-component-icon oj-clickable-icon-nocontext oj-rowexpander-collapse-icon")) {
//                            link.click();
//                        }
//
//                        table = driver.findElement(By.xpath("//*[@id=\"standard_table_6520_0\"]/table"));
//                        rows = table.findElements(By.tagName("tr"));
//
//                    }

                }
            }
            // Credit line
            journals = journal.checkExistence(journals, location, majorGroupAmountTotal, "C");
        } catch (Exception e) {
            e.getMessage();
        }
    }

    private void fetchCostOfGoodsRowsVersion2(ArrayList<MajorGroup> majorGroups, CostCenter location,
                                              RevenueCenter revenueCenter, OrderType orderType,
                                              ArrayList<Journal> journals, WebDriver driver) throws CloneNotSupportedException {
        try {
            Journal journal = new Journal();

//        driver.get(Constants.CONSUMPTION_COSTOFGOODS_TABLE_LINK);

            WebElement table = driver.findElement(By.xpath("//*[@id=\"standard_table_6520_0\"]/table"));
            List<WebElement> rows = table.findElements(By.tagName("tr"));

            if (rows.size() < 4)
                return;

            ArrayList<String> columns = setupEnvironment.getTableColumns(rows, true, 0);

            MajorGroup majorGroup;
            RevenueCenter MGRevenueCenter;
            String majorGroupName = "";
            float majorGroupAmountTotal = 0;

            for (int i = 2; i < rows.size(); i++) {

                WebElement row = rows.get(i);
                List<WebElement> cols = row.findElements(By.tagName("td"));

                if (cols.size() != columns.size())
                    continue;

                WebElement col;

                col = cols.get(columns.indexOf("name"));

                float majorGroupAmount = 0;

                //col.getAttribute("class").equals("header_1")
                //conversions.checkIfMajorGroup(col.getText().strip().toLowerCase())
                if (col.getAttribute("class").
                        equals("oj-helper-text-align-left oj-table-data-cell oj-form-control-inherit")) {

                    majorGroupAmountTotal = 0;

                    majorGroupName = col.getText().strip().toLowerCase();
                    majorGroup = conversions.checkMajorGroupExistence(majorGroups, majorGroupName);

                    if (!majorGroup.getChecked()) {
                        continue;
                    }

                    MGRevenueCenter = conversions.checkRevenueCenterExistence(majorGroup.getRevenueCenters(), revenueCenter.getRevenueCenter());

                    CostCenter costCenter = new CostCenter();
                    List<CostCenter> costCenters = majorGroup.getCostCenters();
                    for (CostCenter costCenter1 : costCenters) {
                        if (location.locationName.equals(costCenter1.locationName)) {
                            costCenter = costCenter1;
                        }
                    }

                    WebElement link = col.findElement(By.tagName("a"));
                    if (link.getAttribute("class").
                            equals("oj-component-icon oj-clickable-icon-nocontext oj-rowexpander-expand-icon")) {
                        link.click();
                    }

                    table = driver.findElement(By.xpath("//*[@id=\"standard_table_6520_0\"]/table"));
                    rows = table.findElements(By.tagName("tr"));

                    // Debit lines
                    for (int j = i + 1; j < rows.size(); j++) {
                        WebElement FGRow = rows.get(j);
                        List<WebElement> FGCols = FGRow.findElements(By.tagName("td"));
                        WebElement FGCol;

                        FGCol = FGCols.get(columns.indexOf("name"));

                        if (!FGCol.getAttribute("class").
                                equals("oj-helper-text-align-left indent_3 oj-table-data-cell oj-form-control-inherit")) {
                            i = j - 1;
                            break;
                        }

                        if (FGCols.size() != columns.size()) {
                            continue;
                        }

                        FamilyGroup familyGroup;

                        // Check if family group exists
                        String familyGroupName = FGCol.getText().strip().toLowerCase();
                        familyGroup = conversions.checkFamilyGroupExistence(majorGroup.getFamilyGroups()
                                , familyGroupName);

                        if (familyGroup.familyGroup.equals(""))
                            continue;

                        majorGroupAmount = Math.abs(conversions.convertStringToFloat(FGCols.get(columns.indexOf("margin_less_item_discounts")).getText()));
                        majorGroupAmountTotal += majorGroupAmount;

                        journals = journal.checkFGExistence(journals, majorGroup, familyGroup, majorGroupAmount
                                , location, MGRevenueCenter, orderType, familyGroup.departmentCode);

                        link = FGCol.findElement(By.tagName("a"));
                        if (link.getAttribute("class").
                                equals("oj-component-icon oj-clickable-icon-nocontext oj-rowexpander-collapse-icon")) {
                            link.click();
                        }

                        table = driver.findElement(By.xpath("//*[@id=\"standard_table_6520_0\"]/table"));
                        rows = table.findElements(By.tagName("tr"));

                    }

                    // Credit line
                    journals = journal.checkExistence(journals, majorGroup, majorGroupAmountTotal,
                            costCenter, MGRevenueCenter, orderType, "", "C");
                }
            }
        } catch (Exception e) {
            e.getMessage();
        }
    }

}


