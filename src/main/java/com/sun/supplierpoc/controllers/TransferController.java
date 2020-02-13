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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

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

        try {

            sendTransferData();
        } catch (SoapFaultException e) {
            e.printStackTrace();
        } catch (ComponentException e) {
            e.printStackTrace();
        }

        SyncJobType syncJobType = syncJobTypeRepo.findByNameAndAccountId("Booked Transfers", "1");

        SyncJob syncJob = new SyncJob(constant.RUNNING,  new Date(), null, "1", "1",
                syncJobType.getId());

        syncJobRepo.save(syncJob);

        ArrayList<HashMap<String, Object>> invoices = getTransferData(syncJobType);
        ArrayList<SyncJobData> addedTransfers = saveTransferData(invoices, syncJob);
        if (addedTransfers.size() != 0){
            try {

                sendTransferData();
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

//    ArrayList<SyncJobData> addedInvoices
    public Boolean sendTransferData() throws SoapFaultException, ComponentException {
        boolean useEncryption = false;

        String username = "ACt";
        String password = "P@ssw0rd";

        SecurityProvider securityProvider = new SecurityProvider(HOST, useEncryption);
        IAuthenticationVoucher voucher = securityProvider.Authenticate(username, password);


        String inputPayload =
                "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" +
                        "<SSC>" +
                        "<ErrorContext/>" +
                        "   <User>" +
                        "       <Name>" + username + "</Name>" +
                        "   </User>" +

                        "<SunSystemsContext>" +
                        "<BusinessUnit>"  + "PK1" + "</BusinessUnit>" +
                        "</SunSystemsContext>" +

                        "<MethodContext>" +
                        "<LedgerPostingParameters>" +
                        "<Description>" + "Test import journal 1" + "</Description>" +
                        "<JournalType>" + "" + "</JournalType>" +
                        "<PostingType>" + "1" + "</PostingType>" +
                        "</LedgerPostingParameters>" +
                        "</MethodContext>" +
                        "<Payload>" +
                        "<Ledger>" +
                        "<Line>" +
                            "<AccountCode>" + "11000" + "</AccountCode>" +
                            "<AnalysisCode2>" + "12" + "</AnalysisCode2>" +
                            "<AnalysisCode6>" + "16" + "</AnalysisCode6>" +
                            "<Base2ReportingAmount>" + "485" + "</Base2ReportingAmount>" +
                            "<BaseAmount>" + "485" + "</BaseAmount>" +
                            "<CurrencyCode>" + "GBP" + "</CurrencyCode>" +

                            "<DebitCredit>" + "C" + "</DebitCredit>" +
                            "<JournalLineNumber>" + "1" + "</JournalLineNumber>" +
                            "<JournalSource>" + "PK1" + "</JournalSource>" +
                            "<JournalType>" + "PACC" + "</JournalType>" +
                            "<TransactionAmount>" + "485" + "</TransactionAmount>" +
                            "<CurrencyCode>" + "GBP" + "</CurrencyCode>" +


//<JournalType>PACC</JournalType>
//<TransactionAmount>485</TransactionAmount>
//<TransactionReference>SI/002017</TransactionReference>
//<Accounts>
//<AccountCode>11000</AccountCode>
//<EnterAnalysis1>3</EnterAnalysis1>
//<EnterAnalysis10>3</EnterAnalysis10>
//<EnterAnalysis2>1</EnterAnalysis2>
//<EnterAnalysis3>3</EnterAnalysis3>
//<EnterAnalysis4>3</EnterAnalysis4>
//<EnterAnalysis5>3</EnterAnalysis5>
//<EnterAnalysis6>1</EnterAnalysis6>
//<EnterAnalysis7>3</EnterAnalysis7>
//<EnterAnalysis8>3</EnterAnalysis8>
//<EnterAnalysis9>3</EnterAnalysis9>
//<Analysis2>
//<VAcntCatAnalysis_AcntCode>11000</VAcntCatAnalysis_AcntCode>
//<VAcntCatAnalysis_AnlCode>44</VAcntCatAnalysis_AnlCode>
//</Analysis2>
//<Analysis6>
//<VAcntCatAnalysis_AcntCode>11000</VAcntCatAnalysis_AcntCode>
//<VAcntCatAnalysis_AnlCode>V</VAcntCatAnalysis_AnlCode>
//</Analysis6>
//</Accounts>
//</Line>
//</Ledger>




                        "</Payload>" +
                        "</SSC>";

        String result = "";

        try {

            SoapComponent ssc = new SoapComponent(HOST,PORT);
            ssc.authenticate(voucher);
            result = ssc.execute("PurchaseInvoice", "CreateOrAmend", inputPayload);
            System.out.println(result);
        }
        catch (Exception ex) {
            System.out.print("An error occurred logging in to SunSystems:\r\n");
            ex.printStackTrace();
        }

        // Convert XML to Object
        JAXBContext jaxbContext;
        try
        {
            jaxbContext = JAXBContext.newInstance(PurchaseInvoiceSSC.class);

            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();


            PurchaseInvoiceSSC query = (PurchaseInvoiceSSC) jaxbUnmarshaller.unmarshal(new StringReader(result));

            System.out.println(query.getPayload());

            if (query.getPayload().get(0).getStatus().equals("success")){
                return true;
            }
            return false;
        }
        catch (JAXBException e)
        {
            e.printStackTrace();
        }
        return false;
    }


}
