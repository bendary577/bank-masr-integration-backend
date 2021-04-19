package com.sun.supplierpoc.services;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.controllers.InvoiceController;
import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.models.configurations.CostCenter;
import com.sun.supplierpoc.models.configurations.FamilyGroup;
import com.sun.supplierpoc.models.configurations.MajorGroup;
import com.sun.supplierpoc.models.configurations.RevenueCenter;
import com.sun.supplierpoc.repositories.SyncJobDataRepo;
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
                    Journal journal = new Journal();
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

                    if (setupEnvironment.runReport(businessDate, fromDate, toDate, location, revenueCenter, driver, dateResponse)) {
                        if (dateResponse.getMessage().equals(Constants.WRONG_BUSINESS_DATE)) {
                            driver.quit();
                            response.setStatus(false);
                            response.setMessage(dateResponse.getMessage());
                            return response;
                        } else if (dateResponse.getMessage().equals(Constants.NO_INFO)) {
                            continue;
                        }
                    }

                    driver.get(Constants.CONSUMPTION_COSTOFGOODS_TABLE_LINK);

                    WebElement table = driver.findElement(By.xpath("/html/body/div[3]/table"));
                    List<WebElement> rows = table.findElements(By.tagName("tr"));

                    if (rows.size() < 5)
                        continue;

                    ArrayList<String> columns = setupEnvironment.getTableColumns(rows, false, 0);

                    MajorGroup majorGroup;
                    RevenueCenter MGRevenueCenter;
                    String majorGroupName = "";

                    for (int i = 2; i < rows.size(); i++) {

                        WebElement row = rows.get(i);
                        List<WebElement> cols = row.findElements(By.tagName("td"));

                        if (cols.size() != columns.size())
                            continue;

                        WebElement col;

                        col = cols.get(columns.indexOf("item_group"));
                        float majorGroupAmount = conversions.convertStringToFloat(cols.get(columns.indexOf("cogs")).getText());

                        if (col.getAttribute("class").equals("header_1")) {
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

                            // Credit line
                            journals = journal.checkExistence(journals, majorGroup,majorGroupAmount,
                                    costCenter, MGRevenueCenter, "", "C");

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

                                majorGroupAmount = conversions.convertStringToFloat(FGCols.get(columns.indexOf("cogs")).getText());

                                journals = journal.checkFGExistence(journals, majorGroup, familyGroup, majorGroupAmount
                                        , location, MGRevenueCenter, familyGroup.departmentCode);
                            }
                        }
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

    public ArrayList<JournalBatch> saveCostOfGoodsData(ArrayList<JournalBatch> journalBatches, SyncJobType syncJobType, SyncJob syncJob,
                                                       String businessDate, String fromDate) {
        ArrayList<SyncJobData> addedJournals;
        ArrayList<JournalBatch> addedJournalBatches = new ArrayList<>();
        ArrayList<Journal> journals;
        CostCenter costCenter;

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

                    if(journal.getDCMarker().equals("C")){ // Per cost center account
                        costData.put("totalCr", String.valueOf(conversions.roundUpFloat(journal.getTotalCost())));
                        costData.put("inventoryAccount", costCenter.accountCode);
                    }else{ // Per over group or FG per revenue center
                        costData.put("totalDr", String.valueOf(conversions.roundUpFloat(journal.getTotalCost() * -1)));
                        costData.put("expensesAccount", revenueCenter.getAccountCode());
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
}