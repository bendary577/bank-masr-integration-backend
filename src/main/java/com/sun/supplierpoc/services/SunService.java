package com.sun.supplierpoc.services;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.JournalBatch;
import com.sun.supplierpoc.models.SyncJobData;
import com.sun.supplierpoc.models.SyncJobType;
import com.sun.supplierpoc.models.configurations.AccountCredential;
import com.sun.supplierpoc.models.configurations.Analysis;
import com.sun.supplierpoc.soapModels.JournalSSC;
import com.sun.supplierpoc.soapModels.Message;
import com.systemsunion.security.IAuthenticationVoucher;
import com.systemsunion.ssc.client.ComponentException;
import com.systemsunion.ssc.client.SecurityProvider;
import com.systemsunion.ssc.client.SoapComponent;
import com.systemsunion.ssc.client.SoapFaultException;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

@Service
public class SunService {

    public IAuthenticationVoucher connectToSunSystem(Account account){
        boolean useEncryption = false;
        ArrayList<AccountCredential> accountCredentials = account.getAccountCredentials();
        AccountCredential sunCredentials = account.getAccountCredentialByAccount(Constants.SUN, accountCredentials);

        String username = sunCredentials.getUsername();
        String password = sunCredentials.getPassword();

        IAuthenticationVoucher voucher;
        try {
            SecurityProvider securityProvider = new SecurityProvider(sunCredentials.getHost(), useEncryption);
            voucher = securityProvider.Authenticate(username, password);
        } catch (ComponentException | SoapFaultException e) {
            System.out.println(e.getMessage());
            return null;
        }
        return voucher;
    }

    public HashMap<String, Object> sendJournalData(SyncJobData addedJournalEntry, JournalBatch addedJournalBatch,
                                                   SyncJobType syncJobType, Account account,
                                                   IAuthenticationVoucher voucher){
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
            businessUnitElement.appendChild(doc.createTextNode(syncJobType.getConfiguration().inforConfiguration.businessUnit));
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
            DescriptionElement.appendChild(doc.createTextNode(addedJournalEntry.getData().get("description").toString()));
            LedgerPostingParametersElement.appendChild(DescriptionElement);

            Element journalTypeElement = doc.createElement("JournalType");
            journalTypeElement.appendChild(doc.createTextNode(syncJobType.getConfiguration().inforConfiguration.journalType));
            LedgerPostingParametersElement.appendChild(journalTypeElement);

            Element postingTypeElement = doc.createElement("PostingType");
            postingTypeElement.appendChild(doc.createTextNode(syncJobType.getConfiguration().inforConfiguration.postingType));
            LedgerPostingParametersElement.appendChild(postingTypeElement);

            Element SuspenseAccount = doc.createElement("SuspenseAccount");
            SuspenseAccount.appendChild(doc.createTextNode(syncJobType.getConfiguration().inforConfiguration.suspenseAccount));
            LedgerPostingParametersElement.appendChild(SuspenseAccount);

            Element TransactionAmountAccount = doc.createElement("TransactionAmountAccount");
            TransactionAmountAccount.appendChild(doc.createTextNode(syncJobType.getConfiguration().inforConfiguration.suspenseAccount));
            LedgerPostingParametersElement.appendChild(TransactionAmountAccount);


            ///////////////////////////////////////////  Payload ///////////////////////////////////////////////////////

            Element payloadElement = doc.createElement("Payload");
            SSCRootElement.appendChild(payloadElement);

            Element ledgerElement = doc.createElement("Ledger");
            payloadElement.appendChild(ledgerElement);
            
            if (addedJournalBatch != null){
                SyncJobData salesDifferentData;
                ArrayList<SyncJobData> salesTaxData;
                ArrayList<SyncJobData> salesTenderData;
                ArrayList<SyncJobData> salesDiscountData;
                ArrayList<SyncJobData> salesMajorGroupGrossData;

                salesDifferentData = addedJournalBatch.getSalesDifferentData();
                salesTaxData = addedJournalBatch.getSalesTaxData();
                salesTenderData = addedJournalBatch.getSalesTenderData();
                salesDiscountData = addedJournalBatch.getSalesDiscountData();
                salesMajorGroupGrossData = addedJournalBatch.getSalesMajorGroupGrossData();

                ///////////////////////////////////////////  line Credit ///////////////////////////////////////////////////
                for (SyncJobData taxData : salesTaxData) {
                    if (taxData.getData().containsKey("totalCr")){
                        createJournalLine(true, doc, ledgerElement, syncJobType, taxData);
                    }
                }

                for (SyncJobData salesData : salesMajorGroupGrossData) {
                    if (salesData.getData().containsKey("totalCr")){
                        createJournalLine(true, doc, ledgerElement, syncJobType, salesData);
                    }
                }

                ///////////////////////////////////////////  line Debit ////////////////////////////////////////////////////
                for (SyncJobData tenderData : salesTenderData) {
                    if (tenderData.getData().containsKey("totalDr")){
                        createJournalLine(false, doc, ledgerElement, syncJobType, tenderData);
                    }
                }

                for (SyncJobData discountData : salesDiscountData) {
                    if (discountData.getData().containsKey("totalDr")){
                        createJournalLine(false, doc, ledgerElement, syncJobType, discountData);
                    }
                }

                ///////////////////////////////////////////  line Credit ///////////////////////////////////////////////////
                if (salesDifferentData.getData().containsKey("totalCr")){
                    createJournalLine(true, doc, ledgerElement, syncJobType, salesDifferentData);
                }else{
                    ///////////////////////////////////////////  line Debit ////////////////////////////////////////////////////
                    createJournalLine(false, doc, ledgerElement, syncJobType, salesDifferentData);
                }
                
            }
            else if (addedJournalEntry != null){
                ///////////////////////////////////////////  line Credit ///////////////////////////////////////////////////
                if (addedJournalEntry.getData().containsKey("totalCr")){
                    createJournalLine(true, doc, ledgerElement, syncJobType, addedJournalEntry);
                }
                ///////////////////////////////////////////  line Debit ////////////////////////////////////////////////////
                if (addedJournalEntry.getData().containsKey("totalDr")){
                    createJournalLine(false, doc, ledgerElement, syncJobType, addedJournalEntry);
                }
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

        String result;
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
            StringBuilder message = new StringBuilder();
            for (Message msg : messages) {
                if (msg.getLevel().equals("error")){
                    message.append(" * ");
                    message.append(msg.getUserText());
                }
            }

            data.put("status", status);
            data.put("message", message.toString());
            return data;

        } catch (JAXBException e) {
            e.printStackTrace();
        }
        data.put("status", Constants.FAILED);
        data.put("message", "");
        return data;
    }

    private void createJournalLine(boolean creditDebitFlag, Document doc, Element ledgerElement, SyncJobType syncJobType,
                                  SyncJobData addedJournalEntry) {
        ArrayList<Analysis> analysis = syncJobType.getConfiguration().analysis;

        Element lineElement = doc.createElement("Line");
        ledgerElement.appendChild(lineElement);

//        Element AccountingPeriodElement = doc.createElement("AccountingPeriod");
//        AccountingPeriodElement.appendChild(doc.createTextNode(addedJournalEntry.getData().get("accountingPeriod")));
//        lineElement.appendChild(AccountingPeriodElement);

        Element DescriptionElement = doc.createElement("Description");
        DescriptionElement.appendChild(doc.createTextNode(addedJournalEntry.getData().get("description").toString()));
        lineElement.appendChild(DescriptionElement);

        Element accountCodeElement = doc.createElement("AccountCode");
        if (creditDebitFlag) // Credit
            accountCodeElement.appendChild(doc.createTextNode(addedJournalEntry.getData().get("inventoryAccount").toString()));
        else // Debit
            accountCodeElement.appendChild(doc.createTextNode(addedJournalEntry.getData().get("expensesAccount").toString()));
        lineElement.appendChild(accountCodeElement);

        Element base2ReportingAmountElement = doc.createElement("Base2ReportingAmount");
        if (creditDebitFlag)
            base2ReportingAmountElement.appendChild(doc.createTextNode(String.valueOf(addedJournalEntry.getData().get("totalCr"))));
        else
            base2ReportingAmountElement.appendChild(doc.createTextNode(String.valueOf(addedJournalEntry.getData().get("totalDr"))));
        lineElement.appendChild(base2ReportingAmountElement);

        Element baseAmountElement = doc.createElement("BaseAmount");
        if (creditDebitFlag)
            baseAmountElement.appendChild(doc.createTextNode(String.valueOf(addedJournalEntry.getData().get("totalCr"))));
        else
            baseAmountElement.appendChild(doc.createTextNode(String.valueOf(addedJournalEntry.getData().get("totalDr"))));
        lineElement.appendChild(baseAmountElement);

        Element currencyCodeElement = doc.createElement("CurrencyCode");
        currencyCodeElement.appendChild(doc.createTextNode(syncJobType.getConfiguration().inforConfiguration.currencyCode));
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

        SimpleDateFormat simpleformat = new SimpleDateFormat("ddMMy");
        String date = simpleformat.format(new Date());

        Element TransactionDate = doc.createElement("TransactionDate");
        Element EntryDate = doc.createElement("EntryDate");

        if(addedJournalEntry.getData().containsKey("transactionDate")){
            TransactionDate.appendChild(doc.createTextNode(String.valueOf(addedJournalEntry.getData().get("transactionDate"))));
            EntryDate.appendChild(doc.createTextNode(String.valueOf(addedJournalEntry.getData().get("transactionDate"))));
        }else{
            TransactionDate.appendChild(doc.createTextNode(date));
            EntryDate.appendChild(doc.createTextNode(date));
        }
        lineElement.appendChild(TransactionDate);

        Element transactionAmountElement = doc.createElement("TransactionAmount");
        if (creditDebitFlag)
            transactionAmountElement.appendChild(doc.createTextNode(String.valueOf(addedJournalEntry.getData().get("totalCr"))));
        else
            transactionAmountElement.appendChild(doc.createTextNode(String.valueOf(addedJournalEntry.getData().get("totalDr"))));
        lineElement.appendChild(transactionAmountElement);

        Element transactionReferenceElement = doc.createElement("TransactionReference");
        transactionReferenceElement.appendChild(doc.createTextNode(addedJournalEntry.getData().get("transactionReference").toString()));
        lineElement.appendChild(transactionReferenceElement);

        Element accountsElement = doc.createElement("Accounts");
        lineElement.appendChild(accountsElement);

        lineElement.appendChild(accountCodeElement);

        // T#loctionAnalysis
        String locationAnalysis = syncJobType.getConfiguration().locationAnalysisCode;

        Element enterAnalysis1ElementT3 = doc.createElement("EnterAnalysis"+ locationAnalysis);
        enterAnalysis1ElementT3.appendChild(doc.createTextNode("1"));
        accountsElement.appendChild(enterAnalysis1ElementT3);

        Element analysis2ElementT3 = doc.createElement("Analysis" + locationAnalysis);
        accountsElement.appendChild(analysis2ElementT3);

        if (creditDebitFlag){
            Element vAcntCatAnalysis_AnlCodeElement1 = doc.createElement("VAcntCatAnalysis_AnlCode");
            if (addedJournalEntry.getData().get("fromLocation").equals("")){
                vAcntCatAnalysis_AnlCodeElement1.appendChild(doc.createTextNode(analysis.get(Integer.parseInt(locationAnalysis)).getCodeElement()));
            }else {
                vAcntCatAnalysis_AnlCodeElement1.appendChild(doc.createTextNode(addedJournalEntry.getData().get("fromLocation").toString()));
            }
            analysis2ElementT3.appendChild(vAcntCatAnalysis_AnlCodeElement1);

            Element analysisCode2ElementT3 = doc.createElement("AnalysisCode" + locationAnalysis);
            analysisCode2ElementT3.appendChild(doc.createTextNode(addedJournalEntry.getData().get("fromLocation").toString()));
            lineElement.appendChild(analysisCode2ElementT3);
        }
        else{
            Element vAcntCatAnalysis_AnlCodeElement1 = doc.createElement("VAcntCatAnalysis_AnlCode");
            if (addedJournalEntry.getData().get("fromLocation").equals("")){
                vAcntCatAnalysis_AnlCodeElement1.appendChild(doc.createTextNode(analysis.get(Integer.parseInt(locationAnalysis)).getCodeElement()));
            }else {
                vAcntCatAnalysis_AnlCodeElement1.appendChild(doc.createTextNode(addedJournalEntry.getData().get("toLocation").toString()));
            }
            analysis2ElementT3.appendChild(vAcntCatAnalysis_AnlCodeElement1);

            Element analysisCode2ElementT3 = doc.createElement("AnalysisCode" + locationAnalysis);
            analysisCode2ElementT3.appendChild(doc.createTextNode(addedJournalEntry.getData().get("toLocation").toString()));
            lineElement.appendChild(analysisCode2ElementT3);
        }

        for (Analysis analysisObject: analysis) {
            if (analysisObject.getNumber().equals(locationAnalysis)){
                continue;
            }
            if (analysisObject.getChecked()){
                Element analysisCode2Element = doc.createElement("AnalysisCode"+ analysisObject.getNumber());

                if (addedJournalEntry.getData().containsKey("analysisCodeT" + analysisObject.getNumber())){
                    String analysisCode = addedJournalEntry.getData().get("analysisCodeT" + analysisObject.getNumber()).toString();
                    analysisCode2Element.appendChild(doc.createTextNode(analysisCode));
                }else {
                    analysisCode2Element.appendChild(doc.createTextNode(analysisObject.getCodeElement()));
                }
                lineElement.appendChild(analysisCode2Element);

                Element enterAnalysis1Element = doc.createElement("EnterAnalysis" + analysisObject.getNumber());
                enterAnalysis1Element.appendChild(doc.createTextNode("1"));
                accountsElement.appendChild(enterAnalysis1Element);

                Element analysis2Element = doc.createElement("Analysis" + analysisObject.getNumber());
                accountsElement.appendChild(analysis2Element);

                Element vAcntCatAnalysis_AnlCodeElement = doc.createElement("VAcntCatAnalysis_AnlCode");
                vAcntCatAnalysis_AnlCodeElement.appendChild(doc.createTextNode(analysisObject.getCodeElement()));
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
