package com.sun.supplierpoc.services;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
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

import javax.xml.crypto.Data;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class SalesV2Services {
    @Autowired
    SyncJobDataRepo syncJobDataRepo;
    @Autowired
    SyncJobDataService syncJobDataService;
    @Autowired
    SalesService salesService;

    private Conversions conversions = new Conversions();
    private SetupEnvironment setupEnvironment = new SetupEnvironment();
    private MicrosFeatures microsFeatures = new MicrosFeatures();

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
            if (!microsFeatures.loginMicrosOHRA(driver, Constants.MICROS_V2_LINK, account)) {
                driver.quit();

                response.setStatus(false);
                response.setMessage("Invalid username and password.");
                response.setEntries(new ArrayList<>());
                return response;
            }

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
            if (salesService.checkSalesFunctionResponse(driver, response, statisticsResponse)) return;
        }

        // Service Charges
        boolean syncTotalServiceCharge = salesSyncJobType.getConfiguration().salesConfiguration.syncTotalServiceCharge;
        String totalServiceChargeAccount = salesSyncJobType.getConfiguration().salesConfiguration.totalServiceChargeAccount;

        Response serviceChargeResponse = new Response();
        if (includedServiceCharge.size() > 0 || syncTotalServiceCharge){
            serviceChargeResponse = getSalesServiceCharge(timePeriod, fromDate, toDate, costCenter,
                    syncTotalServiceCharge, totalServiceChargeAccount, includedServiceCharge, driver);
            if (salesService.checkSalesFunctionResponse(driver, response, serviceChargeResponse)) return;
        }

        // Get tender
        Response tenderResponse = new Response();
        if(includedTenders.size() > 0){
            tenderResponse = getSalesTenders(timePeriod, fromDate, toDate,
                    costCenter, includedTenders, driver);
            if (salesService.checkSalesFunctionResponse(driver, response, tenderResponse)) return;
        }

        // Get taxes
        boolean syncTotalTax = configuration.syncTotalTax;
        String totalTaxAccount = configuration.totalTaxAccount;

        Response taxResponse = new Response();
        if(includedTax.size() > 0 || syncTotalTax){
            taxResponse = getSalesTaxes(timePeriod, fromDate, toDate, costCenter, syncTotalTax,
                    totalTaxAccount, includedTax, driver);
            if (salesService.checkSalesFunctionResponse(driver, response, taxResponse)) return;
        }

        // Get discounts
        Response discountResponse;
        boolean syncTotalDiscounts = configuration.syncTotalDiscounts;
        String totalDiscountsAccount = configuration.totalDiscountsAccount;
        ArrayList<Discount> salesDiscounts = new ArrayList<>();

        if (includedDiscount.size() > 0 || syncTotalDiscounts){
            discountResponse = getSalesDiscount(timePeriod, fromDate, toDate, costCenter,
                    syncTotalDiscounts, totalDiscountsAccount, includedDiscount, driver);
            if (salesService.checkSalesFunctionResponse(driver, response, discountResponse)) return;
            salesDiscounts.addAll(discountResponse.getSalesDiscount());
        }

        // Get Major Groups/Family Groups net sales
        String grossDiscountSales = configuration.grossDiscountSales;
        boolean majorGroupDiscount = configuration.MGDiscount;
        boolean revenueCenterDiscount = configuration.RVDiscount;
        boolean syncMajorGroups = configuration.syncMG;
        boolean taxIncluded = configuration.taxIncluded;

        ArrayList<Journal> salesMajorGroupsGross = new ArrayList<>();
        if (configuration.syncPerRV){
            for (RevenueCenter rc : revenueCenters)
            {
                if(!rc.isChecked()){
                    continue;
                }
                Response overGroupGrossResponse;

                overGroupGrossResponse = getSalesMajorGroups(taxIncluded, rc, timePeriod, fromDate, toDate, costCenter,
                        majorGroups, grossDiscountSales, majorGroupDiscount, revenueCenterDiscount, syncMajorGroups,
                        driver);

                if (salesService.checkSalesFunctionResponse(driver, response, overGroupGrossResponse)) return;

                if (majorGroupDiscount || revenueCenterDiscount){
                    salesDiscounts.addAll(overGroupGrossResponse.getSalesDiscount());
                }

                salesMajorGroupsGross.addAll(overGroupGrossResponse.getSalesMajorGroupGross());
            }
        }
        else{
            Response overGroupGrossResponse;

            overGroupGrossResponse = getSalesMajorGroups(taxIncluded, new RevenueCenter(), timePeriod, fromDate, toDate, costCenter,
                    majorGroups, grossDiscountSales, majorGroupDiscount, revenueCenterDiscount, syncMajorGroups,
                    driver);

            if (salesService.checkSalesFunctionResponse(driver, response, overGroupGrossResponse)) return;

            if (majorGroupDiscount || revenueCenterDiscount){
                salesDiscounts.addAll(overGroupGrossResponse.getSalesDiscount());
            }

            salesMajorGroupsGross.addAll(overGroupGrossResponse.getSalesMajorGroupGross());
        }

        // Set Statistics Info
        journalBatch.setSalesStatistics(statisticsResponse.getSalesStatistics());

        // Set Debit Entries (Tenders)
        journalBatch.setSalesTender(tenderResponse.getSalesTender());

        // Set Credit Entries (Taxes, overGroupsGross, Discount and Service charge)
        journalBatch.setSalesTax(taxResponse.getSalesTax());
        journalBatch.setSalesDiscount(salesDiscounts);
        journalBatch.setSalesMajorGroupGross(salesMajorGroupsGross);
        journalBatch.setSalesServiceCharge(serviceChargeResponse.getSalesServiceCharge());

        // Calculate different
        journalBatch.setSalesDifferent(0.0);
        journalBatch.setCostCenter(costCenter);
        journalBatches.add(journalBatch);
        response.setStatus(true);
    }

    private Response getSalesStatistics(String businessDate, String fromDate, String toDate, CostCenter location,
                                        ArrayList<SalesStatistics> statistics, WebDriver driver) {
        Response response = new Response();
        String statisticType;
        SalesStatistics salesStatistics = conversions.checkSalesStatisticsExistence(location.locationName, statistics);

        if(!salesStatistics.checked){
            response.setStatus(true);
            response.setMessage("Not Configured");
            return response;
        }

        try {
            // Open reports
            WebDriverWait wait = new WebDriverWait(driver, 29);

            driver.get("https://mte4-ohra.oracleindustry.com/portal/?root=reports&reports=myReports&myReports=reportGroup&reportGroup=1");
//            wait.until(ExpectedConditions.visibilityOfElementLocated(By.partialLinkText("My Reports")));
//            driver.findElement(By.partialLinkText("My Reports")).click();
//
//            // Choose "Daily Operations" Report
//            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("link_1")));
//            driver.findElement(By.id("link_1")).findElement(By.tagName("h4")).click();

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
            // Run
            driver.findElement(By.xpath("//*[@id=\"save-close-button\"]/button")).click();

            // validate Report parameters
            Response validateParameters= microsFeatures.checkReportParameters(driver,fromDate,toDate,businessDate, location.locationName);

            if(!validateParameters.isStatus()){
                response.setStatus(false);
                response.setMessage(validateParameters.getMessage());
                return response;
            }


            try{
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/div[2]/section/div[1]/div[2]/div/div/div[2]/div/my-reports-cca/report-group-cca/div[1]/div[7]/oj-rna-report-cca[8]/div[1]/oj-rna-report-tile-cca[2]/oj-module/oj-module/table")));
            }catch (Exception e){
                response.setStatus(true);
                response.setMessage(Constants.NO_INFO);
                return response;
            }
       WebElement statTable = driver.findElement(By.xpath("/html/body/div[2]/section/div[1]/div[2]/div/div/div[2]/div/my-reports-cca/report-group-cca/div[1]/div[7]/oj-rna-report-cca[8]/div[1]/oj-rna-report-tile-cca[2]/oj-module/oj-module/table"));
            List<WebElement> rows = statTable.findElements(By.tagName("tr"));

            if (rows.size() < 1){
                response.setStatus(true);
                response.setMessage("There is no statistics info in this location");
                return response;
            }

            ArrayList<String> columns = setupEnvironment.getTableColumns(rows, false, 0);

            for (int i = 1; i < rows.size(); i++) {
                WebElement row = rows.get(i);
                List<WebElement> cols = row.findElements(By.tagName("td"));

                if (cols.size() != columns.size()) {
                    continue;
                }
                statisticType = cols.get(columns.indexOf("name")).getText().toLowerCase().strip();
                switch (statisticType) {
                    case "guests":
                        salesStatistics.NoGuest = conversions.filterString(cols.get(columns.indexOf("count")).getText());
                        break;
                    case "checks":
                        salesStatistics.NoChecks = conversions.filterString(cols.get(columns.indexOf("count")).getText());
                        break;
                    case "tables":
                        salesStatistics.NoTables = conversions.filterString(cols.get(columns.indexOf("count")).getText());
                        break;
                }
            }

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

    private Response getSalesTenders(String businessDate, String fromDate, String toDate,
                                     CostCenter location, ArrayList<Tender> includedTenders, WebDriver driver){
        Response response = new Response();
        Tender tender;
        ArrayList<Tender> tenders = new ArrayList<>();

        try{
            WebDriverWait wait = new WebDriverWait(driver, 29);

            // Open reports
            driver.get(Constants.MICROS_TENDERS_REPORTS);

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

            // Run
             driver.findElement(By.xpath("//*[@id=\"save-close-button\"]/button")).click();


              // Validate Report Parameters
              Response validateParameters= microsFeatures.checkReportParameters(driver,fromDate,toDate,businessDate, location.locationName);

              if(!validateParameters.isStatus()){
                  response.setStatus(false);
                  response.setMessage(validateParameters.getMessage());
                  return response;
              }

                try{
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id='standard_table_7202_0']/table")));
            }catch (Exception e){
                response.setStatus(true);
                response.setMessage(Constants.NO_INFO);
                return response;
            }


            WebElement tendersTable = driver.findElement(By.xpath("//*[@id='standard_table_7202_0']/table"));

            List<WebElement> rows = tendersTable.findElements(By.tagName("tr"));

            if (rows.size() < 1){
                response.setStatus(true);
                response.setMessage("There is no payments in this location");
                return response;
            }

            ArrayList<String> columns = setupEnvironment.getTableColumns(rows, true, 0);
            for (int i = 2; i < rows.size(); i++) {
                WebElement row = rows.get(i);
                List<WebElement> cols = row.findElements(By.tagName("td"));

                if (cols.size() != columns.size()) {
                    continue;
                }

                // Check if tender exists
                Tender tenderData = conversions.checkTenderExistence(includedTenders, cols.get(columns.indexOf("tender_type")).getText().strip(),
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
                tender.setTotal(conversions.convertStringToFloat(cols.get(columns.indexOf("amount")).getText().strip()));

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
            e.printStackTrace();
            driver.quit();
            response.setStatus(false);
            response.setMessage("Failed to get sales payment entries from Oracle Micros Simphony.");
        }

        return response;
    }

    private Response getSalesTaxes(String businessDate, String fromDate, String toDate,
                                   CostCenter location, boolean getTaxTotalFlag, String totalTaxAccount,
                                   ArrayList<Tax> includedTaxes, WebDriver driver) {
        Response response = new Response();
        ArrayList<Tax> salesTax = new ArrayList<>();

        try {
            WebDriverWait wait = new WebDriverWait(driver, 29);

            // Open reports
            driver.get(Constants.MICROS_TAXES_REPORTS);

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

            // Run
            driver.findElement(By.xpath("//*[@id=\"save-close-button\"]/button")).click();

            // Validate Report Parameters
            Response validateParameters= microsFeatures.checkReportParameters(driver,fromDate,toDate,businessDate, location.locationName);

            if(!validateParameters.isStatus()){
                response.setStatus(false);
                response.setMessage(validateParameters.getMessage());
                return response;
            }

            // Fetch tax table
            try{
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/div[2]/section/div[1]/div[2]/div/div/div[2]/div/my-reports-cca/report-group-cca/div[1]/div[7]/oj-rna-report-cca[3]/div[1]/oj-rna-report-tile-cca/oj-module/oj-table/table")));
            }catch (Exception e){
                response.setStatus(true);
                response.setMessage(Constants.NO_INFO);
                return response;
            }
            WebElement taxesTable = driver.findElement(By.xpath("/html/body/div[2]/section/div[1]/div[2]/div/div/div[2]/div/my-reports-cca/report-group-cca/div[1]/div[7]/oj-rna-report-cca[3]/div[1]/oj-rna-report-tile-cca/oj-module/oj-table/table"));
            List<WebElement> rows = taxesTable.findElements(By.tagName("tr"));

            if (rows.size() < 3) {
                response.setStatus(true);
                response.setMessage("There is no tax entries in this location");
                response.setSalesTax(new ArrayList<>());

                return response;
            }

            ArrayList<String> columns = setupEnvironment.getTableColumns(rows, true, 0);

            for (int i = 1; i < rows.size(); i++) {
                WebElement row = rows.get(i);
                List<WebElement> cols = row.findElements(By.tagName("td"));

                if (cols.size() != columns.size()) {
                    continue;
                }

                Tax tax = new Tax();

                WebElement td = cols.get(columns.indexOf("tax_name"));
                if(getTaxTotalFlag){
                    if (td.getText().equals("Total")) {
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
                    taxAmount = conversions.convertStringToFloat(cols.get(columns.indexOf("tax_collected")).getText().strip());


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

    private Response getSalesDiscount(String businessDate, String fromDate, String toDate, CostCenter location,
                                      boolean getDiscountTotalFlag, String totalDiscountsAccount,
                                      ArrayList<Discount> discounts, WebDriver driver) {
        Response response = new Response();
        ArrayList<Discount> salesDiscount = new ArrayList<>();
        try {
            WebDriverWait wait = new WebDriverWait(driver, 29);

            // Open reports
            driver.get(Constants.MICROS_DISCOUNT_REPORTS);

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

            // Run
            driver.findElement(By.xpath("//*[@id=\"save-close-button\"]/button")).click();

            // Validate Report Parameters
            Response validateParameters= microsFeatures.checkReportParameters(driver,fromDate,toDate,businessDate, location.locationName);

            if(!validateParameters.isStatus()){
                response.setStatus(false);
                response.setMessage(validateParameters.getMessage());
                return response;
            }

            // Fetch tenders table
            try{
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#custom_report100046 > div.oj-flex.oj-sm-12 > div > div:nth-child(11) > table")));
            }catch (Exception e){
                response.setStatus(true);
                response.setMessage(Constants.NO_INFO);
                return response;
            }
            WebElement discountsTable = driver.findElement(By.cssSelector("#custom_report100046 > div.oj-flex.oj-sm-12 > div > div:nth-child(11) > table"));
            List<WebElement> rows = discountsTable.findElements(By.tagName("tr"));

            if (rows.size() < 1){
                response.setStatus(true);
                response.setMessage("There is no discount entries in this location");
                response.setSalesTender(new ArrayList<>());

                return response;
            }

            ArrayList<String> columns = setupEnvironment.getTableColumns(rows, false, 0);

            for (int i = 1; i < rows.size(); i++) {
                WebElement row = rows.get(i);
                List<WebElement> cols = row.findElements(By.tagName("td"));

                if (cols.size() != columns.size()) {
                    continue;
                }
                Discount discount;

                WebElement td = cols.get(columns.indexOf("discount_type"));
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

    private Response getSalesMajorGroups(boolean taxIncluded, RevenueCenter revenueCenter, String businessDate,
                                         String fromDate, String toDate,
                                         CostCenter location, ArrayList<MajorGroup> majorGroups,
                                         String grossDiscountSales, boolean majorGroupDiscount,
                                         boolean revenueCenterDiscount, boolean syncMajorGroups,
                                         WebDriver driver) {
        Response response = new Response();
        ArrayList<Journal> majorGroupsGross = new ArrayList<>();
        ArrayList<Discount> salesDiscount = new ArrayList<>();

        try {
            WebDriverWait wait = new WebDriverWait(driver, 5);

            // Open reports
            if(!driver.getCurrentUrl().equals(Constants.MICROS_SALES_SUMMARY)){
                driver.get(Constants.MICROS_SALES_SUMMARY);

                // Wait until loading appears
                try{
                    wait = new WebDriverWait(driver, 60);
                    wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("progressCircle_7032")));
                }catch (Exception e){
                    response.setStatus(true);
                    response.setMessage("There is no sales per major group found in this location");
                    return response;
                }
            }

            // Wait until loading disppears
            if(driver.findElements(By.id("progressCircle_7032")).size() > 0){
                try{
                    wait = new WebDriverWait(driver, 60);
                    wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("progressCircle_7032")));
                }catch (Exception e){
                    response.setStatus(true);
                    response.setMessage("There is no sales per major group found in this location");
                    return response;
                }
            }

            // Filter Report
             Response dateResponse = microsFeatures.selectDateRangeMicros(businessDate, fromDate, toDate, location.locationName,
                     revenueCenter.getRevenueCenter(),"", driver);

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

            // Run
            driver.findElement(By.xpath("//*[@id=\"save-close-button\"]/button")).click();

            // Validate Report Parameters
            Response validateParameters= microsFeatures.checkReportParameters(driver,fromDate,toDate,businessDate, location.locationName);

            if(!validateParameters.isStatus()){
                response.setStatus(false);
                response.setMessage(validateParameters.getMessage());
                return response;
            }

            // Wait until the report is completely loaded, or get no data flag
            if(driver.findElements(By.id("progressCircle_7032")).size() > 0){
                try{
                    wait = new WebDriverWait(driver, 60);
                    wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("progressCircle_7032")));
                }catch (Exception e){
                    response.setStatus(true);
                    response.setMessage("There is no sales per major group found in this location");
                    return response;
                }
            }

            // Fetch major groups table - check no data flag

            if(driver.findElements(By.xpath("//*[@id=\"report_web_component7032\"]/div[1]/div[2]/div")).size() > 0){
                response.setStatus(true);
                response.setMessage("There is no sales per major group found in this location");
                return response;
            }

            try{
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"standard_table_7032_0\"]/table")));
            }catch (Exception e){
                response.setStatus(true);
                response.setMessage("There is no sales per major group found in this location");
                return response;
            }

//            if(driver.findElements(By.xpath("//*[@id=\"standard_table_7032_0\"]/table")).size() == 0){
//                response.setStatus(true);
//                response.setMessage("There is no sales per major group found in this location");
//                return response;
//            }

            WebElement tendersTable = driver.findElement(By.xpath("//*[@id=\"standard_table_7032_0\"]/table"));
            List<WebElement> rows = tendersTable.findElements(By.tagName("tr"));

            if (rows.size() < 1){
                response.setStatus(true);
                response.setMessage("There is no sales per major group found in this location");
                return response;
            }

            ArrayList<String> columns = setupEnvironment.getTableColumns(rows, true, 0);
            MajorGroup majorGroup;
            RevenueCenter MGRevenueCenter = new RevenueCenter();
            String majorGroupName = "";

            float majorGroupAmount;
            float discountAmount;

            for (int i = 2; i < rows.size(); i++) {
                WebElement row = rows.get(i);
                List<WebElement> cols = row.findElements(By.tagName("td"));

                if (cols.size() != columns.size()) {
                    continue;
                }

                Journal journal = new Journal();
                WebElement col = cols.get(columns.indexOf("name"));
                
                majorGroupName = col.getText().strip().toLowerCase();
                
                if (col.getAttribute("class").
                        equals("oj-helper-text-align-left oj-table-data-cell oj-form-control-inherit")) {
                    
                    majorGroup = conversions.checkMajorGroupExistence(majorGroups, majorGroupName);
                    
                }else{
                    continue;
                }
                
                if (!majorGroup.getChecked()) {
                    continue;
                }

                if(!revenueCenter.getRevenueCenter().equals("")){
                    MGRevenueCenter = conversions.checkRevenueCenterExistence(majorGroup.getRevenueCenters(), revenueCenter.getRevenueCenter());
                }

                /* Need to sync sales amount after appling the discount amount */
                if (grossDiscountSales.equals(Constants.SALES_GROSS_LESS_DISCOUNT)) {
                    majorGroupAmount = conversions.convertStringToFloat(cols.get(columns.indexOf("sales_less_item_discounts")).getText().strip());
                } else {
                    majorGroupAmount = conversions.convertStringToFloat(cols.get(columns.indexOf("gross_sales_total")).getText().strip());
                }

                majorGroupsGross = journal.checkExistence(majorGroupsGross, majorGroup
                        , 0, majorGroupAmount, 0, location, MGRevenueCenter, "");

                /* Discount amount */
                discountAmount = conversions.convertStringToFloat(cols.get(columns.indexOf("item_discount_total")).getText().strip());

                if (discountAmount != 0){
                    Discount groupDiscount = new Discount();

                    groupDiscount.setDiscount(majorGroup.getMajorGroup() + " Discount " + revenueCenter.getRevenueCenter());
                    if(!MGRevenueCenter.getRevenueCenter().equals("") && !MGRevenueCenter.getDiscountAccount().equals("")){
                        groupDiscount.setAccount(MGRevenueCenter.getDiscountAccount());
                    }else {
                        groupDiscount.setAccount(majorGroup.getDiscountAccount());
                    }
/*                    if(majorGroupDiscount){
                        groupDiscount.setAccount(majorGroup.getDiscountAccount());
                    }else if (revenueCenterDiscount){
                        groupDiscount.setAccount(MGRevenueCenter.getDiscountAccount());
                    }*/

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

            response.setStatus(true);
            response.setMessage("");
            response.setSalesMajorGroupGross(majorGroupsGross);
            response.setSalesDiscount(salesDiscount);
        } catch (Exception e) {
            driver.quit();

            response.setStatus(false);
            response.setMessage(e.getMessage());
        }

        return response;
    }

    private Response getSalesServiceCharge(String businessDate, String fromDate, String toDate,
                                   CostCenter location, boolean getSCTotalFlag, String totalSCAccount,
                                           ArrayList<ServiceCharge> serviceCharges, WebDriver driver) {
        Response response = new Response();
        ServiceCharge serviceCharge;
        ArrayList<ServiceCharge> salesServiceCharges = new ArrayList<>();

        try {
            WebDriverWait wait = new WebDriverWait(driver, 29);

            // Open reports
            driver.get(Constants.MICROS_SERVICE_CHARGE_REPORT);

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
            } catch (Exception e) {
            }

            // Run
            driver.findElement(By.xpath("//*[@id=\"save-close-button\"]/button")).click();

            // Validate Report Parameters
            Response validateParameters= microsFeatures.checkReportParameters(driver,fromDate,toDate,businessDate, location.locationName);

            if(!validateParameters.isStatus()){
                response.setStatus(false);
                response.setMessage(validateParameters.getMessage());
                return response;
            }

            // Fetch service charges table
            try{
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"standard_table_7097_0\"]/table")));
            }catch (Exception e){
                response.setStatus(true);
                response.setMessage(Constants.NO_INFO);
                return response;
            }

            /* view/expand service charge types */
            WebElement expander = driver.findElement(By.id("row_expander_7097_0_0:0"));
            if(expander != null)
                expander.click();

            /* Wait until table be ready for reading */
            TimeUnit.SECONDS.sleep(2);
            WebElement serviceChargeTable = driver.findElement(By.xpath("//*[@id=\"standard_table_7097_0\"]/table"));

            List<WebElement> rows = serviceChargeTable.findElements(By.tagName("tr"));

            if (rows.size() < 3) {
                response.setStatus(true);
                response.setMessage("There is no service charges entries in this location");
                response.setSalesTax(new ArrayList<>());

                return response;
            }

            ArrayList<String> columns = setupEnvironment.getTableColumns(rows, true, 0);

            for (int i = 1; i < rows.size(); i++) {
                WebElement row = rows.get(i);
                List<WebElement> cols = row.findElements(By.tagName("td"));

                if (cols.size() != columns.size()) {
                    continue;
                }

                WebElement td = cols.get(columns.indexOf("service_charge_name"));
                if(getSCTotalFlag){
                    if (td.getText().equals("Total")) {

                        float serviceChargeTotal = conversions.convertStringToFloat(cols.get(columns.indexOf("service_charges_amount")).getText().strip());
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

                    float serviceChargeTotal = conversions.convertStringToFloat(cols.get(columns.indexOf("service_charges_amount")).getText().strip());
                    serviceCharge.setTotal(serviceChargeTotal);
                    serviceCharge.setCostCenter(location);
                    salesServiceCharges.add(serviceCharge);
                }
            }

            response.setStatus(true);
            response.setMessage("");
            response.setSalesServiceCharge(salesServiceCharges);
            response.setEntries(new ArrayList<>());

        } catch (Exception e) {
            e.printStackTrace();
            driver.quit();

            response.setStatus(false);
            response.setMessage(e.getMessage());
            response.setEntries(new ArrayList<>());
        }

        return response;
    }


}
