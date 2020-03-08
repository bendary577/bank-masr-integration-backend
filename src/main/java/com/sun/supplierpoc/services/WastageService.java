package com.sun.supplierpoc.services;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.controllers.InvoiceController;
import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.repositories.SyncJobDataRepo;
import com.sun.supplierpoc.seleniumMethods.SetupEnvironment;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static com.sun.supplierpoc.services.TransferService.checkPagination;

@Service

public class WastageService {

    @Autowired
    SyncJobDataRepo syncJobDataRepo;
    @Autowired
    InvoiceController invoiceController;

    private Conversions conversions = new Conversions();
    private SetupEnvironment setupEnvironment = new SetupEnvironment();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public HashMap<String, Object> getWastageData(SyncJobType syncJobType, SyncJobType syncJobTypeApprovedInvoice,
                                                   Account account) {

        HashMap<String, Object> data = new HashMap<>();

        WebDriver driver = setupEnvironment.setupSeleniumEnv(false);
        ArrayList<HashMap<String, Object>> wastes = new ArrayList<>();

        ArrayList<HashMap<String, String>> costCenters = (ArrayList<HashMap<String, String>>) syncJobTypeApprovedInvoice.getConfiguration().get("costCenters");
        ArrayList<HashMap<String, String>> items = (ArrayList<HashMap<String, String>>) syncJobType.getConfiguration().get("items");

        ArrayList<HashMap<String, Object>> journalEntries = new ArrayList<>();

        try {
            String url = "https://mte03-ohim-prod.hospitality.oracleindustry.com/Webclient/FormLogin.aspx";

            if (!setupEnvironment.loginOHIM(driver, url, account)) {
                driver.quit();

                data.put("status", Constants.FAILED);
                data.put("message", "Invalid username and password.");
                data.put("wastes", wastes);
                return data;
            }

            String bookedWasteUrl = "https://mte03-ohra-prod.hospitality.oracleindustry.com/finengine/reportAction.do?method=run&reportID=497";
            driver.get(bookedWasteUrl);

            driver.findElement(By.name("filterPanel_btnRefresh")).click();

            List<WebElement> rows = driver.findElements(By.tagName("tr"));

            ArrayList<String> columns = setupEnvironment.getTableColumns(rows, true, 13);

            while (true) {
                for (int i = 14; i < rows.size(); i++) {
                    HashMap<String, Object> waste = new HashMap<>();

                    WebElement row = rows.get(i);
                    List<WebElement> cols = row.findElements(By.tagName("td"));
                    if (cols.size() != columns.size()) {
                        continue;
                    }

                    WebElement td = cols.get(columns.indexOf("from_cost_center"));
                    HashMap<String, Object> oldCostCenterData = invoiceController.checkCostCenterExistence(costCenters, td.getText().strip(), false);

                    if (!(boolean) oldCostCenterData.get("status")) {
                        continue;
                    }
                    waste.put("from_cost_center", oldCostCenterData.get("costCenter"));


                    td = cols.get(columns.indexOf("to_cost_center"));
                    oldCostCenterData = invoiceController.checkCostCenterExistence(costCenters, td.getText().strip(), false);

                    if (!(boolean) oldCostCenterData.get("status")) {
                        continue;
                    }
                    waste.put("to_cost_center", oldCostCenterData.get("costCenter"));

                    td = cols.get(columns.indexOf("document"));
                    waste.put(columns.get(columns.indexOf("document")), td.getText());
                    String detailsLink = td.findElement(By.tagName("a")).getAttribute("href");
                    waste.put("details_url", detailsLink);

                    for (int j = columns.indexOf("delivery_date"); j < cols.size(); j++) {
                        waste.put(columns.get(j), cols.get(j).getText());
                    }
                    wastes.add(waste);
                }

                // check if there is other pages
                if (driver.findElements(By.linkText("Next")).size() == 0){
                    break;
                }
                else {
                    checkPagination(driver);
                    rows = driver.findElements(By.tagName("tr"));
                }
            }
            for (HashMap<String, Object> waste: wastes) {
                getBookedTransferDetails(items, waste, driver, journalEntries);
            }

            driver.quit();

            data.put("status", Constants.SUCCESS);
            data.put("message", "");
            data.put("wastes", journalEntries);
            return data;

        } catch (Exception e) {
            e.printStackTrace();
            driver.quit();

            data.put("status", Constants.FAILED);
            data.put("message", e);
            data.put("wastes", wastes);
            return data;
        }
    }

    private void getBookedTransferDetails(
            ArrayList<HashMap<String, String>> items, HashMap<String, Object> transfer, WebDriver driver,
            ArrayList<HashMap<String, Object>> journalEntries){
        ArrayList<Journal> journals = new ArrayList<>();

        try {
            driver.get((String) transfer.get("details_url"));
            List<WebElement> rows = driver.findElements(By.tagName("tr"));
            ArrayList<String> columns = setupEnvironment.getTableColumns(rows, true, 22);

            for (int i = 23; i < rows.size(); i++) {
                HashMap<String, Object> transferDetails = new HashMap<>();
                WebElement row = rows.get(i);
                List<WebElement> cols = row.findElements(By.tagName("td"));

                if (cols.size() != columns.size()) {
                    continue;
                }

                // check if this item belong to selected items
                WebElement td = cols.get(columns.indexOf("item"));

                HashMap<String, Object> oldItemData = conversions.checkItemExistence(items, td.getText().strip());

                if (!(boolean) oldItemData.get("status")) {
                    continue;
                }

                HashMap<String, String> oldItem = (HashMap<String, String>) oldItemData.get("item");
                String overGroup = oldItem.get("over_group");

                transferDetails.put("item", td.getText());

                td = cols.get(columns.indexOf("total"));
                transferDetails.put("total", td.getText());

                Journal journal = new Journal();
                journals = journal.checkExistence(journals, overGroup, 0,0, 0,
                        conversions.convertStringToFloat((String) transferDetails.get("total")));

            }

            for (Journal journal : journals) {
                HashMap<String, Object> journalEntry = new HashMap<>();
                HashMap<String, String> fromCostCenter = (HashMap<String, String>) transfer.get("from_cost_center");
                HashMap<String, String> toCostCenter = (HashMap<String, String>) transfer.get("to_cost_center");

                journalEntry.put("total", journal.getTotalTransfer());
                journalEntry.put("from_cost_center", fromCostCenter.get("costCenter"));
                journalEntry.put("from_account_code", fromCostCenter.get("accountCode"));

                journalEntry.put("to_cost_center", toCostCenter.get("costCenter"));
                journalEntry.put("to_account_code", fromCostCenter.get("accountCode"));

                journalEntry.put("description", "Transfer From " + fromCostCenter.get("costCenter") + " to "+
                        toCostCenter.get("costCenter") + " - " + journal.getOverGroup());

                journalEntry.put("transactionReference", "");
                journalEntry.put("overGroup", journal.getOverGroup());


                journalEntries.add(journalEntry);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public ArrayList<SyncJobData> saveWastageSunData(ArrayList<HashMap<String, String>> wastes, SyncJob syncJob) {
        ArrayList<SyncJobData> addedTransfers = new ArrayList<>();

        for (HashMap<String, String> waste : wastes) {

            SyncJobData syncJobData = new SyncJobData(waste, Constants.RECEIVED, "", new Date(),
                    syncJob.getId());
            syncJobDataRepo.save(syncJobData);

            addedTransfers.add(syncJobData);
        }
        return addedTransfers;

    }



}
