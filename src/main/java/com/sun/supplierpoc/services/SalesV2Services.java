package com.sun.supplierpoc.services;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.JournalBatch;
import com.sun.supplierpoc.models.Response;
import com.sun.supplierpoc.models.SyncJobType;
import com.sun.supplierpoc.models.configurations.*;
import com.sun.supplierpoc.repositories.SyncJobDataRepo;
import com.sun.supplierpoc.seleniumMethods.BasicFeatures;
import com.sun.supplierpoc.seleniumMethods.SetupEnvironment;
import org.openqa.selenium.Alert;
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
    private BasicFeatures basicFeatures = new BasicFeatures();

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
            if (!basicFeatures.loginMicrosOHRA(driver, Constants.MICROS_V2_LINK, account)) {
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


//        // Get tender
//        Response tenderResponse = new Response();
//        if(includedTenders.size() > 0){
//            tenderResponse = getSalesTenders(timePeriod, fromDate, toDate,
//                    costCenter, includedTenders, driver);
//            if (salesService.checkSalesFunctionResponse(driver, response, tenderResponse)) return;
//        }

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

        // Open reports
        WebDriverWait wait = new WebDriverWait(driver, 30);

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.partialLinkText("My Reports")));
        driver.findElement(By.partialLinkText("My Reports")).click();

        // Choose "Daily Operations" Report
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("link_4")));
        driver.findElement(By.id("link_4")).findElement(By.tagName("h4")).click();

        // Filter Report
        Response dateResponse = basicFeatures.selectDateRangeMicros(businessDate, fromDate, location.locationName,
                null,"", driver);

        if (!dateResponse.isStatus()){
            response.setStatus(false);
            response.setMessage(dateResponse.getMessage());
            return response;
        }

        try {

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
        ArrayList<Tender> tenders = new ArrayList<>();

        return response;
    }
}
