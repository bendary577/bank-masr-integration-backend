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
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class JournalV2Service {
    @Autowired
    SyncJobDataRepo syncJobDataRepo;
    @Autowired
    InvoiceController invoiceController;
    @Autowired
    private SyncJobDataService syncJobDataService;

    private final Conversions conversions = new Conversions();
    private final SetupEnvironment setupEnvironment = new SetupEnvironment();
    private MicrosFeatures microsFeatures = new MicrosFeatures();
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /*
     * Get consumptions entries based on cost center
     * */
    public Response getJournalDataByCostCenterLink(SyncJobType journalSyncJobType, ArrayList<CostCenter> costCenters,
                                                   ArrayList<ItemGroup> itemGroups, Account account) {
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
        WebDriverWait wait;

        try {
            if (!microsFeatures.loginMicrosOHRA(driver, Constants.MICROS_V2_LINK, account)) {
                driver.quit();

                response.setStatus(false);
                response.setMessage("Invalid username and password.");
                response.setEntries(new ArrayList<>());
                return response;
            }

            try {
                wait = new WebDriverWait(driver, 5);
                wait.until(ExpectedConditions.alertIsPresent());
            } catch (Exception e) {
                System.out.println("Waiting");
            }

            String journalUrl = Constants.MICROS_COS_REPORTS_COST_CENTER;
            driver.get(journalUrl);

            try {
                wait = new WebDriverWait(driver, 5);
                wait.until(ExpectedConditions.alertIsPresent());
            } catch (Exception e) {
                System.out.println("Waiting");
            }

            // Filter Report
            Response dateResponse = microsFeatures.selectDateRangeMicros(businessDate, fromDate, toDate, null,
                    null, "", driver);

            if (!dateResponse.isStatus()) {
                response.setStatus(false);
                response.setMessage(dateResponse.getMessage());
                return response;
            }

            // Run
            driver.findElement(By.xpath("//*[@id=\"save-close-button\"]/button")).click();

            try {
                wait = new WebDriverWait(driver, 5);
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("tr")));
                System.out.println("No Alert");
            } catch (Exception e) {
                System.out.println("Waiting");
            }

            List<WebElement> rows = driver.findElements(By.tagName("tr"));

            if (rows.size() < 4) {

                driver.quit();
                response.setStatus(true);
                response.setMessage(Constants.NO_INFO);
                return response;
            }

            ArrayList columns = setupEnvironment.getTableColumns(rows, false, 5);

            ArrayList<HashMap<String, Object>> selectedCostCenter = new ArrayList<>();

            for (int i = 7; i < rows.size(); i++) {

                HashMap<String, Object> costCenter = new HashMap<>();

                WebElement row = rows.get(i);
                List<WebElement> cols = row.findElements((By.tagName("td")));

                if (cols.size() != columns.size()) {
                    continue;
                }

                ////////////////////////////////////////////////////////////////////////////////////////////
                try {
                    WebElement td = cols.get(columns.indexOf("cost_center"));

                } catch (Exception e) {

                }
                WebElement td = cols.get(columns.indexOf("cost_center"));
                CostCenter oldCostCenter = conversions.checkCostCenterExistence(costCenters, td.getText().strip(), true);

//                td.sendKeys(Keys.CONTROL +"t");
                ArrayList<String> tabs = new ArrayList<String>(driver.getWindowHandles());
                driver.switchTo().window(tabs.get(0));

                if (!oldCostCenter.checked) {
                    continue;
                }

                String extensions = cols.get(0).findElement(By.tagName("a")).getAttribute("href");
                extensions = extensions.substring(extensions.indexOf(":")).substring(0, extensions.indexOf("'"));

                costCenter.put("extension", extensions);
                costCenter.put("costCenter", oldCostCenter);

                selectedCostCenter.add(costCenter);

            }

            for (HashMap<String, Object> costCenter : selectedCostCenter) {

                journalBatch = new JournalBatch();
                journals = new ArrayList<>();

                driver.get(Constants.MICROS_REPORT_BASE_LINK + "         " + costCenter.get("extensions"));

                try {
                    wait = new WebDriverWait(driver, 5);
                    wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("tr")));
                    System.out.println("No Alert");
                } catch (Exception e) {
                    System.out.println("Waiting");
                }

                WebElement table = driver.findElement(By.xpath("/html/body/div[1]/section/div[1]/div[2]/div/div/div[2]/div/my-reports-cca/report-group-cca/div[1]/div[1]/div[7]/iframe-cca/div[1]/div/div[2]/table"));
                rows = table.findElements(By.tagName("tr"));

                if (rows.size() <= 3) {
                    continue;
                }

                columns = setupEnvironment.getTableColumns(rows, false, 0);

                String group;
                for (int i = 3; i < rows.size(); i++) {
                    HashMap<String, Object> transferDetails = new HashMap<>();

                    WebElement row = rows.get(i);

                    List<WebElement> cols = row.findElements(By.tagName("td"));

                    if (cols.size() != columns.size()) {
                        continue;
                    }

                    WebElement td = cols.get(columns.indexOf("item_group"));

                    ItemGroup itemGroup = conversions.checkItemGroupExistence(itemGroups, td.getText().strip());

                    if (!itemGroup.getChecked()) {
                        continue;
                    }

                    if (journalSyncJobType.getConfiguration().syncPerGroup.equals("OverGroup")) {
                        group = itemGroup.getOverGroup();
                    } else {
                        group = itemGroup.getItemGroup();
                    }

                    transferDetails.put("actual_cost", cols.get(columns.indexOf("actual_cost")).getText().strip());

                    Journal journal = new Journal();

                    float cost = conversions.convertStringToFloat(transferDetails.get("actual_cost").toString());
                    journals = journal.checkExistence(journals, group, 0, cost, 0);
                }

                journalBatch.setCostCenter((CostCenter) costCenter.get("costCenter"));
                journalBatch.setConsumption(journals);
                journalBatches.add(journalBatch);
            }

            response.setStatus(true);
            response.setJournalBatches(journalBatches);
            return response;

        } catch (Exception e) {
            e.printStackTrace();
            driver.quit();

            response.setStatus(false);
            response.setMessage("Failed to get consumption entries from Oracle Hospitality.");
            return response;
        }
    }

    /*
     * Get consumptions entries based on cost center
     * */
    public Response getJournalDataByCostCenter(SyncJobType journalSyncJobType, ArrayList<CostCenter> tempCostCenters,
                                               ArrayList<ItemGroup> itemGroups, Account account) {
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
        WebDriverWait wait;

        try {
            if (!microsFeatures.loginMicrosOHRA(driver, Constants.MICROS_V2_LINK, account)) {
                driver.quit();

                response.setStatus(false);
                response.setMessage("Invalid username and password.");
                response.setEntries(new ArrayList<>());
                return response;
            }

            try {
                wait = new WebDriverWait(driver, 5);
                wait.until(ExpectedConditions.alertIsPresent());
            } catch (Exception e) {
                System.out.println("Waiting");
            }

            response = getRows(driver, businessDate, fromDate, toDate);
            List<WebElement> rows;

            if (response.isStatus()) {
                rows = response.getRows();
            } else {
                return response;
            }

            if (rows.size() < 4) {
                driver.quit();
                response.setStatus(true);
                response.setMessage(Constants.NO_INFO);
                return response;
            }

            ArrayList columns = setupEnvironment.getTableColumns(rows, false, 4);

            ArrayList<HashMap<String, Object>> selectedCostCenter = new ArrayList<>();

            List<CostCenter> costCenters = new ArrayList<>();

            for (int i = 7; i < rows.size(); i++) {

                WebElement row = rows.get(i);
                List<WebElement> cols = row.findElements((By.tagName("td")));

                if (cols.size() != columns.size()) {
                    continue;
                }

                WebElement td = cols.get(columns.indexOf("cost_center"));
                CostCenter costCenter = conversions.checkCostCenterExistence(tempCostCenters, td.getText().strip(), true);

                if (!costCenter.checked) {
                    continue;
                }

                costCenters.add(costCenter);

            }

            for (CostCenter costCenter : costCenters) {

                journalBatch = new JournalBatch();
                journals = new ArrayList<>();


                try {
                    wait = new WebDriverWait(driver, 5);
                    wait.until(ExpectedConditions.alertIsPresent());
                } catch (Exception e) {
                    System.out.println("Waiting");
                }

                // Open Cost Center
                driver.findElement(By.linkText(costCenter.costCenter)).click();

                try {
                    wait = new WebDriverWait(driver, 5);
                    wait.until(ExpectedConditions.alertIsPresent());
                } catch (Exception e) {
                    System.out.println("Waiting");
                }

                List<WebElement> costRows = driver.findElements(By.tagName("tr"));

                if (costRows.size() <= 3) {
                    driver.findElement(By.linkText("Inventory Cost of Sales")).click();
                    continue;
                }

                ArrayList<String> costColumns = setupEnvironment.getTableColumns(costRows, false, 3);

                String group;
                for (int j = 6; j < costRows.size(); j++) {

                    HashMap<String, Object> consumptionDetails = new HashMap<>();

                    WebElement costRow = costRows.get(j);

                    List<WebElement> costCols = costRow.findElements(By.tagName("td"));

                    if (costCols.size() != columns.size()) {
                        continue;
                    }

                    WebElement costTd = costCols.get(costColumns.indexOf("item_group"));

                    ItemGroup itemGroup = conversions.checkItemGroupExistence(itemGroups, costTd.getText().strip());

                    if (!itemGroup.getChecked()) {
                        continue;
                    }

                    if (journalSyncJobType.getConfiguration().syncPerGroup.equals("OverGroup")) {
                        group = itemGroup.getOverGroup();
                    } else {
                        group = itemGroup.getItemGroup();
                    }

                    String actualUsage = costCols.get(costColumns.indexOf("actual_usage")).getText().strip();
                    double actualUsageDouble = 0;

                    try {
                        actualUsageDouble = Double.parseDouble(actualUsage);
                    } catch (Exception e) {
                        if (actualUsage.contains("(")) {
                            actualUsageDouble = Double.parseDouble(
                                    actualUsage.substring((actualUsage.indexOf("(") + 1), actualUsage.indexOf(")")));
                        }
                    }
                    if (actualUsageDouble == 0) {
                        continue;
                    }
                    consumptionDetails.put("actual_usage", actualUsage);

                    Journal journal = new Journal();

                    float cost = conversions.convertStringToFloat(actualUsage);
                    journals = journal.checkExistence(journals, group, 0, cost, 0);

                }

                driver.findElement(By.linkText("Inventory Cost of Sales")).click();
                if (journals.size() == 0) {
                    continue;
                }

                journalBatch.setCostCenter(costCenter);
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
            response.setMessage("Failed to get consumption entries from Oracle Hospitality.");
            return response;
        }
    }

    /*
     * Get consumptions entries based on location
     * */

    public Response getJournalData(SyncJobType journalSyncJobType,
                                   ArrayList<CostCenter> costCentersLocation,
                                   ArrayList<ItemGroup> itemGroups, List<CostCenter> costCenters, Account account) {
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

            try {
                WebDriverWait wait = new WebDriverWait(driver, 5);
                wait.until(ExpectedConditions.alertIsPresent());
            } catch (Exception ignored) {
            }

            WebDriverWait wait = new WebDriverWait(driver, 30);

            for (CostCenter costCenter : costCentersLocation) {
                journalBatch = new JournalBatch();
                journals = new ArrayList<>();

                if (!costCenter.checked)
                    continue;

                if (!driver.getCurrentUrl().equals(Constants.MICROS_CONSUMPTION_REPORT_LINK)) {
                    driver.get(Constants.MICROS_CONSUMPTION_REPORT_LINK);

                    try {
                        wait = new WebDriverWait(driver, 60);
                        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("loadingFrame")));
                    } catch (Exception ex) {
                        System.out.println(ex.getMessage());
                    }
                }

                // Filter Report
                Response dateResponse = microsFeatures.selectDateRangeMicros(businessDate, fromDate, toDate, costCenter.locationName,
                        null, "", driver);

                if (!dateResponse.isStatus()) {
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

                //Run
                driver.findElement(By.xpath("//*[@id=\"save-close-button\"]/button")).click();

                // Validate Report Parameters
                Response validateParameters = microsFeatures.checkReportParameters(driver, fromDate, toDate, businessDate, null);

                if (!validateParameters.isStatus()) {
                    response.setStatus(false);
                    response.setMessage(validateParameters.getMessage());
                    return response;
                }

                try {
                    wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("tableContainer")));
                } catch (Exception e) {
                    System.out.println("Waiting");
                }

                WebElement table = driver.findElement(By.id("tableContainer"));
                List<WebElement> rows = table.findElements(By.tagName("tr"));

                if (rows.size() < 1)
                    continue;

                ArrayList<String> columns = setupEnvironment.getTableColumns(rows, false, 0);

                ArrayList<HashMap<String, String>> costExtensions = new ArrayList<>();

                for (int i = 2; i < rows.size(); i++) {

                    WebElement row = rows.get(i);
                    List<WebElement> cols = row.findElements(By.tagName("td"));
                    if (cols.size() != columns.size()) {
                        continue;
                    }
                    String costCenterName = cols.get(0).getText();
                    HashMap<String, String> map = new HashMap<>();
                    map.put("costCenterName", costCenterName);
                    costExtensions.add(map);
                }

                String group;
                for (HashMap<String, String> extension : costExtensions) {
                    try {
                        driver.findElement(By.partialLinkText(extension.get("costCenterName"))).click();

                        /* Wait until table loaded */
                        try {
                            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("tableContainer")));
                        } catch (Exception e) {
                            System.out.println("Waiting");
                        }

                        table = driver.findElement(By.id("tableContainer"));
                        rows = table.findElements(By.tagName("tr"));

                        if (rows.size() < 1)
                            continue;

                        columns = setupEnvironment.getTableColumns(rows, false, 0);

                        for (int i = 3; i < rows.size(); i++) {
                            HashMap<String, Object> transferDetails = new HashMap<>();
                            WebElement row = rows.get(i);
                            List<WebElement> cols = row.findElements(By.tagName("td"));

                            if (cols.size() != columns.size()) {
                                continue;
                            }

                            // check if this Item group belong to selected Item groups
                            WebElement td = cols.get(columns.indexOf("item_group"));

                            ItemGroup itemGroup = conversions.checkItemGroupExistence(itemGroups, td.getText().strip());

                            if (!itemGroup.getChecked()) {
                                continue;
                            }

                            if (journalSyncJobType.getConfiguration().syncPerGroup.equals("OverGroups"))
                                group = itemGroup.getOverGroup();
                            else
                                group = itemGroup.getItemGroup();

                            for (int j = 0; j < cols.size(); j++) {
                                transferDetails.put(columns.get(j), cols.get(j).getText().strip());
                            }

                            Journal journal = new Journal();

                            for (CostCenter tempCostCenter : costCenters) {
                                if (tempCostCenter.costCenter.equals(extension.get("costCenterName"))) {
                                    journal.setCostCenter(tempCostCenter);
                                    break;
                                }
                            }
                            float cost = conversions.convertStringToFloat((String) transferDetails.get("actual_usage"));
                            float transfer = conversions.convertStringToFloat((String) transferDetails.get("net_transfers"));
                            float netReceipts = conversions.convertStringToFloat((String) transferDetails.get("net_receipts"));

                            journals = journal.checkExistence(journals, group, netReceipts, cost, transfer);
                        }

                        driver.findElement(By.id("100067")).click(); // Inventory Cost of Sales
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                journalBatch.setCostCenter(costCenter);
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
            response.setMessage("Failed to get consumption entries from Oracle Hospitality.");
            return response;
        }
    }


    private Response getRows(WebDriver driver, String businessDate, String fromDate, String toDate) {

        Response response = new Response();

        WebDriverWait wait;

        String journalUrl = Constants.MICROS_COS_REPORTS_COST_CENTER;
        driver.get(journalUrl);

        try {
            wait = new WebDriverWait(driver, 5);
            wait.until(ExpectedConditions.alertIsPresent());
        } catch (Exception e) {
            System.out.println("Waiting");
        }

        // Filter Report
        Response dateResponse = microsFeatures.selectDateRangeMicros(businessDate, fromDate, toDate, null,
                null, "", driver);

        if (!dateResponse.isStatus()) {
            response.setStatus(false);
            response.setMessage(dateResponse.getMessage());
            return response;
        }

        // Run
        driver.findElement(By.xpath("//*[@id=\"save-close-button\"]/button")).click();

        try {
            wait = new WebDriverWait(driver, 5);
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("tr")));
            System.out.println("No Alert");
        } catch (Exception e) {
            System.out.println("Waiting");
        }
        List<WebElement> rows = driver.findElements(By.tagName("tr"));

        response.setStatus(true);
        response.setRows(rows);

        return response;

    }


    /*
     * Get consumptions entries based on cost center
     * */
    public Response getJournalDataByCostCenterAndLocation(SyncJobType journalSyncJobType, ArrayList<CostCenter> consumptionLocations,
                                                          ArrayList<CostCenter> consumptionCostCenters, ArrayList<ItemGroup> itemGroups, Account account) {
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
        ArrayList<ConsumptionJournal> costCenterJournals;
        ConsumptionJournal consumptionJournal = new ConsumptionJournal();

        JournalBatch journalBatch;
        ArrayList<JournalBatch> journalBatches = new ArrayList<>();

        String businessDate = journalSyncJobType.getConfiguration().timePeriod;
        String fromDate = journalSyncJobType.getConfiguration().fromDate;
        String toDate = journalSyncJobType.getConfiguration().toDate;
        WebDriverWait wait;

        try {
            if (!microsFeatures.loginMicrosOHRA(driver, Constants.MICROS_V2_LINK, account)) {
                driver.quit();

                response.setStatus(false);
                response.setMessage("Invalid username and password.");
                response.setEntries(new ArrayList<>());
                return response;
            }

            try {
                wait = new WebDriverWait(driver, 5);
                wait.until(ExpectedConditions.alertIsPresent());
            } catch (Exception e) {
                System.out.println("Waiting");
            }

            response = getRows(driver, businessDate, fromDate, toDate);
            List<WebElement> rows;

            if (response.isStatus()) {
                rows = response.getRows();
            } else {
                return response;
            }

            if (rows.size() < 4) {
                driver.quit();
                response.setStatus(true);
                response.setMessage(Constants.NO_INFO);
                return response;
            }

            ArrayList columns = setupEnvironment.getTableColumns(rows, false, 4);

            ArrayList<CostCenter> costCenters = new ArrayList<>();
            journalBatch = new JournalBatch();
            journals = new ArrayList<>();
            costCenterJournals = new ArrayList<>();

            for (int i = 7; i < rows.size(); i++) {

                WebElement row = rows.get(i);
                List<WebElement> cols = row.findElements((By.tagName("td")));
                if (cols.size() != columns.size()) {
                    continue;
                }
                WebElement td = cols.get(columns.indexOf("cost_center"));
//                ConsumptionLocation consumptionCostCenter = conversions.checkConCostCenterExistence(consumptionLocations, td.getText().strip());
                CostCenter costCenter = conversions.checkCostCenterExistence(consumptionCostCenters, td.getText().strip(), true);

                if (costCenter.checked) {
                    costCenters.add(costCenter);
                }
            }

            for (CostCenter costCenter : costCenters) {


                try {
                    wait = new WebDriverWait(driver, 5);
                    wait.until(ExpectedConditions.alertIsPresent());
                } catch (Exception e) {
                    System.out.println("Waiting");
                }
                // Open Cost Center
                driver.findElement(By.linkText(costCenter.costCenter)).click();
                try {
                    wait = new WebDriverWait(driver, 5);
                    wait.until(ExpectedConditions.alertIsPresent());
                } catch (Exception e) {
                    System.out.println("Waiting");
                }
                List<WebElement> costRows = driver.findElements(By.tagName("tr"));
                if (costRows.size() <= 3) {
                    driver.findElement(By.linkText("Inventory Cost of Sales")).click();
                    continue;
                }
                ArrayList<String> costColumns = setupEnvironment.getTableColumns(costRows, false, 3);
                String group;
                float costCenterTotalCost = 0;
                for (int j = 6; j < costRows.size(); j++) {

                    HashMap<String, Object> transferDetails = new HashMap<>();

                    WebElement costRow = costRows.get(j);

                    List<WebElement> costCols = costRow.findElements(By.tagName("td"));

                    if (costCols.size() != columns.size()) {
                        continue;
                    }

                    WebElement costTd = costCols.get(costColumns.indexOf("item_group"));

                    ItemGroup itemGroup;

                    if (!costCenter.accountCode.equals(""))
                        itemGroup = conversions.checkItemGroupExistence(itemGroups, costTd.getText().strip());
                    else
                        itemGroup = conversions.checkItemGroupExistence(itemGroups, costTd.getText().strip());

                    if (itemGroup.getItemGroup().equals(""))
                        continue;

                    group = itemGroup.getItemGroup();


                    float cost = 0;
                    String costString = costCols.get(costColumns.indexOf("actual_usage")).getText().strip();

                    transferDetails.put("actual_usage", cost);
                    try {
                        cost = conversions.convertStringToFloat(costString);
                    } catch (Exception e) {
                        if (costString.contains("(")) {
                            cost = Float.parseFloat(
                                    costString.substring((costString.indexOf("(") + 1), costString.indexOf(")")));
                        }
                    }

                    if (cost == 0) {
                        continue;
                    }

                    costCenterTotalCost += cost;

                    costCenterJournals = consumptionJournal.checkJournalExistence(costCenterJournals, group, cost, itemGroup.getExpensesAccount(),
                            costCenter, "D");
                }

                costCenterJournals = consumptionJournal.checkJournalExistence(costCenterJournals, "", costCenterTotalCost, costCenter.accountCode,
                        costCenter, "C");

                driver.findElement(By.linkText("Inventory Cost of Sales")).click();

            }

            journalBatch.setCostCenter(consumptionLocations.get(0));
            journalBatch.setConsumptionJournals(costCenterJournals);
            journalBatches.add(journalBatch);

            driver.quit();

            response.setStatus(true);
            response.setJournalBatches(journalBatches);
            return response;

        } catch (Exception e) {
            e.printStackTrace();
            driver.quit();

            response.setStatus(false);
            response.setMessage("Failed to get consumption entries from Oracle Hospitality.");
            return response;
        }
    }
}
