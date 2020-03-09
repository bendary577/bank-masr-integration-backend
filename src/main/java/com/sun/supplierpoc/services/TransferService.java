package com.sun.supplierpoc.services;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.controllers.InvoiceController;
import com.sun.supplierpoc.models.*;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


@Service
public class TransferService {

    @Autowired
    SyncJobDataRepo syncJobDataRepo;
    @Autowired
    InvoiceController invoiceController;

    private Conversions conversions = new Conversions();
    private SetupEnvironment setupEnvironment = new SetupEnvironment();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public HashMap<String, Object> getTransferData(SyncJobType syncJobType, SyncJobType syncJobTypeApprovedInvoice,
                                                   Account account) {
        HashMap<String, Object> data = new HashMap<>();

        WebDriver driver = setupEnvironment.setupSeleniumEnv(false);
        ArrayList<HashMap<String, Object>> transfers = new ArrayList<>();

        ArrayList<CostCenter> costCenters =  syncJobTypeApprovedInvoice.getConfiguration().getCostCenters();;
        ArrayList<Item> items =  syncJobType.getConfiguration().getItems();

        ArrayList<HashMap<String, Object>> journalEntries = new ArrayList<>();

        try {
            String url = "https://mte03-ohim-prod.hospitality.oracleindustry.com/Webclient/FormLogin.aspx";

            if (!setupEnvironment.loginOHIM(driver, url, account)) {
                driver.quit();

                data.put("status", Constants.FAILED);
                data.put("message", "Invalid username and password.");
                data.put("transfers", transfers);
                return data;
            }

            String bookedTransfersUrl = "https://mte03-ohim-prod.hospitality.oracleindustry.com/Webclient/Store/TransferNew/TrStatus.aspx?Type=Booked";
            driver.get(bookedTransfersUrl);

            Select select = new Select(driver.findElement(By.id("_ctl5")));
            select.selectByVisibleText("Last Month");

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
                    transfer.put("from_cost_center", oldCostCenterData);


                    td = cols.get(columns.indexOf("to_cost_center"));
                    oldCostCenterData = conversions.checkCostCenterExistence(costCenters, td.getText().strip(), false);

                    if (! oldCostCenterData.checked) {
                        continue;
                    }
                    transfer.put("to_cost_center", oldCostCenterData);

                    td = cols.get(columns.indexOf("document"));
                    transfer.put(columns.get(columns.indexOf("document")), td.getText());
                    String detailsLink = td.findElement(By.tagName("a")).getAttribute("href");
                    transfer.put("details_url", detailsLink);

                    for (int j = columns.indexOf("delivery_date"); j < cols.size(); j++) {
                        transfer.put(columns.get(j), cols.get(j).getText());
                    }
                    transfers.add(transfer);
                }

                // check if there is other pages
                if (driver.findElements(By.linkText("Next")).size() == 0){
                    break;
                }
                else {
                    checkPagination(driver);
                    table = driver.findElement(By.id("dg_main"));
                    rows = table.findElements(By.tagName("tr"));
                }
            }
            for (HashMap<String, Object> transfer: transfers) {
                getBookedTransferDetails(items, transfer, driver, journalEntries);
            }

            driver.quit();

            data.put("status", Constants.SUCCESS);
            data.put("message", "");
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

    public static void checkPagination(WebDriver driver) {
        String first_element_text = driver.findElement(By.id("dg_rc_0_1")).getText();
        driver.findElement(By.linkText("Next")).click();
        String element_txt = "";

        WebDriverWait wait = new WebDriverWait(driver, 20);
        WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("dg_rc_0_1")));
        try {
            element_txt = element.getText();
        } catch (Exception e) {
            element_txt = "";
        }

        while (element_txt.equals(first_element_text)){
            wait = new WebDriverWait(driver, 20);
            element = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("dg_rc_0_1")));
            try {
                element_txt = element.getText();
            } catch (Exception e) {
                element_txt = "";
            }
        }
    }

    private void getBookedTransferDetails(
            ArrayList<Item> items, HashMap<String, Object> transfer, WebDriver driver,
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

                // check if this Item belong to selected items
                WebElement td = cols.get(columns.indexOf("Item"));

                Item oldItemData = conversions.checkItemExistence(items, td.getText().strip());

                if (!oldItemData.isChecked()) {
                    continue;
                }

                String overGroup = oldItemData.getOverGroup();

                transferDetails.put("Item", td.getText());

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

    public HashMap<String, Object> sendTransferData(SyncJobData addedJournalEntry, SyncJobType syncJobType,  SyncJobType syncJobTypeJournal) throws SoapFaultException, ComponentException {
        HashMap<String, Object> data = new HashMap<>();

        boolean useEncryption = false;

        String username = "ACt";
        String password = "P@ssw0rd";
        IAuthenticationVoucher voucher;
        String sccXMLStringValue = "";

        try {
            SecurityProvider securityProvider = new SecurityProvider(Constants.HOST, useEncryption);
            voucher = securityProvider.Authenticate(username, password);
        } catch (ComponentException | SoapFaultException e) {
            e.printStackTrace();

            data.put("status", Constants.FAILED);
            data.put("message", "Failed to connect to sun system.");
            return data;
        }

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

            Element DescriptionElement = doc.createElement("Description");
            DescriptionElement.appendChild(doc.createTextNode(addedJournalEntry.getData().get("description")));
            LedgerPostingParametersElement.appendChild(DescriptionElement);

            Element postingTypeElement = doc.createElement("PostingType");
            postingTypeElement.appendChild(doc.createTextNode(syncJobType.getConfiguration().getPostingType()));
            LedgerPostingParametersElement.appendChild(postingTypeElement);

            ///////////////////////////////////////////  Payload ///////////////////////////////////////////////////////

            Element payloadElement = doc.createElement("Payload");
            SSCRootElement.appendChild(payloadElement);

            Element ledgerElement = doc.createElement("Ledger");
            payloadElement.appendChild(ledgerElement);

            ///////////////////////////////////////////  line Credit ///////////////////////////////////////////////////
            createJournalLine(true, doc, ledgerElement, syncJobType, syncJobTypeJournal, addedJournalEntry);

            ///////////////////////////////////////////  line Debit ////////////////////////////////////////////////////
            createJournalLine(false, doc, ledgerElement, syncJobType, syncJobTypeJournal, addedJournalEntry);

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

            SoapComponent ssc = new SoapComponent(Constants.HOST, Constants.PORT);
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

            System.out.println(query.getPayload());

            boolean status = query.getPayload().get(0).getLine().getStatus().equals("success");
            ArrayList<Message> messages = query.getPayload().get(0).getLine().getMessages().getMessage();
            String message = "";
            for (Message msg : messages) {
                if (msg.getLevel().equals("error")){
                    message += " - ";
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

    private void createJournalLine(boolean creditDebitFlag, Document doc, Element ledgerElement, SyncJobType syncJobType,
                                   SyncJobType syncJobTypeJournal, SyncJobData addedJournalEntry) {
        ArrayList<OverGroup> overGroups = syncJobTypeJournal.getConfiguration().getOverGroups();
        ArrayList<Analysis> analysis = syncJobType.getConfiguration().getAnalysis();
        OverGroup oldOverGroupData = conversions.checkOverGroupExistence(overGroups, addedJournalEntry.getData().get("overGroup"));


        Element lineElement = doc.createElement("Line");
        ledgerElement.appendChild(lineElement);

        Element DescriptionElement = doc.createElement("Description");
        DescriptionElement.appendChild(doc.createTextNode(addedJournalEntry.getData().get("description")));
        lineElement.appendChild(DescriptionElement);

        Element accountCodeElement = doc.createElement("AccountCode");
        if (creditDebitFlag) // Credit
            accountCodeElement.appendChild(doc.createTextNode(oldOverGroupData.getInventoryAccount()));
        else // Debit
            accountCodeElement.appendChild(doc.createTextNode(oldOverGroupData.getExpensesAccount()));
        lineElement.appendChild(accountCodeElement);

        Element base2ReportingAmountElement = doc.createElement("Base2ReportingAmount");
        if (creditDebitFlag)
            base2ReportingAmountElement.appendChild(doc.createTextNode(String.valueOf(addedJournalEntry.getData().get("total"))));
        else
            base2ReportingAmountElement.appendChild(doc.createTextNode("-" + String.valueOf(addedJournalEntry.getData().get("total"))));
        lineElement.appendChild(base2ReportingAmountElement);
        lineElement.appendChild(base2ReportingAmountElement);

        Element baseAmountElement = doc.createElement("BaseAmount");
        if (creditDebitFlag)
            baseAmountElement.appendChild(doc.createTextNode(String.valueOf(addedJournalEntry.getData().get("total"))));
        else
            baseAmountElement.appendChild(doc.createTextNode("-" + String.valueOf(addedJournalEntry.getData().get("total"))));
        lineElement.appendChild(baseAmountElement);

        Element currencyCodeElement = doc.createElement("CurrencyCode");
        currencyCodeElement.appendChild(doc.createTextNode(syncJobType.getConfiguration().getCurrencyCode()));
        lineElement.appendChild(currencyCodeElement);

        Element debitCreditElement = doc.createElement("DebitCredit");
        if (creditDebitFlag)
            debitCreditElement.appendChild(doc.createTextNode("C"));
        else
            debitCreditElement.appendChild(doc.createTextNode("D"));
        lineElement.appendChild(debitCreditElement);

        Element journalLineNumberElement = doc.createElement("JournalLineNumber");
        if (creditDebitFlag)
            journalLineNumberElement.appendChild(doc.createTextNode("1"));
        else
            journalLineNumberElement.appendChild(doc.createTextNode("2"));
        lineElement.appendChild(journalLineNumberElement);

        Element journalSourceElement = doc.createElement("JournalSource");
        journalSourceElement.appendChild(doc.createTextNode(syncJobType.getConfiguration().getJournalSource()));
        lineElement.appendChild(journalSourceElement);

        Element journalTypeElement = doc.createElement("JournalType");
        journalTypeElement.appendChild(doc.createTextNode(syncJobType.getConfiguration().getJournalType()));
        lineElement.appendChild(journalTypeElement);

        Element transactionAmountElement = doc.createElement("TransactionAmount");
        if (creditDebitFlag)
            transactionAmountElement.appendChild(doc.createTextNode(String.valueOf(addedJournalEntry.getData().get("total"))));
        else
            transactionAmountElement.appendChild(doc.createTextNode("-" + String.valueOf(addedJournalEntry.getData().get("total"))));
        lineElement.appendChild(transactionAmountElement);

        Element transactionReferenceElement = doc.createElement("TransactionReference");
        transactionReferenceElement.appendChild(doc.createTextNode(addedJournalEntry.getData().get("transactionReference")));
        lineElement.appendChild(transactionReferenceElement);

        Element accountsElement = doc.createElement("Accounts");
        lineElement.appendChild(accountsElement);

        lineElement.appendChild(accountCodeElement);

        // T3
        Element analysisCode2ElementT3 = doc.createElement("AnalysisCode3");
        analysisCode2ElementT3.appendChild(doc.createTextNode("13"));
        lineElement.appendChild(analysisCode2ElementT3);

        Element enterAnalysis1ElementT3 = doc.createElement("EnterAnalysis3");
        enterAnalysis1ElementT3.appendChild(doc.createTextNode("1"));
        accountsElement.appendChild(enterAnalysis1ElementT3);

        Element analysis2ElementT3 = doc.createElement("Analysis3");
        accountsElement.appendChild(analysis2ElementT3);

        if (creditDebitFlag){
            Element vAcntCatAnalysis_AnlCodeElement = doc.createElement("VAcntCatAnalysis_AnlCode");
            vAcntCatAnalysis_AnlCodeElement.appendChild(doc.createTextNode(addedJournalEntry.getData().get("from_account_code")));
            analysis2ElementT3.appendChild(vAcntCatAnalysis_AnlCodeElement);
        }
        else{
            Element vAcntCatAnalysis_AnlCodeElement = doc.createElement("VAcntCatAnalysis_AnlCode");
            vAcntCatAnalysis_AnlCodeElement.appendChild(doc.createTextNode(addedJournalEntry.getData().get("to_account_code")));
            analysis2ElementT3.appendChild(vAcntCatAnalysis_AnlCodeElement);
        }

        for (Analysis analysisObject: analysis) {
            if (analysisObject.getChecked()){

                Element analysisCode2Element = doc.createElement("AnalysisCode"+ analysisObject.getNumber());
                analysisCode2Element.appendChild(doc.createTextNode("1"+ analysisObject.getNumber()));
                lineElement.appendChild(analysisCode2Element);

                Element enterAnalysis1Element = doc.createElement("EnterAnalysis" + analysisObject.getNumber());
                enterAnalysis1Element.appendChild(doc.createTextNode("1"));
                accountsElement.appendChild(enterAnalysis1Element);

                Element analysis2Element = doc.createElement("Analysis" + analysisObject.getNumber());
                accountsElement.appendChild(analysis2Element);

//                Element vAcntCatAnalysis_AcntCodeElement = doc.createElement("VAcntCatAnalysis_AcntCode");
//                if (creditDebitFlag)
//                    vAcntCatAnalysis_AcntCodeElement.appendChild(doc.createTextNode(addedJournalEntry.getData().get("from_account_code")));
//                else
//                    vAcntCatAnalysis_AcntCodeElement.appendChild(doc.createTextNode(addedJournalEntry.getData().get("to_account_code")));
//                analysis2Element.appendChild(vAcntCatAnalysis_AcntCodeElement);

                Element vAcntCatAnalysis_AnlCodeElement = doc.createElement("VAcntCatAnalysis_AnlCode");
                vAcntCatAnalysis_AnlCodeElement.appendChild(doc.createTextNode((String) analysisObject.getCodeElement()));
                analysis2Element.appendChild(vAcntCatAnalysis_AnlCodeElement);

            }
            else{
                Element enterAnalysis1Element = doc.createElement("EnterAnalysis" + analysisObject.getNumber());
                enterAnalysis1Element.appendChild(doc.createTextNode("3"));
                accountsElement.appendChild(enterAnalysis1Element);
            }
        }
    }

}
