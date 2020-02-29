package com.sun.supplierpoc.services;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.controllers.InvoiceController;
import com.sun.supplierpoc.models.Journal;
import com.sun.supplierpoc.models.SyncJob;
import com.sun.supplierpoc.models.SyncJobData;
import com.sun.supplierpoc.models.SyncJobType;
import com.sun.supplierpoc.repositories.SyncJobDataRepo;
import com.sun.supplierpoc.seleniumMethods.SetupEnvironment;
import com.sun.supplierpoc.soapModels.JournalSSC;
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
    static int PORT = 8080;
    static String HOST = "192.168.1.21";

    @Autowired
    SyncJobDataRepo syncJobDataRepo;
    @Autowired
    InvoiceController invoiceController;

    private Conversions conversions = new Conversions();
    private SetupEnvironment setupEnvironment = new SetupEnvironment();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public HashMap<String, Object> getTransferData(SyncJobType syncJobType) {
        HashMap<String, Object> data = new HashMap<>();

        WebDriver driver = setupEnvironment.setupSeleniumEnv(false);
        ArrayList<HashMap<String, Object>> transfers = new ArrayList<>();

        ArrayList<HashMap<String, String>> costCenters = (ArrayList<HashMap<String, String>>) syncJobType.getConfiguration().get("costCenters");
        ArrayList<HashMap<String, String>> items = (ArrayList<HashMap<String, String>>) syncJobType.getConfiguration().get("items");

        ArrayList<HashMap<String, Object>> journalEntries = new ArrayList<>();

        try {
            String url = "https://mte03-ohim-prod.hospitality.oracleindustry.com/Webclient/FormLogin.aspx";

            if (!setupEnvironment.loginOHIM(driver, url)) {
                driver.quit();

                data.put("status", Constants.FAILED);
                data.put("message", "Invalid username and password.");
                data.put("transfers", transfers);
                return data;
            }

            String bookedTransfersUrl = "https://mte03-ohim-prod.hospitality.oracleindustry.com/Webclient/Store/TransferNew/TrStatus.aspx?Type=Booked";
            driver.get(bookedTransfersUrl);

            Select select = new Select(driver.findElement(By.id("_ctl5")));
            select.selectByVisibleText("All");

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
                    HashMap<String, Object> oldCostCenterData = invoiceController.checkCostCenterExistence(costCenters, td.getText().strip(), false);

                    if (!(boolean) oldCostCenterData.get("status")) {
                        continue;
                    }
                    transfer.put("cost_center", oldCostCenterData.get("costCenter"));


                    td = cols.get(columns.indexOf("document"));
                    transfer.put(columns.get(0), td.getText());
                    String detailsLink = td.findElement(By.tagName("a")).getAttribute("href");
                    transfer.put("details_url", detailsLink);

                    for (int j = 1; j < cols.size(); j++) {
                        transfer.put(columns.get(j), cols.get(j).getText());
                    }
                    transfers.add(transfer);
                }

                // check if there is other pages
                if (driver.findElements(By.linkText("Next")).size() == 0){
                    break;
                }
                else {
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

                journalEntry.put("total", journal.getTotalTransfer());
                journalEntry.put("from", transfer.get("from_cost_center"));
                journalEntry.put("to", transfer.get("to_cost_center"));
                journalEntry.put("description", "Transfer From " + transfer.get("from_cost_center") + " to "+
                        transfer.get("to_cost_center") + " - " + journal.getOverGroup());

                journalEntries.add(journalEntry);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public ArrayList<SyncJobData> saveTransferSunData(ArrayList<HashMap<String, String>> transfers, SyncJob syncJob) {
        ArrayList<SyncJobData> addedTransfers = new ArrayList<>();

        for (HashMap<String, String> transfer : transfers) {
            HashMap<String, String> data = new HashMap<>();

            data.put("total", transfer.get("total") == null? "0":transfer.get("total"));
            data.put("from_cost_center",transfer.get("from") == null? "":transfer.get("from"));
            data.put("to_cost_center", transfer.get("to") == null? "":transfer.get("to"));
            data.put("description", transfer.get("description") == null? "":transfer.get("description"));

            SyncJobData syncJobData = new SyncJobData(data, Constants.RECEIVED, "", new Date(),
                    syncJob.getId());
            syncJobDataRepo.save(syncJobData);

            addedTransfers.add(syncJobData);
        }
        return addedTransfers;

    }

    public Boolean sendTransferData(SyncJobData addedJournalEntry, SyncJobType syncJobType) throws SoapFaultException, ComponentException {
        boolean useEncryption = false;

        String username = "ACt";
        String password = "P@ssw0rd";

        SecurityProvider securityProvider = new SecurityProvider(HOST, useEncryption);
        IAuthenticationVoucher voucher = securityProvider.Authenticate(username, password);

        String sccXMLStringValue = "";
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        try {
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
            businessUnitElement.appendChild(doc.createTextNode((String) syncJobType.getConfiguration().get("businessUnit")));
            sunSystemContextElement.appendChild(businessUnitElement);

            ///////////////////////////////////////////  MethodContext /////////////////////////////////////////////////
            Element methodContextElement = doc.createElement("MethodContext");
            SSCRootElement.appendChild(methodContextElement);

            Element LedgerPostingParametersElement = doc.createElement("LedgerPostingParameters");
            methodContextElement.appendChild(LedgerPostingParametersElement);

            Element DescriptionElement = doc.createElement("Description");
            DescriptionElement.appendChild(doc.createTextNode("Test import journal"));
            LedgerPostingParametersElement.appendChild(DescriptionElement);

            Element postingTypeElement = doc.createElement("PostingType");
            postingTypeElement.appendChild(doc.createTextNode((String) syncJobType.getConfiguration().get("postingType")));
            LedgerPostingParametersElement.appendChild(postingTypeElement);

            ///////////////////////////////////////////  Payload ///////////////////////////////////////////////////////

            Element payloadElement = doc.createElement("Payload");
            SSCRootElement.appendChild(payloadElement);

            Element ledgerElement = doc.createElement("Ledger");
            payloadElement.appendChild(ledgerElement);

            ///////////////////////////////////////////  line Credit ///////////////////////////////////////////////////
            createJournalLine(true, doc, ledgerElement, syncJobType, addedJournalEntry);

            ///////////////////////////////////////////  line Debit ////////////////////////////////////////////////////
            createJournalLine(false, doc, ledgerElement, syncJobType, addedJournalEntry);

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

            SoapComponent ssc = new SoapComponent(HOST, PORT);
            ssc.authenticate(voucher);
            result = ssc.execute("Journal", "Import", sccXMLStringValue);
        } catch (Exception ex) {
            System.out.print("An error occurred logging in to SunSystems:\r\n");
            ex.printStackTrace();

            return false;
        }

        ///////////////////////////////////////////  Convert XML to Object /////////////////////////////////////////////
        JAXBContext jaxbContext;
        try {
            jaxbContext = JAXBContext.newInstance(JournalSSC.class);

            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

            JournalSSC query = (JournalSSC) jaxbUnmarshaller.unmarshal(new StringReader(result));

            System.out.println(query.getPayload());

            return query.getPayload().get(0).getLine().getStatus().equals("success");
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void createJournalLine(boolean creditDebitFlag, Document doc, Element ledgerElement, SyncJobType syncJobType,
                                   SyncJobData addedJournalEntry) {

        ArrayList<HashMap<String, Object>> analysis = (ArrayList<HashMap<String, Object>>) syncJobType.getConfiguration().get("analysis");

        Element lineElement = doc.createElement("Line");
        ledgerElement.appendChild(lineElement);

        Element accountCodeElement = doc.createElement("AccountCode");
        accountCodeElement.appendChild(doc.createTextNode((String) syncJobType.getConfiguration().get("accountCode")));
        lineElement.appendChild(accountCodeElement);

        Element base2ReportingAmountElement = doc.createElement("Base2ReportingAmount");
        if (creditDebitFlag)
            base2ReportingAmountElement.appendChild(doc.createTextNode(addedJournalEntry.getData().get("total")));
        else
            base2ReportingAmountElement.appendChild(doc.createTextNode("-" + addedJournalEntry.getData().get("total")));
        lineElement.appendChild(base2ReportingAmountElement);

        Element baseAmountElement = doc.createElement("BaseAmount");
        if (creditDebitFlag)
            baseAmountElement.appendChild(doc.createTextNode(addedJournalEntry.getData().get("total")));
        else
            baseAmountElement.appendChild(doc.createTextNode("-" + addedJournalEntry.getData().get("total")));
        lineElement.appendChild(baseAmountElement);

        Element currencyCodeElement = doc.createElement("CurrencyCode");
        currencyCodeElement.appendChild(doc.createTextNode((String) syncJobType.getConfiguration().get("currencyCode")));
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
        journalSourceElement.appendChild(doc.createTextNode((String) syncJobType.getConfiguration().get("journalSource")));
        lineElement.appendChild(journalSourceElement);

        Element journalTypeElement = doc.createElement("JournalType");
        journalTypeElement.appendChild(doc.createTextNode((String) syncJobType.getConfiguration().get("journalType")));
        lineElement.appendChild(journalTypeElement);

        Element transactionAmountElement = doc.createElement("TransactionAmount");
        if (creditDebitFlag)
            transactionAmountElement.appendChild(doc.createTextNode(addedJournalEntry.getData().get("total")));
        else
            transactionAmountElement.appendChild(doc.createTextNode("-" + addedJournalEntry.getData().get("total")));
        lineElement.appendChild(transactionAmountElement);

        Element transactionReferenceElement = doc.createElement("TransactionReference");
        transactionReferenceElement.appendChild(doc.createTextNode((String) syncJobType.getConfiguration().get("transactionReference")));
        lineElement.appendChild(transactionReferenceElement);

        Element accountsElement = doc.createElement("Accounts");
        lineElement.appendChild(accountsElement);

        lineElement.appendChild(accountCodeElement);

        for (HashMap<String, Object> analysisObject: analysis) {
            if ((Boolean) analysisObject.get("checked")){

                Element analysisCode2Element = doc.createElement("AnalysisCode"+ analysisObject.get("number"));
                analysisCode2Element.appendChild(doc.createTextNode("1"+ analysisObject.get("number")));
                lineElement.appendChild(analysisCode2Element);

                Element enterAnalysis1Element = doc.createElement("EnterAnalysis" + analysisObject.get("number"));
                enterAnalysis1Element.appendChild(doc.createTextNode("1"));
                accountsElement.appendChild(enterAnalysis1Element);

                Element analysis2Element = doc.createElement("Analysis" + analysisObject.get("number"));
                accountsElement.appendChild(analysis2Element);

                Element vAcntCatAnalysis_AcntCodeElement = doc.createElement("VAcntCatAnalysis_AcntCode");
                vAcntCatAnalysis_AcntCodeElement.appendChild(doc.createTextNode((String) syncJobType.getConfiguration().get("accountCode")));
                analysis2Element.appendChild(vAcntCatAnalysis_AcntCodeElement);

                Element vAcntCatAnalysis_AnlCodeElement = doc.createElement("VAcntCatAnalysis_AnlCode");
                vAcntCatAnalysis_AnlCodeElement.appendChild(doc.createTextNode((String) analysisObject.get("codeElement")));
                analysis2Element.appendChild(vAcntCatAnalysis_AnlCodeElement);

            }
            else{
                Element enterAnalysis1Element = doc.createElement("EnterAnalysis" + analysisObject.get("number"));
                enterAnalysis1Element.appendChild(doc.createTextNode("3"));
                accountsElement.appendChild(enterAnalysis1Element);
            }
        }
    }

}
