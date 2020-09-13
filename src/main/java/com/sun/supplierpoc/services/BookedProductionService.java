package com.sun.supplierpoc.services;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.controllers.SyncJobDataController;
import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.models.configurations.CostCenter;
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

    public Response getBookedProductionData(SyncJobType syncJobType, ArrayList<CostCenter> costCenters, Account account){

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
                // wait until table is ready
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
                for (int i = 1; i < rows.size(); i++) {
                    BookedProduction bookedProductionRow = new BookedProduction();

                    WebElement row = rows.get(i);
                    List<WebElement> cols = row.findElements(By.tagName("td"));
                    if (cols.size() !=  columns.size()){
                        continue;
                    }

                    // check if cost center chosen
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
                    bookedProductionRow.setValue(conversions.convertStringToFloat(
                            cols.get(columns.indexOf("value")).getText().strip()));

                    bookedProduction.add(bookedProductionRow);
                }

                // check if there is other pages
                if (driver.findElements(By.linkText("Next")).size() == 0){
                    break;
                }
                else {
                    TransferService.checkPagination(driver, "dg_rc_0_1");
                    bodyTable = driver.findElement(By.id("G_dg"));
                    rows = bodyTable.findElements(By.tagName("tr"));
                }
            }

            driver.quit();

            response.setStatus(true);
            response.setMessage("");
            response.setBookedProduction(bookedProduction);
            return response;



        } catch (Exception e) {
            driver.quit();

            response.setStatus(false);
            response.setMessage("Failed to get booked production from Oracle Hospitality.");
            response.setBookedProduction(new ArrayList<>());
            return response;
        }
    }

    public ArrayList<SyncJobData> saveBookedProductionData(ArrayList<BookedProduction> bookedProductions, SyncJob syncJob,
                                                   SyncJobType syncJobType){
        ArrayList<SyncJobData> addedInvoices = new ArrayList<>();
        ArrayList<SyncJobData> savedInvoices = syncJobTypeController.getSyncJobData(syncJobType.getId());

        for (BookedProduction bookedProduction : bookedProductions) {
            // check existence of invoice in middleware (UNIQUE: invoiceNo with over group)
//            SyncJobData oldInvoice = conversions.checkInvoiceExistence(savedInvoices, bookedProductions.get("invoiceNo"),
//                    bookedProductions.get("overGroup"));
//            if (oldInvoice != null){ continue; }
//
//            SyncJobData syncJobData = new SyncJobData(bookedProductions, Constants.RECEIVED, "", new Date(),
//                    syncJob.getId());
//            syncJobDataRepo.save(syncJobData);
//            addedInvoices.add(syncJobData);

        }
        return addedInvoices;

    }

}
