package com.sun.supplierpoc.services;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.controllers.SyncJobDataController;
import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.models.configurations.*;
import com.sun.supplierpoc.repositories.SyncJobDataRepo;
import com.sun.supplierpoc.seleniumMethods.SetupEnvironment;
import com.sun.supplierpoc.soapModels.Supplier;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Driver;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class InvoiceService {
    @Autowired
    private SyncJobDataRepo syncJobDataRepo;
    @Autowired
    private SyncJobDataController syncJobTypeController;
    @Autowired
    private SyncJobDataService syncJobDataService;

    private Conversions conversions = new Conversions();
    private SetupEnvironment setupEnvironment = new SetupEnvironment();
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public HashMap<String, Object> getInvoicesData(Boolean creditNoteFlag, int typeFlag
                                                   , ArrayList<Supplier> suppliers, ArrayList<CostCenter> costCenters,
                                                   ArrayList<Item> items, ArrayList<OverGroup> overGroups, Account account,
                                                   String timePeriod, String fromDate, String toDate){
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
            if (!setupEnvironment.loginOHIM(driver, Constants.OHIM_LOGIN_LINK , account)){
                driver.quit();

                response.put("status", Constants.FAILED);
                response.put("message", "Invalid username and password.");
                response.put("invoices", journalEntries);
                return response;
            }

            if (typeFlag == 1){
                HashMap<String, Object> invoiceResponse = this.getAccountPayableData(creditNoteFlag,
                        suppliers, costCenters, driver, Constants.APPROVED_INVOICES_LINK,
                        timePeriod, fromDate, toDate);
                if (!invoiceResponse.get("status").equals(Constants.SUCCESS)){
                    return invoiceResponse;
                }
                invoices = (ArrayList<HashMap<String, Object>>) invoiceResponse.get("invoices");
            }else if (typeFlag == 2){
                HashMap<String, Object> accountPayableResponse = this.getAccountPayableData(creditNoteFlag, suppliers,
                        costCenters, driver, Constants.ACCOUNT_PAYABLE_LINK,
                        timePeriod, fromDate, toDate);
                if (!accountPayableResponse.get("status").equals(Constants.SUCCESS)){
                    return accountPayableResponse;
                }
                ArrayList<HashMap<String, Object>> accountPayable = (ArrayList<HashMap<String, Object>>) accountPayableResponse.get("invoices");
                invoices.addAll(accountPayable);
            }else{
                HashMap<String, Object> invoiceResponse = this.getAccountPayableData(creditNoteFlag,
                        suppliers, costCenters, driver, Constants.APPROVED_INVOICES_LINK,
                        timePeriod, fromDate, toDate);
                if (!invoiceResponse.get("status").equals(Constants.SUCCESS)){
                    return invoiceResponse;
                }
                invoices = (ArrayList<HashMap<String, Object>>) invoiceResponse.get("invoices");

                HashMap<String, Object> accountPayableResponse = this.getAccountPayableData(creditNoteFlag,
                        suppliers, costCenters, driver, Constants.ACCOUNT_PAYABLE_LINK,
                        timePeriod, fromDate, toDate);
                if (!accountPayableResponse.get("status").equals(Constants.SUCCESS)){
                    return accountPayableResponse;
                }
                ArrayList<HashMap<String, Object>> accountPayable = (ArrayList<HashMap<String, Object>>) accountPayableResponse.get("invoices");
                invoices.addAll(accountPayable);
            }

            for (HashMap<String, Object> invoice:invoices) {
                getInvoiceDetails(items, overGroups, invoice, driver, journalEntries, creditNoteFlag);
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


    private HashMap<String, Object> getAccountPayableData(Boolean creditNoteFlag,
                                                          ArrayList<Supplier> suppliers,
                                                          ArrayList<CostCenter> costCenters, WebDriver driver,
                                                          String url, String timePeriod, String fromDate, String toDate){
        HashMap<String, Object> response;
        ArrayList<HashMap<String, Object>> invoices = new ArrayList<>();
        driver.get(url);

        Select select = new Select(driver.findElement(By.id("_ctl5")));

        response = setupEnvironment.selectTimePeriodOHIM(timePeriod, fromDate, toDate, select, driver);

        if (!response.get("status").equals(Constants.SUCCESS)){
            return response;
        }

        if (creditNoteFlag){
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
            response.put("invoices", new ArrayList<>());
            return response;
        }

        try {
            Alert al = driver.switchTo().alert();
            al.accept();
            driver.quit();

            response.put("status", Constants.FAILED);
            response.put("message", al.getText());
            response.put("invoices", new ArrayList<>());
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
                td = cols.get(columns.indexOf("vendor"));

                Supplier oldSupplierData = conversions.checkSupplierExistence(suppliers, td.getText().strip());

                if (oldSupplierData == null) {
                    continue;
                }
                invoice.put(columns.get(columns.indexOf("vendor")), oldSupplierData);


                String link = cols.get(columns.indexOf("invoice_no.")).findElement(By.tagName("a")).getAttribute("href");
                link = link.substring(link.indexOf('(') + 1, link.indexOf(','));
                String fullLink = "https://mte03-ohim-prod.hospitality.oracleindustry.com/Webclient/Purchase/Invoicing/InvoiceDetail.aspx?RNG_ID=" + link;
                invoice.put("reference_link", fullLink);


                td = cols.get(columns.indexOf("invoice_date"));
                String deliveryDate = td.getText().strip();
                SimpleDateFormat formatter1=new SimpleDateFormat("MM/dd/yyyy");
                Date deliveryDateFormatted = null;
                try {
                    deliveryDateFormatted = formatter1.parse(deliveryDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                SimpleDateFormat simpleformat = new SimpleDateFormat("ddMMy");
                String date = simpleformat.format(deliveryDateFormatted);
                invoice.put("invoice_date", date);

                for (int j = 0; j < cols.size(); j++) {
                    if (j == columns.indexOf("cost_center") || j == columns.indexOf("vendor")
                            || j == columns.indexOf("invoice_date"))
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

        response.put("status", Constants.SUCCESS);
        response.put("message", "");
        response.put("invoices", invoices);
        return response;
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

        try {
            Alert al = driver.switchTo().alert();
            al.accept();
        } catch (NoAlertPresentException Ex) {
//            System.out.println("No alert exits");
        }

        // Fetch table rows
        List<WebElement> rows = driver.findElements(By.tagName("tr"));
        if (rows.size() <= 32){ return; }
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

            if(columns.indexOf("gross") != -1){
                td = cols.get(columns.indexOf("gross"));
                invoiceDetails.put("gross", td.getText().strip());
            }

            Journal journal = new Journal();
            journals = journal.checkExistence(journals, overGroup, 0,
                    conversions.convertStringToFloat((String) invoiceDetails.get("gross")),0, 0);

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
                reference = (String) invoice.get("invoice_no.");

                if (reference.length() > 30){
                    reference = reference.substring(0, 30);
                }
                journalEntry.put("reference", reference);
                journalEntry.put("transactionReference", reference);
            }
            else {
                journalEntry.put("reference", reference);
                if (reference.length() > 30){
                    reference = reference.substring(0, 30);
                }
                journalEntry.put("transactionReference", reference);
            }

            journalEntry.put("accountingPeriod", ((String)invoice.get("invoice_date")).substring(2,6));
            journalEntry.put("transactionDate", invoice.get("invoice_date"));

            journalEntry.put("totalCr", conversions.roundUpFloat(journal.getTotalCost()));
            journalEntry.put("totalDr", conversions.roundUpFloat(journal.getTotalCost()) * -1);

            if (!flag){
                journalEntry.put("fromCostCenter", supplier.getData().get("supplier"));
                journalEntry.put("fromAccountCode", supplier.getData().get("accountCode"));

                journalEntry.put("toCostCenter", toCostCenter.costCenter);
                journalEntry.put("toAccountCode", toCostCenter.accountCode);
            }else{
                journalEntry.put("toCostCenter", supplier.getData().get("supplier"));
                journalEntry.put("toAccountCode", supplier.getData().get("accountCode"));

                journalEntry.put("fromCostCenter", toCostCenter.costCenter);
                journalEntry.put("fromAccountCode", toCostCenter.accountCode);
            }

            journalEntry.put("fromLocation", toCostCenter.accountCode);
            journalEntry.put("toLocation", toCostCenter.accountCode);

            journalEntry.put("status", invoice.get("status"));
            journalEntry.put("invoiceDate", invoice.get("invoice_date"));

            journalEntry.put("createdBy", invoice.get("created_by"));
            journalEntry.put("createdAt", invoice.get("created_at"));

            String description = "";
            if (!flag){
                description = "Invoice F "+ supplier.getData().get("supplier") + " T " + toCostCenter.costCenterReference;
            }else{
                description = "Credit Note F "+ supplier.getData().get("supplier") + " T " + toCostCenter.costCenterReference;
            }

            if (description.length() > 50){
                description = description.substring(0, 50);
            }

            journalEntry.put("description", description);
            journalEntry.put("overGroup", journal.getOverGroup());

            OverGroup oldOverGroupData = conversions.checkOverGroupExistence(overGroups, journal.getOverGroup());

            if (!flag){ // Invoice from supplier to cost center
                journalEntry.put("inventoryAccount", supplier.getData().get("accountCode"));
                journalEntry.put("expensesAccount", oldOverGroupData.getExpensesAccount());
            }else{  // Credit Note from cost center to supplier
                journalEntry.put("inventoryAccount", oldOverGroupData.getInventoryAccount());
                journalEntry.put("expensesAccount", supplier.getData().get("accountCode"));
            }

            journalEntries.add(journalEntry);
        }

    }

    //////////////////////////////////////////////// Invoices Receipts //////////////////////////////////////////////////


    public HashMap<String, Object> getInvoicesReceiptsData(Boolean creditNoteFlag, int typeFlag, Configuration configuration,
                                                           ArrayList<CostCenter> costCenters, ArrayList<Supplier> suppliers,
                                                           ArrayList<Item> items, ArrayList<ItemGroup> itemGroups,
                                                           ArrayList<OverGroup> overGroups, Account account,
                                                           String timePeriod, String fromDate, String toDate){

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
            if (!setupEnvironment.loginOHIM(driver, Constants.OHIM_LOGIN_LINK , account)){
                driver.quit();

                response.put("status", Constants.FAILED);
                response.put("message", "Invalid username and password.");
                response.put("invoices", journalEntries);
                return response;
            }

            driver.get(Constants.RECEIPTS_LINK);

            // Open filter search
            String filterStatus = driver.findElement(By.id("filterPanel_btnToggleFilter")).getAttribute("value");

            if (filterStatus.equals("Show Filter")){
                driver.findElement(By.id("filterPanel_btnToggleFilter")).click();
            }

            Select select = new Select(driver.findElement(By.id("_ctl5")));

            response = setupEnvironment.selectTimePeriodOHIM(timePeriod, fromDate, toDate, select, driver);

            if (!response.get("status").equals(Constants.SUCCESS)){
                return response;
            }

            if (creditNoteFlag){
                driver.findElement(By.id("igtxttbxDocument")).sendKeys("RTV");
            }

            driver.findElement(By.name("filterPanel_btnRefresh")).click();

            try{
                WebDriverWait wait = new WebDriverWait(driver, 60);
                wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("tableLoadingBar")));

            } catch (Exception e) {
                driver.quit();

                response.put("status", Constants.FAILED);
                response.put("message", "Oracle Hospitality takes long time to load, Please try again after few minutes.");
                response.put("invoices", new ArrayList<>());
                return response;
            }

            try {
                Alert al = driver.switchTo().alert();
                al.accept();
                driver.quit();

                response.put("status", Constants.FAILED);
                response.put("message", al.getText());
                response.put("invoices", new ArrayList<>());
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

            WebElement table= driver.findElement(By.id("G_dg"));
            WebElement tableHeader= driver.findElement(By.xpath("/html/body/form/table/tbody/tr[5]/td/table/tbody/tr[1]/td/div/table"));

            List<WebElement> rows = table.findElements(By.tagName("tr"));
            List<WebElement> headerRows = tableHeader.findElements(By.tagName("tr"));

            ArrayList<String> columns = setupEnvironment.getTableColumns(headerRows, true, 0);

            getInvoices(rows, columns, typeFlag, costCenters, suppliers, driver, invoices, table);

            for (HashMap<String, Object> invoice:invoices) {
                getInvoiceReceiptsDetails(configuration, items, itemGroups, overGroups, invoice, driver, journalEntries, creditNoteFlag);
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
            response.put("message", "Failed to get invoices from Oracle Hospitality.");
            response.put("invoices", journalEntries);
            return response;
        }

    }

    private void getInvoices(List<WebElement> rows, ArrayList<String> columns, int typeFlag, ArrayList<CostCenter> costCenters
                             , ArrayList<Supplier> suppliers, WebDriver driver, ArrayList<HashMap<String, Object>> invoices , WebElement table){
        while (true){
            for (int i = 1; i < rows.size(); i++) {
                HashMap<String, Object> invoice = new HashMap<>();

                WebElement row = rows.get(i);
                List<WebElement> cols = row.findElements(By.tagName("td"));
                if (cols.size() !=  columns.size()){
                    continue;
                }

                WebElement status = cols.get(columns.indexOf("status"));

                if (typeFlag == 1){
                    if(!status.getText().strip().equals(Constants.APPROVED_INVOICE_Status)){
                        continue;
                    }
                }else if (typeFlag == 2){
                    if(!status.getText().strip().equals(Constants.ACCOUNT_PAYABLE_RTV_Status)
                            && !status.getText().strip().equals(Constants.ACCOUNT_PAYABLE_Status)){
                        continue;
                    }
                }

                // check if cost center chosen
                WebElement td = cols.get(columns.indexOf("cost_center"));
                CostCenter oldCostCenterData = conversions.checkCostCenterExistence(costCenters, td.getText().strip(), false);

                if (!oldCostCenterData.checked) {
                    continue;
                }

                invoice.put(columns.get(columns.indexOf("cost_center")), oldCostCenterData);

                // check if vendor exits in middleware
                td = cols.get(columns.indexOf("vendor"));

                Supplier oldSupplierData = conversions.checkSupplierExistence(suppliers, td.getText().strip());

                if (oldSupplierData == null) {
                    continue;
                }
                invoice.put(columns.get(columns.indexOf("vendor")), oldSupplierData);

                String link = cols.get(columns.indexOf("document")).findElement(By.tagName("a")).getAttribute("href");
                link = link.substring(link.indexOf("'") + 1, link.lastIndexOf("'"));

                String fullLink = "https://mte3-ohim.oracleindustry.com/InventoryManagement/Purchase/Receiving/" + link;
                invoice.put("reference_link", fullLink);

                td = cols.get(columns.indexOf("delivery_date"));
                String deliveryDate = td.getText().strip();
                SimpleDateFormat formatter1=new SimpleDateFormat("MM/dd/yyyy");
                Date deliveryDateFormatted = null;
                try {
                    deliveryDateFormatted = formatter1.parse(deliveryDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                SimpleDateFormat simpleformat = new SimpleDateFormat("ddMMy");
                String date = simpleformat.format(deliveryDateFormatted);
                invoice.put("invoice_date", date);

                for (int j = 0; j < cols.size(); j++) {
                    if (j == columns.indexOf("cost_center") || j == columns.indexOf("vendor")
                            || j == columns.indexOf("delivery_date"))
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
                table = driver.findElement(By.id("G_dg"));
                rows = table.findElements(By.tagName("tr"));
            }
        }
    }

    private void getInvoiceReceiptsDetails(Configuration configuration,
            ArrayList<Item> items, ArrayList<ItemGroup> itemGroups, ArrayList<OverGroup> overGroups, HashMap<String, Object> invoice, WebDriver driver,
                                           ArrayList<HashMap<String, Object>> journalEntries, boolean flag){
        ArrayList<Journal> journals = new ArrayList<>();

        // Get Receipt page
        driver.get((String) invoice.get("reference_link"));

        WebDriverWait wait = new WebDriverWait(driver, 20);

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("igtxttbxReference")));
        String reference = driver.findElement(By.id("igtxttbxReference")).getAttribute("value");

        try {
            Alert al = driver.switchTo().alert();
            al.accept();
        } catch (NoAlertPresentException Ex) {
        }

        // Fetch table rows
        try {
            WebElement table= driver.findElement(By.id("G_dg"));
            List<WebElement> rows = table.findElements(By.tagName("tr"));

            WebElement tableHeader= driver.findElement(By.xpath("/html/body/form/table/tbody/tr[6]/td/table/tbody/tr[1]/td/div/table"));
            List<WebElement> headerRows = tableHeader.findElements(By.tagName("tr"));
            ArrayList<String> columns = setupEnvironment.getTableColumns(headerRows, true, 0);

            if (rows.size() < 1){ return; }

            String group;
            for (int i = 1; i < rows.size(); i++) {

                HashMap<String, Object> invoiceDetails = new HashMap<>();
                WebElement row = rows.get(i);
                List<WebElement> cols = row.findElements(By.tagName("td"));

                if (cols.size() != columns.size()) {
                    continue;
                }

                // check if this Item belong to selected items
                WebElement td = cols.get(columns.indexOf("item"));

                Item item = conversions.checkItemExistence(items, td.getText().strip());

                if (!item.isChecked()) {
                    continue;
                }

                if(configuration.syncPerGroup.equals("OverGroups"))
                    group = item.getOverGroup();
                else
                    group = item.getItemGroup();

                invoiceDetails.put("Item", td.getText().strip());

                if(columns.indexOf("gross") != -1){
                    td = cols.get(columns.indexOf("gross"));
                    invoiceDetails.put("gross", td.getText().strip());
                }

                if(columns.indexOf("vat[%]") != -1){
                    td = cols.get(columns.indexOf("vat[%]"));
                    invoiceDetails.put("vat", td.getText().strip());
                }
                LoggerFactory.getLogger(InvoiceService.class).info("Item" + item.getItem() + "vat"  +  td.getText().strip());
                Journal journal = new Journal();
                journals = journal.checkExistenceB(journals, group, 0,
                        conversions.convertStringToFloat((String) invoiceDetails.get("gross")),
                        0, 0, invoiceDetails.get("vat").toString());
            }

            for (Journal journal : journals) {
                HashMap<String, Object> journalEntry = new HashMap<>();
                Supplier supplier = (Supplier) invoice.get("vendor");
                CostCenter toCostCenter = (CostCenter) invoice.get("cost_center");

                if(configuration.syncPerGroup.equals("OverGroups")){
                    OverGroup oldOverGroupData = conversions.checkOverGroupExistence(overGroups, journal.getOverGroup());
                    if(!oldOverGroupData.getChecked())
                        continue;
                    if (oldOverGroupData.getExpensesAccount().equals("") || oldOverGroupData.getInventoryAccount().equals(""))
                        continue;

                    if (!flag){ // Invoice from supplier to cost center
                        journalEntry.put("inventoryAccount", supplier.getAccountCode());
                        journalEntry.put("expensesAccount", oldOverGroupData.getExpensesAccount());
                    }else{  // Credit Note from cost center to supplier
                        journalEntry.put("inventoryAccount", oldOverGroupData.getInventoryAccount());
                        journalEntry.put("expensesAccount", supplier.getAccountCode());
                    }
                }
                else{
                    ItemGroup itemGroup = conversions.checkItemGroupExistence(itemGroups, journal.getOverGroup());
                    if(!itemGroup.getChecked())
                        continue;
                    if (itemGroup.getExpensesAccount().equals("") || itemGroup.getInventoryAccount().equals(""))
                        continue;

                    if (!flag){ // Invoice from supplier to cost center
                        journalEntry.put("inventoryAccount", supplier.getAccountCode());
                        journalEntry.put("expensesAccount", itemGroup.getExpensesAccount());
                    }else{  // Credit Note from cost center to supplier
                        journalEntry.put("inventoryAccount", itemGroup.getInventoryAccount());
                        journalEntry.put("expensesAccount", supplier.getAccountCode());
                    }
                }

                if (toCostCenter.costCenterReference.equals("")){
                    toCostCenter.costCenterReference = toCostCenter.costCenter;
                }
                if(toCostCenter.location != null)
                    syncJobDataService.prepareAnalysisForInvoices(journalEntry, configuration, toCostCenter, null, null, supplier, journal);
                else
                    syncJobDataService.prepareAnalysisForInvoices(journalEntry, configuration, toCostCenter, null, null, supplier, journal);

                journalEntry.put("invoiceNo", invoice.get("document"));

                if (reference == null || reference.equals("")){
                    reference = (String) invoice.get("document");
                    //  internal field length is 30).
                    if (reference.length() > 30){
                        reference = reference.substring(0, 30);
                    }
                    journalEntry.put("reference", reference);
                    journalEntry.put("transactionReference", reference);
                }
                else {
                    journalEntry.put("reference", reference);
                    //  internal field length is 30).
                    if (reference.length() > 30){
                        reference = reference.substring(0, 30);
                    }
                    journalEntry.put("transactionReference", reference);
                }

                journalEntry.put("accountingPeriod", ((String)invoice.get("invoice_date")).substring(2,6));
                journalEntry.put("transactionDate", invoice.get("invoice_date"));

                journalEntry.put("totalCr", String.valueOf(conversions.roundUpFloat(journal.getTotalCost())));
                journalEntry.put("totalDr", String.valueOf(conversions.roundUpFloat(journal.getTotalCost()) * -1));

                if (!flag){
                    journalEntry.put("fromCostCenter", supplier.getSupplierName());
                    journalEntry.put("fromAccountCode", supplier.getAccountCode());

                    journalEntry.put("toCostCenter", toCostCenter.costCenter);
                    journalEntry.put("toAccountCode", toCostCenter.accountCode);
                }else{
                    journalEntry.put("toCostCenter", supplier.getSupplierName());
                    journalEntry.put("toAccountCode", supplier.getAccountCode());

                    journalEntry.put("fromCostCenter", toCostCenter.costCenter);
                    journalEntry.put("fromAccountCode", toCostCenter.accountCode);
                }

                journalEntry.put("fromLocation", toCostCenter.accountCode);
                journalEntry.put("toLocation", toCostCenter.accountCode);

                journalEntry.put("status", invoice.get("status"));
                journalEntry.put("invoiceDate", invoice.get("invoice_date"));

                journalEntry.put("createdBy", invoice.get("created_by"));
                journalEntry.put("createdAt", invoice.get("created_at"));

                String description;
                if (!flag){
                    description = "Inv F "+ supplier.getSupplierReference() + " T " + toCostCenter.costCenterReference;
                }else{
                    description = "CN F "+ supplier.getSupplierReference() + " T " + toCostCenter.costCenterReference;
                }

                if (description.length() > 50){
                    description = description.substring(0, 50);
                }

                journalEntry.put("description", description);
//                journalEntry.put("overGroup", journal.getOverGroup());

                journalEntries.add(journalEntry);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<SyncJobData> saveInvoicesData(ArrayList<HashMap<String, Object>> invoices, SyncJob syncJob,
                                                   SyncJobType syncJobType, Boolean flag){
        ArrayList<SyncJobData> addedInvoices = new ArrayList<>();
        ArrayList<SyncJobData> savedInvoices = syncJobTypeController.getSyncJobData(syncJobType.getId());

        for (HashMap<String, Object> invoice : invoices) {

            if (!flag) {
                if ((invoice.get("invoiceNo")).toString().length() >= 3){
                    if ((invoice.get("invoiceNo")).toString().substring(0, 3).equals("RTV")) {
                        continue;
                    }
                }
            }

            SyncJobData syncJobData = new SyncJobData(invoice, Constants.RECEIVED, "", new Date(),
                    syncJob.getId());
            syncJobDataRepo.save(syncJobData);
            addedInvoices.add(syncJobData);
        }

        // Get Failed entries
        ArrayList<SyncJobData>  failedReceipts = syncJobTypeController.getFailedSyncJobData(syncJobType.getId());
        for (SyncJobData failedSyncJobData : failedReceipts ) {
            failedSyncJobData.setStatus(Constants.RETRY_TO_SEND);
            failedSyncJobData.setSyncJobId(syncJob.getId());
            syncJobDataRepo.save(failedSyncJobData);
        }
        addedInvoices.addAll(failedReceipts);

        return addedInvoices;
    }
}
