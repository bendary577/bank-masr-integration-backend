package com.sun.supplierpoc.services;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.controllers.SyncJobDataController;
import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.models.configurations.CostCenter;
import com.sun.supplierpoc.repositories.SyncJobDataRepo;
import com.sun.supplierpoc.seleniumMethods.SetupEnvironment;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

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

            ArrayList<HashMap<String, Object>> bookedProduction = new ArrayList<>();
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




        } catch (Exception e) {
            driver.quit();

            response.setStatus(false);
            response.setMessage("Failed to get booked production from Oracle Hospitality.");
            response.setBookedProduction(new ArrayList<>());
            return response;
        }


        return response;
    }

    public ArrayList<SyncJobData> saveBookedProductionData(ArrayList<HashMap<String, String>> invoices, SyncJob syncJob,
                                                   SyncJobType syncJobType){
        ArrayList<SyncJobData> addedInvoices = new ArrayList<>();
        ArrayList<SyncJobData> savedInvoices = syncJobTypeController.getSyncJobData(syncJobType.getId());

        for (HashMap<String, String> invoice : invoices) {
            // check existence of invoice in middleware (UNIQUE: invoiceNo with over group)
            SyncJobData oldInvoice = conversions.checkInvoiceExistence(savedInvoices, invoice.get("invoiceNo"),
                    invoice.get("overGroup"));
            if (oldInvoice != null){ continue; }

            SyncJobData syncJobData = new SyncJobData(invoice, Constants.RECEIVED, "", new Date(),
                    syncJob.getId());
            syncJobDataRepo.save(syncJobData);
            addedInvoices.add(syncJobData);

        }
        return addedInvoices;

    }

}
