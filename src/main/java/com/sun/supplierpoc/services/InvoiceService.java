package com.sun.supplierpoc.services;

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
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Service
public class InvoiceService {

    static int PORT = 8080;
    static String HOST= "192.168.1.21";

    @Autowired
    private SyncJobDataRepo syncJobDataRepo;

    public SetupEnvironment setupEnvironment = new SetupEnvironment();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public HashMap<String, Object> getInvoicesData(Boolean typeFlag, SyncJobType syncJobType){
        HashMap<String, Object> data = new HashMap<>();

        WebDriver driver = setupEnvironment.setupSeleniumEnv(false);
        ArrayList<HashMap<String, Object>> invoices = new ArrayList<>();

        try {
            String url = "https://mte03-ohim-prod.hospitality.oracleindustry.com/Webclient/FormLogin.aspx";

            if (!setupEnvironment.loginOHIM(driver, url)){
                driver.quit();

                data.put("status", Constants.FAILED);
                data.put("message", "Invalid username and password.");
                data.put("invoices", invoices);
                return data;
            }

            String approvedInvoices = "https://mte03-ohim-prod.hospitality.oracleindustry.com/Webclient/Purchase/Invoicing/IvcOverviewView.aspx?type=1";
            driver.get(approvedInvoices);

            Select select = new Select(driver.findElement(By.id("_ctl5")));
            select.selectByVisibleText((String) syncJobType.getConfiguration().get("timePeriod"));

            if (typeFlag){
                driver.findElement(By.id("igtxttbxInvoiceFilter")).sendKeys("RTV");
            }

            driver.findElement(By.name("filterPanel_btnRefresh")).click();

            List<WebElement> rows = driver.findElements(By.tagName("tr"));

            ArrayList<String> columns = setupEnvironment.getTableColumns(rows, true, 39);

            for (int i = 40; i < rows.size(); i++) {
                HashMap<String, Object> invoice = new HashMap<>();

                WebElement row = rows.get(i);
                List<WebElement> cols = row.findElements(By.tagName("td"));
                if (cols.size() !=  columns.size()){
                    continue;
                }

                for (int j = 1; j < cols.size(); j++) {
                    invoice.put(columns.get(j), cols.get(j).getText());
                }
                invoices.add(invoice);
            }

            driver.quit();

            data.put("status", Constants.SUCCESS);
            data.put("message", "");
            data.put("invoices", invoices);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            driver.quit();

            data.put("status", Constants.FAILED);
            data.put("message", e);
            data.put("invoices", invoices);
            return data;
        }

    }

    public ArrayList<SyncJobData> saveInvoicesData(ArrayList<HashMap<String, String>> invoices, SyncJob syncJob,
                                                   Boolean flag){
        ArrayList<SyncJobData> addedInvoices = new ArrayList<>();

        for (HashMap<String, String> invoice : invoices) {
            // Invoice Part
            if (!flag) {
                if (invoice.get("invoice_no.").substring(0, 3).equals("RTV")) {
                    continue;
                }
            }

            HashMap<String, String> data = new HashMap<>();

            data.put("invoiceNo", invoice.get("invoice_no."));
            data.put("vendor", invoice.get("vendor"));
            data.put("costCenter", invoice.get("cost_center"));
            data.put("status", invoice.get("status"));
            data.put("invoiceDate", invoice.get("'invoice_date'"));
            data.put("net", invoice.get("net"));
            data.put("vat", invoice.get("vat"));
            data.put("gross", invoice.get("gross"));
            data.put("createdBy", invoice.get("'created_by"));
            data.put("createdAt", invoice.get("created_at"));

            SyncJobData syncJobData = new SyncJobData(data, Constants.RECEIVED, "", new Date(),
                    syncJob.getId());
            syncJobDataRepo.save(syncJobData);

            addedInvoices.add(syncJobData);
        }
        return addedInvoices;

    }

    // NOT USED
    public Boolean sendInvoicesData(ArrayList<SyncJobData> addedInvoices) throws SoapFaultException, ComponentException {
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

                        "<Payload>" +
                        "<PurchaseInvoice>" +
                        "<PurchaseTransactionType>" + "PI_INVENTORY" + "</PurchaseTransactionType>" +
                        "<SupplierCode>" + "80020" + "</SupplierCode>" +
                        "<TransactionDate>" + "10062004" + "</TransactionDate>" +
                        "</PurchaseInvoice>" +
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
