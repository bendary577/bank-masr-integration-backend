package com.sun.supplierpoc.services;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.controllers.SyncJobDataController;
import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.models.configurations.CostCenter;
import com.sun.supplierpoc.models.configurations.Item;
import com.sun.supplierpoc.models.configurations.OverGroup;
import com.sun.supplierpoc.models.configurations.WasteGroup;
import com.sun.supplierpoc.repositories.SyncJobDataRepo;
import com.sun.supplierpoc.seleniumMethods.SetupEnvironment;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Service
public class BookedProductionService {
    @Autowired
    private SyncJobDataRepo syncJobDataRepo;
    @Autowired
    private SyncJobDataController syncJobTypeController;

    private Conversions conversions = new Conversions();
    private SetupEnvironment setupEnvironment = new SetupEnvironment();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public Response getBookedProductionData(SyncJobType syncJobType, ArrayList<Item> items,
                                            ArrayList<OverGroup> overGroups, ArrayList<CostCenter> costCenters,
                                            Account account){
        Response response = new Response();

        WebDriver driver;
        try{
            driver = setupEnvironment.setupSeleniumEnv(false);
        }
        catch (Exception ex){
            response.setStatus(false);
            response.setMessage("Failed to establish connection with firefox driver.");
            response.setBookedProduction(new ArrayList<>());
            return response;
        }

        try {
            if (!setupEnvironment.loginOHIM(driver, Constants.OHIM_LOGIN_LINK, account)) {
                driver.quit();

                response.setStatus(false);
                response.setMessage("Invalid username and password.");
                response.setBookedProduction(new ArrayList<>());
                return response;
            }

            ArrayList<BookedProduction> bookedProduction = new ArrayList<>();
            ArrayList<BookedProduction> detailedBookedProduction = new ArrayList<>();

            driver.get(Constants.BOOKED_PRODUCTION_LINK);

            Select select = new Select(driver.findElement(By.id("_ctl5")));
            String timePeriod = syncJobType.getConfiguration().getTimePeriod();

            HashMap<String, Object> timePeriodResponse = setupEnvironment.selectTimePeriodOHIM(timePeriod, select, driver);

            if (!timePeriodResponse.get("status").equals(Constants.SUCCESS)){
                response.setStatus(false);
                response.setMessage((String) timePeriodResponse.get("message"));
                response.setBookedProduction(new ArrayList<>());
                return response;
            }

            driver.findElement(By.name("filterPanel_btnRefresh")).click();

            try{
                WebDriverWait wait = new WebDriverWait(driver, 60);
                wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("tableLoadingBar")));

            } catch (Exception e) {
                driver.quit();

                response.setStatus(false);
                response.setMessage("Oracle Hospitality takes long time to load, Please try again after few minutes.");
                response.setBookedProduction(new ArrayList<>());
                return response;
            }

            try{
                WebDriverWait wait = new WebDriverWait(driver, 60);
                wait.until(ExpectedConditions.presenceOfElementLocated(By.id("G_dg")));

            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

            WebElement bodyTable = driver.findElement(By.id("G_dg"));
            WebElement headerTable = driver.findElement(By.id("dg_main"));

            List<WebElement> rows = bodyTable.findElements(By.tagName("tr"));
            List<WebElement> headerRows = headerTable.findElements(By.tagName("tr"));

            ArrayList<String> columns = setupEnvironment.getTableColumns(headerRows, true, 0);

            while (true){
                BookedProduction bookedProductionRow;
                for (int i = 1; i < rows.size(); i++) {
                    bookedProductionRow = new BookedProduction();

                    WebElement row = rows.get(i);
                    List<WebElement> cols = row.findElements(By.tagName("td"));
                    if (cols.size() !=  columns.size()){
                        continue;
                    }

                    WebElement td = cols.get(columns.indexOf("cost_center"));
                    CostCenter oldCostCenterData = conversions.checkCostCenterExistence(costCenters, td.getText().strip(), false);

                    if (!oldCostCenterData.checked) {
                        continue;
                    }

                    bookedProductionRow.setCostCenter(oldCostCenterData);

                    td = cols.get(columns.indexOf("date"));
                    String deliveryDate = td.getText().strip();

                    SimpleDateFormat formatter1=new SimpleDateFormat("MM/dd/yyyy");
                    Date deliveryDateFormatted =formatter1.parse(deliveryDate);

                    SimpleDateFormat simpleformat = new SimpleDateFormat("ddMMy");
                    String date = simpleformat.format(deliveryDateFormatted);

                    bookedProductionRow.setDate(date);

                    bookedProductionRow.setName(cols.get(columns.indexOf("name")).getText().strip());
                    bookedProductionRow.setStatus(cols.get(columns.indexOf("status")).getText().strip());

                    bookedProductionRow.setChangedAt(cols.get(columns.indexOf("changed_at")).getText().strip());
                    bookedProductionRow.setChangedBy(cols.get(columns.indexOf("changed_by")).getText().strip());
                    bookedProductionRow.setCreatedAt(cols.get(columns.indexOf("created_at")).getText().strip());
                    bookedProductionRow.setCreatedBy(cols.get(columns.indexOf("created_by")).getText().strip());

                    String link =
                            "https://mte03-ohim-prod.hospitality.oracleindustry.com/Webclient/Production/ProductionD/" +
                            cols.get(columns.indexOf("name")).findElement(By.tagName("a")).getAttribute("href");
                    bookedProductionRow.setDetailsLink(link);

                    bookedProductionRow.setValue(conversions.convertStringToFloat(
                            cols.get(columns.indexOf("value")).getText().strip()));

                    bookedProduction.add(bookedProductionRow);
                }

                if (driver.findElements(By.linkText("Next")).size() == 0){
                    break;
                }
                else {
                    TransferService.checkPagination(driver, "dg_rc_0_1");
                    bodyTable = driver.findElement(By.id("G_dg"));
                    rows = bodyTable.findElements(By.tagName("tr"));
                }
            }

            for (BookedProduction productionRow:bookedProduction) {
                getBookedProductionDetails(items, overGroups, productionRow, detailedBookedProduction, driver);
            }

            driver.quit();

            response.setStatus(true);
            response.setMessage("");
            response.setBookedProduction(detailedBookedProduction);
            return response;

        } catch (Exception e) {
            driver.quit();

            response.setStatus(false);
            response.setMessage("Failed to get booked production from Oracle Hospitality.");
            response.setBookedProduction(new ArrayList<>());
            return response;
        }
    }

    private void getBookedProductionDetails(
            ArrayList<Item> items, ArrayList<OverGroup> overGroups, BookedProduction bookedProduction,
            ArrayList<BookedProduction> detailedBookedProductions, WebDriver driver){
        ArrayList<Journal> journals = new ArrayList<>();

        try {
            driver.get(bookedProduction.getDetailsLink());

            WebElement headerTable = driver.findElement(By.xpath("/html/body/form/table/tbody/tr[5]/td/table/tbody/tr[1]/td/div/table"));
            WebElement bodyTable = driver.findElement(By.id("G_dg"));

            List<WebElement> headerRows = headerTable.findElements(By.tagName("tr"));
            List<WebElement> rows = bodyTable.findElements(By.tagName("tr"));

            ArrayList<String> columns = setupEnvironment.getTableColumns(headerRows, true, 0);

            for (int i = 1; i < rows.size(); i++) {
                HashMap<String, Object> productionDetails = new HashMap<>();
                WebElement row = rows.get(i);
                List<WebElement> cols = row.findElements(By.tagName("td"));

                if (cols.size() != columns.size()) { continue; }

                WebElement td = cols.get(columns.indexOf("item"));
                Item oldItemData = conversions.checkItemExistence(items, td.getText().strip());
                if (!oldItemData.isChecked()) {
                    continue;
                }

                String overGroup = oldItemData.getOverGroup();

                productionDetails.put("Item", td.getText().strip());

                td = cols.get(columns.indexOf("value"));
                productionDetails.put("value", td.getText().strip());

                Journal journal = new Journal();
                journals = journal.checkExistence(journals, overGroup, conversions.convertStringToFloat((String) productionDetails.get("value")),
                        0,0, 0);

            }

            for (Journal journal : journals) {
                OverGroup oldOverGroupData = conversions.checkOverGroupExistence(overGroups, journal.getOverGroup());

                if (!oldOverGroupData.getChecked()) {
                    continue;
                }

                bookedProduction.setValue(conversions.roundUpFloat(journal.getTotalWaste()));
                bookedProduction.setOverGroup(oldOverGroupData);

                detailedBookedProductions.add(bookedProduction);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public ArrayList<SyncJobData> saveBookedProductionData(ArrayList<BookedProduction> bookedProductions,
                                                           SyncJob syncJob, SyncJobType bookedProductionSyncJobType){
        ArrayList<SyncJobData> addedBookedProduction = new ArrayList<>();

        for (BookedProduction bookedProduction : bookedProductions) {
            HashMap<String, String> bookedProductionData = new HashMap<>();

            bookedProductionData.put("status", bookedProduction.getStatus());

            bookedProductionData.put("transactionDate", bookedProduction.getDate());

            bookedProductionData.put("totalCr", String.valueOf(conversions.roundUpFloat(bookedProduction.getValue())));
            bookedProductionData.put("totalDr", String.valueOf(conversions.roundUpFloat(bookedProduction.getValue()) * -1));

            bookedProductionData.put("fromCostCenter", bookedProduction.getCostCenter().costCenter);
            bookedProductionData.put("toCostCenter", bookedProduction.getCostCenter().costCenter);

            bookedProductionData.put("fromAccountCode", bookedProduction.getCostCenter().accountCode);
            bookedProductionData.put("toAccountCode", bookedProduction.getCostCenter().accountCode);

            bookedProductionData.put("fromLocation", bookedProduction.getCostCenter().accountCode);
            bookedProductionData.put("toLocation", bookedProduction.getCostCenter().accountCode);

            String description = "Booked Production F " + bookedProduction.getCostCenter().costCenterReference;
            if (description.length() > 50){
                description = description.substring(0, 50);
            }

            bookedProductionData.put("description", description);

            bookedProductionData.put("transactionReference", "Production Transaction Reference");

            bookedProductionData.put("inventoryAccount", bookedProduction.getOverGroup().getInventoryAccount());
            bookedProductionData.put("expensesAccount", bookedProductionSyncJobType.getConfiguration().getExpensesAccount());

            bookedProductionData.put("createdBy", bookedProduction.getCreatedBy());
            bookedProductionData.put("createdAt", bookedProduction.getCreatedAt());


            SyncJobData syncJobData = new SyncJobData(bookedProductionData, Constants.RECEIVED, "", new Date(),
                    syncJob.getId());
            syncJobDataRepo.save(syncJobData);
            addedBookedProduction.add(syncJobData);

        }
        return addedBookedProduction;
    }
}
