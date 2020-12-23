package com.sun.supplierpoc.services;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.controllers.InvoiceController;
import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.models.configurations.*;
import com.sun.supplierpoc.repositories.SyncJobDataRepo;
import com.sun.supplierpoc.seleniumMethods.SetupEnvironment;
import com.sun.supplierpoc.soapModels.JournalSSC;
import com.sun.supplierpoc.soapModels.Message;
import com.systemsunion.security.IAuthenticationVoucher;
import com.systemsunion.ssc.client.ComponentException;
import com.systemsunion.ssc.client.SecurityProvider;
import com.systemsunion.ssc.client.SoapComponent;
import com.systemsunion.ssc.client.SoapFaultException;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


@Service
public class TransferService {

    @Autowired
    SyncJobDataRepo syncJobDataRepo;
    @Autowired
    InvoiceController invoiceController;

    private Conversions conversions = new Conversions();
    private SetupEnvironment setupEnvironment = new SetupEnvironment();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public HashMap<String, Object> getTransferData(SyncJobType syncJobTypeTransfer,
                                                   ArrayList<CostCenter> costCenters, ArrayList<Item> items,
                                                   ArrayList<OverGroup> overGroups , Account account) {
        HashMap<String, Object> data = new HashMap<>();
        WebDriver driver;
        try{
            driver = setupEnvironment.setupSeleniumEnv(false);
        }
        catch (Exception ex){
            data.put("status", Constants.FAILED);
            data.put("message", "Failed to establish connection with firefox driver.");
            data.put("invoices", new ArrayList<>());
            return data;
        }

        ArrayList<HashMap<String, Object>> transfers = new ArrayList<>();
        String timePeriod = syncJobTypeTransfer.getConfiguration().getTimePeriod();
        String fromDate = syncJobTypeTransfer.getConfiguration().getFromDate();
        String toDate = syncJobTypeTransfer.getConfiguration().getToDate();

        ArrayList<HashMap<String, Object>> journalEntries = new ArrayList<>();

        try {
            if (!setupEnvironment.loginOHIM(driver, Constants.OHIM_LOGIN_LINK, account)) {
                driver.quit();

                data.put("status", Constants.FAILED);
                data.put("message", "Invalid username and password.");
                data.put("transfers", transfers);
                return data;
            }

            String bookedTransfersUrl = "https://mte03-ohim-prod.hospitality.oracleindustry.com/Webclient/Store/TransferNew/TrStatus.aspx?Type=Booked";
            driver.get(bookedTransfersUrl);

            Select select = new Select(driver.findElement(By.id("_ctl5")));

            data = setupEnvironment.selectTimePeriodOHIM(timePeriod, fromDate, toDate, select, driver);

            if (!data.get("status").equals(Constants.SUCCESS)){
                return data;
            }

            driver.findElement(By.name("filterPanel_btnRefresh")).click();

            WebElement table = driver.findElement(By.id("dg_main"));
            List<WebElement> rows = table.findElements(By.tagName("tr"));

            ArrayList<String> columns = setupEnvironment.getTableColumns(rows, true, 0);

            while (true) {
                for (int i = 1; i < rows.size(); i++) {
                    HashMap<String, Object> transfer = new HashMap<>();

                    WebElement row = rows.get(i);
                    List<WebElement> cols = row.findElements(By.tagName("td"));
                    if (cols.size() != columns.size()) {
                        continue;
                    }

                    WebElement td = cols.get(columns.indexOf("from_cost_center"));
                    CostCenter oldCostCenterData = conversions.checkCostCenterExistence(costCenters, td.getText().strip(), false);

                    if (! oldCostCenterData.checked) {
                        continue;
                    }
                    transfer.put("fromCostCenter", oldCostCenterData);

                    td = cols.get(columns.indexOf("to_cost_center"));
                    oldCostCenterData = conversions.checkCostCenterExistence(costCenters, td.getText().strip(), false);

                    if (! oldCostCenterData.checked) {
                        continue;
                    }
                    transfer.put("toCostCenter", oldCostCenterData);

                    td = cols.get(columns.indexOf("document"));
                    transfer.put(columns.get(columns.indexOf("document")), td.getText().strip());
                    String detailsLink = td.findElement(By.tagName("a")).getAttribute("href");
                    transfer.put("details_url", detailsLink);


                    td = cols.get(columns.indexOf("delivery_date"));
                    String deliveryDate = td.getText().strip();
                    // 7/11/2020 "Hospitality Format"
                    SimpleDateFormat formatter1=new SimpleDateFormat("MM/dd/yyyy");
                    Date deliveryDateFormatted =formatter1.parse(deliveryDate);

                    SimpleDateFormat simpleformat = new SimpleDateFormat("ddMMy");
                    String date = simpleformat.format(deliveryDateFormatted);

                    transfer.put("delivery_date", date);

                    for (int j = columns.indexOf("delivery_date")+1 ; j < cols.size(); j++) {
                        transfer.put(columns.get(j), cols.get(j).getText().strip());
                    }
                    transfers.add(transfer);
                }

                // check if there is other pages
                if (driver.findElements(By.linkText("Next")).size() == 0){
                    break;
                }
                else {
                    checkPagination(driver, "dg_rc_0_1");
                    table = driver.findElement(By.id("dg_main"));
                    rows = table.findElements(By.tagName("tr"));
                }
            }
            for (HashMap<String, Object> transfer: transfers) {
                getBookedTransferDetails(items, overGroups, transfer, driver, journalEntries);
            }

            driver.quit();

            data.put("status", Constants.SUCCESS);
            data.put("message", "Get transfers Successfully.");
            data.put("transfers", journalEntries);
            return data;

        } catch (Exception e) {
            e.printStackTrace();
            driver.quit();

            data.put("status", Constants.FAILED);
            data.put("message", e);
            data.put("transfers", transfers);
            return data;
        }
    }

    public static boolean checkPagination(WebDriver driver, String itemChanged) {
        String first_element_text = driver.findElement(By.id(itemChanged)).getText().strip();
        driver.findElement(By.linkText("Next")).click();
        String element_txt = "";

        WebDriverWait wait = new WebDriverWait(driver, 20);
        WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(By.id(itemChanged)));
        try {
            element_txt = element.getText().strip();
        } catch (Exception e) {
            element_txt = "";
        }

        while (true){
            if (element_txt.equals(first_element_text)){
                wait = new WebDriverWait(driver, 20);
                element = wait.until(ExpectedConditions.presenceOfElementLocated(By.id(itemChanged)));
                try {
                    element_txt = element.getText().strip();
                } catch (Exception e) {
                    element_txt = "";
                }
            }
            else break;
        }
        return true;
    }

    private void getBookedTransferDetails(
            ArrayList<Item> items, ArrayList<OverGroup> overGroups, HashMap<String, Object> transfer, WebDriver driver,
            ArrayList<HashMap<String, Object>> journalEntries){
        ArrayList<Journal> journals = new ArrayList<>();

        try {
            driver.get((String) transfer.get("details_url"));
            List<WebElement> rows = driver.findElements(By.tagName("tr"));

            if (rows.size() <= 22){ return; }

            ArrayList<String> columns = setupEnvironment.getTableColumns(rows, true, 22);

            for (int i = 23; i < rows.size(); i++) {
                HashMap<String, Object> transferDetails = new HashMap<>();
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

                transferDetails.put("Item", td.getText().strip());

                td = cols.get(columns.indexOf("total"));
                transferDetails.put("total", td.getText().strip());

                Journal journal = new Journal();
                journals = journal.checkExistence(journals, overGroup, 0,0, 0,
                        conversions.convertStringToFloat((String) transferDetails.get("total")));

            }

            for (Journal journal : journals) {
                HashMap<String, Object> journalEntry = new HashMap<>();
                CostCenter fromCostCenter = (CostCenter) transfer.get("fromCostCenter");
                CostCenter toCostCenter = (CostCenter) transfer.get("toCostCenter");

                if (fromCostCenter.costCenterReference.equals("")){
                    fromCostCenter.costCenterReference = fromCostCenter.costCenter;
                }
                if (toCostCenter.costCenterReference.equals("")){
                    toCostCenter.costCenterReference = toCostCenter.costCenter;
                }

                journalEntry.put("accountingPeriod", ((String)transfer.get("delivery_date")).substring(2,6));
                journalEntry.put("transactionDate", transfer.get("delivery_date"));

                journalEntry.put("totalCr", conversions.roundUpFloat(journal.getTotalTransfer()));
                journalEntry.put("totalDr", conversions.roundUpFloat(journal.getTotalTransfer()) * -1);

                journalEntry.put("fromCostCenter", fromCostCenter.costCenter);
                journalEntry.put("fromAccountCode", fromCostCenter.accountCode);

                journalEntry.put("toCostCenter", toCostCenter.costCenter);
                journalEntry.put("toAccountCode", toCostCenter.accountCode);

                journalEntry.put("fromLocation", fromCostCenter.accountCode);
                journalEntry.put("toLocation", toCostCenter.accountCode);

                String description = "Tr F " + fromCostCenter.costCenterReference + " T "+
                        toCostCenter.costCenterReference + " - " + journal.getOverGroup();
                if (description.length() > 50){
                    description = description.substring(0, 50);
                }

                journalEntry.put("description", description);

                if (!transfer.get("reference").equals("")){
                    String reference = (String)transfer.get("reference");
                    if(reference.length() > 30){
                        reference = reference.substring(0, 30);
                    }
                    journalEntry.put("transactionReference", reference);
                }else {
                    journalEntry.put("transactionReference", "Transfer Reference");
                }
                journalEntry.put("overGroup", journal.getOverGroup());

                OverGroup oldOverGroupData = conversions.checkOverGroupExistence(overGroups, journal.getOverGroup());

                journalEntry.put("inventoryAccount", oldOverGroupData.getInventoryAccount());
                journalEntry.put("expensesAccount", oldOverGroupData.getExpensesAccount());

                journalEntries.add(journalEntry);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public ArrayList<SyncJobData> saveTransferSunData(ArrayList<HashMap<String, String>> transfers, SyncJob syncJob) {
        ArrayList<SyncJobData> addedTransfers = new ArrayList<>();

        for (HashMap<String, String> transfer : transfers) {

            SyncJobData syncJobData = new SyncJobData(transfer, Constants.RECEIVED, "", new Date(),
                    syncJob.getId());
            syncJobDataRepo.save(syncJobData);

            addedTransfers.add(syncJobData);
        }
        return addedTransfers;

    }
}
