package com.sun.supplierpoc.services;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.JournalBatch;
import com.sun.supplierpoc.models.Response;
import com.sun.supplierpoc.models.SyncJobType;
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
import java.util.List;

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
//        if (statistics.size() > 0){
//            statisticsResponse = getSalesStatistics(timePeriod, fromDate, toDate, costCenter, statistics, driver);
//            if (salesService.checkSalesFunctionResponse(driver, response, statisticsResponse)) return;
//        }


        // Get tender
        Response tenderResponse = new Response();
//        if(includedTenders.size() > 0){
//            tenderResponse = getSalesTenders(timePeriod, fromDate, toDate,
//                    costCenter, includedTenders, driver);
//            if (salesService.checkSalesFunctionResponse(driver, response, tenderResponse)) return;
//        }

        // Get taxes
        boolean syncTotalTax = configuration.syncTotalTax;
        String totalTaxAccount = configuration.totalTaxAccount;

        Response taxResponse = getSalesTaxes(timePeriod, fromDate, toDate, costCenter, syncTotalTax,
                totalTaxAccount, includedTax, driver);
        if (salesService.checkSalesFunctionResponse(driver, response, taxResponse)) return;

        // Set Statistics Info
        journalBatch.setSalesStatistics(statisticsResponse.getSalesStatistics());

        // Set Debit Entries (Tenders)
        journalBatch.setSalesTender(tenderResponse.getSalesTender());

        // Set Credit Entries (Taxes, overGroupsGross, Discount and Service charge)
        journalBatch.setSalesTax(taxResponse.getSalesTax());

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
            WebDriverWait wait = new WebDriverWait(driver, 30);

            wait.until(ExpectedConditions.visibilityOfElementLocated(By.partialLinkText("My Reports")));
            driver.findElement(By.partialLinkText("My Reports")).click();

            // Choose "Daily Operations" Report
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("link_1")));
            driver.findElement(By.id("link_1")).findElement(By.tagName("h4")).click();

            // Filter Report
            Response dateResponse = microsFeatures.selectDateRangeMicros(businessDate, fromDate, location.locationName,
                    null,"", driver);

            if (!dateResponse.isStatus()){
                response.setStatus(false);
                response.setMessage(dateResponse.getMessage());
                return response;
            }

            // Run
            driver.findElement(By.xpath("//*[@id=\"save-close-button\"]/button")).click();

            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"report_tile3631_1\"]/oj-module/oj-module/table")));
            WebElement statTable = driver.findElement(By.xpath("//*[@id=\"report_tile3631_1\"]/oj-module/oj-module/table"));
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
            WebDriverWait wait = new WebDriverWait(driver, 30);

            // Open reports
            driver.get(Constants.MICROS_TENDERS_REPORTS);

            // Filter Report
            Response dateResponse = microsFeatures.selectDateRangeMicros(businessDate, fromDate, location.locationName,
                    null,"", driver);

            if (!dateResponse.isStatus()){
                response.setStatus(false);
                response.setMessage(dateResponse.getMessage());
                return response;
            }

            // Run
            driver.findElement(By.xpath("//*[@id=\"save-close-button\"]/button")).click();

            // Fetch tenders table
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/div[2]/section/div[1]/div[2]/div/div/div[2]/div/my-reports-cca/report-group-cca/div[1]/div[7]/oj-rna-report-cca[4]/div[1]/oj-rna-report-tile-cca/oj-module/oj-table/table")));
            WebElement tendersTable = driver.findElement(By.xpath("/html/body/div[2]/section/div[1]/div[2]/div/div/div[2]/div/my-reports-cca/report-group-cca/div[1]/div[7]/oj-rna-report-cca[4]/div[1]/oj-rna-report-tile-cca/oj-module/oj-table/table"));
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
            WebDriverWait wait = new WebDriverWait(driver, 30);

            // Open reports
            driver.get(Constants.MICROS_TAXES_REPORTS);

            // Filter Report
            Response dateResponse = microsFeatures.selectDateRangeMicros(businessDate, fromDate, location.locationName,
                    null,"", driver);

            if (!dateResponse.isStatus()){
                response.setStatus(false);
                response.setMessage(dateResponse.getMessage());
                return response;
            }

            // Run
            driver.findElement(By.xpath("//*[@id=\"save-close-button\"]/button")).click();

            // Fetch tenders table
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"standard_table_4254_0\"]/table")));
            WebElement taxesTable = driver.findElement(By.xpath("//*[@id=\"standard_table_4254_0\"]/table"));
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
}
