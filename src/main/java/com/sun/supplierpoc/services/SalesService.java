package com.sun.supplierpoc.services;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.controllers.InvoiceController;
import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.models.configurations.*;
import com.sun.supplierpoc.repositories.SyncJobDataRepo;
import com.sun.supplierpoc.seleniumMethods.SetupEnvironment;
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
    @Autowired
    SyncJobDataService syncJobDataService;

    private Conversions conversions = new Conversions();
    private SetupEnvironment setupEnvironment = new SetupEnvironment();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public Response getSalesData(SyncJobType salesSyncJobType,
                                 ArrayList<CostCenter> costCentersLocation, ArrayList<MajorGroup> majorGroups,
                                 ArrayList<Tender> includedTenders,  ArrayList<Tax> includedTax,
                                 ArrayList<Discount> includedDiscount, ArrayList<ServiceCharge> includedServiceCharge,
                                 ArrayList<RevenueCenter> revenueCenters, ArrayList<SalesStatistics> statistics,
                                 Account account) {

        Response response = new Response();
        ArrayList<JournalBatch> journalBatches = new ArrayList<>();

        String timePeriod = salesSyncJobType.getConfiguration().timePeriod;
        String fromDate = salesSyncJobType.getConfiguration().fromDate;
        String toDate = salesSyncJobType.getConfiguration().toDate;

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
            if (!setupEnvironment.loginOHRA(driver, Constants.OHRA_LINK, account)) {
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
            } catch (Exception Ex) { }

            if (costCentersLocation.size() > 0){
                for (CostCenter costCenter : costCentersLocation) {
                    if(costCenter.checked){
                        callSalesFunction(salesSyncJobType, majorGroups, includedTenders, includedTax, includedDiscount,
                                includedServiceCharge, revenueCenters, statistics, timePeriod, fromDate, toDate, costCenter,
                                journalBatches, driver, response);
                        if (!response.isStatus() && !response.getMessage().equals(Constants.INVALID_LOCATION)){
                            return response;
                        }
                    }
                }
            }
            else {
                callSalesFunction(salesSyncJobType, majorGroups, includedTenders, includedTax, includedDiscount,
                        includedServiceCharge, revenueCenters, statistics, timePeriod, fromDate, toDate, new CostCenter(),
                        journalBatches, driver, response);
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
                                   ArrayList<RevenueCenter> revenueCenters, ArrayList<SalesStatistics> statistics,
                                   String timePeriod, String fromDate, String toDate, CostCenter costCenter,
                                   ArrayList<JournalBatch> journalBatches,
                                   WebDriver driver, Response response){
        JournalBatch journalBatch = new JournalBatch();
        SalesConfiguration configuration = salesSyncJobType.getConfiguration().salesConfiguration;

        // Get statistics
        Response statisticsResponse = new Response();
        if (statistics.size() > 0){
            statisticsResponse = getSalesStatistics(timePeriod, fromDate, toDate, costCenter, statistics, driver);
            if (checkSalesFunctionResponse(driver, response, statisticsResponse)) return;
        }

        // Get tender
        Response tenderResponse = new Response();
        if(includedTenders.size() > 0){
            tenderResponse = getSalesTenders(timePeriod, fromDate, toDate,
                    costCenter, includedTenders, driver);
            if (checkSalesFunctionResponse(driver, response, tenderResponse)) return;
        }

        // Get Major Groups/Family Groups net sales
        String grossDiscountSales = configuration.grossDiscountSales;
        boolean majorGroupDiscount = configuration.MGDiscount;
        boolean revenueCenterDiscount = configuration.RVDiscount;
        boolean syncMajorGroups = configuration.syncMG;
        boolean taxIncluded = configuration.taxIncluded;

        ArrayList<Journal> salesMajorGroupsGross = new ArrayList<>();
        ArrayList<Discount> salesDiscounts = new ArrayList<>();

        if (revenueCenters.size() > 0 ){
            for (RevenueCenter rc : revenueCenters)
            {
                if(!rc.isChecked()){
                    continue;
                }
                Response overGroupGrossResponse;

                overGroupGrossResponse = getSalesMajorGroups(taxIncluded, rc, timePeriod, fromDate, toDate, costCenter,
                        majorGroups, grossDiscountSales, majorGroupDiscount, revenueCenterDiscount, syncMajorGroups,
                        driver);

                if (checkSalesFunctionResponse(driver, response, overGroupGrossResponse)) return;

                if (majorGroupDiscount || revenueCenterDiscount){
                    salesDiscounts.addAll(overGroupGrossResponse.getSalesDiscount());
                }

                salesMajorGroupsGross.addAll(overGroupGrossResponse.getSalesMajorGroupGross());
            }
        }
        else {
            Response overGroupGrossResponse;

            overGroupGrossResponse = getSalesMajorGroups(taxIncluded, new RevenueCenter(), timePeriod, fromDate, toDate, costCenter,
                    majorGroups, grossDiscountSales, majorGroupDiscount, revenueCenterDiscount, syncMajorGroups,
                    driver);

            if (checkSalesFunctionResponse(driver, response, overGroupGrossResponse)) return;

            if (majorGroupDiscount || revenueCenterDiscount){
                salesDiscounts.addAll(overGroupGrossResponse.getSalesDiscount());
            }

            salesMajorGroupsGross.addAll(overGroupGrossResponse.getSalesMajorGroupGross());
        }


        // Get discounts
        Response discountResponse;
        boolean syncTotalDiscounts = configuration.syncTotalDiscounts;
        String totalDiscountsAccount = configuration.totalDiscountsAccount;

        if (includedDiscount.size() > 0 || syncTotalDiscounts){
            discountResponse = getSalesDiscount(timePeriod, fromDate, toDate, costCenter,
                    syncTotalDiscounts, totalDiscountsAccount, includedDiscount, driver);
            if (checkSalesFunctionResponse(driver, response, discountResponse)) return;
            salesDiscounts.addAll(discountResponse.getSalesDiscount());
        }

        // Get taxes
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

        // Set Debit Entries (Tenders)
        journalBatch.setSalesTender(tenderResponse.getSalesTender());

        // Set Credit Entries (Taxes, overGroupsGross, Discount and Service charge)
        journalBatch.setSalesTax(taxResponse.getSalesTax());
        journalBatch.setSalesMajorGroupGross(salesMajorGroupsGross);
        journalBatch.setSalesDiscount(salesDiscounts);
        journalBatch.setSalesServiceCharge(serviceChargeResponse.getSalesServiceCharge());

        // Set Statistics Info
        journalBatch.setSalesStatistics(statisticsResponse.getSalesStatistics());

        // Calculate different
        journalBatch.setSalesDifferent(0.0);
        journalBatch.setCostCenter(costCenter);
        journalBatches.add(journalBatch);

        response.setStatus(true);
    }

    public boolean checkSalesFunctionResponse(WebDriver driver, Response response, Response reportResponse) {
        if (!reportResponse.isStatus()) {
            response.setMessage(reportResponse.getMessage());
            response.setStatus(false);

            if(reportResponse.getMessage().equals(Constants.INVALID_BUSINESS_DATE)){
                driver.quit();
            } else if(reportResponse.getMessage().equals(Constants.WRONG_BUSINESS_DATE)){
                driver.quit();
            } else return !reportResponse.getMessage().equals(Constants.INVALID_REVENUE_CENTER);
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
        if (setupEnvironment.runReport(businessDate, fromDate, toDate, location, new RevenueCenter(), driver, response)) return response;

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
        if (setupEnvironment.runReport(businessDate, fromDate, toDate, location, new RevenueCenter(), driver, response)) return response;

        List<WebElement> rows;
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

            rows = driver.findElements(By.tagName("tr"));
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

    private Response getSalesMajorGroups(boolean taxIncluded, RevenueCenter revenueCenter, String businessDate,
                                         String fromDate, String toDate,
                                         CostCenter location, ArrayList<MajorGroup> majorGroups,
                                         String grossDiscountSales, boolean majorGroupDiscount,
                                         boolean revenueCenterDiscount, boolean syncMajorGroups,
                                         WebDriver driver) {
        Response response = new Response();
        ArrayList<Journal> majorGroupsGross = new ArrayList<>();
        ArrayList<Discount> salesDiscount = new ArrayList<>();

        if(taxIncluded)
            driver.get(Constants.SYSTEM_SALES_REPORT_LINK);
        else
            driver.get(Constants.OVER_GROUP_GROSS_REPORT_LINK);

        if (setupEnvironment.runReport(businessDate, fromDate, toDate, location, revenueCenter, driver, response)) return response;

        String overGroupGrossLink;
        if(taxIncluded)
            overGroupGrossLink = Constants.OHRA_LINK + "/finengine/reportRunAction.do?method=run&reportID=EAME_SalesMixDailyDetail_VAT&rptroot=1191";
        else
            overGroupGrossLink = Constants.OHRA_LINK + "/finengine/reportRunAction.do?rptroot=15&reportID=SalesMixDailyDetail&method=run";

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

            if ((!taxIncluded && columns.indexOf("group") == -1) && (taxIncluded && columns.indexOf("item_group") != -1)){
                driver.quit();
                response.setStatus(false);
                response.setMessage("Failed to get majorGroup gross entries, Please contact support team.");
                response.setEntries(new ArrayList<>());
                return response;
            }

            MajorGroup majorGroup;
            RevenueCenter MGRevenueCenter = new RevenueCenter();
            String majorGroupName = "";
            String familyGroupName = "";

            float majorGroupAmount;
            float discountAmount;

            for (int i = 7; i < rows.size(); i++) {
                WebElement row = rows.get(i);
                List<WebElement> cols = row.findElements(By.tagName("td"));

                if (cols.size() != columns.size())
                    continue;

                Journal journal = new Journal();
                WebElement col;

                if(taxIncluded)
                    col = cols.get(columns.indexOf("item_group"));
                else
                    col = cols.get(columns.indexOf("group"));

                if (col.getAttribute("class").equals("header_1") || col.getAttribute("class").equals("header_2")){
                    majorGroupName = col.getText().strip().toLowerCase();
                    majorGroup = conversions.checkMajorGroupExistence(majorGroups, majorGroupName);

                    if (!majorGroup.getChecked()) {
                        continue;
                    }

                    if(!revenueCenter.getRevenueCenter().equals("")){
                        MGRevenueCenter = conversions.checkRevenueCenterExistence(majorGroup.getRevenueCenters(), revenueCenter.getRevenueCenter());
                    }

                    /*
                     * Sync major groups entries
                     * */
                    if(syncMajorGroups){
                        majorGroupAmount = getMajorGroupAmount(taxIncluded, grossDiscountSales, columns, cols);

                        majorGroupsGross = journal.checkExistence(majorGroupsGross, majorGroup
                                , 0, majorGroupAmount, 0, location, MGRevenueCenter, "");

                        if(taxIncluded)
                            discountAmount = conversions.convertStringToFloat(cols.get(columns.indexOf("discounts_vat")).getText().strip());
                        else
                            discountAmount = conversions.convertStringToFloat(cols.get(columns.indexOf("item_discounts")).getText().strip());

                        if (discountAmount != 0){
                            Discount groupDiscount = new Discount();

                            groupDiscount.setDiscount(majorGroup.getMajorGroup() + " Discount " + revenueCenter.getRevenueCenter());
                            if(majorGroupDiscount){
                                groupDiscount.setAccount(majorGroup.getDiscountAccount());
                            }else if (revenueCenterDiscount){
                                groupDiscount.setAccount(MGRevenueCenter.getDiscountAccount());
                            }

                            Discount discountParent = conversions.checkDiscountExistence(salesDiscount,
                                    majorGroup.getMajorGroup() + " Discount");

                            int oldDiscountIndex = salesDiscount.indexOf(discountParent);
                            if(oldDiscountIndex == -1){
                                groupDiscount.setTotal(discountAmount);
                                groupDiscount.setCostCenter(location);
                                salesDiscount.add(groupDiscount);
                            }else{
                                salesDiscount.get(oldDiscountIndex).setTotal(discountAmount + discountParent.getTotal());
                            }
                        }
                    }
                    /*
                     * Sync family groups entries
                     * */
                    else{
                        for (int j = i+1; j < rows.size(); j++) {
                            WebElement FGRow = rows.get(j);
                            List<WebElement> FGCols = FGRow.findElements(By.tagName("td"));
                            WebElement FGCol ;
                            if(taxIncluded)
                                FGCol = FGCols.get(columns.indexOf("item_group"));
                            else
                                FGCol = FGCols.get(columns.indexOf("group"));

                            if (FGCol.getAttribute("class").equals("header_1") || FGCol.getAttribute("class").equals("header_2")){
                                i = j-1;
                                break;
                            }

                            // Check if family group exists
                            familyGroupName = FGCol.getText().strip().toLowerCase();
                            FamilyGroup familyGroup = conversions.checkFamilyGroupExistence(majorGroup.getFamilyGroups()
                                    , familyGroupName);

                            if(familyGroup.familyGroup.equals(""))
                                continue;

                            majorGroupAmount = getMajorGroupAmount(taxIncluded, grossDiscountSales, columns, FGCols);

                            majorGroupsGross = journal.checkFGExistence(majorGroupsGross, majorGroup, familyGroup, majorGroupAmount
                                    , location, MGRevenueCenter, null, familyGroup.departmentCode);

                            if(taxIncluded)
                                discountAmount = conversions.convertStringToFloat(FGCols.get(columns.indexOf("discounts_vat")).getText().strip());
                            else
                                discountAmount = conversions.convertStringToFloat(FGCols.get(columns.indexOf("item_discounts")).getText().strip());

                            if (discountAmount != 0){
                                Discount familyGroupDiscount = new Discount();

                                familyGroupDiscount.setFamilyGroup(familyGroup);
                                familyGroupDiscount.setDiscount(familyGroup.familyGroup + " Discount " + revenueCenter.getRevenueCenter());
                                if(majorGroupDiscount){
                                    familyGroupDiscount.setAccount(majorGroup.getDiscountAccount());
                                }else if (revenueCenterDiscount){
                                    familyGroupDiscount.setAccount(MGRevenueCenter.getDiscountAccount());
                                }

                                Discount discountParent = conversions.checkDiscountExistence(salesDiscount,
                                        familyGroup.familyGroup + " Discount " + revenueCenter.getRevenueCenter());

                                int oldDiscountIndex = salesDiscount.indexOf(discountParent);
                                if(oldDiscountIndex == -1){
                                    familyGroupDiscount.setTotal(discountAmount);
                                    familyGroupDiscount.setCostCenter(location);
                                    salesDiscount.add(familyGroupDiscount);
                                }else{
                                    salesDiscount.get(oldDiscountIndex).setTotal(discountAmount + discountParent.getTotal());
                                }
                            }
                        }
                    }
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

    private float getMajorGroupAmount(boolean taxIncluded, String grossDiscountSales, ArrayList<String> columns, List<WebElement> cols) {
        float majorGroupAmount;
        if(taxIncluded){
            if (grossDiscountSales.equals(Constants.SALES_GROSS_LESS_DISCOUNT)){
                majorGroupAmount = conversions.convertStringToFloat(cols.get(columns.indexOf("net_vat_after_disc.")).getText().strip());
            }else {
                majorGroupAmount = conversions.convertStringToFloat(cols.get(columns.indexOf("net_vat_before_disc.")).getText().strip());
            }
        }else {
            if (grossDiscountSales.equals(Constants.SALES_GROSS_LESS_DISCOUNT)){
                majorGroupAmount = conversions.convertStringToFloat(cols.get(columns.indexOf("sales_less_item_disc")).getText().strip());
            }else {
                majorGroupAmount = conversions.convertStringToFloat(cols.get(columns.indexOf("gross_sales")).getText().strip());
            }
        }
        return majorGroupAmount;
    }

    private Response getSalesDiscount(String businessDate, String fromDate, String toDate, CostCenter location,
                                      boolean getDiscountTotalFlag, String totalDiscountsAccount,
                                      ArrayList<Discount> discounts, WebDriver driver) {
        Response response = new Response();
        ArrayList<Discount> salesDiscount = new ArrayList<>();

        driver.get(Constants.DISCOUNT_REPORT_LINK);
        if (setupEnvironment.runReport(businessDate, fromDate, toDate, location, new RevenueCenter(), driver, response)) return response;

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
        if (setupEnvironment.runReport(businessDate, fromDate, toDate, location, new RevenueCenter(), driver, response)) return response;

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

    private Response getSalesStatistics(String businessDate, String fromDate, String toDate, CostCenter location,
                                        ArrayList<SalesStatistics> statistics, WebDriver driver) {
        Response response = new Response();
        SalesStatistics salesStatistics = conversions.checkSalesStatisticsExistence(location.locationName, statistics);

        if(!salesStatistics.checked){
            response.setStatus(true);
            response.setMessage("Not Configured");
            return response;
        }

        driver.get(Constants.SYSTEM_SALES_REPORT_LINK);
        if (setupEnvironment.runReport(businessDate, fromDate, toDate, location, new RevenueCenter(), driver, response)) return response;

        try {
            driver.get(Constants.SALES_SUMMARY_LINK);

            WebElement statTable = driver.findElement(By.xpath("/html/body/div[6]/table"));
            List<WebElement> rows = statTable.findElements(By.tagName("tr"));

            if (rows.size() < 1){
                response.setStatus(true);
                response.setMessage("There is no statistics info in this location");
                return response;
            }

            ArrayList<String> columns = setupEnvironment.getTableColumns(rows, false, 0);
            ArrayList<String> statisticValues = setupEnvironment.getTableColumns(rows, false, 1);

            salesStatistics.NoGuest = conversions.filterString(statisticValues.get(columns.indexOf("guests")));
            salesStatistics.NoChecks = conversions.filterString(statisticValues.get(columns.indexOf("checks")));
            salesStatistics.NoTables = conversions.filterString(statisticValues.get(columns.indexOf("tables")));

            response.setStatus(true);
            response.setMessage("");
            response.setSalesStatistics(salesStatistics);
        } catch (Exception e) {
            driver.quit();
            response.setStatus(false);
            response.setMessage(e.getMessage());
        }
        return response;
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

    public ArrayList<JournalBatch> saveSalesJournalBatchesData(Response salesResponse, SyncJob syncJob,
                                                               Configuration configuration , Account account) {
        ArrayList<JournalBatch> addedJournalBatches = new ArrayList<>();

        String businessDate =  configuration.timePeriod;
        String fromDate =  configuration.fromDate;
        ArrayList<Discount> includedDiscountTypes = configuration.salesConfiguration.discounts;

        String transactionDate = conversions.getTransactionDate(businessDate, fromDate);

        ArrayList<JournalBatch> journalBatches = salesResponse.getJournalBatches();
        for (JournalBatch journalBatch : journalBatches) {
            float totalTender = 0;
            float totalDiscount = 0;
            float totalTax = 0;
            float totalServiceCharge = 0;
            float totalMajorGroupNet = 0;

            // Save majorGroup {Credit}
            ArrayList<Journal> majorGroupsGross = journalBatch.getSalesMajorGroupGross();
            for (Journal majorGroupJournal : majorGroupsGross) {
                if (majorGroupJournal.getTotalCost() == 0)
                    continue;

                saveMajorGroup(journalBatch, transactionDate, configuration, syncJob, majorGroupJournal);

                float majorGroupGrossTotal = majorGroupJournal.getTotalCost();
                totalMajorGroupNet += Math.abs(majorGroupGrossTotal);
            }

            // Save taxes {Credit}
            ArrayList<Tax> taxes = journalBatch.getSalesTax();
            for (Tax tax : taxes) {
                if (tax.getTotal() == 0)
                    continue;

                saveTax(journalBatch, transactionDate, configuration, syncJob, tax);

                float taxTotal = tax.getTotal();
                totalTax += taxTotal;
            }

            // Save service charge {Credit}
            ArrayList<ServiceCharge> serviceChargeGross = journalBatch.getSalesServiceCharge();
            for (ServiceCharge serviceCharge : serviceChargeGross) {
                if (serviceCharge.getTotal() == 0)
                    continue;

                saveServiceCharge(journalBatch, transactionDate, configuration, syncJob, serviceCharge);

                float serviceChargeTotal = serviceCharge.getTotal();
                totalServiceCharge += serviceChargeTotal;
            }

            ArrayList<Discount> discounts = journalBatch.getSalesDiscount();
            for (Discount discount : discounts) {
                if (discount.getTotal() == 0)
                    continue;

                HashMap<String, Object> discountData = new HashMap<>();

                discountData.put("accountingPeriod", transactionDate.substring(2,6));
                discountData.put("transactionDate", transactionDate);

                if (configuration.salesConfiguration.grossDiscountSales.equals(Constants.SALES_GROSS)
                        && account.getERD().equals(Constants.EXPORT_TO_SUN_ERD)){
                    discountData.put("expensesAccount", discount.getAccount());
                    discountData.put("totalDr", String.valueOf(conversions.roundUpFloat2Digest(discount.getTotal())));
                }else{
                    discountData.put("inventoryAccount", discount.getAccount());
                    discountData.put("totalCr", String.valueOf(conversions.roundUpFloat2Digest(discount.getTotal())));
                }

                discountData.put("fromCostCenter", discount.getCostCenter().costCenter);
                discountData.put("fromAccountCode", discount.getCostCenter().accountCode);

                discountData.put("toCostCenter", discount.getCostCenter().costCenter);
                discountData.put("toAccountCode", discount.getCostCenter().accountCode);

                discountData.put("fromLocation", discount.getCostCenter().accountCode);
                discountData.put("toLocation", discount.getCostCenter().accountCode);

                String description = "";
                String reference = "Discount";

                if(discount.getDiscount().equals("")){
                    description = "Discount Cost";
                }else{
                    description = discount.getDiscount();
                }

                if (!discount.getCostCenter().costCenterReference.equals("")){
                    reference = discount.getCostCenter().costCenterReference;
                    description = discount.getCostCenter().costCenterReference + " " + description;
                }

                if (description.length() > 50) {
                    description = description.substring(0, 50);
                }

                discountData.put("description", description);
                discountData.put("transactionReference", reference);

                syncJobDataService.prepareAnalysis(discountData, configuration, discount.getCostCenter(), discount.getFamilyGroup(), null);

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
                HashMap<String, Object> discountData = new HashMap<>();

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


                    discountData.put("expensesAccount", discount.getAccount());
                    syncJobDataService.prepareAnalysis(discountData, configuration, discount.getCostCenter(), discount.getFamilyGroup(), null);

                    String description = "";
                    String reference = "Discount";
                    if (discount.getCostCenter().costCenterReference.equals("")){
                        description = "Discount Expense";
                    }else{
                        description = "Discount Expense F " + discount.getCostCenter().costCenterReference;
                        reference = discount.getCostCenter().costCenterReference;
                    }
                    if (description.length() > 50) {
                        description = description.substring(0, 50);
                    }

                    discountData.put("description", description);
                    discountData.put("transactionReference", reference);


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

                    discountData.put("expensesAccount", discount.getAccount());

                    String description = "";
                    String reference = "AR account";
                    if (discount.getCostCenter().costCenterReference.equals("")){
                        description = "AR account";
                    }else{
                        description = "AR account F " + discount.getCostCenter().costCenterReference;
                        reference = discount.getCostCenter().costCenterReference;
                    }
                    if (description.length() > 50) {
                        description = description.substring(0, 50);
                    }

                    discountData.put("description", description);
                    discountData.put("transactionReference", reference);

                    syncJobDataService.prepareAnalysis(discountData, configuration, discount.getCostCenter(), discount.getFamilyGroup(), null);

                    SyncJobData syncJobData = new SyncJobData(discountData, Constants.RECEIVED, "", new Date(),
                            syncJob.getId());
                    syncJobDataRepo.save(syncJobData);
                    journalBatch.getSalesDiscountData().add(syncJobData);
                }
            }

            // Save tenders {Debit}
            ArrayList<Tender> tenders = journalBatch.getSalesTender();
            for (Tender tender : tenders) {
                if (tender.getTotal() == 0)
                    continue;

                saveTender(journalBatch, transactionDate, configuration, syncJob, tender);

                float tenderTotal = conversions.roundUpFloat2Digest(tender.getTotal());
                totalTender += tenderTotal;
            }

            float totalDr = totalTender;
            float totalCr;
            if(configuration.salesConfiguration.grossDiscountSales.equals(Constants.SALES_GROSS)){
                totalCr = totalMajorGroupNet + totalDiscount + totalTax + totalServiceCharge;
            }else
            {
                totalCr = totalMajorGroupNet + totalTax + totalServiceCharge;
            }

            if (totalCr != totalDr) {
                HashMap<String, Object> differentData = new HashMap<>();

                differentData.put("accountingPeriod", transactionDate.substring(2,6));
                differentData.put("transactionDate", transactionDate);

                // {Debit} - ShortagePOS
                if (totalCr > totalDr ) {
                    String cashShortagePOS = configuration.salesConfiguration.cashShortagePOS;
                    differentData.put("totalDr", String.valueOf(conversions.roundUpFloat2Digest(totalCr - totalDr)));
                    differentData.put("expensesAccount", cashShortagePOS);
                }
                // {Credit} - SurplusPOS
                else {
                    String cashSurplusPOS = configuration.salesConfiguration.cashSurplusPOS;
                    differentData.put("totalCr", String.valueOf(conversions.roundUpFloat2Digest(totalDr - totalCr)));
                    differentData.put("inventoryAccount", cashSurplusPOS);
                }

                differentData.put("fromCostCenter", journalBatch.getCostCenter().costCenter);
                differentData.put("fromAccountCode", journalBatch.getCostCenter().accountCode);

                differentData.put("toCostCenter", journalBatch.getCostCenter().costCenter);
                differentData.put("toAccountCode", journalBatch.getCostCenter().accountCode);

                differentData.put("fromLocation", journalBatch.getCostCenter().accountCode);
                differentData.put("toLocation", journalBatch.getCostCenter().accountCode);

                String description = "";
                String reference = "Different";

                if (journalBatch.getCostCenter().costCenterReference.equals("")){
                    description = "Different";
                }else{
                    description = journalBatch.getCostCenter().costCenterReference + " - different";
                    reference = journalBatch.getCostCenter().costCenterReference;
                }

                if (description.length() > 50){
                    description = description.substring(0, 50);
                }

                differentData.put("description", description);
                differentData.put("transactionReference", reference);

                syncJobDataService.prepareAnalysis(differentData, configuration, journalBatch.getCostCenter(), null, null);

                SyncJobData syncJobData = new SyncJobData(differentData, Constants.RECEIVED, "", new Date(),
                        syncJob.getId());
                syncJobDataRepo.save(syncJobData);
                journalBatch.setSalesDifferentData(syncJobData);
            }

            // Statistics
            if(journalBatch.getSalesStatistics().checked){
                saveSalesStatistics(journalBatch, transactionDate, configuration, syncJob);
            }

            addedJournalBatches.add(journalBatch);
        }
        return addedJournalBatches;
    }

    private void saveSalesStatistics(JournalBatch journalBatch, String transactionDate, Configuration configuration,
                                     SyncJob syncJob){
        HashMap<String, Object> statisticsData = new HashMap<>();

        statisticsData.put("accountingPeriod", transactionDate.substring(2,6));
        statisticsData.put("transactionDate", transactionDate);

        statisticsData.put("fromCostCenter", journalBatch.getCostCenter().costCenter);
        statisticsData.put("fromAccountCode", journalBatch.getCostCenter().accountCode);
        statisticsData.put("fromLocation", journalBatch.getCostCenter().accountCode);
        statisticsData.put("toLocation", journalBatch.getCostCenter().accountCode);
        statisticsData.put("toCostCenter", journalBatch.getCostCenter().costCenter);
        statisticsData.put("toAccountCode", journalBatch.getCostCenter().accountCode);

        // Number of guests
        statisticsData.put("totalCr", journalBatch.getSalesStatistics().NoGuest);
        statisticsData.put("inventoryAccount", journalBatch.getSalesStatistics().NoGuestAccount);

        String description = "No Guests";
        String reference = "No Guests";

        if (!journalBatch.getCostCenter().costCenterReference.equals("")){
            description = journalBatch.getCostCenter().costCenterReference + " - " + description;
            reference = journalBatch.getCostCenter().costCenterReference;
        }
        statisticsData.put("transactionReference", reference);
        statisticsData.put("description", description);

        syncJobDataService.prepareAnalysis(statisticsData, configuration, journalBatch.getCostCenter(), null, null);

        SyncJobData syncJobGuestsData = new SyncJobData(statisticsData, Constants.RECEIVED, "", new Date(),
                syncJob.getId());
        syncJobDataRepo.save(syncJobGuestsData);
        journalBatch.getStatisticsData().add(syncJobGuestsData);

        // Number of tables
        statisticsData = new HashMap<>();

        statisticsData.put("accountingPeriod", transactionDate.substring(2,6));
        statisticsData.put("transactionDate", transactionDate);

        statisticsData.put("fromCostCenter", journalBatch.getCostCenter().costCenter);
        statisticsData.put("fromAccountCode", journalBatch.getCostCenter().accountCode);
        statisticsData.put("fromLocation", journalBatch.getCostCenter().accountCode);
        statisticsData.put("toLocation", journalBatch.getCostCenter().accountCode);
        statisticsData.put("toCostCenter", journalBatch.getCostCenter().costCenter);
        statisticsData.put("toAccountCode", journalBatch.getCostCenter().accountCode);
        statisticsData.put("totalCr", journalBatch.getSalesStatistics().NoTables);
        statisticsData.put("inventoryAccount", journalBatch.getSalesStatistics().NoTablesAccount);

        description = "No Tables";
        reference = "No Tables";
        if (!journalBatch.getCostCenter().costCenterReference.equals("")){
            description = journalBatch.getCostCenter().costCenterReference + " - " + description;
            reference = journalBatch.getCostCenter().costCenterReference;
        }
        statisticsData.put("transactionReference", reference);
        statisticsData.put("description", description);

        syncJobDataService.prepareAnalysis(statisticsData, configuration, journalBatch.getCostCenter(), null, null);

        SyncJobData syncJobTablesData = new SyncJobData(statisticsData, Constants.RECEIVED, "", new Date(),
                syncJob.getId());
        syncJobDataRepo.save(syncJobTablesData);
        journalBatch.getStatisticsData().add(syncJobTablesData);

        // Number of checks
        statisticsData = new HashMap<>();

        statisticsData.put("accountingPeriod", transactionDate.substring(2,6));
        statisticsData.put("transactionDate", transactionDate);

        statisticsData.put("fromCostCenter", journalBatch.getCostCenter().costCenter);
        statisticsData.put("fromAccountCode", journalBatch.getCostCenter().accountCode);
        statisticsData.put("fromLocation", journalBatch.getCostCenter().accountCode);
        statisticsData.put("toLocation", journalBatch.getCostCenter().accountCode);
        statisticsData.put("toCostCenter", journalBatch.getCostCenter().costCenter);
        statisticsData.put("toAccountCode", journalBatch.getCostCenter().accountCode);
        statisticsData.put("totalCr", journalBatch.getSalesStatistics().NoChecks);
        statisticsData.put("inventoryAccount", journalBatch.getSalesStatistics().NoChecksAccount);

        description = "No Checks";
        reference= "No Checks";
        if (!journalBatch.getCostCenter().costCenterReference.equals("")){
            description = journalBatch.getCostCenter().costCenterReference + " - " + description;
            reference = journalBatch.getCostCenter().costCenterReference;
        }
        statisticsData.put("transactionReference", reference);
        statisticsData.put("description", description);

        syncJobDataService.prepareAnalysis(statisticsData, configuration, journalBatch.getCostCenter(), null, null);

        SyncJobData syncJobChecksData = new SyncJobData(statisticsData, Constants.RECEIVED, "", new Date(),
                syncJob.getId());
        syncJobDataRepo.save(syncJobChecksData);
        journalBatch.getStatisticsData().add(syncJobChecksData);
    }

    private void saveTender(JournalBatch journalBatch, String transactionDate, Configuration configuration,
                            SyncJob syncJob, Tender tender){
        float subTenderTotal = tender.getTotal();
        float tenderCommunicationTotal;

        String reference = "Tender";

        if (tender.getCommunicationRate() > 0){
            tenderCommunicationTotal = (subTenderTotal * tender.getCommunicationRate())/100;
            subTenderTotal = subTenderTotal - tenderCommunicationTotal;

            // Create two entries for tender
            HashMap<String, Object> tenderData = new HashMap<>();

            tenderData.put("accountingPeriod", transactionDate.substring(2,6));
            tenderData.put("transactionDate", transactionDate);

            if (tender.getTotal() < 0){
                tenderData.put("totalDr", String.valueOf(conversions.roundUpFloat2Digest(tenderCommunicationTotal)));
            }else {
                tenderData.put("totalDr", String.valueOf(conversions.roundUpFloat2Digest(tenderCommunicationTotal) * -1));
            }

            tenderData.put("fromCostCenter", tender.getCostCenter().costCenter);
            tenderData.put("fromAccountCode", tender.getCostCenter().accountCode);

            tenderData.put("toCostCenter", tender.getCostCenter().costCenter);
            tenderData.put("toAccountCode", tender.getCostCenter().accountCode);

            tenderData.put("fromLocation", tender.getCostCenter().accountCode);
            tenderData.put("toLocation", tender.getCostCenter().accountCode);


            tenderData.put("expensesAccount", tender.getCommunicationAccount());

            String description = "";

            if (tender.getCostCenter().costCenterReference.equals("")){
                description = tender.getCommunicationTender();
            }else{
                description = tender.getCostCenter().costCenterReference + " " + tender.getCommunicationTender();
                reference = tender.getCostCenter().costCenterReference;
            }

            if (description.length() > 50) {
                description = description.substring(0, 50);
            }

            tenderData.put("description", description);
            tenderData.put("transactionReference", reference);

            if(configuration.salesConfiguration.addTenderAnalysis)
                syncJobDataService.prepareAnalysis(tenderData, configuration, tender.getCostCenter(), null, tender);
            else
                syncJobDataService.prepareAnalysis(tenderData, configuration, null, null, null);

            SyncJobData syncJobData = new SyncJobData(tenderData, Constants.RECEIVED, "", new Date(),
                    syncJob.getId());
            syncJobDataRepo.save(syncJobData);
            journalBatch.getSalesTenderData().add(syncJobData);
        }

        HashMap<String, Object> tenderData = new HashMap<>();

        tenderData.put("accountingPeriod", transactionDate.substring(2,6));
        tenderData.put("transactionDate", transactionDate);

        if (tender.getTotal() < 0){
            tenderData.put("totalDr", String.valueOf(conversions.roundUpFloat2Digest(subTenderTotal)));
        }else {
            tenderData.put("totalDr", String.valueOf(conversions.roundUpFloat2Digest(subTenderTotal) * -1));
        }

        tenderData.put("fromCostCenter", tender.getCostCenter().costCenter);
        tenderData.put("fromAccountCode", tender.getCostCenter().accountCode);

        tenderData.put("toCostCenter", tender.getCostCenter().costCenter);
        tenderData.put("toAccountCode", tender.getCostCenter().accountCode);

        tenderData.put("fromLocation", tender.getCostCenter().accountCode);
        tenderData.put("toLocation", tender.getCostCenter().accountCode);

        tenderData.put("expensesAccount", tender.getAccount());

        String description = "";
        if (tender.getCostCenter().costCenterReference.equals("")){
            description = tender.getTender();
        }else{
            description = tender.getCostCenter().costCenterReference + " " + tender.getTender();
            reference = tender.getCostCenter().costCenterReference;
        }
        if (description.length() > 50) {
            description = description.substring(0, 50);
        }

        tenderData.put("description", description);
        tenderData.put("transactionReference", reference);

        if(configuration.salesConfiguration.addTenderAnalysis)
            syncJobDataService.prepareAnalysis(tenderData, configuration, tender.getCostCenter(), null, tender);
        else
            syncJobDataService.prepareAnalysis(tenderData, configuration, null, null, null);

        SyncJobData syncJobData = new SyncJobData(tenderData, Constants.RECEIVED, "", new Date(),
                syncJob.getId());
        syncJobDataRepo.save(syncJobData);
        journalBatch.getSalesTenderData().add(syncJobData);
    }

    private void saveTax(JournalBatch journalBatch, String transactionDate, Configuration configuration,
                         SyncJob syncJob, Tax tax){
        HashMap<String, Object> taxData = new HashMap<>();

        taxData.put("accountingPeriod", transactionDate.substring(2,6));
        taxData.put("transactionDate", transactionDate);

        taxData.put("totalCr", String.valueOf(conversions.roundUpFloat2Digest(tax.getTotal())));
        taxData.put("inventoryAccount", tax.getAccount());

        taxData.put("fromCostCenter", tax.getCostCenter().costCenter);
        taxData.put("fromAccountCode", tax.getCostCenter().accountCode);
        taxData.put("fromLocation", tax.getCostCenter().accountCode);
        taxData.put("toLocation", tax.getCostCenter().accountCode);
        taxData.put("toCostCenter", tax.getCostCenter().costCenter);
        taxData.put("toAccountCode", tax.getCostCenter().accountCode);

        String description = "";
        String reference = "Taxes";

        if (tax.getCostCenter().costCenterReference.equals("")){
            description = tax.getTax();
        }else {
            description = tax.getCostCenter().costCenterReference + " " + tax.getTax();
            reference = tax.getCostCenter().costCenterReference;
        }

        if (description.length() > 50) {
            description = description.substring(0, 50);
        }

        taxData.put("description", description);
        taxData.put("transactionReference", reference);

        syncJobDataService.prepareAnalysis(taxData, configuration, tax.getCostCenter(), null, null);

        SyncJobData syncJobData = new SyncJobData(taxData, Constants.RECEIVED, "", new Date(),
                syncJob.getId());
        syncJobDataRepo.save(syncJobData);
        journalBatch.getSalesTaxData().add(syncJobData);
    }

    private void saveMajorGroup(JournalBatch journalBatch, String transactionDate, Configuration configuration,
                                SyncJob syncJob, Journal majorGroupJournal){

        HashMap<String, Object> majorGroupData = new HashMap<>();

        majorGroupData.put("accountingPeriod", transactionDate.substring(2,6));
        majorGroupData.put("transactionDate", transactionDate);

        majorGroupData.put("totalCr", String.valueOf(conversions.roundUpFloat2Digest(majorGroupJournal.getTotalCost())));
        // Major Group account
        if(majorGroupJournal.getMajorGroup().getRevenueCenters().size() > 0
                && !majorGroupJournal.getRevenueCenter().getAccountCode().equals("")){
            majorGroupData.put("inventoryAccount", majorGroupJournal.getRevenueCenter().getAccountCode());
        }else {
            majorGroupData.put("inventoryAccount", majorGroupJournal.getMajorGroup().getAccount());
        }

        majorGroupData.put("fromCostCenter", majorGroupJournal.getCostCenter().costCenter);
        majorGroupData.put("fromAccountCode", majorGroupJournal.getCostCenter().accountCode);
        majorGroupData.put("fromLocation", majorGroupJournal.getCostCenter().accountCode);
        majorGroupData.put("toLocation", majorGroupJournal.getCostCenter().accountCode);
        majorGroupData.put("toCostCenter", majorGroupJournal.getCostCenter().costCenter);
        majorGroupData.put("toAccountCode", majorGroupJournal.getCostCenter().accountCode);

        String description = "";
        String reference = "Major Group";

        if (!majorGroupJournal.getCostCenter().costCenterReference.equals("")){
            description = majorGroupJournal.getCostCenter().costCenterReference + " ";
            reference = majorGroupJournal.getCostCenter().costCenterReference + " ";
        }

        if(!configuration.salesConfiguration.syncMG){
            description += majorGroupJournal.getFamilyGroup().familyGroup + " ";
            syncJobDataService.prepareAnalysis(majorGroupData, configuration, majorGroupJournal.getCostCenter(), majorGroupJournal.getFamilyGroup(), null);
        }else {
            description += majorGroupJournal.getMajorGroup().getMajorGroup() + " ";
            syncJobDataService.prepareAnalysis(majorGroupData, configuration, majorGroupJournal.getCostCenter(), null, null);
        }

        if(!majorGroupJournal.getRevenueCenter().getRevenueCenter().equals(""))
            description += majorGroupJournal.getRevenueCenter().getRevenueCenter();

        if (description.length() > 50) {
            description = description.substring(0, 50);
        }

        majorGroupData.put("description", description);
        majorGroupData.put("transactionReference", reference);

        SyncJobData syncJobData = new SyncJobData(majorGroupData, Constants.RECEIVED, "", new Date(),
                syncJob.getId());
        syncJobDataRepo.save(syncJobData);
        journalBatch.getSalesMajorGroupGrossData().add(syncJobData);
    }

    private void saveServiceCharge(JournalBatch journalBatch, String transactionDate, Configuration configuration,
                                   SyncJob syncJob, ServiceCharge serviceCharge){
        HashMap<String, Object> serviceChargeData = new HashMap<>();

        serviceChargeData.put("accountingPeriod", transactionDate.substring(2,6));
        serviceChargeData.put("transactionDate", transactionDate);

        serviceChargeData.put("totalCr", String.valueOf(conversions.roundUpFloat2Digest(serviceCharge.getTotal())));

        serviceChargeData.put("fromCostCenter", serviceCharge.getCostCenter().costCenter);
        serviceChargeData.put("fromAccountCode", serviceCharge.getCostCenter().accountCode);

        serviceChargeData.put("toCostCenter", serviceCharge.getCostCenter().costCenter);
        serviceChargeData.put("toAccountCode", serviceCharge.getCostCenter().accountCode);

        serviceChargeData.put("fromLocation", serviceCharge.getCostCenter().accountCode);
        serviceChargeData.put("toLocation", serviceCharge.getCostCenter().accountCode);

        serviceChargeData.put("inventoryAccount", serviceCharge.getAccount());

        String description = "";
        String reference = "Service Charge";

        if (serviceCharge.getCostCenter().costCenterReference.equals("")){
            description = "Service Charge";
        }else{
            description = serviceCharge.getCostCenter().costCenterReference + " " + serviceCharge.getServiceCharge();
            reference = serviceCharge.getCostCenter().costCenterReference;
        }

        if (description.length() > 50) {
            description = description.substring(0, 50);
        }

        serviceChargeData.put("description", description);
        serviceChargeData.put("transactionReference", reference);

        syncJobDataService.prepareAnalysis(serviceChargeData, configuration, serviceCharge.getCostCenter(), null, null);

        SyncJobData syncJobData = new SyncJobData(serviceChargeData, Constants.RECEIVED, "", new Date(),
                syncJob.getId());
        syncJobDataRepo.save(syncJobData);
        journalBatch.getSalesMajorGroupGrossData().add(syncJobData);
    }
}
