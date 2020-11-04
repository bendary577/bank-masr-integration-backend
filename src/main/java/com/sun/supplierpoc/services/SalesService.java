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
import java.util.*;

@Service
public class SalesService {
    @Autowired
    SyncJobDataRepo syncJobDataRepo;
    @Autowired
    TransferService transferService;
    @Autowired
    InvoiceController invoiceController;

    private Conversions conversions = new Conversions();
    private SetupEnvironment setupEnvironment = new SetupEnvironment();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public Response getSalesData(SyncJobType salesSyncJobType, ArrayList<CostCenter> costCenters,
                                 ArrayList<CostCenter> costCentersLocation, ArrayList<MajorGroup> majorGroups,
                                 ArrayList<Tender> includedTenders,  ArrayList<Tax> includedTax,
                                 ArrayList<Discount> includedDiscount, ArrayList<ServiceCharge> includedServiceCharge,
                                 ArrayList<RevenueCenter> revenueCenters,
                                 Account account) {

        Response response = new Response();
        ArrayList<JournalBatch> journalBatches = new ArrayList<>();

        String timePeriod = salesSyncJobType.getConfiguration().getTimePeriod();
        String fromDate = salesSyncJobType.getConfiguration().getFromDate();
        String toDate = salesSyncJobType.getConfiguration().getToDate();
        String grossDiscountSales = salesSyncJobType.getConfiguration().getGrossDiscountSales();

        WebDriver driver;
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

            if (costCenters.size() > 0){
                for (CostCenter costCenter : costCenters) {
                    // check if cost center has location mapping
                    CostCenter costCenterLocation = conversions.checkCostCenterExistence(costCentersLocation, costCenter.costCenter, false);

                    if (!costCenterLocation.checked) {
                        continue;
                    }
                    callSalesFunction(salesSyncJobType, majorGroups, includedTenders, includedTax, includedDiscount,
                            includedServiceCharge, revenueCenters, timePeriod, fromDate, toDate, grossDiscountSales,
                            costCenter, costCenterLocation.location, journalBatches, account, driver, response);
                    if (!response.isStatus()){
                        return response;
                    }
                }
            }
            else {
                callSalesFunction(salesSyncJobType, majorGroups, includedTenders, includedTax, includedDiscount,
                        includedServiceCharge, revenueCenters, timePeriod, fromDate, toDate, grossDiscountSales,
                        new CostCenter(), "", journalBatches, account, driver, response);
                if (!response.isStatus()){
                    return response;
                }
            }

            driver.quit();

            response.setStatus(true);
            response.setMessage("");
            response.setJournalBatches(journalBatches);
        } catch (Exception ex) {
            driver.quit();

            response.setStatus(false);
            response.setMessage("Failed to get sales entries");
            response.setEntries(new ArrayList<>());
        }
        return response;
    }

    private void callSalesFunction(SyncJobType salesSyncJobType, ArrayList<MajorGroup> majorGroups,
                                   ArrayList<Tender> includedTenders, ArrayList<Tax> includedTax,
                                   ArrayList<Discount> includedDiscount, ArrayList<ServiceCharge> includedServiceCharge,
                                   ArrayList<RevenueCenter> revenueCenters, String timePeriod, String fromDate,
                                   String toDate, String grossDiscountSales, CostCenter costCenter, String location,
                                   ArrayList<JournalBatch> journalBatches, Account account,
                                   WebDriver driver, Response response){
        JournalBatch journalBatch = new JournalBatch();

        // Get tender
        Response tenderResponse = getSalesTenders(location, timePeriod, fromDate, toDate,
                costCenter, includedTenders, driver);
        if (checkSalesFunctionResponse(driver, response, tenderResponse)) return;

        // Get taxes
        Response taxResponse = getSalesTaxes(location, timePeriod, fromDate, toDate,
                costCenter, false, includedTax, driver);
        if (checkSalesFunctionResponse(driver, response, taxResponse)) return;

        // Get over group gross
        ArrayList<Journal> salesMajorGroupsGross = new ArrayList<>();
        if (revenueCenters.size() > 0 ){
            for (RevenueCenter rc : revenueCenters)
            {
                Response overGroupGrossResponse = getSalesOverGroupGross(rc, location,
                        timePeriod, fromDate, toDate, costCenter, majorGroups, grossDiscountSales, driver);
                if (!overGroupGrossResponse.isStatus()) {
                    if (overGroupGrossResponse.getMessage().equals(Constants.INVALID_LOCATION)) {
                        continue;
                    }
                    response.setStatus(false);
                    response.setMessage(overGroupGrossResponse.getMessage());
                    return;
                }

                salesMajorGroupsGross.addAll(overGroupGrossResponse.getSalesMajorGroupGross());
            }
        }
        else {
            Response overGroupGrossResponse = getSalesOverGroupGross(new RevenueCenter(), location,
                    timePeriod, fromDate, toDate, costCenter, majorGroups, grossDiscountSales, driver);
            if (checkSalesFunctionResponse(driver, response, overGroupGrossResponse)) return;

            salesMajorGroupsGross.addAll(overGroupGrossResponse.getSalesMajorGroupGross());
        }

        Response discountResponse = new Response();
        if ((salesSyncJobType.getConfiguration().getGrossDiscountSales().equals(Constants.SALES_GROSS)
                || account.getERD().equals(Constants.EXPORT_TO_SUN_ERD)) && includedDiscount.size() > 0){
            // Get discounts
            discountResponse = getSalesDiscount(location, timePeriod,
                    fromDate, toDate, costCenter, false, includedDiscount, driver);
            if (checkSalesFunctionResponse(driver, response, discountResponse)) return;
        }

        // Get serviceCharge
        Response serviceChargeResponse = new Response();
        if (includedServiceCharge.size() > 0){
            serviceChargeResponse = getTotalSalesServiceCharge(location, timePeriod,
                    fromDate, toDate, costCenter,  includedServiceCharge, driver);
            if (checkSalesFunctionResponse(driver, response, serviceChargeResponse)) return;
        }

        // Set Debit Entries (Tenders)
        journalBatch.setSalesTender(tenderResponse.getSalesTender());

        // Set Credit Entries (Taxes, overGroupsGross, Discount and Service charge)
        journalBatch.setSalesTax(taxResponse.getSalesTax());
        journalBatch.setSalesMajorGroupGross(salesMajorGroupsGross);
        journalBatch.setSalesDiscount(discountResponse.getSalesDiscount());
        journalBatch.setSalesServiceCharge(serviceChargeResponse.getSalesServiceCharge());

        // Calculate different
        journalBatch.setSalesDifferent(0.0);
        journalBatch.setCostCenter(costCenter);
        journalBatches.add(journalBatch);

        response.setStatus(true);
    }

    private boolean checkSalesFunctionResponse(WebDriver driver, Response response, Response overGroupGrossResponse) {
        if (!overGroupGrossResponse.isStatus()) {
            if (overGroupGrossResponse.getMessage().equals(Constants.INVALID_LOCATION)) {
                driver.quit();

                response.setStatus(false);
                response.setMessage(Constants.INVALID_LOCATION);
                response.setEntries(new ArrayList<>());
            }
            response.setStatus(false);
            response.setMessage(overGroupGrossResponse.getMessage());
            return true;
        }
        return false;
    }

    private Response getSalesTenders(String location, String businessDate, String fromDate, String toDate,
                                     CostCenter costCenter, ArrayList<Tender> includedTenders, WebDriver driver) {
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

        Response dateResponse = setupEnvironment.selectTimePeriodOHRA(businessDate, fromDate, toDate, location,
                "", driver);

        if (!dateResponse.isStatus()){
            response.setStatus(false);
            response.setMessage( dateResponse.getMessage());
            response.setSalesTender(tenders);
            return response;
        }

        driver.findElement(By.id("Run Report")).click();

        try {
            driver.get(Constants.TENDERS_TABLE_LINK);

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
            response.setMessage("Failed to get sales entries from Oracle Hospitality.");
        }

        return response;
    }

    private Response getSalesTaxes(String location, String businessDate, String fromDate, String toDate,
                                   CostCenter costCenter, boolean getTaxTotalFlag, ArrayList<Tax> includedTaxes,
                                   WebDriver driver) {
        Response response = new Response();

        ArrayList<Tax> salesTax = new ArrayList<>();

        driver.get(Constants.TAXES_REPORT_LINK);

        try {
            WebDriverWait wait = new WebDriverWait(driver, 20);
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("loadingFrame")));
        } catch (Exception Ex) {
            System.out.println("There is no loader");
        }

        Response dateResponse = setupEnvironment.selectTimePeriodOHRA(businessDate, fromDate, toDate, location,
                "", driver);

        if (!dateResponse.isStatus()){
            response.setStatus(false);
            response.setMessage( dateResponse.getMessage());
            response.setSalesTax(salesTax);
            return response;
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

                Tax tax = new Tax();

                if(getTaxTotalFlag){
                    WebElement td = cols.get(0);
                    if (td.getText().equals("Total Taxes:")) {
                        tax = conversions.checkTaxExistence(includedTaxes, "total");
                        if (!tax.isChecked()) {
                            break;
                        }
                        tax.setTax("Total Tax");
                        tax.setTotal(conversions.convertStringToFloat(cols.get(1).getText().strip()));
                        tax.setCostCenter(costCenter);
                        salesTax.add(tax);
                        break;
                    }
                }else{
                    // Check if tax exists
                    Tax taxData = conversions.checkTaxExistence(includedTaxes, cols.get(0).getText().strip());
                    if (!taxData.isChecked()) {
                        continue;
                    }
                    tax.setTax(taxData.getTax());
                    tax.setAccount(taxData.getAccount());
                    tax.setCostCenter(costCenter);
                    tax.setTotal(conversions.convertStringToFloat(cols.get(1).getText().strip()));
                    salesTax.add(tax);
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

    private Response getSalesOverGroupGross(RevenueCenter revenueCenter, String location, String businessDate,
                                            String fromDate, String toDate,
                                            CostCenter costCenter, ArrayList<MajorGroup> majorGroups,
                                            String grossDiscountSales, WebDriver driver) {
        Response response = new Response();
        ArrayList<Journal> majorGroupsGross = new ArrayList<>();

        driver.get(Constants.OVER_GROUP_GROSS_REPORT_LINK);

        try{
            WebDriverWait wait = new WebDriverWait(driver, 20);
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("loadingFrame")));
        } catch (Exception Ex) {
            System.out.println("There is no loader");
        }

        Response dateResponse = setupEnvironment.selectTimePeriodOHRA(businessDate, fromDate, toDate, location,
                revenueCenter.getRevenueCenter(), driver);

        if (!dateResponse.isStatus()){
            response.setStatus(false);
            response.setMessage( dateResponse.getMessage());
            response.setSalesMajorGroupGross(majorGroupsGross);
            return response;
        }

        driver.findElement(By.id("Run Report")).click();

        String overGroupGrossLink = Constants.OHRA_LINK +
                "/finengine/reportRunAction.do?rptroot=15&reportID=SalesMixDailyDetail&method=run";

        try {
            driver.get(overGroupGrossLink);

            List<WebElement> rows = driver.findElements(By.tagName("tr"));

            if (rows.size() <= 5){
                response.setStatus(true);
                response.setMessage("There is no major groups entries in this location");
                response.setSalesTender(new ArrayList<>());

                return response;
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
                    MajorGroup majorGroup = conversions.checkMajorGroupExistence(majorGroups,
                            td.getText().strip().toLowerCase());

                    if (!majorGroup.getChecked()) {
                        majorGroup = conversions.checkMajorGroupExistence(majorGroups,
                                td.getText().strip().toLowerCase() + " "+ revenueCenter.getRevenueCenter().toLowerCase());

                        if (!majorGroup.getChecked()) {
                            continue;
                        }
                    }

                    Journal journal = new Journal();
                    float majorGroupGross;
                    if (grossDiscountSales.equals(Constants.SALES_GROSS_LESS_DISCOUNT)){
                        majorGroupGross = conversions.convertStringToFloat(cols.get(columns.indexOf("sales_less_item_disc")).getText().strip());
                    }else {
                        majorGroupGross = conversions.convertStringToFloat(cols.get(columns.indexOf("gross_sales")).getText().strip());
                    }

                    majorGroupsGross = journal.checkExistence(majorGroupsGross, majorGroup
                            , 0, majorGroupGross, 0, 0, costCenter, revenueCenter);
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

    private Response getSalesDiscount(String location, String businessDate, String fromDate, String toDate,
                                            CostCenter costCenter, boolean getDiscountTotalFlag, ArrayList<Discount> discounts,
                                      WebDriver driver) {
        Response response = new Response();
        ArrayList<Discount> salesDiscount = new ArrayList<>();

        driver.get(Constants.DISCOUNT_REPORT_LINK);
        try{
            WebDriverWait wait = new WebDriverWait(driver, 20);
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("loadingFrame")));
        } catch (Exception Ex) {
            System.out.println("There is no loader");
        }

        Response dateResponse = setupEnvironment.selectTimePeriodOHRA(businessDate, fromDate, toDate, location,
                "", driver);

        if (!dateResponse.isStatus()){
            response.setStatus(false);
            response.setMessage(dateResponse.getMessage());
            return response;
        }

        driver.findElement(By.id("Run Report")).click();

        try {
            driver.get(Constants.DISCOUNT_TABLE_LINK);

            List<WebElement> rows = driver.findElements(By.tagName("tr"));

            if (rows.size() <= 5){
                response.setStatus(true);
                response.setMessage("There is no discount entries in this location");
                response.setSalesTender(new ArrayList<>());

                return response;
            }

            ArrayList<String> columns = setupEnvironment.getTableColumns(rows, false, 5);

            for (int i = 6; i < rows.size(); i++) {
                WebElement row = rows.get(i);
                List<WebElement> cols = row.findElements(By.tagName("td"));

                if (cols.size() != columns.size()) {
                    continue;
                }
                Discount discount;

                if(getDiscountTotalFlag){
                    WebElement td = cols.get(0);
                    if (td.getText().equals("Total Discounts:")) {
                        discount = conversions.checkDiscountExistence(discounts, "discount cost");
                        if (!discount.isChecked()) {
                            break;
                        }

                        float discountTotal = conversions.convertStringToFloat(cols.get(columns.indexOf("total")).getText().strip());

                        discount.setTotal(discountTotal);
                        discount.setCostCenter(costCenter);
                        salesDiscount.add(discount);
                        break;
                    }
                }else{
                    if (columns.indexOf("discount_type") != -1){
                        WebElement td = cols.get(columns.indexOf("discount_type"));
                        discount = conversions.checkDiscountExistence(discounts, td.getText().strip().toLowerCase());

                        if (!discount.isChecked()) {
                            continue;
                        }

                        float discountTotal = conversions.convertStringToFloat(cols.get(columns.indexOf("total")).getText().strip());

                        discount.setTotal(discountTotal);
                        discount.setCostCenter(costCenter);
                        salesDiscount.add(discount);
                    }else{
                        driver.quit();
                        response.setStatus(false);
                        response.setMessage("Failed to get discount entries, Please contact support team.");
                    }
                }
            }
            response.setStatus(true);
            response.setMessage("");
            response.setSalesDiscount(salesDiscount);
        } catch (Exception e) {
            driver.quit();

            response.setStatus(false);
            response.setMessage(e.getMessage());
        }
        return response;
    }

    private Response getTotalSalesServiceCharge(String location, String businessDate, String fromDate, String toDate,
                                      CostCenter costCenter, ArrayList<ServiceCharge> serviceCharges,WebDriver driver) {
        Response response = new Response();
        ArrayList<ServiceCharge> salesServiceCharges = new ArrayList<>();

        driver.get(Constants.SERVICE_CHARGE_REPORT_LINK);
        try{
            WebDriverWait wait = new WebDriverWait(driver, 20);
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("loadingFrame")));
        } catch (Exception Ex) {
            System.out.println("There is no loader");
        }

        Response dateResponse = setupEnvironment.selectTimePeriodOHRA(businessDate, fromDate, toDate, location,
                "", driver);

        if (!dateResponse.isStatus()){
            response.setStatus(false);
            response.setMessage(dateResponse.getMessage());
            return response;
        }

        driver.findElement(By.id("Run Report")).click();

        try {
            driver.get(Constants.SERVICE_CHARGE_TABLE_LINK);

            List<WebElement> rows = driver.findElements(By.tagName("tr"));

            if (rows.size() <= 5){
                response.setStatus(true);
                response.setMessage("There is no service charge entries in this location");
                response.setSalesTender(new ArrayList<>());

                return response;
            }

            ArrayList<String> columns = setupEnvironment.getTableColumns(rows, false, 5);

            ServiceCharge serviceCharge = new ServiceCharge();
            for (int i = 6; i < rows.size(); i++) {
                WebElement row = rows.get(i);
                List<WebElement> cols = row.findElements(By.tagName("td"));

                if (cols.size() != columns.size()) {
                    continue;
                }

                WebElement td = cols.get(0);
                if (td.getText().equals("Total Service Charges:")) {
                    serviceCharge = conversions.checkServiceChargeExistence(serviceCharges, "total");
                    if(!serviceCharge.isChecked()){
                        break;
                    }
                    float serviceChargeTotal = conversions.convertStringToFloat(cols.get(columns.indexOf("total")).getText().strip());

                    serviceCharge.setTotal(serviceChargeTotal);
                    serviceCharge.setCostCenter(costCenter);
                    salesServiceCharges.add(serviceCharge);
                    break;
                }
            }
            response.setStatus(true);
            response.setMessage("");
            response.setSalesServiceCharge(salesServiceCharges);
        } catch (Exception e) {
            driver.quit();

            response.setStatus(false);
            response.setMessage(e.getMessage());
        }
        return response;
    }


    public ArrayList<JournalBatch> saveSalesJournalBatchesData(Response salesResponse, SyncJob syncJob,
                                                               SyncJobType syncJobType, Account account) {
        ArrayList<JournalBatch> addedJournalBatches = new ArrayList<>();

        String businessDate =  syncJobType.getConfiguration().getTimePeriod();
        String fromDate =  syncJobType.getConfiguration().getFromDate();
        ArrayList<Discount> includedDiscountTypes = syncJobType.getConfiguration().getDiscounts();

        String transactionDate = conversions.getTransactionDate(businessDate, fromDate);

        ArrayList<JournalBatch> journalBatches = salesResponse.getJournalBatches();
        for (JournalBatch journalBatch : journalBatches) {
            float totalTender = 0;
            float totalDiscount = 0;
            float totalTax = 0;
            float totalServiceCharge = 0;
            float totalMajorGroupNet = 0;

            // Save tenders {Debit}
            ArrayList<Tender> tenders = journalBatch.getSalesTender();
            for (Tender tender : tenders) {
                if (tender.getTotal() == 0)
                    continue;

                HashMap<String, String> tenderData = new HashMap<>();

                tenderData.put("accountingPeriod", transactionDate.substring(2,6));
                tenderData.put("transactionDate", transactionDate);

                if (tender.getTotal() < 0){
                    tenderData.put("totalDr", String.valueOf(conversions.roundUpFloat(tender.getTotal())));
                }else {
                    tenderData.put("totalDr", String.valueOf(conversions.roundUpFloat(tender.getTotal()) * -1));
                }

                tenderData.put("fromCostCenter", tender.getCostCenter().costCenter);
                tenderData.put("fromAccountCode", tender.getCostCenter().accountCode);

                tenderData.put("toCostCenter", tender.getCostCenter().costCenter);
                tenderData.put("toAccountCode", tender.getCostCenter().accountCode);

                tenderData.put("fromLocation", tender.getCostCenter().accountCode);
                tenderData.put("toLocation", tender.getCostCenter().accountCode);

                tenderData.put("transactionReference", "Tender Reference");

                tenderData.put("expensesAccount", tender.getAccount());

                String description = "";
                if (tender.getCostCenter().costCenter.equals("")){
                    description = "Sales F " + tender.getTender();
                }else {
                    description = "Sales F " + tender.getCostCenter().costCenterReference + " " + tender.getTender();
                }
                if (description.length() > 50) {
                    description = description.substring(0, 50);
                }

                tenderData.put("description", description);

                SyncJobData syncJobData = new SyncJobData(tenderData, Constants.RECEIVED, "", new Date(),
                        syncJob.getId());
                syncJobDataRepo.save(syncJobData);
                journalBatch.getSalesTenderData().add(syncJobData);

                float tenderTotal = tender.getTotal();
                totalTender += tenderTotal;
            }

            // Save taxes {Credit}
            ArrayList<Tax> taxes = journalBatch.getSalesTax();
            for (Tax tax : taxes) {
                if (tax.getTotal() == 0)
                    continue;

                HashMap<String, String> taxData = new HashMap<>();

                taxData.put("accountingPeriod", transactionDate.substring(2,6));
                taxData.put("transactionDate", transactionDate);

                taxData.put("totalCr", String.valueOf(conversions.roundUpFloat(tax.getTotal())));

                taxData.put("fromCostCenter", tax.getCostCenter().costCenter);
                taxData.put("fromAccountCode", tax.getCostCenter().accountCode);

                taxData.put("toCostCenter", tax.getCostCenter().costCenter);
                taxData.put("toAccountCode", tax.getCostCenter().accountCode);

                taxData.put("fromLocation", tax.getCostCenter().accountCode);
                taxData.put("toLocation", tax.getCostCenter().accountCode);

                taxData.put("transactionReference", "Taxes Reference");

                // Vat out account
//                String vatOut = syncJobType.getConfiguration().getVatOut();
                taxData.put("inventoryAccount", tax.getAccount());

                String description = "";
                if (tax.getCostCenter().costCenter.equals("")){
                    description = "Sales F " + tax.getTax();
                }else {
                    description = "Sales F " + tax.getCostCenter().costCenterReference + " " + tax.getTax();
                }

                if (description.length() > 50) {
                    description = description.substring(0, 50);
                }

                taxData.put("description", description);

                SyncJobData syncJobData = new SyncJobData(taxData, Constants.RECEIVED, "", new Date(),
                        syncJob.getId());
                syncJobDataRepo.save(syncJobData);
                journalBatch.getSalesTaxData().add(syncJobData);

                float taxTotal = tax.getTotal();
                totalTax += taxTotal;
            }

            // Save majorGroup {Credit}
            ArrayList<Journal> majorGroupsGross = journalBatch.getSalesMajorGroupGross();
            for (Journal majorGroupJournal : majorGroupsGross) {
                if (majorGroupJournal.getTotalCost() == 0)
                    continue;

                HashMap<String, String> majorGroupData = new HashMap<>();

                majorGroupData.put("accountingPeriod", transactionDate.substring(2,6));
                majorGroupData.put("transactionDate", transactionDate);

                majorGroupData.put("totalCr", String.valueOf(conversions.roundUpFloat(majorGroupJournal.getTotalCost())));

                majorGroupData.put("fromCostCenter", majorGroupJournal.getCostCenter().costCenter);
                majorGroupData.put("fromAccountCode", majorGroupJournal.getCostCenter().accountCode);

                majorGroupData.put("toCostCenter", majorGroupJournal.getCostCenter().costCenter);
                majorGroupData.put("toAccountCode", majorGroupJournal.getCostCenter().accountCode);

                majorGroupData.put("fromLocation", majorGroupJournal.getCostCenter().accountCode);
                majorGroupData.put("toLocation", majorGroupJournal.getCostCenter().accountCode);

                majorGroupData.put("transactionReference", "MajorGroup Reference");

                // Major Group account
                majorGroupData.put("inventoryAccount", majorGroupJournal.getMajorGroup().getAccount());

                String description = "";
                if (majorGroupJournal.getCostCenter().costCenter.equals("")){
                    description = "Sales F " + majorGroupJournal.getMajorGroup().getMajorGroup() ;
                }else {
                    description = "Sales F " + majorGroupJournal.getCostCenter().costCenterReference + " " + majorGroupJournal.getMajorGroup().getMajorGroup();
                }

                if (description.length() > 50) {
                    description = description.substring(0, 50);
                }

                majorGroupData.put("description", description);

                SyncJobData syncJobData = new SyncJobData(majorGroupData, Constants.RECEIVED, "", new Date(),
                        syncJob.getId());
                syncJobDataRepo.save(syncJobData);
                journalBatch.getSalesMajorGroupGrossData().add(syncJobData);

                float majorGroupGrossTotal = majorGroupJournal.getTotalCost();
                totalMajorGroupNet += majorGroupGrossTotal;
            }

            // Save service charge {Credit}
            ArrayList<ServiceCharge> serviceChargeGross = journalBatch.getSalesServiceCharge();
            for (ServiceCharge serviceCharge : serviceChargeGross) {
                if (serviceCharge.getTotal() == 0)
                    continue;

                HashMap<String, String> majorGroupData = new HashMap<>();

                majorGroupData.put("accountingPeriod", transactionDate.substring(2,6));
                majorGroupData.put("transactionDate", transactionDate);

                majorGroupData.put("totalCr", String.valueOf(conversions.roundUpFloat(serviceCharge.getTotal())));

                majorGroupData.put("fromCostCenter", serviceCharge.getCostCenter().costCenter);
                majorGroupData.put("fromAccountCode", serviceCharge.getCostCenter().accountCode);

                majorGroupData.put("toCostCenter", serviceCharge.getCostCenter().costCenter);
                majorGroupData.put("toAccountCode", serviceCharge.getCostCenter().accountCode);

                majorGroupData.put("fromLocation", serviceCharge.getCostCenter().accountCode);
                majorGroupData.put("toLocation", serviceCharge.getCostCenter().accountCode);

                majorGroupData.put("transactionReference", "MajorGroup Reference");
                majorGroupData.put("inventoryAccount", serviceCharge.getAccount());

                String description = "";
                if (serviceCharge.getCostCenter().costCenter.equals("")){
                    description = "Service Charge";
                }else {
                    description = "Sales F " + serviceCharge.getCostCenter().costCenterReference + " " + serviceCharge.getServiceCharge();
                }

                if (description.length() > 50) {
                    description = description.substring(0, 50);
                }

                majorGroupData.put("description", description);

                SyncJobData syncJobData = new SyncJobData(majorGroupData, Constants.RECEIVED, "", new Date(),
                        syncJob.getId());
                syncJobDataRepo.save(syncJobData);
                journalBatch.getSalesMajorGroupGrossData().add(syncJobData);

                float serviceChargeTotal = serviceCharge.getTotal();
                totalServiceCharge += serviceChargeTotal;
            }

            ArrayList<Discount> discounts = journalBatch.getSalesDiscount();
            for (Discount discount : discounts) {
                if (discount.getTotal() == 0)
                    continue;

                HashMap<String, String> discountData = new HashMap<>();

                discountData.put("accountingPeriod", transactionDate.substring(2,6));
                discountData.put("transactionDate", transactionDate);

                discountData.put("totalCr", String.valueOf(conversions.roundUpFloat(discount.getTotal())));

                discountData.put("fromCostCenter", discount.getCostCenter().costCenter);
                discountData.put("fromAccountCode", discount.getCostCenter().accountCode);

                discountData.put("toCostCenter", discount.getCostCenter().costCenter);
                discountData.put("toAccountCode", discount.getCostCenter().accountCode);

                discountData.put("fromLocation", discount.getCostCenter().accountCode);
                discountData.put("toLocation", discount.getCostCenter().accountCode);

                discountData.put("transactionReference", "Discount Cost");

                discountData.put("inventoryAccount", discount.getAccount());

                String description = "";
                if (discount.getCostCenter().costCenter.equals("")){
                    description = "Discount Cost";
                }else {
                    description = "Discount Cost F " + discount.getCostCenter().costCenterReference + " " + discount.getDiscount();
                }
                if (description.length() > 50) {
                    description = description.substring(0, 50);
                }

                discountData.put("description", description);

                SyncJobData syncJobData = new SyncJobData(discountData, Constants.RECEIVED, "", new Date(),
                        syncJob.getId());
                syncJobDataRepo.save(syncJobData);
                journalBatch.getSalesDiscountData().add(syncJobData);

                float discountTotal = discount.getTotal();
                totalDiscount += discountTotal;
            }

            if(totalDiscount != 0 && account.getERD().equals(Constants.EXPORT_TO_SUN_ERD)){
                // Add debit discount entries totalDiscount/2
                Discount discount = conversions.checkDiscountExistence(includedDiscountTypes, "discount expense");
                HashMap<String, String> discountData = new HashMap<>();

                if (discount.isChecked()){
                    discountData.put("accountingPeriod", transactionDate.substring(2,6));
                    discountData.put("transactionDate", transactionDate);

                    discountData.put("totalDr", String.valueOf(totalDiscount/2));

                    discountData.put("fromCostCenter", discount.getCostCenter().costCenter);
                    discountData.put("fromAccountCode", discount.getCostCenter().accountCode);

                    discountData.put("toCostCenter", discount.getCostCenter().costCenter);
                    discountData.put("toAccountCode", discount.getCostCenter().accountCode);

                    discountData.put("fromLocation", discount.getCostCenter().accountCode);
                    discountData.put("toLocation", discount.getCostCenter().accountCode);

                    discountData.put("transactionReference", "Discount Expense");

                    discountData.put("expensesAccount", discount.getAccount());

                    String description = "";
                    if (discount.getCostCenter().costCenter.equals("")){
                        description = "Discount Expense";
                    }else {
                        description = "Discount Expense F " + discount.getCostCenter().costCenterReference;
                    }
                    if (description.length() > 50) {
                        description = description.substring(0, 50);
                    }

                    discountData.put("description", description);

                    SyncJobData syncJobData = new SyncJobData(discountData, Constants.RECEIVED, "", new Date(),
                            syncJob.getId());
                    syncJobDataRepo.save(syncJobData);
                    journalBatch.getSalesDiscountData().add(syncJobData);
                }


                discount = conversions.checkDiscountExistence(includedDiscountTypes, "AR account");
                if (discount.isChecked()){
                    discountData = new HashMap<>();

                    discountData.put("accountingPeriod", transactionDate.substring(2,6));
                    discountData.put("transactionDate", transactionDate);

                    discountData.put("totalDr", String.valueOf(totalDiscount/2));

                    discountData.put("fromCostCenter", discount.getCostCenter().costCenter);
                    discountData.put("fromAccountCode", discount.getCostCenter().accountCode);

                    discountData.put("toCostCenter", discount.getCostCenter().costCenter);
                    discountData.put("toAccountCode", discount.getCostCenter().accountCode);

                    discountData.put("fromLocation", discount.getCostCenter().accountCode);
                    discountData.put("toLocation", discount.getCostCenter().accountCode);

                    discountData.put("transactionReference", "AR account");

                    discountData.put("expensesAccount", discount.getAccount());

                    String description = "";
                    if (discount.getCostCenter().costCenter.equals("")){
                        description = "AR account";
                    }else {
                        description = "AR account F " + discount.getCostCenter().costCenterReference;
                    }
                    if (description.length() > 50) {
                        description = description.substring(0, 50);
                    }

                    discountData.put("description", description);

                    SyncJobData syncJobData = new SyncJobData(discountData, Constants.RECEIVED, "", new Date(),
                            syncJob.getId());
                    syncJobDataRepo.save(syncJobData);
                    journalBatch.getSalesDiscountData().add(syncJobData);
                }
            }

            float totalDr = totalTender;
            float totalCr;
            if(syncJobType.getConfiguration().getGrossDiscountSales().equals(Constants.SALES_GROSS)){
                totalCr = totalMajorGroupNet + totalDiscount + totalTax + totalServiceCharge;
            }else
            {
                totalCr = totalMajorGroupNet + totalTax + totalServiceCharge;
            }

            if (totalCr != totalDr) {
                HashMap<String, String> differentData = new HashMap<>();

                differentData.put("accountingPeriod", transactionDate.substring(2,6));
                differentData.put("transactionDate", transactionDate);

                // {Debit} - ShortagePOS
                if (totalCr > totalDr ) {
                    String cashShortagePOS = syncJobType.getConfiguration().getCashShortagePOS();
                    differentData.put("totalDr", String.valueOf(conversions.roundUpFloat(totalCr - totalDr)));
                    differentData.put("expensesAccount", cashShortagePOS);
                }
                // {Credit} - SurplusPOS
                else {
                    String cashSurplusPOS = syncJobType.getConfiguration().getCashSurplusPOS();
                    differentData.put("totalCr", String.valueOf(conversions.roundUpFloat(totalDr - totalCr)));
                    differentData.put("inventoryAccount", cashSurplusPOS);
                }

                differentData.put("fromCostCenter", journalBatch.getCostCenter().costCenter);
                differentData.put("fromAccountCode", journalBatch.getCostCenter().accountCode);

                differentData.put("toCostCenter", journalBatch.getCostCenter().costCenter);
                differentData.put("toAccountCode", journalBatch.getCostCenter().accountCode);

                differentData.put("fromLocation", journalBatch.getCostCenter().accountCode);
                differentData.put("toLocation", journalBatch.getCostCenter().accountCode);

                // 30 Char only
                differentData.put("transactionReference", "Different Reference");

                String description = "";
                if (journalBatch.getCostCenter().costCenter.equals("")){
                    description = "Sales For " + "different";
                }else {
                    description = "Sales For " + journalBatch.getCostCenter().costCenterReference + " - different";
                }

                if (description.length() > 50){
                    description = description.substring(0, 50);
                }

                differentData.put("description", description);

                SyncJobData syncJobData = new SyncJobData(differentData, Constants.RECEIVED, "", new Date(),
                        syncJob.getId());
                syncJobDataRepo.save(syncJobData);
                journalBatch.setSalesDifferentData(syncJobData);
            }
            addedJournalBatches.add(journalBatch);

        }
        return addedJournalBatches;
    }

    public void updateJournalBatchStatus(JournalBatch journalBatch, HashMap<String, Object> response){
        SyncJobData salesDifferentData = journalBatch.getSalesDifferentData();
        ArrayList<SyncJobData> salesTaxData = journalBatch.getSalesTaxData();
        ArrayList<SyncJobData> salesTenderData = journalBatch.getSalesTenderData();
        ArrayList<SyncJobData> salesMajorGroupGrossData = journalBatch.getSalesMajorGroupGrossData();

        String reason = "";
        String status = "";

        if ((Boolean) response.get("status")){
            status = Constants.SUCCESS;
            reason = "";
        }
        else {
            status = Constants.FAILED;
            reason = (String) response.get("message");
        }

        salesDifferentData.setStatus(status);
        salesDifferentData.setReason(reason);
        syncJobDataRepo.save(salesDifferentData);

        for (SyncJobData data : salesTaxData) {
            data.setStatus(status);
            data.setReason(reason);
            syncJobDataRepo.save(data);
        }
        for (SyncJobData data : salesTenderData) {
            data.setStatus(status);
            data.setReason(reason);
            syncJobDataRepo.save(data);
        }
        for (SyncJobData data : salesMajorGroupGrossData) {
            data.setStatus(status);
            data.setReason(reason);
            syncJobDataRepo.save(data);
        }
    }
}
