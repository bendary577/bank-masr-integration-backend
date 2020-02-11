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
import com.sun.supplierpoc.soapModels.SSC;
import com.systemsunion.security.IAuthenticationVoucher;
import com.systemsunion.ssc.client.*;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;



@RestController

public class InvoiceController {
    static int PORT = 8080;
    static String HOST= "192.168.133.128";

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

    @RequestMapping("/getApprovedInvoices")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ArrayList<SyncJobData> getApprovedInvoices() {
        SyncJobType syncJobType = syncJobTypeRepo.findByNameAndAccountId("Approved Invoices", "1");

        SyncJob syncJob = new SyncJob(constant.RUNNING,  new Date(), null, "1", "1",
                syncJobType.getId());

        syncJobRepo.save(syncJob);

        ArrayList<HashMap<String, Object>> invoices = getInvoicesData(false);
        ArrayList<SyncJobData> addedInvoices = saveInvoicesData(invoices, syncJob, false);
        if (addedInvoices.size() != 0){
            try {
                sendInvoicesData(addedInvoices);
            } catch (SoapFaultException e) {
                e.printStackTrace();
            } catch (ComponentException e) {
                e.printStackTrace();
            }
        }

        syncJob.setStatus(constant.SUCCESS);
        syncJob.setEndDate(new Date());
        syncJobRepo.save(syncJob);

        return addedInvoices;
    }

    public ArrayList<HashMap<String, Object>> getInvoicesData(Boolean typeFlag){

        WebDriver driver = setupEnvironment.setupSeleniumEnv();
        ArrayList<HashMap<String, Object>> invoices = new ArrayList<>();

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
                return invoices;
            }

            String approvedInvoices = "https://mte03-ohim-prod.hospitality.oracleindustry.com/Webclient/Purchase/Invoicing/IvcOverviewView.aspx?type=1";
            driver.get(approvedInvoices);

            Select select = new Select(driver.findElement(By.id("_ctl5")));
            select.selectByVisibleText("All");

            if (typeFlag){
                driver.findElement(By.id("igtxttbxInvoiceFilter")).sendKeys("RTV");
            }

            driver.findElement(By.name("filterPanel_btnRefresh")).click();

            List<WebElement> rows = driver.findElements(By.tagName("tr"));

            ArrayList<String> columns = new ArrayList<>();
            WebElement row = rows.get(39);
            List<WebElement> cols = row.findElements(By.tagName("th"));

            for (int j = 1; j < cols.size(); j++) {
                columns.add(conversions.transformColName(cols.get(j).getText()));
            }

            for (int i = 40; i < rows.size(); i++) {
                HashMap<String, Object> invoice = new HashMap<>();

                row = rows.get(i);
                cols = row.findElements(By.tagName("td"));
                if (cols.size() < 14){
                    continue;
                }

                for (int j = 1; j < cols.size(); j++) {
                    invoice.put(columns.get(j - 1), cols.get(j).getText());
                }
                invoices.add(invoice);
            }

            driver.quit();
            return invoices;
        } catch (Exception e) {
            e.printStackTrace();
            driver.quit();
            return invoices;
        }

    }

    public ArrayList<SyncJobData> saveInvoicesData(ArrayList<HashMap<String, Object>> invoices, SyncJob syncJob,
                                                   Boolean flag){
        ArrayList<SyncJobData> addedInvoices = new ArrayList<>();

        for (int i = 0; i < invoices.size(); i++) {
            HashMap<String, Object> invoice = invoices.get(i);

            // Invoice Part
            if (!flag){
                if (invoice.get("invoice_no.").toString().substring(0, 3).equals("RTV")){
                    continue;
                }
            }

            HashMap<String, Object> data = new HashMap<>();

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

            SyncJobData syncJobData = new SyncJobData(data, constant.RECEIVED, "", new Date(),
                    syncJob.getId());
            syncJobDataRepo.save(syncJobData);

            addedInvoices.add(syncJobData);
        }
        return addedInvoices;

    }

    public Boolean sendInvoicesData(ArrayList<SyncJobData> addedInvoices) throws SoapFaultException, ComponentException {
        boolean useEncryption = false;

        String username = "ACt";
        String password = "P@ssw0rd";

        SecurityProvider securityProvider = new SecurityProvider(HOST, useEncryption);
        IAuthenticationVoucher voucher = securityProvider.Authenticate(username, password);

        SoapComponent component = null;
        if (useEncryption) {
            component = new SecureSoapComponent(HOST, PORT);
        } else {
            component = new SoapComponent(HOST, PORT);
        }
        component.authenticate(voucher);

        String inputPayload =   "<SSC>" +
                "   <User>" +
                "       <Name>" + username + "</Name>" +
                "   </User>" +
                "   <SunSystemsContext>" +
                "       <BusinessUnit>PK1</BusinessUnit>" +
                "   </SunSystemsContext>" +
                "   <Payload>" +

                "   </Payload>" +
                "</SSC>";

        String strOut = component.execute("purchase invoices", "Create", inputPayload);

        // Convert XML to Object
        JAXBContext jaxbContext;
        try
        {
            jaxbContext = JAXBContext.newInstance(SSC.class);

            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

            SSC query = (SSC) jaxbUnmarshaller.unmarshal(new StringReader(strOut));

            System.out.println(query);

            return true;
        }
        catch (JAXBException e)
        {
            e.printStackTrace();
        }

        return false;
        }
}
