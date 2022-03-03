package com.sun.supplierpoc.services;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.controllers.InvoiceController;
import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.models.configurations.*;
import com.sun.supplierpoc.repositories.SyncJobDataRepo;
import com.sun.supplierpoc.seleniumMethods.SetupEnvironment;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SalesApiService {

    @Autowired
    SyncJobDataRepo syncJobDataRepo;
    @Autowired
    TransferService transferService;
    @Autowired
    InvoiceController invoiceController;
    @Autowired
    SyncJobDataService syncJobDataService;

    private final Conversions conversions = new Conversions();

    private final SetupEnvironment setupEnvironment = new SetupEnvironment();

    public Response getSalesData(SyncJobType salesSyncJobType, ArrayList<CostCenter> costCentersLocation,
                                 ArrayList<SalesAPIStatistics> statistics, Account account, List<OrderTypeChannels> orderTypeChannels, String endpoint) {

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
            if (!setupEnvironment.loginOHRA(driver, Constants.OHRA_GCS_LINK, account)) {
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
            }

            if (costCentersLocation.size() > 0) {
                for (CostCenter costCenter : costCentersLocation) {
                    if (costCenter.checked) {
                        callSalesFunction(statistics, timePeriod, fromDate, toDate, costCenter,
                                journalBatches, driver, endpoint, response, orderTypeChannels);
                        if (!response.isStatus() && !response.getMessage().equals(Constants.INVALID_LOCATION)) {
                            return response;
                        }
                    }
                }
            } else {
                callSalesFunction(statistics, timePeriod, fromDate, toDate, new CostCenter(),
                        journalBatches, driver, endpoint, response, orderTypeChannels);
                if (!response.isStatus()) {
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


    private void callSalesFunction(ArrayList<SalesAPIStatistics> salesAPIStatistics, String timePeriod, String fromDate, String toDate,
                                   CostCenter costCenter, ArrayList<JournalBatch> journalBatches, WebDriver driver, String endPoint,
                                   Response response, List<OrderTypeChannels> orderTypeChannels) {

        JournalBatch journalBatch = new JournalBatch();

        // Get statistics
        Response statisticsResponse = new Response();
        if (salesAPIStatistics.size() > 0) {
            statisticsResponse = getSalesStatistics(timePeriod, fromDate, toDate, costCenter, endPoint,
                    salesAPIStatistics, driver, orderTypeChannels);
            if (checkSalesFunctionResponse(driver, response, statisticsResponse)) return;
        }

        // Set Statistics Info
        journalBatch.setSalesAPIStatistics(statisticsResponse.getSalesAPIStatistics());

        // Calculate different
        journalBatch.setSalesDifferent(0.0);
        journalBatch.setCostCenter(costCenter);
        journalBatches.add(journalBatch);

        response.setStatus(true);
    }

    private Response getSalesStatistics(String businessDate, String fromDate, String toDate, CostCenter location, String endPoint,
                                        ArrayList<SalesAPIStatistics> salesStatistics, WebDriver driver, List<OrderTypeChannels> tempOrderTypeChannels) {

        Response response = new Response();
        SalesAPIStatistics salesAPIStatistics;
        if (!location.locationName.equals("")) {
            salesAPIStatistics = conversions.checkSalesAPIStatisticsExistence(location.locationName, salesStatistics);
        } else {
            salesAPIStatistics = salesStatistics.get(0);
        }

        salesAPIStatistics.dateFrom = fromDate;
        salesAPIStatistics.dateTo = toDate;

        if (!salesAPIStatistics.checked) {
            response.setStatus(false);
            response.setMessage("Not Configured");
            return response;
        }

        driver.get(Constants.SYSTEM_SALES_REPORT_LINK_GCS);

        try {
            WebDriverWait wait = new WebDriverWait(driver, 2);
            wait.until(ExpectedConditions.alertIsPresent());
        } catch (Exception e) {
            LoggerFactory.getLogger("Sales").info("No Alert");
        }

        if (setupEnvironment.runReport(businessDate, fromDate, toDate, location, new RevenueCenter(), driver, response)) {
            return response;
        }

        try {

            driver.get(Constants.SALES_SUMMARY_API_LINK);

            try {
                WebDriverWait wait = new WebDriverWait(driver, 2);
                wait.until(ExpectedConditions.alertIsPresent());
            } catch (Exception e) {
                LoggerFactory.getLogger("Sales").info("No Alert");
            }

            WebElement statTable = driver.findElement(By.xpath("/html/body/table/tbody/tr/td[1]/div[2]/table"));
            List<WebElement> rows = statTable.findElements(By.tagName("tr"));

            if (rows.size() < 1) {
                return response;
            }
            ArrayList<String> statisticValues = setupEnvironment.getTableColumns(rows, false, 2);
            salesAPIStatistics.NoChecks = conversions.filterString(statisticValues.get(1));

            statTable = driver.findElement(By.xpath("/html/body/table/tbody/tr/td[1]/div[1]/table"));
            rows = statTable.findElements(By.tagName("tr"));

            if (rows.size() < 1) {
                response.setStatus(true);
                response.setMessage("There is no statistics info in this location");
                return response;
            }
            statisticValues = setupEnvironment.getTableColumns(rows, false, 5);

            salesAPIStatistics.NetSales = conversions.filterString(statisticValues.get(2));

            if (endPoint.equals("Daily")) {
                statTable = driver.findElement(By.xpath("/html/body/div[6]/table"));
            } else {
                statTable = driver.findElement(By.xpath("/html/body/div[4]/table"));
            }
            rows = statTable.findElements(By.tagName("tr"));

            if (rows.size() < 1) {
                response.setStatus(false);
                response.setMessage("There is no statistics info in this location");
                return response;
            }

            ArrayList<String> columns = setupEnvironment.getTableColumns(rows, false, 0);

            List<OrderTypeChannels> orderTypeChannels = new ArrayList<>();
            tempOrderTypeChannels.stream().forEach(o -> orderTypeChannels.add(o.clone()));

            for (int i = 2; i < rows.size(); i++) {
                statisticValues = setupEnvironment.getTableColumns(rows, false, i);
                String OrderType = conversions.filterString(statisticValues.get(columns.indexOf("order_type")));

                OrderTypeChannels orderTypeChannel = orderTypeChannels.stream().
                        filter(channel -> channel.getOrderType().toLowerCase(Locale.ROOT).equals(OrderType)).collect(Collectors.toList())
                        .stream().findFirst().orElse(null);

                if (orderTypeChannel == null) {
                    response.setStatus(false);
                    response.setMessage("Please Configure Channel With Order type " + OrderType);
                    return response;
                }

                OrderTypeChannels finalOrderTypeChannel = orderTypeChannel;
                List<OrderTypeChannels> RepeatedOrderTypeChannels = orderTypeChannels.stream().
                        filter(channel -> channel.getChannel().toLowerCase(Locale.ROOT).equals(finalOrderTypeChannel.getChannel().toLowerCase())).collect(Collectors.toList());

                if(RepeatedOrderTypeChannels.size() > 1){
                    orderTypeChannel = RepeatedOrderTypeChannels.get(0);
                }

                Double netSales = Double.parseDouble(conversions.filterString(statisticValues.get(columns.indexOf("gross_sales_after_disc."))));
                int checkPerType = Integer.parseInt(conversions.filterString(statisticValues.
                        get(columns.indexOf("checks"))));

                orderTypeChannel.setNetSales(String.valueOf(Double.parseDouble(orderTypeChannel.getNetSales()) + netSales));
                orderTypeChannel.setCheckCount(String.valueOf(Integer.parseInt(orderTypeChannel.getCheckCount()) +checkPerType));

            }

            salesAPIStatistics.setOrderTypeChannels(orderTypeChannels);

            response.setStatus(true);
            response.setMessage("");
            response.setSalesAPIStatistics(salesAPIStatistics);
        } catch (Exception e) {
            driver.quit();
            response.setStatus(false);
            response.setMessage(e.getMessage());
        }
        return response;
    }

    public boolean checkSalesFunctionResponse(WebDriver driver, Response response, Response reportResponse) {
        if (!reportResponse.isStatus()) {
            response.setMessage(reportResponse.getMessage());
            response.setStatus(false);

            if (reportResponse.getMessage().equals(Constants.INVALID_BUSINESS_DATE)) {
                driver.quit();
            } else if (reportResponse.getMessage().equals(Constants.WRONG_BUSINESS_DATE)) {
                driver.quit();
            } else return !reportResponse.getMessage().equals(Constants.INVALID_REVENUE_CENTER);
            return true;
        }
        return false;
    }

    public ArrayList<JournalBatch> saveSalesJournalBatchesData(Response salesResponse, SyncJob syncJob,
                                                               Configuration configuration, Account account) {
        ArrayList<JournalBatch> addedJournalBatches = new ArrayList<>();

        ArrayList<JournalBatch> journalBatches = salesResponse.getJournalBatches();
        for (JournalBatch journalBatch : journalBatches) {

            // Statistics
            if (journalBatch.getSalesAPIStatistics().checked) {
                HashMap<String, Object> statisticsData = new HashMap<>();

                SalesAPIStatistics salesAPIStatistics = journalBatch.getSalesAPIStatistics();

                statisticsData.put("location", salesAPIStatistics.location);
                statisticsData.put("leaseCode", salesAPIStatistics.leaseCode);

                statisticsData.put("unitNo", salesAPIStatistics.unitNo);
                statisticsData.put("brand", salesAPIStatistics.brand);
                statisticsData.put("registeredName", salesAPIStatistics.registeredName);
                statisticsData.put("NoChecks", salesAPIStatistics.NoChecks);
                statisticsData.put("NetSales", salesAPIStatistics.NetSales);
                statisticsData.put("salesDateFrom", salesAPIStatistics.dateFrom);
                statisticsData.put("salesDateTo", salesAPIStatistics.dateTo);

                SyncJobData syncJobChecksData = new SyncJobData(statisticsData, Constants.RECEIVED, "", new Date(),
                        syncJob.getId());
                syncJobDataRepo.save(syncJobChecksData);
                journalBatch.getStatisticsData().add(syncJobChecksData);
            }

            addedJournalBatches.add(journalBatch);
        }
        return addedJournalBatches;
    }


    public void updateJournalBatchStatus(JournalBatch journalBatch, HashMap<String, Object> response) {
        SyncJobData salesDifferentData = journalBatch.getSalesDifferentData();
        ArrayList<SyncJobData> salesTaxData = journalBatch.getSalesTaxData();
        ArrayList<SyncJobData> salesTenderData = journalBatch.getSalesTenderData();
        ArrayList<SyncJobData> salesMajorGroupGrossData = journalBatch.getSalesMajorGroupGrossData();

        String reason = "";
        String status = "";

        if ((Boolean) response.get("status")) {
            status = Constants.SUCCESS;
            reason = "";
        } else {
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
