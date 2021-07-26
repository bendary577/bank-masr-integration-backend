package com.sun.supplierpoc.services;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.controllers.InvoiceController;
import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.models.configurations.CostCenter;
import com.sun.supplierpoc.models.configurations.ItemGroup;
import com.sun.supplierpoc.repositories.SyncJobDataRepo;
import com.sun.supplierpoc.seleniumMethods.MicrosFeatures;
import com.sun.supplierpoc.seleniumMethods.SetupEnvironment;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
    public Response getJournalDataByCostCenter(SyncJobType journalSyncJobType, ArrayList<CostCenter> costCenters,
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
        WebDriverWait wait = new WebDriverWait(driver, 30);

        try {
            if (!microsFeatures.loginMicrosOHRA(driver, Constants.MICROS_V2_LINK, account)) {
                driver.quit();

                response.setStatus(false);
                response.setMessage("Invalid username and password.");
                response.setEntries(new ArrayList<>());
                return response;
            }

            String journalUrl = Constants.MICROS_COS_REPORTS;
            driver.get(journalUrl);

            // Filter Report
            Response dateResponse = microsFeatures.selectDateRangeMicros(businessDate, fromDate, null,
                    null,"", driver);

            if (!dateResponse.isStatus()){
                response.setStatus(false);
                response.setMessage(dateResponse.getMessage());
                return response;
            }

            // Run
            driver.findElement(By.xpath("//*[@id=\"save-close-button\"]/button")).click();

            List<WebElement> rows = driver.findElements(By.tagName("tr"));

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
