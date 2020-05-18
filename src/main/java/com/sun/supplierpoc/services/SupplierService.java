package com.sun.supplierpoc.services;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.controllers.SyncJobDataController;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.SyncJob;
import com.sun.supplierpoc.models.SyncJobData;
import com.sun.supplierpoc.models.SyncJobType;
import com.sun.supplierpoc.models.configurations.AccountCredential;
import com.sun.supplierpoc.models.data.Data;
import com.sun.supplierpoc.repositories.SyncJobDataRepo;
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

@Service
public class SupplierService {
    @Autowired
    private SyncJobDataRepo syncJobDataRepo;
    @Autowired
    private SyncJobDataController syncJobTypeController;

    public SetupEnvironment setupEnvironment = new SetupEnvironment();
    public Conversions conversions = new Conversions();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public HashMap<String, Object> getSuppliersData(SyncJobType syncJobType, Account account){
        HashMap<String, Object> data = new HashMap<>();
        boolean useEncryption = false;

        ArrayList<AccountCredential> accountCredentials = account.getAccountCredentials();
        AccountCredential sunCredentials = account.getAccountCredentialByAccount(Constants.SUN, accountCredentials);

        String username = sunCredentials.getUsername();
        String password = sunCredentials.getPassword();

        IAuthenticationVoucher voucher;
        String sccXMLStringValue = "";

        try {
            SecurityProvider securityProvider = new SecurityProvider(Constants.HOST, useEncryption);
            voucher = securityProvider.Authenticate(username, password);
        } catch (ComponentException | SoapFaultException e) {
            data.put("status", Constants.FAILED);
            data.put("message", "Failed to connect to sun system.");
            data.put("suppliers", new ArrayList<> ());
            return data;
        }

        SoapComponent component = null;
        if (useEncryption) {
            component = new SecureSoapComponent(Constants.HOST, Constants.PORT);
        } else {
            component = new SoapComponent(Constants.HOST, Constants.PORT);
        }
        component.authenticate(voucher);

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

            ///////////////////////////////////////////  Payload ///////////////////////////////////////////////////////
            Element payloadElement = doc.createElement("Payload");
            SSCRootElement.appendChild(payloadElement);

            ///////////////////////////////////////////  Select ////////////////////////////////////////////////////////
            Element selectElement = doc.createElement("Select");
            payloadElement.appendChild(selectElement);

            ///////////////////////////////////////////  Supplier //////////////////////////////////////////////////////

            Element supplierElement = doc.createElement("Supplier");
            selectElement.appendChild(supplierElement);

            Element accountCodeElement = doc.createElement("AccountCode");
            supplierElement.appendChild(accountCodeElement);

            Element supplierCodeElement = doc.createElement("SupplierCode");
            supplierElement.appendChild(supplierCodeElement);

            Element supplierNameElement = doc.createElement("SupplierName");
            supplierElement.appendChild(supplierNameElement);

            Element statusElement = doc.createElement("Status");
            supplierElement.appendChild(statusElement);

            Element eMailAddressElement = doc.createElement("EMailAddress");
            supplierElement.appendChild(eMailAddressElement);

            Element paymentTermsGroupCodeElement = doc.createElement("PaymentTermsGroupCode");
            supplierElement.appendChild(paymentTermsGroupCodeElement);

            ///////////////////////////////////////////  Address Contact //////////////////////////////////////////////

            Element addressContactElement = doc.createElement("Address_Contact");
            supplierElement.appendChild(addressContactElement);

            Element contactIdentifierElement = doc.createElement("ContactIdentifier");
            addressContactElement.appendChild(contactIdentifierElement);

            ///////////////////////////////////////////  Supplier Address //////////////////////////////////////////////

            Element supplierAddressElement = doc.createElement("SupplierAddress");
            supplierElement.appendChild(supplierAddressElement);

            Element telephoneNumberElement = doc.createElement("TelephoneNumber");
            supplierAddressElement.appendChild(telephoneNumberElement);

            Element addressCodeElement = doc.createElement("AddressCode");
            supplierAddressElement.appendChild(addressCodeElement);

            Element addressLine1Element = doc.createElement("AddressLine1");
            supplierAddressElement.appendChild(addressLine1Element);

            Element addressLine2Element = doc.createElement("AddressLine2");
            supplierAddressElement.appendChild(addressLine2Element);

            Element addressLine3Element = doc.createElement("AddressLine3");
            supplierAddressElement.appendChild(addressLine3Element);

            Element postalCodeElement = doc.createElement("PostalCode");
            supplierAddressElement.appendChild(postalCodeElement);

            ///////////////////////////////////////////  Transform Document to XML String //////////////////////////////
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            sccXMLStringValue = writer.getBuffer().toString();
        } catch (ParserConfigurationException | TransformerException e) {
            e.printStackTrace();
        }
        String strOut = "";

        try {
            strOut = component.execute("Supplier", "Query", sccXMLStringValue);
        } catch (ComponentException | SoapFaultException e) {
            data.put("status", Constants.FAILED);
            data.put("message", e.getMessage());
            data.put("suppliers", new ArrayList<> ());
            return data;
        }

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
            data.put("status", Constants.FAILED);
            data.put("message", e.getMessage());
            data.put("suppliers", new ArrayList<> ());
            return data;
        }
    }

    public ArrayList<SyncJobData> saveSuppliersData(ArrayList<Supplier> suppliers, SyncJob syncJob, SyncJobType syncJobType) {
        ArrayList<SyncJobData> addedSuppliers = new ArrayList<>();
        ArrayList<SyncJobData> savedSuppliers = syncJobTypeController.getSyncJobData(syncJobType.getId());

        for (Supplier supplier : suppliers) {
            HashMap<String, String> data = new HashMap<>();

            data.put("supplierId", "");
            data.put("supplier", supplier.getSupplierName());
            data.put("supplierNumber", supplier.getSupplierCode());
            data.put("accountCode", supplier.getAccountCode());
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

            // check existence of supplier in middleware (UNIQUE: supplierNumber)
            SyncJobData oldSupplier = conversions.checkSupplierExistence(savedSuppliers, supplier.getSupplierCode());
            if (oldSupplier != null){
                oldSupplier.setData(data);
                syncJobDataRepo.save(oldSupplier);
                if (!oldSupplier.getStatus().equals(Constants.FAILED)){
                    continue;
                }
//                else {
//                    addedSuppliers.add(oldSupplier);
//                }
            }

            SyncJobData syncJobData = new SyncJobData(data, Constants.RECEIVED, "", new Date(),
                    syncJob.getId());
            syncJobDataRepo.save(syncJobData);
            addedSuppliers.add(syncJobData);
        }
        return addedSuppliers;
    }

    public HashMap<String, Object> sendSuppliersData(ArrayList<SyncJobData> suppliers, SyncJob syncJob,
                                                     SyncJobType syncJobType, Account account){
        HashMap<String, Object> response = new HashMap<>();

        WebDriver driver;
        try{
            driver = setupEnvironment.setupSeleniumEnv(false);
        }
        catch (Exception ex){
            response.put("status", Constants.FAILED);
            response.put("message", "Failed to establish connection with firefox driver.");
            response.put("invoices", new ArrayList<>());
            return response;
        }

        try {
            String url = "https://mte03-ohim-prod.hospitality.oracleindustry.com/Webclient/FormLogin.aspx";
            driver.get(url);

            if (!setupEnvironment.loginOHIM(driver, url, account)){
                driver.quit();

                response.put("status", Constants.FAILED);
                response.put("message", "Invalid username and password.");
                return response;
            }

            for (SyncJobData supplier : suppliers) {
                String vendorPage = "https://mte03-ohim-prod.hospitality.oracleindustry.com/Webclient/MasterData/Vendors/OverviewVendor.aspx";
                driver.get(vendorPage);
                driver.findElement(By.linkText("New")).click();

                // wait to make sure elements exits
                WebDriverWait wait = new WebDriverWait(driver, 10);
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("igtxtLF_NAME")));
                driver.findElement(By.id("igtxtLF_NAME")).sendKeys(((HashMap) supplier.getData()).get("supplier").toString());

                wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("igtxttb__ctl0_LF_KONR")));
                driver.findElement(By.id("igtxttb__ctl0_LF_KONR")).sendKeys(((HashMap) supplier.getData()).get("supplierNumber").toString());

                //////////////////////////////////////  Set Hidden Elements  ///////////////////////////////////////////
                JavascriptExecutor js = (JavascriptExecutor) driver;
                js.executeScript("document.getElementById('tb__ctl0_cfTaxes_Value').setAttribute('type','text')");
                js.executeScript("document.getElementById('tb__ctl0_cfTaxes_Value').style.display = 'block';");

                String tax = syncJobType.getConfiguration().getTaxes();
                driver.findElement(By.id("tb__ctl0_cfTaxes_Text")).sendKeys(tax);
                Thread.sleep(2000);
                driver.findElement(By.id("tb__ctl0_cfTaxes_Text")).sendKeys(Keys.ARROW_DOWN);
                driver.findElement(By.id("tb__ctl0_cfTaxes_Text")).sendKeys(Keys.ENTER);

                try {
                    wait = new WebDriverWait(driver, 20);
                    WebElement taxValue = driver.findElement(By.id("tb__ctl0_cfTaxes_Value"));
                    wait.until(ExpectedConditions.textToBePresentInElementValue(taxValue, tax));
                } catch (Exception e) {
                    String message = "Time out our while setting supplier tax";
                    supplier.setStatus(Constants.FAILED);
                    supplier.setReason(message);
                    supplier.setSyncJobId(syncJob.getId());
                    syncJobDataRepo.save(supplier);
                    continue;
                }

                js.executeScript("document.getElementById('tb__ctl0_cfVendorGroup_Value').setAttribute('type','text')");
                js.executeScript("document.getElementById('tb__ctl0_cfVendorGroup_Value').style.display = 'block';");

                String group = (String) syncJobType.getConfiguration().getGroups();
                driver.findElement(By.id("tb__ctl0_cfVendorGroup_Text")).sendKeys(group);
                Thread.sleep(2000);
                driver.findElement(By.id("tb__ctl0_cfVendorGroup_Text")).sendKeys(Keys.ARROW_DOWN);
                driver.findElement(By.id("tb__ctl0_cfVendorGroup_Text")).sendKeys(Keys.ENTER);

                try {
                    wait = new WebDriverWait(driver, 20);
                    WebElement groupValue = driver.findElement(By.id("tb__ctl0_cfVendorGroup_Value"));
                    wait.until(ExpectedConditions.textToBePresentInElementValue(groupValue, group));
                } catch (Exception e) {
                    String message = "Time out our while setting supplier group";
                    supplier.setStatus(Constants.FAILED);
                    supplier.setReason(message);
                    supplier.setSyncJobId(syncJob.getId());
                    syncJobDataRepo.save(supplier);
                    continue;
                }

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

                wait = new WebDriverWait(driver, 20);
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("igtxttbStreet")));

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
//
//                driver.findElement(By.linkText("Save")).click();
//                try {
//                    new WebDriverWait(driver, 5)
//                            .ignoring(NoAlertPresentException.class)
//                            .until(ExpectedConditions.alertIsPresent());
//
//                    Alert al = driver.switchTo().alert();
//                    al.accept();
//
//                    supplier.setStatus(Constants.FAILED);
//                    supplier.setReason("Already Exits");
//                    supplier.setSyncJobId(syncJob.getId());
//                    syncJobDataRepo.save(supplier);
//                    continue;
//                } catch (NoAlertPresentException Ex) {
//                    System.out.println("No alert");
//                }

                supplier.setStatus(Constants.SUCCESS);
                supplier.setReason("");
                supplier.setSyncJobId(syncJob.getId());
                syncJobDataRepo.save(supplier);
            }

            driver.quit();

            response.put("status", Constants.SUCCESS);
            response.put("message", "Save Suppliers Successfully.");
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            driver.quit();

            response.put("status", Constants.FAILED);
            response.put("message", e.getMessage());
            return response;
        }
    }
}
