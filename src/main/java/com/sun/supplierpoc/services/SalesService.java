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
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class SalesService {
    @Autowired
    SyncJobDataRepo syncJobDataRepo;
    @Autowired
    InvoiceController invoiceController;

    private Conversions conversions = new Conversions();
    private SetupEnvironment setupEnvironment = new SetupEnvironment();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public Response getSalesData(SyncJobType salesSyncJobType, ArrayList<CostCenter> costCenters,
                                 ArrayList<CostCenter> costCentersLocation, ArrayList<MajorGroup> majorGroups,
                                 ArrayList<Tender> includedTenders, Account account) {

        Response response = new Response();

        String timePeriod = salesSyncJobType.getConfiguration().getTimePeriod();

        WebDriver driver = null;
        try {
            driver = setupEnvironment.setupSeleniumEnv(false);
        } catch (Exception ex) {
            response.setStatus(false);
            response.setMessage("Failed to establish connection with firefox driver.");
            response.setEntries(new ArrayList<>());
            return response;
        }

        try {
            if (!setupEnvironment.loginOHRA(driver, Constants.OHRA_LOGIN_LINK, account)) {
                driver.quit();

                response.setStatus(false);
                response.setMessage("Invalid username and password.");
                response.setEntries(new ArrayList<>());
                return response;
            }

            // Wait to make sure user info is set
            try {
                WebDriverWait wait = new WebDriverWait(driver, 3);
                wait.until(ExpectedConditions.alertIsPresent());

                Alert al = driver.switchTo().alert();
                al.accept();
            } catch (Exception Ex) {
                System.out.println("No alert exits");
            }

            for (CostCenter costCenter : costCenters) {
                // check if cost center has location mapping
                CostCenter costCenterLocation = conversions.checkCostCenterExistence(costCentersLocation, costCenter.costCenter, false);

                if (!costCenterLocation.checked) {
                    continue;
                }

                // Get tender
                Response tenderResponse = getSalesTenders(costCenterLocation.locationName, timePeriod, costCenter,
                        includedTenders, driver);
                if (!tenderResponse.isStatus()) {
                    if (tenderResponse.getMessage().equals(Constants.INVALID_LOCATION)) {
                        continue;
                    }
                    response.setStatus(false);
                    response.setMessage(tenderResponse.getMessage());
                    return response;
                }

                // Get taxes
                Response taxResponse = getSalesTaxes(costCenterLocation.locationName, timePeriod, costCenter, driver);
                if (!taxResponse.isStatus()) {
                    if (taxResponse.getMessage().equals(Constants.INVALID_LOCATION)) {
                        continue;
                    }
                    response.setStatus(false);
                    response.setMessage(taxResponse.getMessage());
                    return response;
                }

                // Get over group gross
                Response overGroupGrossResponse = getSalesOverGroupGross(costCenterLocation.locationName, timePeriod,
                        costCenter, majorGroups, driver);
                if (!overGroupGrossResponse.isStatus()) {
                    if (overGroupGrossResponse.getMessage().equals(Constants.INVALID_LOCATION)) {
                        continue;
                    }
                    response.setStatus(false);
                    response.setMessage(overGroupGrossResponse.getMessage());
                    return response;
                }

                // Set Debit Entries (Tenders)
                response.getSalesTender().addAll(tenderResponse.getSalesTender());

                // Set Debit Entries (Taxes And overGroupsGross)
                response.getSalesTax().addAll(taxResponse.getSalesTax());
                response.getSalesMajorGroupGross().addAll(overGroupGrossResponse.getSalesMajorGroupGross());

            }
            driver.quit();

            response.setStatus(true);
            response.setMessage("");
        } catch (Exception ex) {
            driver.quit();

            response.setStatus(false);
            response.setMessage("Failed to get sales entries");
            response.setEntries(new ArrayList<>());
        }

        return response;
    }

    public Response getSalesTenders(String location, String businessDate, CostCenter costCenter,
                                    ArrayList<Tender> includedTenders, WebDriver driver) {
        Response response = new Response();
        ArrayList<Tender> tenders = new ArrayList<>();

        if (!driver.getCurrentUrl().equals(Constants.TENDERS_REPORT_LINK)) {
            driver.get(Constants.TENDERS_REPORT_LINK);
        }

        try {
            WebDriverWait wait = new WebDriverWait(driver, 20);
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("loadingFrame")));
        } catch (Exception Ex) {
            System.out.println("There is no loader");
        }

        Select selectLocation = new Select(driver.findElement(By.id("locationData")));
        try {
            selectLocation.selectByVisibleText(location);
        } catch (Exception e) {
            response.setStatus(false);
            response.setMessage(Constants.INVALID_LOCATION);
            response.setEntries(new ArrayList<>());
            return response;
        }

        // Wait until location value changed
        String selectLocationOption = selectLocation.getFirstSelectedOption().getText().strip();
        while (!selectLocationOption.equals(location)) {
            try {
                selectLocation.selectByVisibleText(location);
            } catch (Exception e) {
                response.setStatus(false);
                response.setMessage(Constants.INVALID_LOCATION);
                response.setEntries(new ArrayList<>());
                return response;
            }
        }

        Select selectBusinessDate = new Select(driver.findElement(By.id("calendarData")));
        try {
            selectBusinessDate.selectByVisibleText(businessDate);
        } catch (Exception e) {
            driver.quit();

            response.setStatus(false);
            response.setMessage(Constants.INVALID_BUSINESS_DATE);
            response.setEntries(new ArrayList<>());
            return response;
        }

        // Wait until business date value changed
        String selectBusinessDateOption = selectBusinessDate.getFirstSelectedOption().getText().strip();
        while (!selectBusinessDateOption.equals(businessDate)) {
            try {
                selectBusinessDate.selectByVisibleText(businessDate);
            } catch (Exception e) {
                driver.quit();

                response.setStatus(false);
                response.setMessage(Constants.INVALID_BUSINESS_DATE);
                response.setEntries(new ArrayList<>());
                return response;
            }
            continue;
        }

        driver.findElement(By.id("Run Report")).click();

        String tenderReportLink = Constants.OHRA_LINK +
                "/finengine/reportRunAction.do?rptroot=19&reportID=TendersDailyDetail&method=run";

        try {
            driver.get(tenderReportLink);

            List<WebElement> rows = driver.findElements(By.tagName("tr"));
            if (rows.size() < 5) {
                response.setStatus(true);
                response.setMessage("There is no tender entries in this location");
                response.setSalesTender(new ArrayList<>());

                return response;
            }
            ArrayList<String> columns = setupEnvironment.getTableColumns(rows, false, 5);

            for (int i = 7; i < rows.size(); i++) {
                Tender tender = new Tender();
                WebElement row = rows.get(i);
                List<WebElement> cols = row.findElements(By.tagName("td"));

                if (cols.size() != columns.size()) {
                    continue;
                }

                // Check if tender exists
                Tender tenderData = conversions.checkTenderExistence(includedTenders, cols.get(0).getText().strip());
                if (!tenderData.isChecked()) {
                    continue;
                }

                tender.setTender(tenderData.getTender());
                tender.setCostCenter(costCenter);
                tender.setAccount(tenderData.getAccount());
                tender.setTotal(conversions.convertStringToFloat(cols.get(1).getText().strip()));

                tenders.add(tender);
            }

            response.setStatus(true);
            response.setMessage("");
            response.setSalesTender(tenders);

        } catch (Exception e) {
            driver.quit();
            response.setStatus(false);
            response.setMessage(e.getMessage());
        }

        return response;
    }

    public Response getSalesTaxes(String location, String businessDate, CostCenter costCenter, WebDriver driver) {
        Response response = new Response();

        ArrayList<Tax> salesTax = new ArrayList<>();

        driver.get(Constants.TAXES_REPORT_LINK);

        try {
            WebDriverWait wait = new WebDriverWait(driver, 20);
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("loadingFrame")));
        } catch (Exception Ex) {
            System.out.println("There is no loader");
        }

        Select selectLocation = new Select(driver.findElement(By.id("locationData")));
        try {
            selectLocation.selectByVisibleText(location);
        } catch (Exception e) {
            response.setStatus(false);
            response.setMessage(Constants.INVALID_LOCATION);
            response.setEntries(new ArrayList<>());
            return response;
        }

        // Wait until location value changed
        String selectLocationOption = selectLocation.getFirstSelectedOption().getText().strip();
        while (!selectLocationOption.equals(location)) {
            selectLocationOption = selectLocation.getFirstSelectedOption().getText().strip();
        }

        Select selectBusinessDate = new Select(driver.findElement(By.id("calendarData")));
        try {
            selectBusinessDate.selectByVisibleText(businessDate);
        } catch (Exception e) {
            driver.quit();

            response.setStatus(false);
            response.setMessage(Constants.INVALID_BUSINESS_DATE);
            response.setEntries(new ArrayList<>());
            return response;
        }

        // Wait until business date value changed
        String selectBusinessDateOption = selectBusinessDate.getFirstSelectedOption().getText().strip();
        while (!selectBusinessDateOption.equals(businessDate)) {
            selectBusinessDateOption = selectBusinessDate.getFirstSelectedOption().getText().strip();
        }

        driver.findElement(By.id("Run Report")).click();

        String taxReportLink = Constants.OHRA_LINK +
                "/finengine/reportRunAction.do?rptroot=18&reportID=TaxesDailyDetail&method=run";

        try {
            driver.get(taxReportLink);

            List<WebElement> rows = driver.findElements(By.tagName("tr"));
            if (rows.size() < 5) {
                response.setStatus(true);
                response.setMessage("There is no tax entries in this location");
                response.setSalesTax(new ArrayList<>());

                return response;
            }

            ArrayList<String> columns = setupEnvironment.getTableColumns(rows, false, 5);

            for (int i = 6; i < rows.size(); i++) {
                WebElement row = rows.get(i);
                List<WebElement> cols = row.findElements(By.tagName("td"));

                if (cols.size() != columns.size()) {
                    continue;
                }
                WebElement td = cols.get(0);
                if (td.getText().equals("Total Taxes:")) {
                    Tax tax = new Tax();
                    tax.setTax("Total Tax");
                    tax.setTotal(conversions.convertStringToFloat(cols.get(1).getText().strip()));
                    tax.setCostCenter(costCenter);
                    salesTax.add(tax);
                    break;
                }
            }

            response.setStatus(true);
            response.setMessage("");
            response.setSalesTax(salesTax);
            response.setEntries(new ArrayList<>());

        } catch (Exception e) {
            driver.quit();

            response.setStatus(false);
            response.setMessage(e.getMessage());
            response.setEntries(new ArrayList<>());
        }

        return response;
    }

    public Response getSalesOverGroupGross(String location, String businessDate, CostCenter costCenter,
                                           ArrayList<MajorGroup> majorGroups, WebDriver driver) {
        Response response = new Response();
        ArrayList<Journal> majorGroupsGross = new ArrayList<>();

        driver.get(Constants.OVER_GROUP_GROSS_REPORT_LINK);

        try{
            WebDriverWait wait = new WebDriverWait(driver, 20);
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("loadingFrame")));
        } catch (Exception Ex) {
            System.out.println("There is no loader");
        }

        Select selectLocation = new Select(driver.findElement(By.id("locationData")));
        try {
            selectLocation.selectByVisibleText(location);
        } catch (Exception e) {
            response.setStatus(false);
            response.setMessage(Constants.INVALID_LOCATION);
            response.setEntries(new ArrayList<>());
            return response;
        }

        // Wait until location value changed
        String selectLocationOption = selectLocation.getFirstSelectedOption().getText().strip();
        while (!selectLocationOption.equals(location)) {
            selectLocationOption = selectLocation.getFirstSelectedOption().getText().strip();
        }

        Select selectBusinessDate = new Select(driver.findElement(By.id("calendarData")));
        try {
            selectBusinessDate.selectByVisibleText(businessDate);
        } catch (Exception e) {
            driver.quit();

            response.setStatus(false);
            response.setMessage(Constants.INVALID_BUSINESS_DATE);
            response.setEntries(new ArrayList<>());
            return response;
        }

        // Wait until business date value changed
        String selectBusinessDateOption = selectBusinessDate.getFirstSelectedOption().getText().strip();
        while (!selectBusinessDateOption.equals(businessDate)) {
            selectBusinessDateOption = selectBusinessDate.getFirstSelectedOption().getText().strip();
        }

        driver.findElement(By.id("Run Report")).click();

//        String overGroupGrossLink = Constants.OHRA_LINK +
//                "/finengine/reportRunAction.do?rptroot=46&reportID=SalesMixItemsSummary&method=run";

        String overGroupGrossLink = Constants.OHRA_LINK +
                "/finengine/reportRunAction.do?rptroot=15&reportID=SalesMixDailyDetail&method=run";

        try {
            driver.get(overGroupGrossLink);

            List<WebElement> rows = driver.findElements(By.tagName("tr"));

            if (rows.size() <= 11){
                response.setStatus(true);
                response.setMessage("There is no new entries");
                response.setSalesMajorGroupGross(majorGroupsGross);
            }
            ArrayList<String> columns = setupEnvironment.getTableColumns(rows, false, 5);

            for (int i = 7; i < rows.size(); i++) {
                WebElement row = rows.get(i);
                List<WebElement> cols = row.findElements(By.tagName("td"));

                if (cols.size() != columns.size()) {
                    continue;
                }

                if (columns.indexOf("group") != -1){
                    WebElement td = cols.get(columns.indexOf("group"));
                    MajorGroup majorGroup = conversions.checkMajorGroupExistence(majorGroups, td.getText().strip().toLowerCase());

                    if (!majorGroup.getChecked()) {
                        continue;
                    }

                    Journal journal = new Journal();
                    float majorGroupGross = conversions.convertStringToFloat(cols.get(columns.indexOf("sales_less_item_disc")).getText().strip());

                    majorGroupsGross = journal.checkExistence(majorGroupsGross, majorGroup
                            , 0, majorGroupGross, 0, 0, costCenter);
                }else{
                    driver.quit();
                    response.setStatus(false);
                    response.setMessage("Failed to get majorGroup gross entries, Please contact support team.");
                    response.setEntries(new ArrayList<>());
                }
            }

            response.setStatus(true);
            response.setMessage("");
            response.setSalesMajorGroupGross(majorGroupsGross);

        } catch (Exception e) {
            driver.quit();

            response.setStatus(false);
            response.setMessage(e.getMessage());
            response.setEntries(new ArrayList<>());
        }

        return response;
    }

    public ArrayList<SyncJobData> saveSalesData(Response salesResponse, SyncJob syncJob, SyncJobType syncJobType) {
        float totalTender = 0;
        float totalTax = 0;
        float totalOverGroupNet = 0;

        ArrayList<DifferentCostCenter> differentCostCenters = new ArrayList<>();

        ArrayList<SyncJobData> addedSales = new ArrayList<>();


        int year = Calendar.getInstance().get(Calendar.YEAR);
        int month = Calendar.getInstance().get(Calendar.MONTH);

        // 01072020
        String transactionDate;

        SimpleDateFormat simpleformat = new SimpleDateFormat("ddMMy");
        transactionDate = simpleformat.format(new Date());

        // Need to test it later
        if (syncJobType.getConfiguration().getTimePeriod().equals("Month to Date")){
            transactionDate = "01" + String.valueOf(month - 1) + String.valueOf(year);
        }

//        transactionDate = "01062020";

        // Save tenders {Debit}
        ArrayList<Tender> tenders = salesResponse.getSalesTender();
        for (int i = 0; i < tenders.size(); i++) {
            Tender tender = tenders.get(i);

            HashMap<String, String> tenderData = new HashMap<>();

            tenderData.put("transactionDate", transactionDate);

            tenderData.put("totalDr", String.valueOf(Math.round(tender.getTotal()) * -1));

            tenderData.put("from_cost_center", tender.getCostCenter().costCenter);
            tenderData.put("from_account_code", tender.getCostCenter().accountCode);

            tenderData.put("to_cost_center", tender.getCostCenter().costCenter);
            tenderData.put("to_account_code", tender.getCostCenter().accountCode);

            tenderData.put("transactionReference", "Tender Transaction Reference");

            tenderData.put("expensesAccount", tender.getAccount());

            String description = "Sales F " + tender.getCostCenter().costCenterReference + " " + tender.getTender();
            if (description.length() > 50){
                description = description.substring(0, 50);
            }

            tenderData.put("description", description);

            SyncJobData syncJobData = new SyncJobData(tenderData, Constants.RECEIVED, "", new Date(),
                    syncJob.getId());
            syncJobDataRepo.save(syncJobData);
            addedSales.add(syncJobData);

            DifferentCostCenter differentCostCenter = new DifferentCostCenter();
            float tenderTotal = tender.getTotal();
            differentCostCenters = differentCostCenter.checkExistence(differentCostCenters, tender.getCostCenter(),
                    tenderTotal, 0, 0);
        }

        // Save taxes {Credit}
        ArrayList<Tax> taxes = salesResponse.getSalesTax();
        for (int i = 0; i < taxes.size(); i++) {
            Tax tax = taxes.get(i);
            HashMap<String, String> taxData = new HashMap<>();

            taxData.put("transactionDate", transactionDate);

            taxData.put("totalCr", String.valueOf(Math.round(tax.getTotal())));

            taxData.put("from_cost_center", tax.getCostCenter().costCenter);
            taxData.put("from_account_code", tax.getCostCenter().accountCode);

            taxData.put("to_cost_center", tax.getCostCenter().costCenter);
            taxData.put("to_account_code", tax.getCostCenter().accountCode);

            taxData.put("transactionReference", "Tender Transaction Reference");

            // Vat out account
            String vatOut = syncJobType.getConfiguration().getVatOut();
            taxData.put("inventoryAccount", vatOut);

            String description = "Sales F " + tax.getCostCenter().costCenterReference + " " + tax.getTax();
            if (description.length() > 50){
                description = description.substring(0, 50);
            }

            taxData.put("description", description);

            SyncJobData syncJobData = new SyncJobData(taxData, Constants.RECEIVED, "", new Date(),
                    syncJob.getId());
            syncJobDataRepo.save(syncJobData);
            addedSales.add(syncJobData);

            DifferentCostCenter differentCostCenter = new DifferentCostCenter();
            float tenderTotal = tax.getTotal();
            differentCostCenters = differentCostCenter.checkExistence(differentCostCenters, tax.getCostCenter(), 0,
                    tenderTotal, 0);
        }

        // Save majorGroup {Credit}
        ArrayList<Journal> majorGroupsGross = salesResponse.getSalesMajorGroupGross();
        for (int i = 0; i < majorGroupsGross.size(); i++) {
            Journal majorGroupJournal = majorGroupsGross.get(i);
            HashMap<String, String> majorGroupData = new HashMap<>();

            majorGroupData.put("transactionDate", transactionDate);

            majorGroupData.put("totalCr", String.valueOf(Math.round(majorGroupJournal.getTotalCost())));

            majorGroupData.put("from_cost_center", majorGroupJournal.getCostCenter().costCenter);
            majorGroupData.put("from_account_code", majorGroupJournal.getCostCenter().accountCode);

            majorGroupData.put("to_cost_center", majorGroupJournal.getCostCenter().costCenter);
            majorGroupData.put("to_account_code", majorGroupJournal.getCostCenter().accountCode);

            majorGroupData.put("transactionReference", "MG Transaction Reference");

            // Major Group account
            majorGroupData.put("inventoryAccount", majorGroupJournal.getMajorGroup().getAccount());

            String description = "Sales F " + majorGroupJournal.getCostCenter().costCenterReference + " " + majorGroupJournal.getMajorGroup().getMajorGroup();
            if (description.length() > 50){
                description = description.substring(0, 50);
            }

            majorGroupData.put("description", description);

            SyncJobData syncJobData = new SyncJobData(majorGroupData, Constants.RECEIVED, "", new Date(),
                    syncJob.getId());
            syncJobDataRepo.save(syncJobData);
            addedSales.add(syncJobData);

            DifferentCostCenter differentCostCenter = new DifferentCostCenter();
            float majorGroupGrossTotal = majorGroupJournal.getTotalCost();
            differentCostCenters = differentCostCenter.checkExistence(differentCostCenters, majorGroupJournal.getCostCenter(),
                    0, 0, majorGroupGrossTotal);
        }

        // Check if there is different {According to different result} per costCenter
        for (int i = 0; i < differentCostCenters.size(); i++) {
            DifferentCostCenter differentCostCenter = differentCostCenters.get(i);

            totalTender = differentCostCenter.getTotalTender();
            totalTax = differentCostCenter.getTotalTax();
            totalOverGroupNet = differentCostCenter.getTotalOverGroupNet();

            if ((totalOverGroupNet + totalTax) != totalTender) {
                HashMap<String, String> differentData = new HashMap<>();

                differentData.put("transactionDate", transactionDate);

                // {Debit} - ShortagePOS
                if ((totalOverGroupNet + totalTax) > totalTender) {
                    String cashShortagePOS = syncJobType.getConfiguration().getCashShortagePOS();
                    differentData.put("totalDr", String.valueOf(Math.round((totalOverGroupNet + totalTax) - totalTender)));
                    differentData.put("expensesAccount", cashShortagePOS);
                }
                // {Credit} - SurplusPOS
                else {
                    String cashSurplusPOS = syncJobType.getConfiguration().getCashSurplusPOS();
                    differentData.put("totalCr", String.valueOf(Math.round(totalTender - (totalOverGroupNet + totalTax))));
                    differentData.put("inventoryAccount", cashSurplusPOS);
                }

                differentData.put("from_cost_center", differentCostCenter.getCostCenter().costCenter);
                differentData.put("from_account_code", differentCostCenter.getCostCenter().accountCode);

                differentData.put("to_cost_center", differentCostCenter.getCostCenter().costCenter);
                differentData.put("to_account_code", differentCostCenter.getCostCenter().accountCode);

                differentData.put("transactionReference", "Tender Transaction Reference");

                String description = "Sales For " + differentCostCenter.getCostCenter().costCenterReference + " - different";
                if (description.length() > 50){
                    description = description.substring(0, 50);
                }

                differentData.put("description", description);

                SyncJobData syncJobData = new SyncJobData(differentData, Constants.RECEIVED, "", new Date(),
                        syncJob.getId());
                syncJobDataRepo.save(syncJobData);
                addedSales.add(syncJobData);
            }
        }
        return addedSales;
    }


}
