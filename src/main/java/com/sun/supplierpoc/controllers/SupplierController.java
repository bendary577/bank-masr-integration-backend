package com.sun.supplierpoc.controllers;
import com.google.gson.JsonObject;
import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.models.SyncJobData;
import com.sun.supplierpoc.repositories.SyncJobDataRepo;
import com.sun.supplierpoc.repositories.SyncJobRepo;
import com.sun.supplierpoc.repositories.SyncJobTypeRepo;
import com.sun.supplierpoc.seleniumMethods.SetupEnvironment;
import com.sun.supplierpoc.soapModels.SSC;
import com.sun.supplierpoc.soapModels.Supplier;
import com.systemsunion.security.IAuthenticationVoucher;
import com.systemsunion.ssc.client.*;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
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
import java.util.Set;


@RestController
public class SupplierController {

    static int PORT = 8080;
    static String HOST= "192.168.133.128";

    @Autowired
    private SyncJobRepo syncJobRepo;
    @Autowired
    private SyncJobTypeRepo syncJobTypeRepo;
    @Autowired
    private SyncJobDataRepo syncJobDataRepo;

    public Constants constant = new Constants();
    public SetupEnvironment setupEnvironment = new SetupEnvironment();

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public ArrayList<Supplier> getSuppliersData() throws SoapFaultException, ComponentException {

        boolean useEncryption = false;

        String username = "ACt";
        String password = "P@ssw0rd";

        // obtain a SunSystems Security Voucher via SecurityProvider SOAP API
        SecurityProvider securityProvider = new SecurityProvider(HOST, useEncryption);
        IAuthenticationVoucher voucher = securityProvider.Authenticate(username, password);

        // setup and authenticate a SOAP API component proxy
        SoapComponent component = null;
        if (useEncryption) {
            component = new SecureSoapComponent(HOST, PORT);
        } else {
            component = new SoapComponent(HOST, PORT);
        }
        component.authenticate(voucher);


        // call 'Query' method via SOAP API ...
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
                            "<EMailAddress/>" +
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
        System.out.println(inputPayload);
        String strOut = component.execute("Supplier", "Query", inputPayload);

        // Convert XML to Object
        JAXBContext jaxbContext;
        try
        {
            jaxbContext = JAXBContext.newInstance(SSC.class);

            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

            SSC query = (SSC) jaxbUnmarshaller.unmarshal(new StringReader(strOut));

            System.out.println(query);

            return query.getPayload();
        }
        catch (JAXBException e)
        {
            e.printStackTrace();
        }

        return new ArrayList<> ();
    }

    public ArrayList<SyncJobData> saveSuppliersData(ArrayList<Supplier> suppliers) {
        ArrayList<SyncJobData> addedSuppliers = new ArrayList<>();

        for (int i = 0; i < suppliers.size(); i++) {
            Supplier supplier = suppliers.get(i);

            HashMap<String, Object> data = new HashMap<String, Object>();

            data.put("supplierId", "");
            data.put("supplier", supplier.getSupplierName());
            data.put("supplierNumber", supplier.getSupplierCode());
            data.put("status", supplier.getStatus());
            data.put("customerNumber", "");
            data.put("paymentTerms", supplier.getPaymentTermsGroupCode());
            data.put("phoneNumber", supplier.getSupplierAddress().getTelephoneNumber());
            data.put("email", supplier.getEMailAddress());
            data.put("address", supplier.getSupplierAddress().getAddressCode());
            data.put("line1", supplier.getSupplierAddress().getAddressLine1());
            data.put("line2", supplier.getSupplierAddress().getAddressLine2());
            data.put("line3", supplier.getSupplierAddress().getAddressLine3());
            data.put("postalCode", supplier.getSupplierAddress().getPostalCode());
            data.put("faxNumber", "");


            SyncJobData syncJobData = new SyncJobData(data, constant.RECEIVED, "", new Date(),
                    "0");
            System.out.println(syncJobDataRepo.save(syncJobData));
            addedSuppliers.add(syncJobData);

        }
        return addedSuppliers;
    }

    public Boolean sendSuppliersData(ArrayList<SyncJobData> suppliers) throws SoapFaultException, ComponentException{
        WebDriver driver = setupEnvironment.setupSeleniumEnv();
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

        for(int i = 0; i < suppliers.size(); i++) {
            SyncJobData supplier = suppliers.get(i);

            String vendorPage = "https://mte03-ohim-prod.hospitality.oracleindustry.com/Webclient/MasterData/Vendors/OverviewVendor.aspx";
            driver.get(vendorPage);
            driver.findElement(By.linkText("New")).click();

            // supplier name
            driver.findElement(By.id("igtxtLF_NAME")).sendKeys(((HashMap) supplier.getData()).get("supplier").toString());
            // supplier number
            driver.findElement(By.id("igtxttb__ctl0_LF_KONR")).sendKeys(((HashMap) supplier.getData()).get("supplierNumber").toString());

            driver.findElement(By.id("igtxttb__ctl0_LF_KDNNR")).sendKeys(((HashMap) supplier.getData()).get("customerNumber").toString());
            driver.findElement(By.id("igtxttb__ctl0_LF_TEL")).sendKeys(((HashMap) supplier.getData()).get("phoneNumber").toString());
            driver.findElement(By.id("igtxttb__ctl0_LF_TELEX")).sendKeys(((HashMap) supplier.getData()).get("email").toString());
//            driver.findElement(By.id("igtxttb__ctl0_LF_SACHB")).sendKeys(((HashMap) supplier.getData()).get("contactFirstName").toString());
            driver.findElement(By.id("igtxttb__ctl0_LF_ZBED")).sendKeys(((HashMap) supplier.getData()).get("paymentTerms").toString());
            driver.findElement(By.id("igtxttb__ctl0_LF_PLZ")).sendKeys(((HashMap) supplier.getData()).get("postalCode").toString());
//            driver.findElement(By.id("igtxttb__ctl0_LF_FAX")).sendKeys(((HashMap) supplier.getData()).get("faxNumber").toString());


            //////////////////////////////////////  Set Address  ///////////////////////////////////////////////////////
            ArrayList<String> handles = new ArrayList<String>(driver.getWindowHandles());
            String windowBefore = handles.get(0);

            driver.findElement(By.id("tb__ctl0_btnEditAddress")).click();

            while (handles.size() == driver.getWindowHandles().size()){
                continue;
            }

            String windowAfter = handles.get(1);

            driver.switchTo().window(windowAfter);

            driver.findElement(By.id("igtxttbStreet")).sendKeys(((HashMap) supplier.getData()).get("address").toString());
            driver.findElement(By.id("igtxttbAddressline1")).sendKeys(((HashMap) supplier.getData()).get("line1").toString());
            driver.findElement(By.id("igtxttbAddressline2")).sendKeys(((HashMap) supplier.getData()).get("line2").toString());

            handles = new ArrayList<>(driver.getWindowHandles());

            driver.findElement(By.id("btnOk")).click();

            while (handles.size() == driver.getWindowHandles().size()){
                continue;
            }
            driver.switchTo().window(windowBefore);

            driver.findElement(By.id("tbtd1")).click();
            driver.findElement(By.id("tb__ctl1_LF_PURCHASEALL")).click();
            driver.findElement(By.id("Save")).click();
            try {
                new WebDriverWait(driver, 60)
                        .ignoring(NoAlertPresentException.class)
                        .until(ExpectedConditions.alertIsPresent());

                Alert al = driver.switchTo().alert();
                al.accept();
            } catch (Exception e) {
                System.out.println(e);
                e.printStackTrace();
            }

        }

        driver.quit();
        return true;
    }

    @RequestMapping("/getSuppliers")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ArrayList<Supplier> getSuppliers() throws SoapFaultException, ComponentException{
        ArrayList<Supplier> suppliers = getSuppliersData();
        ArrayList<SyncJobData> addedSuppliers = saveSuppliersData(suppliers);
        sendSuppliersData(addedSuppliers);

        return suppliers;
    }

}
