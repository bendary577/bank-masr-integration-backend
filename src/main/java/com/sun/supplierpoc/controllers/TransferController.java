package com.sun.supplierpoc.controllers;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.models.SyncJob;
import com.sun.supplierpoc.models.SyncJobData;
import com.sun.supplierpoc.models.SyncJobType;
import com.sun.supplierpoc.repositories.SyncJobDataRepo;
import com.sun.supplierpoc.repositories.SyncJobRepo;
import com.sun.supplierpoc.repositories.SyncJobTypeRepo;
import com.sun.supplierpoc.seleniumMethods.SetupEnvironment;
import com.sun.supplierpoc.soapModels.JournalSSC;
import com.sun.supplierpoc.soapModels.PurchaseInvoiceSSC;
import com.systemsunion.security.IAuthenticationVoucher;
import com.systemsunion.ssc.client.ComponentException;
import com.systemsunion.ssc.client.SecurityProvider;
import com.systemsunion.ssc.client.SoapComponent;
import com.systemsunion.ssc.client.SoapFaultException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.io.StringWriter;




@RestController

public class TransferController {
    static int PORT = 8080;
    static String HOST= "192.168.1.21";

    @Autowired
    private SyncJobRepo syncJobRepo;
    @Autowired
    private SyncJobTypeRepo syncJobTypeRepo;
    @Autowired
    private SyncJobDataRepo syncJobDataRepo;

    public Conversions conversions = new Conversions();
    public Constants constant = new Constants();
    public SetupEnvironment setupEnvironment = new SetupEnvironment();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @RequestMapping("/getBookedTransfer")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ArrayList<SyncJobData> getBookedTransfer() {
        SyncJobType syncJobType = syncJobTypeRepo.findByNameAndAccountId("Booked Transfers", "1");

        SyncJob syncJob = new SyncJob(constant.RUNNING, "", new Date(), null, "1", "1",
                syncJobType.getId());

        syncJobRepo.save(syncJob);

        ArrayList<HashMap<String, Object>> invoices = getTransferData(syncJobType);
        ArrayList<SyncJobData> addedTransfers = saveTransferData(invoices, syncJob);
        if (addedTransfers.size() != 0){
            try {

                sendTransferData(addedTransfers);
            } catch (SoapFaultException e) {
                e.printStackTrace();
            } catch (ComponentException e) {
                e.printStackTrace();
            }
        }

        syncJob.setStatus(constant.SUCCESS);
        syncJob.setEndDate(new Date());
        syncJobRepo.save(syncJob);

        return addedTransfers;
    }

    public ArrayList<HashMap<String, Object>> getTransferData(SyncJobType syncJobType){

        WebDriver driver = setupEnvironment.setupSeleniumEnv();
        ArrayList<HashMap<String, Object>> transfers = new ArrayList<>();

        HashMap<String, Object> costCenters = (HashMap)((HashMap) syncJobType.getConfiguration()).get("costCenters");

        try {
            String url = "https://mte03-ohim-prod.hospitality.oracleindustry.com/Webclient/FormLogin.aspx";
            driver.get(url);

            driver.findElement(By.id("igtxtdfUsername")).sendKeys("Amr");
            driver.findElement(By.id("igtxtdfPassword")).sendKeys("Mic@8000");
            driver.findElement(By.id("igtxtdfCompany")).sendKeys("act");

            String previous_url = driver.getCurrentUrl();
            driver.findElement(By.name("Login")).click();

            if (driver.getCurrentUrl().equals(previous_url)){
                String message = "Invalid username and password.";
                return transfers;
            }

            String bookedTransfersUrl = "https://mte03-ohim-prod.hospitality.oracleindustry.com/Webclient/Store/TransferNew/TrStatus.aspx?Type=Booked";
            driver.get(bookedTransfersUrl);

            Select select = new Select(driver.findElement(By.id("_ctl5")));
            select.selectByVisibleText("All");

            driver.findElement(By.name("filterPanel_btnRefresh")).click();


            List<WebElement> rows = driver.findElements(By.tagName("tr"));

            ArrayList<String> columns = new ArrayList<>();
            WebElement row = rows.get(40);
            List<WebElement> cols = row.findElements(By.tagName("th"));

            for (int j = 1; j < cols.size(); j++) {
                columns.add(conversions.transformColName(cols.get(j).getText()));
            }

            for (int i = 41; i < rows.size(); i++) {
                HashMap<String, Object> transfer = new HashMap<>();

                row = rows.get(i);
                cols = row.findElements(By.tagName("td"));
                if (cols.size() < 10){
                    continue;
                }

                WebElement td = cols.get(2);
                if (!costCenters.containsKey(td.getText())){
                    continue;
                }

                for (int j = 1; j < cols.size(); j++) {
                    transfer.put(columns.get(j - 1), cols.get(j).getText());
                }
                transfers.add(transfer);
            }

            driver.quit();
            return transfers;
        } catch (Exception e) {
            e.printStackTrace();
            driver.quit();
            return transfers;
        }

    }

    public ArrayList<SyncJobData> saveTransferData(ArrayList<HashMap<String, Object>> invoices, SyncJob syncJob){
        ArrayList<SyncJobData> addedInvoices = new ArrayList<>();

        for (int i = 0; i < invoices.size(); i++) {
            HashMap<String, Object> invoice = invoices.get(i);

            if (invoice.get("invoice_no.").toString().substring(0, 3).equals("RTV")){
                continue;
            }
            HashMap<String, Object> data = new HashMap<>();

            data.put("invoiceNo", invoice.get("invoice_no."));


            SyncJobData syncJobData = new SyncJobData(data, constant.RECEIVED, "", new Date(),
                    syncJob.getId());
            syncJobDataRepo.save(syncJobData);

            addedInvoices.add(syncJobData);
        }
        return addedInvoices;

    }

    public Boolean sendTransferData(ArrayList<SyncJobData> addedInvoices) throws SoapFaultException, ComponentException {
        boolean useEncryption = false;

        String username = "ACt";
        String password = "P@ssw0rd";

        SecurityProvider securityProvider = new SecurityProvider(HOST, useEncryption);
        IAuthenticationVoucher voucher = securityProvider.Authenticate(username, password);

        String sccXMLStringValue = null;
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            // Create SSC root element
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
            businessUnitElement.appendChild(doc.createTextNode("PK1"));
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
            postingTypeElement.appendChild(doc.createTextNode("1"));
            LedgerPostingParametersElement.appendChild(postingTypeElement);

            ///////////////////////////////////////////  Payload ///////////////////////////////////////////////////////

            Element payloadElement = doc.createElement("Payload");
            SSCRootElement.appendChild(payloadElement);

            Element ledgerElement = doc.createElement("Ledger");
            payloadElement.appendChild(ledgerElement);

            ///////////////////////////////////////////  line Credit ///////////////////////////////////////////////////
            Element lineElement = doc.createElement("Line");
            ledgerElement.appendChild(lineElement);

            Element accountCodeElement = doc.createElement("AccountCode");
            accountCodeElement.appendChild(doc.createTextNode("11000"));
            lineElement.appendChild(accountCodeElement);

            Element analysisCode2Element = doc.createElement("AnalysisCode2");
            analysisCode2Element.appendChild(doc.createTextNode("12"));
            lineElement.appendChild(analysisCode2Element);

            Element analysisCode6Element = doc.createElement("AnalysisCode6");
            analysisCode6Element.appendChild(doc.createTextNode("16"));
            lineElement.appendChild(analysisCode6Element);

            Element base2ReportingAmountElement = doc.createElement("Base2ReportingAmount");
            base2ReportingAmountElement.appendChild(doc.createTextNode("485"));
            lineElement.appendChild(base2ReportingAmountElement);

            Element baseAmountElement = doc.createElement("BaseAmount");
            baseAmountElement.appendChild(doc.createTextNode("485"));
            lineElement.appendChild(baseAmountElement);

            Element currencyCodeElement = doc.createElement("CurrencyCode");
            currencyCodeElement.appendChild(doc.createTextNode("GBP"));
            lineElement.appendChild(currencyCodeElement);

            Element debitCreditElement = doc.createElement("DebitCredit");
            debitCreditElement.appendChild(doc.createTextNode("C"));
            lineElement.appendChild(debitCreditElement);

            Element journalLineNumberElement = doc.createElement("JournalLineNumber");
            journalLineNumberElement.appendChild(doc.createTextNode("1"));
            lineElement.appendChild(journalLineNumberElement);

            Element journalSourceElement = doc.createElement("JournalSource");
            journalSourceElement.appendChild(doc.createTextNode("PK1"));
            lineElement.appendChild(journalSourceElement);

            Element journalTypeElement = doc.createElement("JournalType");
            journalTypeElement.appendChild(doc.createTextNode("PACC"));
            lineElement.appendChild(journalTypeElement);

            Element transactionAmountElement = doc.createElement("TransactionAmount");
            transactionAmountElement.appendChild(doc.createTextNode("485"));
            lineElement.appendChild(transactionAmountElement);

            Element transactionReferenceElement = doc.createElement("TransactionReference");
            transactionReferenceElement.appendChild(doc.createTextNode("PACC"));
            lineElement.appendChild(transactionReferenceElement);

            Element accountsElement = doc.createElement("Accounts");
            lineElement.appendChild(accountsElement);

            lineElement.appendChild(accountCodeElement);

            ///////////////////////////////////////////  line Debit ////////////////////////////////////////////////////
            Element lineElement2 = doc.createElement("Line");
            ledgerElement.appendChild(lineElement2);

            Element accountCodeElement2 = doc.createElement("AccountCode");
            accountCodeElement2.appendChild(doc.createTextNode("11000"));
            lineElement2.appendChild(accountCodeElement2);

            Element analysisCode2Element2 = doc.createElement("AnalysisCode2");
            analysisCode2Element2.appendChild(doc.createTextNode("12"));
            lineElement2.appendChild(analysisCode2Element2);

            Element analysisCode6Element2 = doc.createElement("AnalysisCode6");
            analysisCode6Element2.appendChild(doc.createTextNode("16"));
            lineElement2.appendChild(analysisCode6Element2);

            Element base2ReportingAmountElement2 = doc.createElement("Base2ReportingAmount");
            base2ReportingAmountElement2.appendChild(doc.createTextNode("-485"));
            lineElement2.appendChild(base2ReportingAmountElement2);

            Element baseAmountElement2 = doc.createElement("BaseAmount");
            baseAmountElement2.appendChild(doc.createTextNode("-485"));
            lineElement2.appendChild(baseAmountElement2);

            Element currencyCodeElement2 = doc.createElement("CurrencyCode");
            currencyCodeElement2.appendChild(doc.createTextNode("GBP"));
            lineElement2.appendChild(currencyCodeElement2);

            Element debitCreditElement2 = doc.createElement("DebitCredit");
            debitCreditElement2.appendChild(doc.createTextNode("D"));
            lineElement2.appendChild(debitCreditElement2);

            Element journalLineNumberElement2 = doc.createElement("JournalLineNumber");
            journalLineNumberElement2.appendChild(doc.createTextNode("1"));
            lineElement2.appendChild(journalLineNumberElement2);

            Element journalSourceElement2 = doc.createElement("JournalSource");
            journalSourceElement2.appendChild(doc.createTextNode("PK1"));
            lineElement2.appendChild(journalSourceElement2);

            Element journalTypeElement2 = doc.createElement("JournalType");
            journalTypeElement2.appendChild(doc.createTextNode("PACC"));
            lineElement2.appendChild(journalTypeElement2);

            Element transactionAmountElement2 = doc.createElement("TransactionAmount");
            transactionAmountElement2.appendChild(doc.createTextNode("-485"));
            lineElement2.appendChild(transactionAmountElement2);

            Element transactionReferenceElement2 = doc.createElement("TransactionReference");
            transactionReferenceElement2.appendChild(doc.createTextNode("PACC"));
            lineElement2.appendChild(transactionReferenceElement2);

            Element accountsElement2 = doc.createElement("Accounts");
            lineElement2.appendChild(accountsElement2);

            lineElement2.appendChild(accountCodeElement2);

            Element enterAnalysis1Element = doc.createElement("EnterAnalysis1");
            enterAnalysis1Element.appendChild(doc.createTextNode("3"));
            accountsElement.appendChild(enterAnalysis1Element);

            Element enterAnalysis10Element = doc.createElement("EnterAnalysis10");
            enterAnalysis10Element.appendChild(doc.createTextNode("3"));
            accountsElement.appendChild(enterAnalysis10Element);

            Element enterAnalysis2Element = doc.createElement("EnterAnalysis2");
            enterAnalysis2Element.appendChild(doc.createTextNode("1"));
            accountsElement.appendChild(enterAnalysis2Element);

            Element enterAnalysis3Element = doc.createElement("EnterAnalysis3");
            enterAnalysis3Element.appendChild(doc.createTextNode("3"));
            accountsElement.appendChild(enterAnalysis3Element);

            Element enterAnalysis4Element = doc.createElement("EnterAnalysis4");
            enterAnalysis4Element.appendChild(doc.createTextNode("3"));
            accountsElement.appendChild(enterAnalysis4Element);

            Element enterAnalysis5Element = doc.createElement("EnterAnalysis5");
            enterAnalysis5Element.appendChild(doc.createTextNode("3"));
            accountsElement.appendChild(enterAnalysis5Element);

            Element enterAnalysis6Element = doc.createElement("EnterAnalysis6");
            enterAnalysis6Element.appendChild(doc.createTextNode("1"));
            accountsElement.appendChild(enterAnalysis6Element);

            Element enterAnalysis7Element = doc.createElement("EnterAnalysis7");
            enterAnalysis7Element.appendChild(doc.createTextNode("3"));
            accountsElement.appendChild(enterAnalysis7Element);

            Element enterAnalysis8Element = doc.createElement("EnterAnalysis8");
            enterAnalysis8Element.appendChild(doc.createTextNode("3"));
            accountsElement.appendChild(enterAnalysis8Element);

            Element enterAnalysis9Element = doc.createElement("EnterAnalysis9");
            enterAnalysis9Element.appendChild(doc.createTextNode("3"));
            accountsElement.appendChild(enterAnalysis9Element);

            Element analysis2Element = doc.createElement("Analysis2");
            accountsElement.appendChild(analysis2Element);

            Element vAcntCatAnalysis_AcntCodeElement = doc.createElement("VAcntCatAnalysis_AcntCode");
            vAcntCatAnalysis_AcntCodeElement.appendChild(doc.createTextNode("11000"));
            analysis2Element.appendChild(vAcntCatAnalysis_AcntCodeElement);

            Element vAcntCatAnalysis_AnlCodeElement = doc.createElement("VAcntCatAnalysis_AnlCode");
            vAcntCatAnalysis_AnlCodeElement.appendChild(doc.createTextNode("44"));
            analysis2Element.appendChild(vAcntCatAnalysis_AnlCodeElement);

            Element analysis6Element = doc.createElement("Analysis6");
            accountsElement.appendChild(analysis6Element);

            Element vAcntCatAnalysis_AcntCodeElement2 = doc.createElement("VAcntCatAnalysis_AcntCode");
            vAcntCatAnalysis_AcntCodeElement2.appendChild(doc.createTextNode("11000"));
            analysis6Element.appendChild(vAcntCatAnalysis_AcntCodeElement2);

            Element vAcntCatAnalysis_AnlCodeElement2 = doc.createElement("VAcntCatAnalysis_AnlCode");
            vAcntCatAnalysis_AnlCodeElement2.appendChild(doc.createTextNode("V"));
            analysis6Element.appendChild(vAcntCatAnalysis_AnlCodeElement2);

            // Transform Document to XML String
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

            SoapComponent ssc = new SoapComponent(HOST,PORT);
            ssc.authenticate(voucher);
            result = ssc.execute("Journal", "Import", sccXMLStringValue);
        }
        catch (Exception ex) {
            System.out.print("An error occurred logging in to SunSystems:\r\n");
            ex.printStackTrace();
        }

        // Convert XML to Object
        JAXBContext jaxbContext;
        try
        {
            jaxbContext = JAXBContext.newInstance(JournalSSC.class);

            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

            JournalSSC query = (JournalSSC) jaxbUnmarshaller.unmarshal(new StringReader(result));

            System.out.println(query.getPayload());

            return query.getPayload().get(0).getLine().getStatus().equals("success");
        }
        catch (JAXBException e)
        {
            e.printStackTrace();
        }
        return false;
    }

}
