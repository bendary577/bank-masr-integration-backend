package com.sun.supplierpoc.services;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.models.SyncJob;
import com.sun.supplierpoc.models.SyncJobData;
import com.sun.supplierpoc.models.SyncJobType;
import com.sun.supplierpoc.repositories.SyncJobDataRepo;
import com.sun.supplierpoc.repositories.SyncJobRepo;
import com.sun.supplierpoc.repositories.SyncJobTypeRepo;
import com.sun.supplierpoc.seleniumMethods.SetupEnvironment;
import com.sun.supplierpoc.soapModels.SSC;
import com.sun.supplierpoc.soapModels.Supplier;
import com.systemsunion.security.IAuthenticationVoucher;
import com.systemsunion.ssc.client.*;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

@Service
public class SupplierService {
    static int PORT = 8080;
    static String HOST= "192.168.1.21";

    @Autowired
    private SyncJobRepo syncJobRepo;
    @Autowired
    private SyncJobTypeRepo syncJobTypeRepo;
    @Autowired
    private SyncJobDataRepo syncJobDataRepo;

    public SetupEnvironment setupEnvironment = new SetupEnvironment();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public HashMap<String, Object> getSuppliersData() throws SoapFaultException, ComponentException {
        HashMap<String, Object> data = new HashMap<>();
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
                "<OutputLimit>5</OutputLimit>" +
                "<Select>" +
                "<Supplier>" +

                "<AccountCode/>" +
                "<SupplierCode/>" +
                "<SupplierName/>" +
                "<Status/>" +
//                            "<EMailAddress/>" +
                "<PaymentTermsGroupCode/>" +

                "<Address_Contact>"+
                "<ContactIdentifier/>" +
                "</Address_Contact>"+

                "<SupplierAddress>"+
                "<TelephoneNumber/>" +
                "<AddressCode/>" +
                "<AddressLine1/>" +
                "<AddressLine2/>" +
                "<AddressLine3/>" +
                "<PostalCode/>" +
                "</SupplierAddress>"+

                "</Supplier>" +
                "</Select>" +
                "   </Payload>" +
                "</SSC>";

        String strOut = component.execute("Supplier", "Query", inputPayload);

        // Convert XML to Object
        JAXBContext jaxbContext;
        try
        {
            jaxbContext = JAXBContext.newInstance(SSC.class);

            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

            SSC query = (SSC) jaxbUnmarshaller.unmarshal(new StringReader(strOut));

            data.put("status", Constants.SUCCESS);
            data.put("message", "");
            data.put("suppliers", query.getPayload());
            return data;
        }
        catch (JAXBException e)
        {
            e.printStackTrace();
        }

        data.put("status", Constants.FAILED);
        data.put("message", "");
        data.put("suppliers", new ArrayList<> ());
        return data;
    }

    public ArrayList<SyncJobData> saveSuppliersData(ArrayList<Supplier> suppliers, SyncJob syncJob) {
        ArrayList<SyncJobData> addedSuppliers = new ArrayList<>();

        for (Supplier supplier : suppliers) {
            HashMap<String, String> data = new HashMap<>();

            data.put("supplierId", "");
            data.put("supplier", supplier.getSupplierName());
            data.put("supplierNumber", supplier.getSupplierCode());
            data.put("status", supplier.getStatus());
            data.put("customerNumber", "");
            data.put("paymentTerms", supplier.getPaymentTermsGroupCode());
            data.put("phoneNumber", supplier.getSupplierAddress().getTelephoneNumber());
            data.put("email", "");
            data.put("address", supplier.getSupplierAddress().getAddressCode());
            data.put("line1", supplier.getSupplierAddress().getAddressLine1());
            data.put("line2", supplier.getSupplierAddress().getAddressLine2());
            data.put("line3", supplier.getSupplierAddress().getAddressLine3());
            data.put("postalCode", supplier.getSupplierAddress().getPostalCode());
            data.put("faxNumber", "");
            data.put("contactFirstName", "");


            SyncJobData syncJobData = new SyncJobData(data, Constants.RECEIVED, "", new Date(),
                    syncJob.getId());
            System.out.println(syncJobDataRepo.save(syncJobData));
            addedSuppliers.add(syncJobData);

        }
        return addedSuppliers;
    }

    public Boolean sendSuppliersData(ArrayList<SyncJobData> suppliers, SyncJob syncJob, SyncJobType syncJobType){
        WebDriver driver = setupEnvironment.setupSeleniumEnv();

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
                return false;
            }

            for (SyncJobData supplier : suppliers) {
                String vendorPage = "https://mte03-ohim-prod.hospitality.oracleindustry.com/Webclient/MasterData/Vendors/OverviewVendor.aspx";
                driver.get(vendorPage);
                driver.findElement(By.linkText("New")).click();

                driver.findElement(By.id("igtxtLF_NAME")).sendKeys(((HashMap) supplier.getData()).get("supplier").toString());
                driver.findElement(By.id("igtxttb__ctl0_LF_KONR")).sendKeys(((HashMap) supplier.getData()).get("supplierNumber").toString());

                //////////////////////////////////////  Set Hidden Elements  ///////////////////////////////////////////
                JavascriptExecutor js = (JavascriptExecutor) driver;
                js.executeScript("document.getElementById('tb__ctl0_cfTaxes_Value').setAttribute('type','text')");
                js.executeScript("document.getElementById('tb__ctl0_cfTaxes_Value').style.display = 'block';");

                String tax = (String) syncJobType.getConfiguration().get("taxes");
                driver.findElement(By.id("tb__ctl0_cfTaxes_Text")).sendKeys(tax);
                driver.findElement(By.id("tb__ctl0_cfTaxes_Text")).sendKeys(Keys.ARROW_DOWN);
                driver.findElement(By.id("tb__ctl0_cfTaxes_Text")).sendKeys(Keys.ENTER);
//                String taxesValue = "[22,\"New\",\"NEW\"]";
//                js.executeScript("document.getElementById('tb__ctl0_cfTaxes_Value').setAttribute('value'," + taxesValue + ")");


                js.executeScript("document.getElementById('tb__ctl0_cfVendorGroup_Value').setAttribute('type','text')");
                js.executeScript("document.getElementById('tb__ctl0_cfVendorGroup_Value').style.display = 'block';");

                String group = (String) syncJobType.getConfiguration().get("groups");
                driver.findElement(By.id("tb__ctl0_cfVendorGroup_Text")).sendKeys(group);
                driver.findElement(By.id("tb__ctl0_cfVendorGroup_Text")).sendKeys(Keys.ARROW_DOWN);
                driver.findElement(By.id("tb__ctl0_cfVendorGroup_Text")).sendKeys(Keys.ENTER);

//                String vendorGroupValue = "[12,\"Dariy\",\"DARIY\"]";
//                js.executeScript("document.getElementById('tb__ctl0_cfVendorGroup_Value').setAttribute('value'," + vendorGroupValue + ")");

                //////////////////////////////////////  Set Vendor Info  ///////////////////////////////////////////////

                driver.findElement(By.id("igtxttb__ctl0_LF_KDNNR")).sendKeys(((HashMap) supplier.getData()).get("customerNumber").toString());
                driver.findElement(By.id("igtxttb__ctl0_LF_TEL")).sendKeys(((HashMap) supplier.getData()).get("phoneNumber").toString());
                driver.findElement(By.id("igtxttb__ctl0_LF_TELEX")).sendKeys(((HashMap) supplier.getData()).get("email").toString());
                driver.findElement(By.id("igtxttb__ctl0_LF_SACHB")).sendKeys(((HashMap) supplier.getData()).get("contactFirstName").toString());
                driver.findElement(By.id("igtxttb__ctl0_LF_ZBED")).sendKeys(((HashMap) supplier.getData()).get("paymentTerms").toString());
                driver.findElement(By.id("igtxttb__ctl0_LF_PLZ")).sendKeys(((HashMap) supplier.getData()).get("postalCode").toString());
                driver.findElement(By.id("igtxttb__ctl0_LF_FAX")).sendKeys(((HashMap) supplier.getData()).get("faxNumber").toString());


                //////////////////////////////////////  Set Address  ///////////////////////////////////////////////////
                ArrayList<String> handles = new ArrayList<>(driver.getWindowHandles());
                String windowBefore = handles.get(0);

                driver.findElement(By.id("tb__ctl0_btnEditAddress")).click();

                while (true) {
                    if (handles.size() != driver.getWindowHandles().size()) {
                        break;
                    }
                }

                handles = new ArrayList<>(driver.getWindowHandles());
                String windowAfter = handles.get(1);

                driver.switchTo().window(windowAfter);

                driver.findElement(By.id("igtxttbStreet")).sendKeys(((HashMap) supplier.getData()).get("address").toString());
                driver.findElement(By.id("igtxttbAddressline1")).sendKeys(((HashMap) supplier.getData()).get("line1").toString());
                driver.findElement(By.id("igtxttbAddressline2")).sendKeys(((HashMap) supplier.getData()).get("line2").toString());

                driver.findElement(By.id("btnOk")).click();

                while (true) {
                    if (handles.size() != driver.getWindowHandles().size()) {
                        break;
                    }
                }
                driver.switchTo().window(windowBefore);

                //////////////////////////////////////  Set Order Settings  ////////////////////////////////////////////

                driver.findElement(By.id("tbtd1")).click();
                driver.findElement(By.id("tb__ctl1_LF_PURCHASEALL")).click();

                //////////////////////////////////////  Save And Check Existence  //////////////////////////////////////

                driver.findElement(By.linkText("Save")).click();
                try {
                    new WebDriverWait(driver, 5)
                            .ignoring(NoAlertPresentException.class)
                            .until(ExpectedConditions.alertIsPresent());

                    Alert al = driver.switchTo().alert();
                    al.accept();

                    supplier.setStatus(Constants.FAILED);
                    supplier.setReason("Already Exits");
                    supplier.setSyncJobId(syncJob.getId());
                    syncJobDataRepo.save(supplier);

                } catch (NoAlertPresentException Ex) {
                    System.out.println("No alert");
                }

                supplier.setStatus(Constants.SUCCESS);
                supplier.setReason("");
                supplier.setSyncJobId(syncJob.getId());
                syncJobDataRepo.save(supplier);
            }

            driver.quit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            driver.quit();
            return false;
        }
    }
}
