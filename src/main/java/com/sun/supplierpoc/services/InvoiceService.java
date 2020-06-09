package com.sun.supplierpoc.services;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.controllers.InvoiceController;
import com.sun.supplierpoc.controllers.SyncJobDataController;
import com.sun.supplierpoc.controllers.SyncJobTypeController;
import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.models.configurations.CostCenter;
import com.sun.supplierpoc.models.configurations.Item;
import com.sun.supplierpoc.models.configurations.OverGroup;
import com.sun.supplierpoc.repositories.SyncJobDataRepo;
import com.sun.supplierpoc.seleniumMethods.SetupEnvironment;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class InvoiceService {
    @Autowired
    private SyncJobDataRepo syncJobDataRepo;
    @Autowired
    private SyncJobDataController syncJobTypeController;

    private Conversions conversions = new Conversions();
    private SetupEnvironment setupEnvironment = new SetupEnvironment();
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public HashMap<String, Object> getInvoicesData(Boolean typeFlag, SyncJobType syncJobType, ArrayList<CostCenter> costCenters,
                                                   ArrayList<Item> items, ArrayList<OverGroup> overGroups, Account account){
        HashMap<String, Object> response = new HashMap<>();

        WebDriver driver;
        try{
            driver = setupEnvironment.setupSeleniumEnv(false);
        }
        catch (Exception ex){
            response.put("status", Constants.FAILED);
            response.put("message", "Failed to establish connection with firefox driver.");
            response.put("invoices", new ArrayList<>());
            return response;
        }

        ArrayList<HashMap<String, Object>> invoices = new ArrayList<>();
        ArrayList<HashMap<String, Object>> journalEntries = new ArrayList<>();

        try {
            String url = "https://mte03-ohim-prod.hospitality.oracleindustry.com/Webclient/FormLogin.aspx";

            if (!setupEnvironment.loginOHIM(driver, url, account)){
                driver.quit();

                response.put("status", Constants.FAILED);
                response.put("message", "Invalid username and password.");
                response.put("invoices", journalEntries);
                return response;
            }

            String approvedInvoices = "https://mte03-ohim-prod.hospitality.oracleindustry.com/Webclient/Purchase/Invoicing/IvcOverviewView.aspx?type=1";
            driver.get(approvedInvoices);

            Select select = new Select(driver.findElement(By.id("_ctl5")));
            String timePeriod = syncJobType.getConfiguration().getTimePeriod();

            response = setupEnvironment.selectTimePeriod(timePeriod, select, driver);

            if (!response.get("status").equals(Constants.SUCCESS)){
                return response;
            }

            if (typeFlag){
                driver.findElement(By.id("igtxttbxInvoiceFilter")).sendKeys("RTV");
            }

            driver.findElement(By.name("filterPanel_btnRefresh")).click();

            try{
                WebDriverWait wait = new WebDriverWait(driver, 60);
                wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("tableLoadingBar")));

            } catch (Exception e) {
                driver.quit();

                response.put("status", Constants.FAILED);
                response.put("message", "Oracle Hospitality takes long time to load, Please try again after few minutes.");
                response.put("invoices", journalEntries);
                return response;
            }

            try {
                Alert al = driver.switchTo().alert();
                al.accept();

                response.put("status", Constants.FAILED);
                response.put("message", al.getText());
                response.put("invoices", journalEntries);
                return response;
            } catch (NoAlertPresentException Ex) {
                System.out.println("No alert exits");
            }

            try{
                WebDriverWait wait = new WebDriverWait(driver, 60);
                wait.until(ExpectedConditions.presenceOfElementLocated(By.id("G_dg")));

            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

            WebElement bodyTable = driver.findElement(By.id("G_dg"));
            WebElement headerTable = driver.findElement(By.xpath("/html/body/form/table/tbody/tr[4]/td/table/tbody/tr[1]/td/div/table"));

            List<WebElement> headerRows = headerTable.findElements(By.tagName("tr"));
            List<WebElement> rows = bodyTable.findElements(By.tagName("tr"));

            ArrayList<String> columns = setupEnvironment.getTableColumns(headerRows, true, 0);

            while (true){
                for (int i = 1; i < rows.size(); i++) {
                    HashMap<String, Object> invoice = new HashMap<>();

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

                    invoice.put(columns.get(columns.indexOf("cost_center")), oldCostCenterData);

                    // check if vendor exits in middleware
    //                    td = cols.get(columns.indexOf("vendor"));
    //                    ArrayList<SyncJobData> suppliers = syncJobDataController.getSyncJobData(syncJobType.getId());
    //                    HashMap<String, Object> oldSupplierData = conversions.checkSupplierExistence(suppliers, td.getText().strip());
    //
    //                    if (!(boolean) oldSupplierData.get("status")) {
    //                        continue;
    //                    }
    //                    invoice.put(columns.get(columns.indexOf("vendor")), oldSupplierData.get("supplier"));

                    // Mock supplier of now
                    HashMap<String, String> supplierData = new HashMap<>();
                    supplierData.put("accountCode", "001");
                    supplierData.put("supplier", "Golden greenz");

                    SyncJobData supplier = new SyncJobData(supplierData, Constants.RECEIVED, "", new Date(), "");

                    invoice.put(columns.get(columns.indexOf("vendor")), supplier);

                    String link = cols.get(columns.indexOf("invoice_no.")).findElement(By.tagName("a")).getAttribute("href");
                    link = link.substring(link.indexOf('(') + 1, link.indexOf(','));
                    String fullLink = "https://mte03-ohim-prod.hospitality.oracleindustry.com/Webclient/Purchase/Invoicing/InvoiceDetail.aspx?RNG_ID=" + link;
                    invoice.put("reference_link", fullLink);

                    for (int j = 0; j < cols.size(); j++) {
                        if (j == columns.indexOf("cost_center") || j == columns.indexOf("vendor"))
                            continue;
                        invoice.put(columns.get(j), cols.get(j).getText().strip());
                    }
                    invoices.add(invoice);
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

            for (HashMap<String, Object> invoice:invoices) {
                getInvoiceDetails(items, overGroups, invoice, driver, journalEntries, typeFlag);
            }

            driver.quit();

            response.put("status", Constants.SUCCESS);
            response.put("message", "");
            response.put("invoices", journalEntries);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            driver.quit();

            response.put("status", Constants.FAILED);
            response.put("message", e.getMessage());
            response.put("invoices", journalEntries);
            return response;
        }
    }

    private void getInvoiceDetails(
            ArrayList<Item> items, ArrayList<OverGroup> overGroups, HashMap<String, Object> invoice, WebDriver driver,
            ArrayList<HashMap<String, Object>> journalEntries, boolean flag){
        ArrayList<Journal> journals = new ArrayList<>();

        // Get Receipt page
        driver.get((String) invoice.get("reference_link"));

        WebDriverWait wait = new WebDriverWait(driver, 20);

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("igtxtdfReference")));
        String reference = driver.findElement(By.id("igtxtdfReference")).getAttribute("value");

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("dg_rc_0_1")));

        // Get Receipt details page
        driver.findElement(By.id("dg_rc_0_1")).findElement(By.tagName("a")).click();

        // Check if there is any alerts
        try {
            Alert al = driver.switchTo().alert();
            al.accept();
        } catch (NoAlertPresentException Ex) {
            System.out.println("No alert exits");
        }

        // Fetch table rows
        List<WebElement> rows = driver.findElements(By.tagName("tr"));
        ArrayList<String> columns = setupEnvironment.getTableColumns(rows, true, 32);

        for (int i = 35; i < rows.size(); i++) {
            HashMap<String, Object> invoiceDetails = new HashMap<>();
            WebElement row = rows.get(i);
            List<WebElement> cols = row.findElements(By.tagName("td"));

            if (cols.size() != columns.size()) {
                continue;
            }

            // check if this Item belong to selected items
            WebElement td = cols.get(columns.indexOf("item"));

            Item oldItemData = conversions.checkItemExistence(items, td.getText().strip());

            if (!oldItemData.isChecked()) {
                continue;
            }

            String overGroup = oldItemData.getOverGroup();
            if (overGroup.equals("")){
                continue;
            }

            invoiceDetails.put("Item", td.getText().strip());

            td = cols.get(columns.indexOf("total"));
            invoiceDetails.put("total", td.getText().strip());

            Journal journal = new Journal();
            journals = journal.checkExistence(journals, overGroup, 0,
                    conversions.convertStringToFloat((String) invoiceDetails.get("total")),0, 0);

        }

        for (Journal journal : journals) {
            HashMap<String, Object> journalEntry = new HashMap<>();

            CostCenter toCostCenter = (CostCenter) invoice.get("cost_center");
            SyncJobData supplier = (SyncJobData) invoice.get("vendor");

            if (toCostCenter.costCenterReference.equals("")){
                toCostCenter.costCenterReference = toCostCenter.costCenter;
            }

            journalEntry.put("invoiceNo", invoice.get("invoice_no."));

            if (reference.equals("")){
                journalEntry.put("reference", invoice.get("invoice_no."));
                journalEntry.put("transactionReference", invoice.get("invoice_no."));
            }
            else {
                journalEntry.put("reference", reference);
                journalEntry.put("transactionReference", reference);
            }

            journalEntry.put("totalCr", Math.round(journal.getTotalCost()));
            journalEntry.put("totalDr", Math.round(journal.getTotalCost()) * -1);

            journalEntry.put("from_cost_center", supplier.getData().get("supplier"));
            journalEntry.put("from_account_code", supplier.getData().get("accountCode"));

            journalEntry.put("to_cost_center", toCostCenter.costCenter);
            journalEntry.put("to_account_code", toCostCenter.accountCode);

            journalEntry.put("status", invoice.get("status"));
            journalEntry.put("invoiceDate", invoice.get("invoice_date"));

            journalEntry.put("createdBy", invoice.get("created_by"));
            journalEntry.put("createdAt", invoice.get("created_at"));

            if (!flag)
                journalEntry.put("description", "Invoice From "+ supplier.getData().get("supplier") + " to " + toCostCenter.costCenterReference);
            else
                journalEntry.put("description", "Credit Note From "+ supplier.getData().get("supplier") + " to " + toCostCenter.costCenterReference);

            journalEntry.put("overGroup", journal.getOverGroup());

            OverGroup oldOverGroupData = conversions.checkOverGroupExistence(overGroups, journal.getOverGroup());

            journalEntry.put("inventoryAccount", oldOverGroupData.getInventoryAccount());
            journalEntry.put("expensesAccount", oldOverGroupData.getExpensesAccount());

            journalEntries.add(journalEntry);
        }

    }

    public ArrayList<SyncJobData> saveInvoicesData(ArrayList<HashMap<String, String>> invoices, SyncJob syncJob,
                                                   SyncJobType syncJobType, Boolean flag){
        ArrayList<SyncJobData> addedInvoices = new ArrayList<>();
        ArrayList<SyncJobData> savedInvoices = syncJobTypeController.getSyncJobData(syncJobType.getId());

        for (HashMap<String, String> invoice : invoices) {
            // check existence of invoice in middleware (UNIQUE: invoiceNo with over group)
            SyncJobData oldInvoice = conversions.checkInvoiceExistence(savedInvoices, invoice.get("invoiceNo"),
                    invoice.get("overGroup"));
            if (oldInvoice != null){
                if (!oldInvoice.getStatus().equals(Constants.FAILED)){
                    continue;
                }
            }

            // Invoice Part
            if (!flag) {
                if ((invoice.get("invoiceNo")).substring(0, 3).equals("RTV")) {
                    continue;
                }
            }

            SyncJobData syncJobData = new SyncJobData(invoice, Constants.RECEIVED, "", new Date(),
                    syncJob.getId());
            syncJobDataRepo.save(syncJobData);

            addedInvoices.add(syncJobData);
        }
        return addedInvoices;

    }

}
