package com.sun.supplierpoc.services;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.controllers.InvoiceController;
import com.sun.supplierpoc.controllers.SyncJobDataController;
import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.models.configurations.*;
import com.sun.supplierpoc.repositories.SyncJobDataRepo;
import com.sun.supplierpoc.seleniumMethods.SetupEnvironment;
import com.sun.supplierpoc.soapModels.JournalSSC;
import com.sun.supplierpoc.soapModels.Message;
import com.systemsunion.security.IAuthenticationVoucher;
import com.systemsunion.ssc.client.SoapComponent;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
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
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class SalesService {
    @Autowired
    SyncJobDataRepo syncJobDataRepo;
    @Autowired
    TransferService transferService;
    @Autowired
    InvoiceController invoiceController;
    @Autowired
    private SyncJobDataController syncJobTypeController;

    private Conversions conversions = new Conversions();
    private SetupEnvironment setupEnvironment = new SetupEnvironment();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public Response getSalesData(SyncJobType salesSyncJobType, ArrayList<CostCenter> costCenters,
                                 ArrayList<CostCenter> costCentersLocation, ArrayList<MajorGroup> majorGroups,
                                 ArrayList<Tender> includedTenders,  ArrayList<Tax> includedTax,
                                 Account account) {

        Response response = new Response();
        ArrayList<JournalBatch> journalBatches = new ArrayList<>();

        String timePeriod = salesSyncJobType.getConfiguration().getTimePeriod();
        String fromDate = salesSyncJobType.getConfiguration().getFromDate();
        String toDate = salesSyncJobType.getConfiguration().getToDate();

        WebDriver driver = null;
        try {
            driver = setupEnvironment.setupSeleniumEnv(false);
        } catch (Exception ex) {
            response.setStatus(false);
            response.setMessage("Failed to establish connection with firefox driver.");
            response.setEntries(new ArrayList<>());
            return response;
        }

        try {
            if (!setupEnvironment.loginOHRA(driver, Constants.OHRA_LOGIN_LINK, account)) {
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
                System.out.println("No alert exits");
            }

            if (costCenters.size() > 0){
                for (CostCenter costCenter : costCenters) {
                    // check if cost center has location mapping
                    CostCenter costCenterLocation = conversions.checkCostCenterExistence(costCentersLocation, costCenter.costCenter, false);

                    if (!costCenterLocation.checked) {
                        continue;
                    }

                    JournalBatch journalBatch = new JournalBatch();

                    // Get tender
                    Response tenderResponse = getSalesTenders(costCenterLocation.locationName, timePeriod, fromDate, toDate,
                            costCenter, includedTenders, driver);
                    if (!tenderResponse.isStatus()) {
                        if (tenderResponse.getMessage().equals(Constants.INVALID_LOCATION)) {
                            continue;
                        }
                        response.setStatus(false);
                        response.setMessage(tenderResponse.getMessage());
                        return response;
                    }

                    // Get taxes
                    Response taxResponse = getSalesTaxes(costCenterLocation.locationName, timePeriod, fromDate, toDate,
                            costCenter, false, includedTax, driver);
                    if (!taxResponse.isStatus()) {
                        if (taxResponse.getMessage().equals(Constants.INVALID_LOCATION)) {
                            continue;
                        }
                        response.setStatus(false);
                        response.setMessage(taxResponse.getMessage());
                        return response;
                    }

                    // Get over group gross
                    Response overGroupGrossResponse = getSalesOverGroupGross(costCenterLocation.locationName, timePeriod,
                            fromDate, toDate, costCenter, majorGroups, driver);
                    if (!overGroupGrossResponse.isStatus()) {
                        if (overGroupGrossResponse.getMessage().equals(Constants.INVALID_LOCATION)) {
                            continue;
                        }
                        response.setStatus(false);
                        response.setMessage(overGroupGrossResponse.getMessage());
                        return response;
                    }

                    journalBatch.setCostCenter(costCenter);

                    // Set Debit Entries (Tenders)
                    journalBatch.setSalesTender(tenderResponse.getSalesTender());

                    // Set Debit Entries (Taxes And overGroupsGross)
                    journalBatch.setSalesTax(taxResponse.getSalesTax());
                    journalBatch.setSalesMajorGroupGross(overGroupGrossResponse.getSalesMajorGroupGross());

                    // Calculate different
                    journalBatch.setSalesDifferent(0.0);
                    journalBatches.add(journalBatch);
                }
            }else {
                JournalBatch journalBatch = new JournalBatch();

                // Get tender
                Response tenderResponse = getSalesTenders("", timePeriod, fromDate, toDate,
                        new CostCenter(), includedTenders, driver);
                if (!tenderResponse.isStatus()) {
                    if (tenderResponse.getMessage().equals(Constants.INVALID_LOCATION)) {
                        driver.quit();

                        response.setStatus(false);
                        response.setMessage(Constants.INVALID_LOCATION);
                        response.setEntries(new ArrayList<>());
                    }
                    response.setStatus(false);
                    response.setMessage(tenderResponse.getMessage());
                    return response;
                }

                // Get taxes
                Response taxResponse = getSalesTaxes("", timePeriod, fromDate, toDate,
                        new CostCenter(), false, includedTax, driver);
                if (!taxResponse.isStatus()) {
                    if (taxResponse.getMessage().equals(Constants.INVALID_LOCATION)) {
                        driver.quit();

                        response.setStatus(false);
                        response.setMessage(Constants.INVALID_LOCATION);
                        response.setEntries(new ArrayList<>());
                    }
                    response.setStatus(false);
                    response.setMessage(taxResponse.getMessage());
                    return response;
                }

                // Get over group gross
                Response overGroupGrossResponse = getSalesOverGroupGross("", timePeriod,
                        fromDate, toDate, new CostCenter(), majorGroups, driver);
                if (!overGroupGrossResponse.isStatus()) {
                    if (overGroupGrossResponse.getMessage().equals(Constants.INVALID_LOCATION)) {
                        driver.quit();

                        response.setStatus(false);
                        response.setMessage(Constants.INVALID_LOCATION);
                        response.setEntries(new ArrayList<>());
                    }
                    response.setStatus(false);
                    response.setMessage(overGroupGrossResponse.getMessage());
                    return response;
                }

                // Set Debit Entries (Tenders)
                journalBatch.setSalesTender(tenderResponse.getSalesTender());

                // Set Debit Entries (Taxes And overGroupsGross)
                journalBatch.setSalesTax(taxResponse.getSalesTax());
                journalBatch.setSalesMajorGroupGross(overGroupGrossResponse.getSalesMajorGroupGross());

                // Calculate different
                journalBatch.setSalesDifferent(0.0);
                journalBatches.add(journalBatch);
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

    private Response getSalesTenders(String location, String businessDate, String fromDate, String toDate,
                                     CostCenter costCenter, ArrayList<Tender> includedTenders, WebDriver driver) {
        Response response = new Response();
        ArrayList<Tender> tenders = new ArrayList<>();

        if (!driver.getCurrentUrl().equals(Constants.TENDERS_REPORT_LINK)) {
            driver.get(Constants.TENDERS_REPORT_LINK);
        }

        try {
            WebDriverWait wait = new WebDriverWait(driver, 20);
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("loadingFrame")));
        } catch (Exception Ex) {
            System.out.println("There is no loader");
        }

        Response dateResponse = setupEnvironment.selectTimePeriodOHRA(businessDate, fromDate, toDate, location, driver);

        if (!dateResponse.isStatus()){
            response.setStatus(false);
            response.setMessage( dateResponse.getMessage());
            response.setSalesTender(tenders);
            return response;
        }

        driver.findElement(By.id("Run Report")).click();

        String tenderReportLink = Constants.OHRA_LINK +
                "/finengine/reportRunAction.do?rptroot=19&reportID=TendersDailyDetail&method=run";

        try {
            driver.get(tenderReportLink);

            List<WebElement> rows = driver.findElements(By.tagName("tr"));
            if (rows.size() < 5) {
                response.setStatus(true);
                response.setMessage("There is no tender entries in this location");
                response.setSalesTender(new ArrayList<>());

                return response;
            }
            ArrayList<String> columns = setupEnvironment.getTableColumns(rows, false, 5);

            for (int i = 7; i < rows.size(); i++) {
                Tender tender = new Tender();
                WebElement row = rows.get(i);
                List<WebElement> cols = row.findElements(By.tagName("td"));

                if (cols.size() != columns.size()) {
                    continue;
                }

                // Check if tender exists
                Tender tenderData = conversions.checkTenderExistence(includedTenders, cols.get(0).getText().strip());
                if (!tenderData.isChecked()) {
                    continue;
                }

                tender.setTender(tenderData.getTender());
                tender.setCostCenter(costCenter);
                tender.setAccount(tenderData.getAccount());
                tender.setTotal(conversions.convertStringToFloat(cols.get(1).getText().strip()));

                tenders.add(tender);
            }

            response.setStatus(true);
            response.setMessage("");
            response.setSalesTender(tenders);

        } catch (Exception e) {
            driver.quit();
            response.setStatus(false);
            response.setMessage("Failed to get sales entries from Oracle Hospitality.");
        }

        return response;
    }

    private Response getSalesTaxes(String location, String businessDate, String fromDate, String toDate,
                                   CostCenter costCenter, boolean getTaxTotalFlag, ArrayList<Tax> includedTaxes,
                                   WebDriver driver) {
        Response response = new Response();

        ArrayList<Tax> salesTax = new ArrayList<>();

        driver.get(Constants.TAXES_REPORT_LINK);

        try {
            WebDriverWait wait = new WebDriverWait(driver, 20);
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("loadingFrame")));
        } catch (Exception Ex) {
            System.out.println("There is no loader");
        }

        Response dateResponse = setupEnvironment.selectTimePeriodOHRA(businessDate, fromDate, toDate, location, driver);

        if (!dateResponse.isStatus()){
            response.setStatus(false);
            response.setMessage( dateResponse.getMessage());
            response.setSalesTax(salesTax);
            return response;
        }

        driver.findElement(By.id("Run Report")).click();

        String taxReportLink = Constants.OHRA_LINK +
                "/finengine/reportRunAction.do?rptroot=18&reportID=TaxesDailyDetail&method=run";

        try {
            driver.get(taxReportLink);

            List<WebElement> rows = driver.findElements(By.tagName("tr"));
            if (rows.size() < 5) {
                response.setStatus(true);
                response.setMessage("There is no tax entries in this location");
                response.setSalesTax(new ArrayList<>());

                return response;
            }

            ArrayList<String> columns = setupEnvironment.getTableColumns(rows, false, 5);

            for (int i = 6; i < rows.size(); i++) {
                WebElement row = rows.get(i);
                List<WebElement> cols = row.findElements(By.tagName("td"));

                if (cols.size() != columns.size()) {
                    continue;
                }

                Tax tax = new Tax();

                if(getTaxTotalFlag){
                    WebElement td = cols.get(0);
                    if (td.getText().equals("Total Taxes:")) {
                        tax.setTax("Total Tax");
                        tax.setTotal(conversions.convertStringToFloat(cols.get(1).getText().strip()));
                        tax.setCostCenter(costCenter);
                        salesTax.add(tax);
                        break;
                    }
                }else{
                    // Check if tax exists
                    Tax taxData = conversions.checkTaxExistence(includedTaxes, cols.get(0).getText().strip());
                    if (!taxData.isChecked()) {
                        continue;
                    }
                    tax.setTax(taxData.getTax());
                    tax.setAccount(taxData.getAccount());
                    tax.setCostCenter(costCenter);
                    tax.setTotal(conversions.convertStringToFloat(cols.get(1).getText().strip()));
                    salesTax.add(tax);
                }
            }

            response.setStatus(true);
            response.setMessage("");
            response.setSalesTax(salesTax);
            response.setEntries(new ArrayList<>());

        } catch (Exception e) {
            driver.quit();

            response.setStatus(false);
            response.setMessage(e.getMessage());
            response.setEntries(new ArrayList<>());
        }

        return response;
    }

    private Response getSalesOverGroupGross(String location, String businessDate, String fromDate, String toDate,
                                            CostCenter costCenter,
                                           ArrayList<MajorGroup> majorGroups, WebDriver driver) {
        Response response = new Response();
        ArrayList<Journal> majorGroupsGross = new ArrayList<>();

        driver.get(Constants.OVER_GROUP_GROSS_REPORT_LINK);

        try{
            WebDriverWait wait = new WebDriverWait(driver, 20);
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("loadingFrame")));
        } catch (Exception Ex) {
            System.out.println("There is no loader");
        }

        Response dateResponse = setupEnvironment.selectTimePeriodOHRA(businessDate, fromDate, toDate, location, driver);

        if (!dateResponse.isStatus()){
            response.setStatus(false);
            response.setMessage( dateResponse.getMessage());
            response.setSalesMajorGroupGross(majorGroupsGross);
            return response;
        }

        driver.findElement(By.id("Run Report")).click();

        String overGroupGrossLink = Constants.OHRA_LINK +
                "/finengine/reportRunAction.do?rptroot=15&reportID=SalesMixDailyDetail&method=run";

        try {
            driver.get(overGroupGrossLink);

            List<WebElement> rows = driver.findElements(By.tagName("tr"));

            if (rows.size() <= 11){
                response.setStatus(true);
                response.setMessage("There is no new entries");
                response.setSalesMajorGroupGross(majorGroupsGross);
            }

            if (rows.size() <= 5){
                response.setStatus(true);
                response.setMessage("There is no major groups entries in this location");
                response.setSalesTender(new ArrayList<>());

                return response;
            }

            ArrayList<String> columns = setupEnvironment.getTableColumns(rows, false, 5);

            for (int i = 7; i < rows.size(); i++) {
                WebElement row = rows.get(i);
                List<WebElement> cols = row.findElements(By.tagName("td"));

                if (cols.size() != columns.size()) {
                    continue;
                }

                if (columns.indexOf("group") != -1){
                    WebElement td = cols.get(columns.indexOf("group"));
                    MajorGroup majorGroup = conversions.checkMajorGroupExistence(majorGroups, td.getText().strip().toLowerCase());

                    if (!majorGroup.getChecked()) {
                        continue;
                    }

                    Journal journal = new Journal();
                    float majorGroupGross = conversions.convertStringToFloat(cols.get(columns.indexOf("sales_less_item_disc")).getText().strip());

                    majorGroupsGross = journal.checkExistence(majorGroupsGross, majorGroup
                            , 0, majorGroupGross, 0, 0, costCenter);
                }else{
                    driver.quit();
                    response.setStatus(false);
                    response.setMessage("Failed to get majorGroup gross entries, Please contact support team.");
                    response.setEntries(new ArrayList<>());
                }
            }

            response.setStatus(true);
            response.setMessage("");
            response.setSalesMajorGroupGross(majorGroupsGross);

        } catch (Exception e) {
            driver.quit();

            response.setStatus(false);
            response.setMessage(e.getMessage());
            response.setEntries(new ArrayList<>());
        }

        return response;
    }

    public ArrayList<JournalBatch> saveSalesJournalBatchesData(Response salesResponse, SyncJob syncJob, SyncJobType syncJobType) {
        ArrayList<JournalBatch> addedJournalBatches = new ArrayList<>();

        String businessDate =  syncJobType.getConfiguration().getTimePeriod();
        String fromDate =  syncJobType.getConfiguration().getFromDate();

        String transactionDate = conversions.getTransactionDate(businessDate, fromDate);

        ArrayList<JournalBatch> journalBatches = salesResponse.getJournalBatches();
        for (JournalBatch journalBatch : journalBatches) {
            float totalTender = 0;
            float totalTax = 0;
            float totalMajorGroupNet = 0;

            // Save tenders {Debit}
            ArrayList<Tender> tenders = journalBatch.getSalesTender();
            for (Tender tender : tenders) {
                if (tender.getTotal() == 0)
                    continue;

                HashMap<String, String> tenderData = new HashMap<>();

                tenderData.put("accountingPeriod", transactionDate.substring(2,6));
                tenderData.put("transactionDate", transactionDate);

                tenderData.put("totalDr", String.valueOf(conversions.roundUpFloat(tender.getTotal()) * -1));

                tenderData.put("fromCostCenter", tender.getCostCenter().costCenter);
                tenderData.put("fromAccountCode", tender.getCostCenter().accountCode);

                tenderData.put("toCostCenter", tender.getCostCenter().costCenter);
                tenderData.put("toAccountCode", tender.getCostCenter().accountCode);

                tenderData.put("fromLocation", tender.getCostCenter().accountCode);
                tenderData.put("toLocation", tender.getCostCenter().accountCode);

                tenderData.put("transactionReference", "Tender Reference");

                tenderData.put("expensesAccount", tender.getAccount());

                String description = "Sales F " + tender.getCostCenter().costCenterReference + " " + tender.getTender();
                if (description.length() > 50) {
                    description = description.substring(0, 50);
                }

                tenderData.put("description", description);

                SyncJobData syncJobData = new SyncJobData(tenderData, Constants.RECEIVED, "", new Date(),
                        syncJob.getId());
                syncJobDataRepo.save(syncJobData);
                journalBatch.getSalesTenderData().add(syncJobData);

                float tenderTotal = tender.getTotal();
                totalTender += tenderTotal;
            }

            // Save taxes {Credit}
            ArrayList<Tax> taxes = journalBatch.getSalesTax();
            for (Tax tax : taxes) {
                if (tax.getTotal() == 0)
                    continue;

                HashMap<String, String> taxData = new HashMap<>();

                taxData.put("accountingPeriod", transactionDate.substring(2,6));
                taxData.put("transactionDate", transactionDate);

                taxData.put("totalCr", String.valueOf(conversions.roundUpFloat(tax.getTotal())));

                taxData.put("fromCostCenter", tax.getCostCenter().costCenter);
                taxData.put("fromAccountCode", tax.getCostCenter().accountCode);

                taxData.put("toCostCenter", tax.getCostCenter().costCenter);
                taxData.put("toAccountCode", tax.getCostCenter().accountCode);

                taxData.put("fromLocation", tax.getCostCenter().accountCode);
                taxData.put("toLocation", tax.getCostCenter().accountCode);

                taxData.put("transactionReference", "Taxes Reference");

                // Vat out account
//                String vatOut = syncJobType.getConfiguration().getVatOut();
                taxData.put("inventoryAccount", tax.getAccount());

                String description = "Sales F " + tax.getCostCenter().costCenterReference + " " + tax.getTax();
                if (description.length() > 50) {
                    description = description.substring(0, 50);
                }

                taxData.put("description", description);

                SyncJobData syncJobData = new SyncJobData(taxData, Constants.RECEIVED, "", new Date(),
                        syncJob.getId());
                syncJobDataRepo.save(syncJobData);
                journalBatch.getSalesTaxData().add(syncJobData);

                float taxTotal = tax.getTotal();
                totalTax += taxTotal;
            }

            // Save majorGroup {Credit}
            ArrayList<Journal> majorGroupsGross = journalBatch.getSalesMajorGroupGross();
            for (Journal majorGroupJournal : majorGroupsGross) {
                if (majorGroupJournal.getTotalCost() == 0)
                    continue;

                HashMap<String, String> majorGroupData = new HashMap<>();

                majorGroupData.put("accountingPeriod", transactionDate.substring(2,6));
                majorGroupData.put("transactionDate", transactionDate);

                majorGroupData.put("totalCr", String.valueOf(conversions.roundUpFloat(majorGroupJournal.getTotalCost())));

                majorGroupData.put("fromCostCenter", majorGroupJournal.getCostCenter().costCenter);
                majorGroupData.put("fromAccountCode", majorGroupJournal.getCostCenter().accountCode);

                majorGroupData.put("toCostCenter", majorGroupJournal.getCostCenter().costCenter);
                majorGroupData.put("toAccountCode", majorGroupJournal.getCostCenter().accountCode);

                majorGroupData.put("fromLocation", majorGroupJournal.getCostCenter().accountCode);
                majorGroupData.put("toLocation", majorGroupJournal.getCostCenter().accountCode);

                majorGroupData.put("transactionReference", "MajorGroup Reference");

                // Major Group account
                majorGroupData.put("inventoryAccount", majorGroupJournal.getMajorGroup().getAccount());

                String description = "Sales F " + majorGroupJournal.getCostCenter().costCenterReference + " " + majorGroupJournal.getMajorGroup().getMajorGroup();
                if (description.length() > 50) {
                    description = description.substring(0, 50);
                }

                majorGroupData.put("description", description);

                SyncJobData syncJobData = new SyncJobData(majorGroupData, Constants.RECEIVED, "", new Date(),
                        syncJob.getId());
                syncJobDataRepo.save(syncJobData);
                journalBatch.getSalesMajorGroupGrossData().add(syncJobData);

                float majorGroupGrossTotal = majorGroupJournal.getTotalCost();
                totalMajorGroupNet += majorGroupGrossTotal;
            }

            if ((totalMajorGroupNet + totalTax) != totalTender) {
                HashMap<String, String> differentData = new HashMap<>();

                differentData.put("accountingPeriod", transactionDate.substring(2,6));
                differentData.put("transactionDate", transactionDate);

                // {Debit} - ShortagePOS
                if ((totalMajorGroupNet + totalTax) > totalTender) {
                    String cashShortagePOS = syncJobType.getConfiguration().getCashShortagePOS();
                    differentData.put("totalDr", String.valueOf(conversions.roundUpFloat((totalMajorGroupNet + totalTax) - totalTender)));
                    differentData.put("expensesAccount", cashShortagePOS);
                }
                // {Credit} - SurplusPOS
                else {
                    String cashSurplusPOS = syncJobType.getConfiguration().getCashSurplusPOS();
                    differentData.put("totalCr", String.valueOf(conversions.roundUpFloat(totalTender - (totalMajorGroupNet + totalTax))));
                    differentData.put("inventoryAccount", cashSurplusPOS);
                }

                differentData.put("fromCostCenter", journalBatch.getCostCenter().costCenter);
                differentData.put("fromAccountCode", journalBatch.getCostCenter().accountCode);

                differentData.put("toCostCenter", journalBatch.getCostCenter().costCenter);
                differentData.put("toAccountCode", journalBatch.getCostCenter().accountCode);

                differentData.put("fromLocation", journalBatch.getCostCenter().accountCode);
                differentData.put("toLocation", journalBatch.getCostCenter().accountCode);

                // 30 Char only
                differentData.put("transactionReference", "Different Reference");

                String description = "Sales For " + journalBatch.getCostCenter().costCenterReference + " - different";
                if (description.length() > 50){
                    description = description.substring(0, 50);
                }

                differentData.put("description", description);

                SyncJobData syncJobData = new SyncJobData(differentData, Constants.RECEIVED, "", new Date(),
                        syncJob.getId());
                syncJobDataRepo.save(syncJobData);
                journalBatch.setSalesDifferentData(syncJobData);
            }
            addedJournalBatches.add(journalBatch);

        }
        return addedJournalBatches;
    }

    public void updateJournalBatchStatus(JournalBatch journalBatch, HashMap<String, Object> response){
        SyncJobData salesDifferentData = journalBatch.getSalesDifferentData();
        ArrayList<SyncJobData> salesTaxData = journalBatch.getSalesTaxData();
        ArrayList<SyncJobData> salesTenderData = journalBatch.getSalesTenderData();
        ArrayList<SyncJobData> salesMajorGroupGrossData = journalBatch.getSalesMajorGroupGrossData();

        String reason = "";
        String status = "";

        if ((Boolean) response.get("status")){
            status = Constants.SUCCESS;
            reason = "";
        }
        else {
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

    public HashMap<String, Object> sendJournalBatches(JournalBatch addedJournalBatch, SyncJobType syncJobType,
                                                   Account account, IAuthenticationVoucher voucher){
        HashMap<String, Object> data = new HashMap<>();

        ArrayList<AccountCredential> accountCredentials = account.getAccountCredentials();
        AccountCredential sunCredentials = account.getAccountCredentialByAccount("Sun", accountCredentials);

        String username = sunCredentials.getUsername();
        String sccXMLStringValue = "";

        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();

            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            ///////////////////////////////////////////  Create SSC root element ///////////////////////////////////////
            Element SSCRootElement = doc.createElement("SSC");
            doc.appendChild(SSCRootElement);

            ///////////////////////////////////////////  User //////////////////////////////////////////////////////////
            Element userElement = doc.createElement("User");
            SSCRootElement.appendChild(userElement);

            Element nameElement = doc.createElement("Name");
            nameElement.appendChild(doc.createTextNode(username));
            userElement.appendChild(nameElement);

            ///////////////////////////////////////////  SunSystemsContext /////////////////////////////////////////////
            Element sunSystemContextElement = doc.createElement("SunSystemsContext");
            SSCRootElement.appendChild(sunSystemContextElement);

            Element businessUnitElement = doc.createElement("BusinessUnit");
            businessUnitElement.appendChild(doc.createTextNode(syncJobType.getConfiguration().getBusinessUnit()));
            sunSystemContextElement.appendChild(businessUnitElement);

            ///////////////////////////////////////////  MethodContext /////////////////////////////////////////////////
            Element methodContextElement = doc.createElement("MethodContext");
            SSCRootElement.appendChild(methodContextElement);

            Element LedgerPostingParametersElement = doc.createElement("LedgerPostingParameters");
            methodContextElement.appendChild(LedgerPostingParametersElement);

            Element PostProvisionalElement = doc.createElement("PostProvisional");
            /*
             * Expected values (N/Y)
             * */
            PostProvisionalElement.appendChild(doc.createTextNode("Y"));
            methodContextElement.appendChild(PostProvisionalElement);

            Element DescriptionElement = doc.createElement("Description");
            DescriptionElement.appendChild(doc.createTextNode("Journal batch"));
            LedgerPostingParametersElement.appendChild(DescriptionElement);

            Element journalTypeElement = doc.createElement("JournalType");
            journalTypeElement.appendChild(doc.createTextNode(syncJobType.getConfiguration().getJournalType()));
            LedgerPostingParametersElement.appendChild(journalTypeElement);

            Element postingTypeElement = doc.createElement("PostingType");
            postingTypeElement.appendChild(doc.createTextNode(syncJobType.getConfiguration().getPostingType()));
            LedgerPostingParametersElement.appendChild(postingTypeElement);

            Element SuspenseAccount = doc.createElement("SuspenseAccount");
            SuspenseAccount.appendChild(doc.createTextNode(syncJobType.getConfiguration().getSuspenseAccount()));
            LedgerPostingParametersElement.appendChild(SuspenseAccount);

            Element TransactionAmountAccount = doc.createElement("TransactionAmountAccount");
            TransactionAmountAccount.appendChild(doc.createTextNode(syncJobType.getConfiguration().getSuspenseAccount()));
            LedgerPostingParametersElement.appendChild(TransactionAmountAccount);


            ///////////////////////////////////////////  Payload ///////////////////////////////////////////////////////

            Element payloadElement = doc.createElement("Payload");
            SSCRootElement.appendChild(payloadElement);

            Element ledgerElement = doc.createElement("Ledger");
            payloadElement.appendChild(ledgerElement);

            SyncJobData salesDifferentData;
            ArrayList<SyncJobData> salesTaxData;
            ArrayList<SyncJobData> salesTenderData;
            ArrayList<SyncJobData> salesMajorGroupGrossData;

            salesDifferentData = addedJournalBatch.getSalesDifferentData();
            salesTaxData = addedJournalBatch.getSalesTaxData();
            salesTenderData = addedJournalBatch.getSalesTenderData();
            salesMajorGroupGrossData = addedJournalBatch.getSalesMajorGroupGrossData();

            ///////////////////////////////////////////  line Credit ///////////////////////////////////////////////////
            for (SyncJobData taxData : salesTaxData) {
                if (taxData.getData().containsKey("totalCr")){
                    transferService.createJournalLine(true, doc, ledgerElement, syncJobType, taxData);
                }
            }

            for (SyncJobData salesData : salesMajorGroupGrossData) {
                if (salesData.getData().containsKey("totalCr")){
                    transferService.createJournalLine(true, doc, ledgerElement, syncJobType, salesData);
                }
            }

            ///////////////////////////////////////////  line Debit ////////////////////////////////////////////////////
            for (SyncJobData tenderData : salesTenderData) {
                if (tenderData.getData().containsKey("totalDr")){
                    transferService.createJournalLine(false, doc, ledgerElement, syncJobType, tenderData);
                }
            }

            ///////////////////////////////////////////  line Credit ///////////////////////////////////////////////////
            if (salesDifferentData.getData().containsKey("totalCr")){
                transferService.createJournalLine(true, doc, ledgerElement, syncJobType, salesDifferentData);
            }else{
                ///////////////////////////////////////////  line Debit ////////////////////////////////////////////////////
                transferService.createJournalLine(false, doc, ledgerElement, syncJobType, salesDifferentData);
            }


            ///////////////////////////////////////////  Transform Document to XML String //////////////////////////////
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            sccXMLStringValue = writer.getBuffer().toString();
        } catch (ParserConfigurationException | TransformerException e) {
            e.printStackTrace();
        }

        String result = "";
        try {
            SoapComponent ssc = new SoapComponent(sunCredentials.getHost(), sunCredentials.getPort());
            ssc.authenticate(voucher);
            result = ssc.execute("Journal", "Import", sccXMLStringValue);
        } catch (Exception ex) {
            System.out.print("An error occurred logging in to SunSystems:\r\n");
            ex.printStackTrace();

            data.put("status", Constants.FAILED);
            data.put("message", "An error occurred logging in to Sun System.");
            return data;
        }

        ///////////////////////////////////////////  Convert XML to Object /////////////////////////////////////////////
        JAXBContext jaxbContext;
        try {
            jaxbContext = JAXBContext.newInstance(JournalSSC.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            JournalSSC query = (JournalSSC) jaxbUnmarshaller.unmarshal(new StringReader(result));

            boolean status = query.getPayload().get(0).getLine().getStatus().equals("success");
            ArrayList<Message> messages = query.getPayload().get(0).getLine().getMessages().getMessage();
            String message = "";
            for (Message msg : messages) {
                if (msg.getLevel().equals("error")){
                    message += " * ";
                    message  +=  msg.getUserText();
                }
            }

            data.put("status", status);
            data.put("message", message);
            return data;

        } catch (JAXBException e) {
            e.printStackTrace();
        }
        data.put("status", Constants.FAILED);
        data.put("message", "");
        return data;
    }

    @Deprecated
    public ArrayList<SyncJobData> saveSalesData(Response salesResponse, SyncJob syncJob, SyncJobType syncJobType) {
        float totalTender = 0;
        float totalTax = 0;
        float totalOverGroupNet = 0;

        ArrayList<DifferentCostCenter> differentCostCenters = new ArrayList<>();

        ArrayList<SyncJobData> addedSales = new ArrayList<>();

        String businessDate =  syncJobType.getConfiguration().getTimePeriod();
        String fromDate =  syncJobType.getConfiguration().getFromDate();

        String transactionDate = conversions.getTransactionDate(businessDate, fromDate);

        // Save tenders {Debit}
        ArrayList<Tender> tenders = salesResponse.getSalesTender();
        for (int i = 0; i < tenders.size(); i++) {
            Tender tender = tenders.get(i);

            HashMap<String, String> tenderData = new HashMap<>();

            tenderData.put("accountingPeriod", transactionDate.substring(2,6));
            tenderData.put("transactionDate", transactionDate);

            tenderData.put("totalDr", String.valueOf(conversions.roundUpFloat(tender.getTotal()) * -1));

            tenderData.put("fromCostCenter", tender.getCostCenter().costCenter);
            tenderData.put("fromAccountCode", tender.getCostCenter().accountCode);

            tenderData.put("toCostCenter", tender.getCostCenter().costCenter);
            tenderData.put("toAccountCode", tender.getCostCenter().accountCode);

            tenderData.put("fromLocation", tender.getCostCenter().accountCode);
            tenderData.put("toLocation", tender.getCostCenter().accountCode);

            tenderData.put("transactionReference", "Tender Reference");

            tenderData.put("expensesAccount", tender.getAccount());

            String description = "Sales F " + tender.getCostCenter().costCenterReference + " " + tender.getTender();
            if (description.length() > 50){
                description = description.substring(0, 50);
            }

            tenderData.put("description", description);

            SyncJobData syncJobData = new SyncJobData(tenderData, Constants.RECEIVED, "", new Date(),
                    syncJob.getId());
            syncJobDataRepo.save(syncJobData);
            addedSales.add(syncJobData);

            DifferentCostCenter differentCostCenter = new DifferentCostCenter();
            float tenderTotal = tender.getTotal();
            differentCostCenters = differentCostCenter.checkExistence(differentCostCenters, tender.getCostCenter(),
                    tenderTotal, 0, 0);
        }

        // Save taxes {Credit}
        ArrayList<Tax> taxes = salesResponse.getSalesTax();
        for (int i = 0; i < taxes.size(); i++) {
            Tax tax = taxes.get(i);
            HashMap<String, String> taxData = new HashMap<>();

            taxData.put("accountingPeriod", transactionDate.substring(2,6));
            taxData.put("transactionDate", transactionDate);

            taxData.put("totalCr", String.valueOf(conversions.roundUpFloat(tax.getTotal())));

            taxData.put("fromCostCenter", tax.getCostCenter().costCenter);
            taxData.put("fromAccountCode", tax.getCostCenter().accountCode);

            taxData.put("toCostCenter", tax.getCostCenter().costCenter);
            taxData.put("toAccountCode", tax.getCostCenter().accountCode);

            taxData.put("fromLocation", tax.getCostCenter().accountCode);
            taxData.put("toLocation", tax.getCostCenter().accountCode);

            taxData.put("transactionReference", "Taxes Transaction Reference");

            // Vat out account
            String vatOut = syncJobType.getConfiguration().getVatOut();
            taxData.put("inventoryAccount", vatOut);

            String description = "Sales F " + tax.getCostCenter().costCenterReference + " " + tax.getTax();
            if (description.length() > 50){
                description = description.substring(0, 50);
            }

            taxData.put("description", description);

            SyncJobData syncJobData = new SyncJobData(taxData, Constants.RECEIVED, "", new Date(),
                    syncJob.getId());
            syncJobDataRepo.save(syncJobData);
            addedSales.add(syncJobData);

            DifferentCostCenter differentCostCenter = new DifferentCostCenter();
            float tenderTotal = tax.getTotal();
            differentCostCenters = differentCostCenter.checkExistence(differentCostCenters, tax.getCostCenter(), 0,
                    tenderTotal, 0);
        }

        // Save majorGroup {Credit}
        ArrayList<Journal> majorGroupsGross = salesResponse.getSalesMajorGroupGross();
        for (int i = 0; i < majorGroupsGross.size(); i++) {
            Journal majorGroupJournal = majorGroupsGross.get(i);
            HashMap<String, String> majorGroupData = new HashMap<>();

            majorGroupData.put("accountingPeriod", transactionDate.substring(2,6));
            majorGroupData.put("transactionDate", transactionDate);

            majorGroupData.put("totalCr", String.valueOf(conversions.roundUpFloat(majorGroupJournal.getTotalCost())));

            majorGroupData.put("fromCostCenter", majorGroupJournal.getCostCenter().costCenter);
            majorGroupData.put("fromAccountCode", majorGroupJournal.getCostCenter().accountCode);

            majorGroupData.put("toCostCenter", majorGroupJournal.getCostCenter().costCenter);
            majorGroupData.put("toAccountCode", majorGroupJournal.getCostCenter().accountCode);

            majorGroupData.put("fromLocation", majorGroupJournal.getCostCenter().accountCode);
            majorGroupData.put("toLocation", majorGroupJournal.getCostCenter().accountCode);

            majorGroupData.put("transactionReference", "MG Transaction Reference");

            // Major Group account
            majorGroupData.put("inventoryAccount", majorGroupJournal.getMajorGroup().getAccount());

            String description = "Sales F " + majorGroupJournal.getCostCenter().costCenterReference + " " + majorGroupJournal.getMajorGroup().getMajorGroup();
            if (description.length() > 50){
                description = description.substring(0, 50);
            }

            majorGroupData.put("description", description);

            SyncJobData syncJobData = new SyncJobData(majorGroupData, Constants.RECEIVED, "", new Date(),
                    syncJob.getId());
            syncJobDataRepo.save(syncJobData);
            addedSales.add(syncJobData);

            DifferentCostCenter differentCostCenter = new DifferentCostCenter();
            float majorGroupGrossTotal = majorGroupJournal.getTotalCost();
            differentCostCenters = differentCostCenter.checkExistence(differentCostCenters, majorGroupJournal.getCostCenter(),
                    0, 0, majorGroupGrossTotal);
        }

        // Check if there is different {According to different result} per costCenter
        for (int i = 0; i < differentCostCenters.size(); i++) {
            DifferentCostCenter differentCostCenter = differentCostCenters.get(i);

            totalTender = differentCostCenter.getTotalTender();
            totalTax = differentCostCenter.getTotalTax();
            totalOverGroupNet = differentCostCenter.getTotalOverGroupNet();

            if ((totalOverGroupNet + totalTax) != totalTender) {
                HashMap<String, String> differentData = new HashMap<>();

                differentData.put("accountingPeriod", transactionDate.substring(2,6));
                differentData.put("transactionDate", transactionDate);

                // {Debit} - ShortagePOS
                if ((totalOverGroupNet + totalTax) > totalTender) {
                    String cashShortagePOS = syncJobType.getConfiguration().getCashShortagePOS();
                    differentData.put("totalDr", String.valueOf(conversions.roundUpFloat((totalOverGroupNet + totalTax) - totalTender)));
                    differentData.put("expensesAccount", cashShortagePOS);
                }
                // {Credit} - SurplusPOS
                else {
                    String cashSurplusPOS = syncJobType.getConfiguration().getCashSurplusPOS();
                    differentData.put("totalCr", String.valueOf(conversions.roundUpFloat(totalTender - (totalOverGroupNet + totalTax))));
                    differentData.put("inventoryAccount", cashSurplusPOS);
                }

                differentData.put("fromCostCenter", differentCostCenter.getCostCenter().costCenter);
                differentData.put("fromAccountCode", differentCostCenter.getCostCenter().accountCode);

                differentData.put("toCostCenter", differentCostCenter.getCostCenter().costCenter);
                differentData.put("toAccountCode", differentCostCenter.getCostCenter().accountCode);

                differentData.put("fromLocation", differentCostCenter.getCostCenter().accountCode);
                differentData.put("toLocation", differentCostCenter.getCostCenter().accountCode);

                differentData.put("transactionReference", "Taxes Transaction Reference");

                String description = "Sales For " + differentCostCenter.getCostCenter().costCenterReference + " - different";
                if (description.length() > 50){
                    description = description.substring(0, 50);
                }

                differentData.put("description", description);

                SyncJobData syncJobData = new SyncJobData(differentData, Constants.RECEIVED, "", new Date(),
                        syncJob.getId());
                syncJobDataRepo.save(syncJobData);
                addedSales.add(syncJobData);
            }
        }
        return addedSales;
    }


}
