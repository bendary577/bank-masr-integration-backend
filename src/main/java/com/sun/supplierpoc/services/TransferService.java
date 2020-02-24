package com.sun.supplierpoc.services;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.controllers.InvoiceController;
import com.sun.supplierpoc.models.CostCenter;
import com.sun.supplierpoc.models.SyncJob;
import com.sun.supplierpoc.models.SyncJobData;
import com.sun.supplierpoc.models.SyncJobType;
import com.sun.supplierpoc.repositories.SyncJobDataRepo;
import com.sun.supplierpoc.repositories.SyncJobRepo;
import com.sun.supplierpoc.repositories.SyncJobTypeRepo;
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

@Service
public class TransferService {
    static int PORT = 8080;
    static String HOST= "192.168.1.21";

    @Autowired
    SyncJobDataRepo syncJobDataRepo;
    @Autowired
    InvoiceController invoiceController;

    public SetupEnvironment setupEnvironment = new SetupEnvironment();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public HashMap<String, Object> getTransferData(SyncJobType syncJobType){
        HashMap<String, Object> data = new HashMap<>();

        WebDriver driver = setupEnvironment.setupSeleniumEnv(false);
        ArrayList<HashMap<String, String>> transfers = new ArrayList<>();

        ArrayList<HashMap<String, String>> costCenters = (ArrayList<HashMap<String, String>>) syncJobType.getConfiguration().get("costCenters");

        try {
            String url = "https://mte03-ohim-prod.hospitality.oracleindustry.com/Webclient/FormLogin.aspx";

            if (!setupEnvironment.loginOHIM(driver, url)){
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

            int pageCounter = 2;
            while (true){
                for (int i = 1; i < rows.size(); i++) {
                    HashMap<String, String> transfer = new HashMap<>();

                    WebElement row = rows.get(i);
                    List<WebElement> cols = row.findElements(By.tagName("td"));
                    if (cols.size() != 16) {
                        continue;
                    }

                    WebElement td = cols.get(columns.indexOf("from_cost_center"));
                    HashMap<String, Object> oldCostCenterData = invoiceController.checkExistence(costCenters, td.getText().strip());

                    if (!(boolean) oldCostCenterData.get("status")) {
                        continue;
                    }
                    for (int j = 1; j < cols.size(); j++) {
                        transfer.put(columns.get(j), cols.get(j).getText());
                    }
                    transfers.add(transfer);
                }

                try{
                    Select selectPage = new Select(driver.findElement(By.xpath("/html/body/form/table/tbody/tr[4]/td/table/tbody/tr[3]/td/select")));
                    selectPage.selectByVisibleText(Integer.toString(pageCounter));
                    System.out.println(pageCounter);
                    pageCounter++;

                    driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

                    table = driver.findElement(By.id("dg_main"));
                    rows = table.findElements(By.tagName("tr"));
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    break;
                }

            }

            driver.quit();

            data.put("status", Constants.SUCCESS);
            data.put("message", "");
            data.put("transfers", transfers);
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

    public ArrayList<SyncJobData> saveTransferData(ArrayList<HashMap<String, String>> invoices, SyncJob syncJob){
        ArrayList<SyncJobData> addedInvoices = new ArrayList<>();

        for (HashMap<String, String> invoice : invoices) {
            HashMap<String, String> data = new HashMap<>();

            data.put("invoiceNo", invoice.get("invoice_no."));


            SyncJobData syncJobData = new SyncJobData(data, Constants.RECEIVED, "", new Date(),
                    syncJob.getId());
            syncJobDataRepo.save(syncJobData);

            addedInvoices.add(syncJobData);
        }
        return addedInvoices;

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

            SoapComponent ssc = new SoapComponent(HOST,PORT);
            ssc.authenticate(voucher);
            result = ssc.execute("Journal", "Import", sccXMLStringValue);
        }
        catch (Exception ex) {
            System.out.print("An error occurred logging in to SunSystems:\r\n");
            ex.printStackTrace();
        }

        ///////////////////////////////////////////  Convert XML to Object /////////////////////////////////////////////
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

    private void createJournalLine(boolean creditDebitFlag, Document doc, Element ledgerElement, SyncJobType syncJobType,
                                   SyncJobData addedJournalEntry){

        Element lineElement = doc.createElement("Line");
        ledgerElement.appendChild(lineElement);

        Element accountCodeElement = doc.createElement("AccountCode");
        accountCodeElement.appendChild(doc.createTextNode((String) syncJobType.getConfiguration().get("accountCode")));
        lineElement.appendChild(accountCodeElement);

        Element analysisCode2Element = doc.createElement("AnalysisCode2");
        analysisCode2Element.appendChild(doc.createTextNode("12"));
        lineElement.appendChild(analysisCode2Element);

        Element analysisCode6Element = doc.createElement("AnalysisCode6");
        analysisCode6Element.appendChild(doc.createTextNode("16"));
        lineElement.appendChild(analysisCode6Element);

        Element base2ReportingAmountElement = doc.createElement("Base2ReportingAmount");
        if (creditDebitFlag)
            base2ReportingAmountElement.appendChild(doc.createTextNode(addedJournalEntry.getData().get("gross")));
        else
            base2ReportingAmountElement.appendChild(doc.createTextNode("-" + addedJournalEntry.getData().get("gross")));
        lineElement.appendChild(base2ReportingAmountElement);

        Element baseAmountElement = doc.createElement("BaseAmount");
        if (creditDebitFlag)
            baseAmountElement.appendChild(doc.createTextNode(addedJournalEntry.getData().get("gross")));
        else
            baseAmountElement.appendChild(doc.createTextNode("-" + addedJournalEntry.getData().get("gross")));
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
            transactionAmountElement.appendChild(doc.createTextNode(addedJournalEntry.getData().get("gross")));
        else
            transactionAmountElement.appendChild(doc.createTextNode("-" + addedJournalEntry.getData().get("gross")));
        lineElement.appendChild(transactionAmountElement);

        Element transactionReferenceElement = doc.createElement("TransactionReference");
        transactionReferenceElement.appendChild(doc.createTextNode((String) syncJobType.getConfiguration().get("transactionReference")));
        lineElement.appendChild(transactionReferenceElement);

        Element accountsElement = doc.createElement("Accounts");
        lineElement.appendChild(accountsElement);

        lineElement.appendChild(accountCodeElement);

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
        vAcntCatAnalysis_AcntCodeElement.appendChild(doc.createTextNode((String) syncJobType.getConfiguration().get("accountCode")));
        analysis2Element.appendChild(vAcntCatAnalysis_AcntCodeElement);

        Element vAcntCatAnalysis_AnlCodeElement = doc.createElement("VAcntCatAnalysis_AnlCode");
        vAcntCatAnalysis_AnlCodeElement.appendChild(doc.createTextNode("44"));
        analysis2Element.appendChild(vAcntCatAnalysis_AnlCodeElement);

        Element analysis6Element = doc.createElement("Analysis6");
        accountsElement.appendChild(analysis6Element);

        Element vAcntCatAnalysis_AcntCodeElement2 = doc.createElement("VAcntCatAnalysis_AcntCode");
        vAcntCatAnalysis_AcntCodeElement2.appendChild(doc.createTextNode((String) syncJobType.getConfiguration().get("accountCode")));
        analysis6Element.appendChild(vAcntCatAnalysis_AcntCodeElement2);

        Element vAcntCatAnalysis_AnlCodeElement2 = doc.createElement("VAcntCatAnalysis_AnlCode");
        vAcntCatAnalysis_AnlCodeElement2.appendChild(doc.createTextNode("V"));
        analysis6Element.appendChild(vAcntCatAnalysis_AnlCodeElement2);
    }

}
