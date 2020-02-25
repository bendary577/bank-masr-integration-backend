package com.sun.supplierpoc.controllers;
import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.models.SyncJob;
import com.sun.supplierpoc.models.SyncJobData;
import com.sun.supplierpoc.models.SyncJobType;
import com.sun.supplierpoc.repositories.SyncJobRepo;
import com.sun.supplierpoc.repositories.SyncJobTypeRepo;
import com.sun.supplierpoc.seleniumMethods.SetupEnvironment;
import com.sun.supplierpoc.services.SupplierService;
import com.sun.supplierpoc.soapModels.SSC;
import com.sun.supplierpoc.soapModels.Supplier;
import com.systemsunion.security.IAuthenticationVoucher;
import com.systemsunion.ssc.client.*;;
import org.openqa.selenium.*;
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
import java.util.List;


@RestController
public class SupplierController {

    @Autowired
    private SyncJobRepo syncJobRepo;
    @Autowired
    private SyncJobTypeRepo syncJobTypeRepo;
    @Autowired
    public SupplierService supplierService;

    public Conversions conversions = new Conversions();
    public SetupEnvironment setupEnvironment = new SetupEnvironment();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @RequestMapping("/getSuppliers")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public HashMap<String, Object> getSuppliers() throws SoapFaultException, ComponentException{
        Boolean addFlag = false;
        HashMap<String, Object> response = new HashMap<>();

        SyncJobType syncJobType = syncJobTypeRepo.findByNameAndAccountId("Suppliers", "1");

        SyncJob syncJob = new SyncJob(Constants.RUNNING, "",  new Date(), null, "1", "1",
                syncJobType.getId());
        syncJobRepo.save(syncJob);

        HashMap<String, Object> data = supplierService.getSuppliersData();

        if (data.get("status").equals(Constants.SUCCESS)) {
            ArrayList<Supplier> suppliers = (ArrayList<Supplier>) data.get("suppliers");

            if (suppliers.size() > 0){
                ArrayList<SyncJobData> addedSuppliers = supplierService.saveSuppliersData(suppliers, syncJob);

                if (addedSuppliers.size() != 0){
                    data  = supplierService.sendSuppliersData(addedSuppliers, syncJob, syncJobType);
                    if (data.get("status").equals(Constants.SUCCESS)){
                        syncJob.setStatus(Constants.SUCCESS);
                        syncJob.setReason("");
                        syncJob.setEndDate(new Date());
                        syncJobRepo.save(syncJob);

                        response.put("message", data.get("message"));
                        response.put("success", true);

                        return response;
                    }
                    else {
                        syncJob.setStatus(Constants.FAILED);
                        syncJob.setReason((String) data.get("message"));
                        syncJob.setEndDate(new Date());
                        syncJobRepo.save(syncJob);

                        response.put("message", data.get("message"));
                        response.put("success", false);

                        return response;
                    }
                }
                else {
                    syncJob.setStatus(Constants.SUCCESS);
                    syncJob.setReason("No new suppliers to add.");
                    syncJob.setEndDate(new Date());
                    syncJobRepo.save(syncJob);

                    response.put("message", "No new suppliers to add.");
                    response.put("success", true);

                    return response;
                }
            }
            else {
                syncJob.setStatus(Constants.SUCCESS);
                syncJob.setReason("There is no suppliers to get from Sun System.");
                syncJob.setEndDate(new Date());
                syncJobRepo.save(syncJob);

                response.put("message", "There is no suppliers to get from Sun System.");
                response.put("success", true);

                return response;
            }

        }
        else {
            syncJob.setStatus("Failed to get suppliers from Sun System");
            syncJob.setReason("Failed to sync suppliers.");
            syncJob.setEndDate(new Date());
            syncJobRepo.save(syncJob);

            response.put("message", "Failed to sync suppliers.");
            response.put("success", false);
            return response;
        }
    }


    @RequestMapping("/getSupplierTaxes")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public HashMap<String, Object> getSupplierTaxes(){

        WebDriver driver = setupEnvironment.setupSeleniumEnv(true);
        HashMap<String, Object> response = new HashMap<>();
        ArrayList<HashMap<String, Object>> taxes = new ArrayList<>();


        try {
            String url = "https://mte03-ohim-prod.hospitality.oracleindustry.com/Webclient/FormLogin.aspx";

            if (!setupEnvironment.loginOHIM(driver, url)){
                driver.quit();

                response.put("status", Constants.FAILED);
                response.put("message", "Invalid username and password.");
                response.put("journals", taxes);
                return response;
            }

            String taxesUrl = "https://mte03-ohim-prod.hospitality.oracleindustry.com/Webclient/MasterData/Taxes/OverviewTax.aspx";
            driver.get(taxesUrl);

            driver.findElement(By.name("filterPanel_btnRefresh")).click();

            List<WebElement> rows = driver.findElements(By.tagName("tr"));

            ArrayList<String> columns = setupEnvironment.getTableColumns(rows, true, 7);

            for (int i = 8; i < rows.size(); i++) {
                HashMap<String, Object> tax = new HashMap<>();

                WebElement row = rows.get(i);
                List<WebElement>  cols = row.findElements(By.tagName("td"));

                for (int j = 0; j < cols.size(); j++) {
                    tax.put(columns.get(j), cols.get(j).getText());
                }
                taxes.add(tax);
            }

            driver.quit();

            response.put("cols", columns);
            response.put("data", taxes);
            response.put("message", "Get supplier taxes successfully.");
            response.put("success", true);

            return response;
        } catch (Exception e) {
            e.printStackTrace();
            driver.quit();

            response.put("data", taxes);
            response.put("message", "Failed to get supplier taxes.");
            response.put("success", false);

            return response;
        }

    }

    @RequestMapping("/getSupplierGroups")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public HashMap<String, Object> getSupplierGroups(){

        WebDriver driver = setupEnvironment.setupSeleniumEnv(true);
        HashMap<String, Object> response = new HashMap<>();
        ArrayList<HashMap<String, Object>> groups = new ArrayList<>();


        try {
            String url = "https://mte03-ohim-prod.hospitality.oracleindustry.com/Webclient/FormLogin.aspx";

            if (!setupEnvironment.loginOHIM(driver, url)){
                driver.quit();

                response.put("status", Constants.FAILED);
                response.put("message", "Invalid username and password.");
                response.put("journals", groups);
                return response;
            }

            String groupsUrl = "https://mte03-ohim-prod.hospitality.oracleindustry.com/Webclient/MasterData/VendorGroups/VendorGroupsOverview.aspx";
            driver.get(groupsUrl);

            driver.findElement(By.name("filterPanel_btnRefresh")).click();

            List<WebElement> rows = driver.findElements(By.tagName("tr"));

            ArrayList<String> columns = setupEnvironment.getTableColumns(rows, true, 7);

            for (int i = 8; i < rows.size(); i++) {
                HashMap<String, Object> group = new HashMap<>();

                WebElement row = rows.get(i);
                List<WebElement> cols = row.findElements(By.tagName("td"));

                for (int j = 0; j < cols.size(); j++) {
                    group.put(columns.get(j), cols.get(j).getText());
                }
                groups.add(group);
            }

            driver.quit();

            response.put("cols", columns);
            response.put("data", groups);
            response.put("message", "Get supplier groups successfully.");
            response.put("success", true);

            return response;
        } catch (Exception e) {
            e.printStackTrace();
            driver.quit();

            response.put("data", groups);
            response.put("message", "Failed to get supplier taxes.");
            response.put("success", false);

            return response;
        }

    }

}
