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

    public Response getSalesData(SyncJobType salesSyncJobType,
                                 ArrayList<CostCenter> costCentersLocation, ArrayList<MajorGroup> majorGroups,
                                 ArrayList<Tender> includedTenders,  ArrayList<Tax> includedTax,
                                 ArrayList<Discount> includedDiscount, ArrayList<ServiceCharge> includedServiceCharge,
                                 ArrayList<RevenueCenter> revenueCenters,
                                 Account account) {

        Response response = new Response();
        ArrayList<JournalBatch> journalBatches = new ArrayList<>();

        String timePeriod = salesSyncJobType.getConfiguration().timePeriod;
        String fromDate = salesSyncJobType.getConfiguration().fromDate;
        String toDate = salesSyncJobType.getConfiguration().toDate;
        String grossDiscountSales = salesSyncJobType.getConfiguration().salesConfiguration.grossDiscountSales;

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

            if (costCentersLocation.size() > 0){
                for (CostCenter costCenter : costCentersLocation) {
                    if(costCenter.checked){
                        callSalesFunction(salesSyncJobType, majorGroups, includedTenders, includedTax, includedDiscount,
                                includedServiceCharge, revenueCenters, timePeriod, fromDate, toDate, grossDiscountSales,
                                costCenter, journalBatches, account, driver, response);
                        if (!response.isStatus() && !response.getMessage().equals(Constants.INVALID_LOCATION)){
                            return response;
                        }
                    }
                }
            }
            else {
                callSalesFunction(salesSyncJobType, majorGroups, includedTenders, includedTax, includedDiscount,
                        includedServiceCharge, revenueCenters, timePeriod, fromDate, toDate, grossDiscountSales,
                        new CostCenter(), journalBatches, account, driver, response);
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
                                   String toDate, String grossDiscountSales, CostCenter costCenter,
                                   ArrayList<JournalBatch> journalBatches, Account account,
                                   WebDriver driver, Response response){
        JournalBatch journalBatch = new JournalBatch();
        SalesConfiguration configuration = salesSyncJobType.getConfiguration().salesConfiguration;

        // Get tender
        Response tenderResponse = getSalesTenders(timePeriod, fromDate, toDate,
                costCenter, includedTenders, driver);
        if (checkSalesFunctionResponse(driver, response, tenderResponse)) return;

        // Get taxes
        boolean taxIncluded = configuration.taxIncluded;
        boolean syncTotalTax = configuration.syncTotalTax;
        String totalTaxAccount = configuration.totalTaxAccount;

        Response taxResponse = getSalesTaxes(timePeriod, fromDate, toDate, costCenter, syncTotalTax,
                totalTaxAccount, includedTax, taxIncluded, driver);
        if (checkSalesFunctionResponse(driver, response, taxResponse)) return;

        // Get serviceCharge
        boolean syncTotalServiceCharge = salesSyncJobType.getConfiguration().salesConfiguration.syncTotalServiceCharge;
        String totalServiceChargeAccount = salesSyncJobType.getConfiguration().salesConfiguration.totalServiceChargeAccount;

        Response serviceChargeResponse = new Response();
        if (includedServiceCharge.size() > 0 || syncTotalServiceCharge){
            serviceChargeResponse = getTotalSalesServiceCharge(timePeriod, fromDate, toDate, costCenter,
                    syncTotalServiceCharge, totalServiceChargeAccount, includedServiceCharge, driver);
            if (checkSalesFunctionResponse(driver, response, serviceChargeResponse)) return;
        }

        // Get Major Groups/Family Groups net sales
        boolean majorGroupDiscount = configuration.MGDiscount;
        ArrayList<Journal> salesMajorGroupsGross = new ArrayList<>();
        ArrayList<Discount> salesDiscounts = new ArrayList<>();

        if (revenueCenters.size() > 0 ){
            for (RevenueCenter rc : revenueCenters)
            {
                Response overGroupGrossResponse = getSalesOverGroupGross(rc,
                        timePeriod, fromDate, toDate, costCenter, majorGroups, grossDiscountSales,
                        majorGroupDiscount, includedDiscount, taxIncluded,
                        driver);
                if (!overGroupGrossResponse.isStatus()) {
                    if (overGroupGrossResponse.getMessage().equals(Constants.INVALID_LOCATION)) {
                        continue;
                    }
                    response.setStatus(false);
                    response.setMessage(overGroupGrossResponse.getMessage());
                    return;
                }

                if (majorGroupDiscount){
                    salesDiscounts.addAll(overGroupGrossResponse.getSalesDiscount());
                }

                salesMajorGroupsGross.addAll(overGroupGrossResponse.getSalesMajorGroupGross());
            }
        }
        else {
            Response overGroupGrossResponse = getSalesOverGroupGross(new RevenueCenter(),
                    timePeriod, fromDate, toDate, costCenter, majorGroups, grossDiscountSales,
                    majorGroupDiscount, includedDiscount, taxIncluded, driver);
            if (checkSalesFunctionResponse(driver, response, overGroupGrossResponse)) return;

            if (majorGroupDiscount){
                salesDiscounts = overGroupGrossResponse.getSalesDiscount();
            }

            salesMajorGroupsGross.addAll(overGroupGrossResponse.getSalesMajorGroupGross());
        }

        // Get discounts
        Response discountResponse;
        boolean syncTotalDiscounts = configuration.syncTotalDiscounts;
        String totalDiscountsAccount = configuration.totalDiscountsAccount;

        if ((includedDiscount.size() > 0 || syncTotalDiscounts) && configuration.grossDiscountSales.equals(Constants.SALES_GROSS)){
            discountResponse = getSalesDiscount(timePeriod, fromDate, toDate, costCenter,
                    syncTotalDiscounts, totalDiscountsAccount, includedDiscount, driver);
            if (checkSalesFunctionResponse(driver, response, discountResponse)) return;
            salesDiscounts.addAll(discountResponse.getSalesDiscount());
        }


        // Set Debit Entries (Tenders)
        journalBatch.setSalesTender(tenderResponse.getSalesTender());

        // Set Credit Entries (Taxes, overGroupsGross, Discount and Service charge)
        journalBatch.setSalesTax(taxResponse.getSalesTax());
        journalBatch.setSalesMajorGroupGross(salesMajorGroupsGross);
        journalBatch.setSalesDiscount(salesDiscounts);
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
            } else if(overGroupGrossResponse.getMessage().equals(Constants.INVALID_BUSINESS_DATE)){
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

    private Response getSalesTenders(String businessDate, String fromDate, String toDate,
                                     CostCenter location, ArrayList<Tender> includedTenders, WebDriver driver) {
        Response response = new Response();
        ArrayList<Tender> tenders = new ArrayList<>();

        if (!driver.getCurrentUrl().equals(Constants.TENDERS_REPORT_LINK)) {
            driver.get(Constants.TENDERS_REPORT_LINK);
        }

        try{
            WebDriverWait wait = new WebDriverWait(driver, 20);
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("loadingFrame")));
        } catch (Exception Ex) {
            System.out.println("There is no loader");
        }

        String message = "";
        int tryMaxCount = 2;
        do{
            Response dateResponse = setupEnvironment.selectTimePeriodOHRA(businessDate, fromDate, toDate,
                    location.locationName, "", driver);

            if (!dateResponse.isStatus()){
                response.setStatus(false);
                response.setMessage( dateResponse.getMessage());
                response.setSalesTender(tenders);
                return response;
            }

            driver.findElement(By.id("Run Report")).click();

            /*
             * Check if selenium failed to select business date, and re-try
             * */
            try {
                Alert locationAlert = driver.switchTo().alert();
                message = locationAlert.getText();
                locationAlert.accept();
            }catch (NoAlertPresentException Ex) {
                System.out.println("No alert exits");
            }
            tryMaxCount--;
        }while (message.equals(Constants.EMPTY_BUSINESS_DATE) && tryMaxCount != 0);

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
                Tender tender;
                WebElement row = rows.get(i);
                List<WebElement> cols = row.findElements(By.tagName("td"));

                if (cols.size() != columns.size()) {
                    continue;
                }

                // Check if tender exists
                Tender tenderData = conversions.checkTenderExistence(includedTenders, cols.get(0).getText().strip(),
                        location.locationName, 0);
                if (!tenderData.isChecked()) {
                    continue;
                }

                tender = new Tender();
                tender.setTender(tenderData.getTender());
                tender.setAccount(tenderData.getAccount());
                tender.setAnalysisCodeT5(tenderData.getAnalysisCodeT5());
                tender.setCommunicationAccount(tenderData.getCommunicationAccount());
                tender.setCommunicationRate(tenderData.getCommunicationRate());
                tender.setCommunicationTender(tenderData.getCommunicationTender());
                tender.setCostCenter(location);
                tender.setTotal(conversions.convertStringToFloat(cols.get(1).getText().strip()));

                // Check if it already exist, increment its value
                tenderData = conversions.checkTenderExistence(tenders, tender.getTender(), location.locationName,
                        tender.getTotal());
                if (tenderData.getTender().equals("")) {
                    tenders.add(tender);
                }
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

    private Response getSalesTaxes(String businessDate, String fromDate, String toDate,
                                   CostCenter location, boolean getTaxTotalFlag, String totalTaxAccount,
                                   ArrayList<Tax> includedTaxes, boolean taxIncluded, WebDriver driver) {
        Response response = new Response();

        ArrayList<Tax> salesTax = new ArrayList<>();

        /*
         * Check if account use tax included or add on
         * */
        if(taxIncluded){
            driver.get(Constants.SYSTEM_SALES_REPORT_LINK);
        }else{
            driver.get(Constants.TAXES_REPORT_LINK);
        }

        try {
            WebDriverWait wait = new WebDriverWait(driver, 20);
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("loadingFrame")));
        } catch (Exception Ex) {
            System.out.println("There is no loader");
        }

        String message = "";
        int tryMaxCount = 2;
        do{
            Response dateResponse = setupEnvironment.selectTimePeriodOHRA(businessDate, fromDate, toDate, location.locationName,
                    "", driver);

            if (!dateResponse.isStatus()){
                response.setStatus(false);
                response.setMessage( dateResponse.getMessage());
                response.setSalesTax(salesTax);
                return response;
            }

            driver.findElement(By.id("Run Report")).click();

            /*
             * Check if selenium failed to select business date, and re-try
             * */
            try {
                Alert locationAlert = driver.switchTo().alert();
                message = locationAlert.getText();
                locationAlert.accept();
            }catch (NoAlertPresentException Ex) {
                System.out.println("No alert exits");
            }
            tryMaxCount--;
        }while (message.equals(Constants.EMPTY_BUSINESS_DATE) && tryMaxCount != 0);

        String taxReportLink;

        if(taxIncluded){
            driver.get(Constants.SYSTEM_SALES_REPORT_LINK);
            taxReportLink = Constants.TAX_INCLUDED_REPORT_LINK;
        }else{
            driver.get(Constants.TAXES_REPORT_LINK);
            taxReportLink = Constants.ADD_ON_TAX_INCLUDED_REPORT_LINK;
        }


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

                WebElement td = cols.get(0);
                if(getTaxTotalFlag){
                    if (td.getText().equals("Total Taxes:")) {
                        tax.setTax("Total Tax");
                        tax.setAccount(totalTaxAccount);
                        tax.setTotal(conversions.convertStringToFloat(cols.get(columns.indexOf("tax_collected")).getText().strip()));
                        tax.setCostCenter(location);
                        salesTax.add(tax);
                        break;
                    }
                }else{
                    // Check if tax exists
                    Tax taxData = conversions.checkTaxExistence(includedTaxes, td.getText().strip());
                    if (!taxData.isChecked()) {
                        continue;
                    }

                    tax.setTax(taxData.getTax());
                    tax.setAccount(taxData.getAccount());
                    tax.setCostCenter(location);
                    float taxAmount;
                    if(taxIncluded){
                        taxAmount = conversions.convertStringToFloat(cols.get(columns.indexOf("tax_collected")).getText().strip());
                    }else{
                        taxAmount = conversions.convertStringToFloat(cols.get(1).getText().strip());
                    }

                    tax.setTotal(taxAmount);
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

    private Response getSalesOverGroupGross(RevenueCenter revenueCenter, String businessDate,
                                            String fromDate, String toDate,
                                            CostCenter location, ArrayList<MajorGroup> majorGroups,
                                            String grossDiscountSales, boolean majorGroupDiscount,
                                            ArrayList<Discount> discounts, boolean taxIncluded,
                                            WebDriver driver) {
        Response response = new Response();
        ArrayList<Journal> majorGroupsGross = new ArrayList<>();
        ArrayList<Discount> salesDiscount = new ArrayList<>();

        /*
        * Check if account use tax included or add-on
        * */
        if(taxIncluded){
            driver.get(Constants.SYSTEM_SALES_REPORT_LINK);
        }else{
            driver.get(Constants.OVER_GROUP_GROSS_REPORT_LINK);
        }

        try{
            WebDriverWait wait = new WebDriverWait(driver, 20);
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("loadingFrame")));
        } catch (Exception Ex) {
            System.out.println("There is no loader");
        }

        String message = "";
        int tryMaxCount = 2;
        do{
            Response dateResponse = setupEnvironment.selectTimePeriodOHRA(businessDate, fromDate, toDate, location.locationName,
                    revenueCenter.getRevenueCenter(), driver);

            if (!dateResponse.isStatus()){
                response.setStatus(false);
                response.setMessage( dateResponse.getMessage());
                response.setSalesMajorGroupGross(majorGroupsGross);
                return response;
            }

            driver.findElement(By.id("Run Report")).click();

            /*
            * Check if selenium failed to select business date, and re-try
            * */
            try {
                Alert locationAlert = driver.switchTo().alert();
                message = locationAlert.getText();
                locationAlert.accept();
            }catch (NoAlertPresentException Ex) {
                System.out.println("No alert exits");
            }
            tryMaxCount--;
        }while (message.equals(Constants.EMPTY_BUSINESS_DATE) && tryMaxCount != 0);

        String overGroupGrossLink = Constants.OHRA_LINK;

        if(taxIncluded){
            overGroupGrossLink += "/finengine/reportRunAction.do?method=run&reportID=EAME_SalesMixDailyDetail_VAT&rptroot=1191";
        }else{
            overGroupGrossLink += "/finengine/reportRunAction.do?rptroot=15&reportID=SalesMixDailyDetail&method=run";
        }

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

            Discount discount;
            MajorGroup majorGroup;
            for (int i = 7; i < rows.size(); i++) {
                WebElement row = rows.get(i);
                List<WebElement> cols = row.findElements(By.tagName("td"));

                if (cols.size() != columns.size()) {
                    continue;
                }

                if (!taxIncluded && columns.indexOf("group") != -1){
                    WebElement col = cols.get(columns.indexOf("group"));
                    if (!col.getAttribute("class").equals("header_1")){ // Group
                        continue;
                    }

                    String majorGroupName = col.getText().strip().toLowerCase();
                    majorGroup = conversions.checkMajorGroupExistence(majorGroups,
                            majorGroupName);

                    if (!majorGroup.getChecked()) {
                        majorGroup = conversions.checkMajorGroupExistence(majorGroups,
                                majorGroupName + " "+ revenueCenter.getRevenueCenter().toLowerCase());

                        if (!majorGroup.getChecked()) {
                            continue;
                        }
                    }

                    Journal journal = new Journal();
                    float majorGroupGross;
                    float discountTotal;

                    if (grossDiscountSales.equals(Constants.SALES_GROSS_LESS_DISCOUNT)){
                        majorGroupGross = conversions.convertStringToFloat(cols.get(columns.indexOf("sales_less_item_disc")).getText().strip());
                    }else {
                        majorGroupGross = conversions.convertStringToFloat(cols.get(columns.indexOf("gross_sales")).getText().strip());
                    }
                    if(majorGroupDiscount){
                        discount = conversions.checkDiscountExistence(discounts, majorGroup.getMajorGroup() + " Discount");
                        if (!discount.isChecked()) {
                            break;
                        }
                        discountTotal = conversions.convertStringToFloat(cols.get(columns.indexOf("item_discounts")).getText().strip());
                        if (discountTotal != 0){
                            Discount groupDiscount = new Discount();
                            groupDiscount.setDiscount(discount.getDiscount());
                            groupDiscount.setAccount(discount.getAccount());

                            Discount discountParent = conversions.checkDiscountExistence(salesDiscount,
                                    majorGroup.getMajorGroup() + " Discount");

                            int oldDiscountIndex = salesDiscount.indexOf(discountParent);
                            if(oldDiscountIndex == -1){
                                groupDiscount.setTotal(discountTotal);
                                groupDiscount.setCostCenter(location);
                                salesDiscount.add(groupDiscount);
                            }else{
                                salesDiscount.get(oldDiscountIndex).setTotal(discountTotal + discountParent.getTotal());
                            }
                        }
                    }

                    majorGroupsGross = journal.checkExistence(majorGroupsGross, majorGroup
                            , 0, majorGroupGross, 0, 0, location, revenueCenter);
                }
                else if(taxIncluded && columns.indexOf("item_group") != -1){
                    WebElement col = cols.get(columns.indexOf("item_group"));
                    if (!col.getAttribute("class").equals("header_2")){ // Group
                        continue;
                    }

                    String majorGroupName = col.getText().strip().toLowerCase();
                    majorGroup = conversions.checkMajorGroupExistence(majorGroups,
                            majorGroupName);

                    if (!majorGroup.getChecked()) {
                        majorGroup = conversions.checkMajorGroupExistence(majorGroups,
                                majorGroupName + " "+ revenueCenter.getRevenueCenter().toLowerCase());

                        if (!majorGroup.getChecked()) {
                            continue;
                        }
                    }

                    Journal journal = new Journal();
                    float majorGroupGross;
                    float discountTotal;

                    if (grossDiscountSales.equals(Constants.SALES_GROSS_LESS_DISCOUNT)){
                        majorGroupGross = conversions.convertStringToFloat(cols.get(columns.indexOf("net_vat_after_disc.")).getText().strip());
                    }else {
                        majorGroupGross = conversions.convertStringToFloat(cols.get(columns.indexOf("net_vat_before_disc.")).getText().strip());
                    }
                    if(majorGroupDiscount){
                        discount = conversions.checkDiscountExistence(discounts, majorGroup.getMajorGroup() + " Discount");
                        if (!discount.isChecked()) {
                            break;
                        }
                        discountTotal = conversions.convertStringToFloat(cols.get(columns.indexOf("discounts_vat")).getText().strip());
                        if (discountTotal != 0){
                            Discount groupDiscount = new Discount();
                            groupDiscount.setDiscount(discount.getDiscount());
                            groupDiscount.setAccount(discount.getAccount());

                            Discount discountParent = conversions.checkDiscountExistence(salesDiscount,
                                    majorGroup.getMajorGroup() + " Discount");

                            int oldDiscountIndex = salesDiscount.indexOf(discountParent);
                            if(oldDiscountIndex == -1){
                                groupDiscount.setTotal(discountTotal);
                                groupDiscount.setCostCenter(location);
                                salesDiscount.add(groupDiscount);
                            }else{
                                salesDiscount.get(oldDiscountIndex).setTotal(discountTotal + discountParent.getTotal());
                            }
                        }
                    }

                    majorGroupsGross = journal.checkExistence(majorGroupsGross, majorGroup
                            , 0, majorGroupGross, 0, 0, location, revenueCenter);
                }
                else{
                    driver.quit();
                    response.setStatus(false);
                    response.setMessage("Failed to get majorGroup gross entries, Please contact support team.");
                    response.setEntries(new ArrayList<>());
                }
            }

            response.setStatus(true);
            response.setMessage("");
            response.setSalesMajorGroupGross(majorGroupsGross);
            response.setSalesDiscount(salesDiscount);
        } catch (Exception e) {
            driver.quit();

            response.setStatus(false);
            response.setMessage(e.getMessage());
            response.setEntries(new ArrayList<>());
        }

        return response;
    }

    private Response getSalesDiscount(String businessDate, String fromDate, String toDate, CostCenter location,
                                      boolean getDiscountTotalFlag, String totalDiscountsAccount,
                                      ArrayList<Discount> discounts, WebDriver driver) {
        Response response = new Response();
        ArrayList<Discount> salesDiscount = new ArrayList<>();

        driver.get(Constants.DISCOUNT_REPORT_LINK);
        try{
            WebDriverWait wait = new WebDriverWait(driver, 20);
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("loadingFrame")));
        } catch (Exception Ex) {
            System.out.println("There is no loader");
        }

        String message = "";
        int tryMaxCount = 2;
        do{
            Response dateResponse = setupEnvironment.selectTimePeriodOHRA(businessDate, fromDate, toDate,
                    location.locationName, "", driver);

            if (!dateResponse.isStatus()){
                response.setStatus(false);
                response.setMessage(dateResponse.getMessage());
                return response;
            }

            driver.findElement(By.id("Run Report")).click();

            /*
             * Check if selenium failed to select business date, and re-try
             * */
            try {
                Alert locationAlert = driver.switchTo().alert();
                message = locationAlert.getText();
                locationAlert.accept();
            }catch (NoAlertPresentException Ex) {
                System.out.println("No alert exits");
            }
            tryMaxCount --;
        }while (message.equals(Constants.EMPTY_BUSINESS_DATE) && tryMaxCount != 0);

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

                WebElement td = cols.get(0);
                if(getDiscountTotalFlag){
                    if (td.getText().equals("Total Discounts:")) {
                        Discount newDiscount = new Discount();

                        float discountTotal = conversions.convertStringToFloat(cols.get(columns.indexOf("total")).getText().strip());

                        newDiscount.setDiscount("Total Discount");
                        newDiscount.setAccount(totalDiscountsAccount);
                        newDiscount.setTotal(discountTotal);
                        newDiscount.setCostCenter(location);
                        salesDiscount.add(newDiscount);
                        break;
                    }
                }else{
                    if (columns.indexOf("discount_type") != -1){
                        td = cols.get(columns.indexOf("discount_type"));
                        discount = conversions.checkDiscountExistence(discounts, td.getText().strip().toLowerCase());

                        if (!discount.isChecked()) {
                            continue;
                        }

                        Discount newDiscount = new Discount();

                        float discountTotal = conversions.convertStringToFloat(cols.get(columns.indexOf("total")).getText().strip());

                        newDiscount.setDiscount(discount.getDiscount());
                        newDiscount.setAccount(discount.getAccount());
                        newDiscount.setTotal(discountTotal);
                        newDiscount.setCostCenter(location);
                        salesDiscount.add(newDiscount);
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

    private Response getTotalSalesServiceCharge(String businessDate, String fromDate, String toDate,
                                      CostCenter location, boolean getSCTotalFlag, String totalSCAccount,
                                                ArrayList<ServiceCharge> serviceCharges,WebDriver driver) {
        Response response = new Response();
        ArrayList<ServiceCharge> salesServiceCharges = new ArrayList<>();

        driver.get(Constants.SERVICE_CHARGE_REPORT_LINK);
        try{
            WebDriverWait wait = new WebDriverWait(driver, 20);
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("loadingFrame")));
        } catch (Exception Ex) {
            System.out.println("There is no loader");
        }

        String message = "";
        int tryMaxCount = 2;

        do{
            Response dateResponse = setupEnvironment.selectTimePeriodOHRA(businessDate, fromDate, toDate, location.locationName,
                    "", driver);

            if (!dateResponse.isStatus()){
                response.setStatus(false);
                response.setMessage(dateResponse.getMessage());
                return response;
            }

            driver.findElement(By.id("Run Report")).click();

            /*
             * Check if selenium failed to select business date, and re-try
             * */
            try {
                Alert locationAlert = driver.switchTo().alert();
                message = locationAlert.getText();
                locationAlert.accept();
            }catch (NoAlertPresentException Ex) {
                System.out.println("No alert exits");
            }
            tryMaxCount --;

        }while (message.equals(Constants.EMPTY_BUSINESS_DATE) && tryMaxCount != 0);

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

            ServiceCharge serviceCharge;
            for (int i = 6; i < rows.size(); i++) {
                WebElement row = rows.get(i);
                List<WebElement> cols = row.findElements(By.tagName("td"));

                if (cols.size() != columns.size()) {
                    continue;
                }

                WebElement td = cols.get(0);
                if(getSCTotalFlag){
                    if (td.getText().equals("Total Service Charges:")) {
                        float serviceChargeTotal = conversions.convertStringToFloat(cols.get(columns.indexOf("total")).getText().strip());
                        serviceCharge = new ServiceCharge();
                        serviceCharge.setServiceCharge("Total Service Charge");
                        serviceCharge.setAccount(totalSCAccount);
                        serviceCharge.setTotal(serviceChargeTotal);
                        serviceCharge.setCostCenter(location);
                        salesServiceCharges.add(serviceCharge);
                        break;
                    }
                }else{
                    serviceCharge = conversions.checkServiceChargeExistence(serviceCharges, td.getText(), location.locationName);
                    if(!serviceCharge.isChecked()){
                        continue;
                    }

                    float serviceChargeTotal = conversions.convertStringToFloat(cols.get(columns.indexOf("total")).getText().strip());
                    serviceCharge.setTotal(serviceChargeTotal);
                    serviceCharge.setCostCenter(location);
                    salesServiceCharges.add(serviceCharge);
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

        String businessDate =  syncJobType.getConfiguration().timePeriod;
        String fromDate =  syncJobType.getConfiguration().fromDate;
        ArrayList<Discount> includedDiscountTypes = syncJobType.getConfiguration().salesConfiguration.discounts;

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

                float subTenderTotal = tender.getTotal();
                float tenderCommunicationTotal;

                if (tender.getCommunicationRate() > 0){
                    tenderCommunicationTotal = (subTenderTotal * tender.getCommunicationRate())/100;
                    subTenderTotal = subTenderTotal - tenderCommunicationTotal;

                    // Create two entries for tender
                    HashMap<String, String> tenderData = new HashMap<>();

                    tenderData.put("accountingPeriod", transactionDate.substring(2,6));
                    tenderData.put("transactionDate", transactionDate);

                    if (tender.getTotal() < 0){
                        tenderData.put("totalDr", String.valueOf(conversions.roundUpFloat(tenderCommunicationTotal)));
                    }else {
                        tenderData.put("totalDr", String.valueOf(conversions.roundUpFloat(tenderCommunicationTotal) * -1));
                    }

                    tenderData.put("fromCostCenter", tender.getCostCenter().costCenter);
                    tenderData.put("fromAccountCode", tender.getCostCenter().accountCode);

                    tenderData.put("toCostCenter", tender.getCostCenter().costCenter);
                    tenderData.put("toAccountCode", tender.getCostCenter().accountCode);

                    tenderData.put("fromLocation", tender.getCostCenter().accountCode);
                    tenderData.put("toLocation", tender.getCostCenter().accountCode);

                    tenderData.put("transactionReference", "Tender");

                    tenderData.put("expensesAccount", tender.getCommunicationAccount());

                    String description = "";
                    if (tender.getCostCenter().costCenterReference.equals("")){
                        description = tender.getCommunicationTender();
                    }else{
                        description = tender.getCostCenter().costCenterReference + " " + tender.getCommunicationTender();
                    }

                    if (description.length() > 50) {
                        description = description.substring(0, 50);
                    }

                    tenderData.put("description", description);

                    if(!tender.getAnalysisCodeT5().equals("")){
                        tenderData.put("analysisCodeT5", tender.getAnalysisCodeT5());
                    }

                    SyncJobData syncJobData = new SyncJobData(tenderData, Constants.RECEIVED, "", new Date(),
                            syncJob.getId());
                    syncJobDataRepo.save(syncJobData);
                    journalBatch.getSalesTenderData().add(syncJobData);
                }

                HashMap<String, String> tenderData = new HashMap<>();

                tenderData.put("accountingPeriod", transactionDate.substring(2,6));
                tenderData.put("transactionDate", transactionDate);

                if (tender.getTotal() < 0){
                    tenderData.put("totalDr", String.valueOf(conversions.roundUpFloat(subTenderTotal)));
                }else {
                    tenderData.put("totalDr", String.valueOf(conversions.roundUpFloat(subTenderTotal) * -1));
                }

                tenderData.put("fromCostCenter", tender.getCostCenter().costCenter);
                tenderData.put("fromAccountCode", tender.getCostCenter().accountCode);

                tenderData.put("toCostCenter", tender.getCostCenter().costCenter);
                tenderData.put("toAccountCode", tender.getCostCenter().accountCode);

                tenderData.put("fromLocation", tender.getCostCenter().accountCode);
                tenderData.put("toLocation", tender.getCostCenter().accountCode);

                tenderData.put("transactionReference", "Tender");

                tenderData.put("expensesAccount", tender.getAccount());

                String description = "";
                if (tender.getCostCenter().costCenterReference.equals("")){
                    description = tender.getTender();
                }else{
                    description = tender.getCostCenter().costCenterReference + " " + tender.getTender();
                }
                if (description.length() > 50) {
                    description = description.substring(0, 50);
                }

                tenderData.put("description", description);

                if(!tender.getAnalysisCodeT5().equals("")){
                    tenderData.put("analysisCodeT5", tender.getAnalysisCodeT5());
                }

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

                taxData.put("transactionReference", "Taxes");
                taxData.put("inventoryAccount", tax.getAccount());

                String description = "";
                if (tax.getCostCenter().costCenterReference.equals("")){
                    description = tax.getTax();
                }else {
                    description = tax.getCostCenter().costCenterReference + " " + tax.getTax();
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

                majorGroupData.put("transactionReference", "MajorGroup");

                // Major Group account
                majorGroupData.put("inventoryAccount", majorGroupJournal.getMajorGroup().getAccount());

                String description = "";
                if (majorGroupJournal.getCostCenter().costCenterReference.equals("")){
                    description = majorGroupJournal.getMajorGroup().getMajorGroup() ;
                }else {
                    description = majorGroupJournal.getCostCenter().costCenterReference + " " + majorGroupJournal.getMajorGroup().getMajorGroup();
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

                majorGroupData.put("transactionReference", "MajorGroup");
                majorGroupData.put("inventoryAccount", serviceCharge.getAccount());

                String description = "";
                if (serviceCharge.getCostCenter().costCenterReference.equals("")){
                    description = "Service Charge";
                }else{
                    description = serviceCharge.getCostCenter().costCenterReference + " " + serviceCharge.getServiceCharge();
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

                if (syncJobType.getConfiguration().salesConfiguration.grossDiscountSales.equals(Constants.SALES_GROSS)
                && account.getERD().equals(Constants.EXPORT_TO_SUN_ERD)){
                    discountData.put("expensesAccount", discount.getAccount());
                    discountData.put("totalDr", String.valueOf(conversions.roundUpFloat(discount.getTotal())));
                }else{
                    discountData.put("inventoryAccount", discount.getAccount());
                    discountData.put("totalCr", String.valueOf(conversions.roundUpFloat(discount.getTotal())));
                }

                discountData.put("fromCostCenter", discount.getCostCenter().costCenter);
                discountData.put("fromAccountCode", discount.getCostCenter().accountCode);

                discountData.put("toCostCenter", discount.getCostCenter().costCenter);
                discountData.put("toAccountCode", discount.getCostCenter().accountCode);

                discountData.put("fromLocation", discount.getCostCenter().accountCode);
                discountData.put("toLocation", discount.getCostCenter().accountCode);
                discountData.put("transactionReference", "Discount");

                String description = "";
                if(discount.getDiscount().equals("")){
                    description = "Discount Cost";
                }else{
                    description = discount.getDiscount();
                }

                if (!discount.getCostCenter().costCenterReference.equals("")){
                    description = discount.getCostCenter().costCenterReference + " " + description;
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
                    if (discount.getCostCenter().costCenterReference.equals("")){
                        description = "Discount Expense";
                    }else{
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
                    if (discount.getCostCenter().costCenterReference.equals("")){
                        description = "AR account";
                    }else{
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
            if(syncJobType.getConfiguration().salesConfiguration.grossDiscountSales.equals(Constants.SALES_GROSS)){
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
                    String cashShortagePOS = syncJobType.getConfiguration().salesConfiguration.cashShortagePOS;
                    differentData.put("totalDr", String.valueOf(conversions.roundUpFloat(totalCr - totalDr)));
                    differentData.put("expensesAccount", cashShortagePOS);
                }
                // {Credit} - SurplusPOS
                else {
                    String cashSurplusPOS = syncJobType.getConfiguration().salesConfiguration.cashSurplusPOS;
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
                differentData.put("transactionReference", "Different");

                String description = "";
                if (journalBatch.getCostCenter().costCenterReference.equals("")){
                    description = "Different";
                }else{
                    description = journalBatch.getCostCenter().costCenterReference + " - different";
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
