package com.sun.supplierpoc.controllers;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.repositories.AccountRepo;
import com.sun.supplierpoc.repositories.GeneralSettingsRepo;
import com.sun.supplierpoc.repositories.SyncJobRepo;
import com.sun.supplierpoc.repositories.SyncJobTypeRepo;
import com.sun.supplierpoc.seleniumMethods.SetupEnvironment;
import com.sun.supplierpoc.services.ImageService;
import com.sun.supplierpoc.services.SupplierService;
import com.sun.supplierpoc.soapModels.Supplier;
import com.systemsunion.ssc.client.ComponentException;
import com.systemsunion.ssc.client.SoapFaultException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.*;


@RestController
public class SupplierController {

    @Autowired
    private SyncJobRepo syncJobRepo;
    @Autowired
    private SyncJobTypeRepo syncJobTypeRepo;
    @Autowired
    private GeneralSettingsRepo generalSettingsRepo;
    @Autowired
    private AccountRepo accountRepo;
    @Autowired
    private SupplierService supplierService;

    public Conversions conversions = new Conversions();
    private SetupEnvironment setupEnvironment = new SetupEnvironment();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @GetMapping("/test/hello")
    public String sayHello() {
        return "Hello World!";
    }

    @RequestMapping("/getSuppliers")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity<HashMap<String, Object>> getSuppliersRequest(Principal principal) throws SoapFaultException, ComponentException{
        HashMap<String, Object> response = new HashMap<>();

        User user = (User)((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();

        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());

        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();
            response = getSuppliers(user.getId(), account);
            if(response.get("success").equals(false)){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }else {
                return ResponseEntity.status(HttpStatus.OK).body(response);
            }
        }
        String message = "Invalid Credentials";
        response.put("message", message);
        response.put("success", false);

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    public HashMap<String, Object> getSuppliers(String userId, Account account) throws SoapFaultException, ComponentException{
        HashMap<String, Object> response = new HashMap<>();
        SyncJobType supplierSyncJobType = syncJobTypeRepo.findByNameAndAccountIdAndDeleted(Constants.SUPPLIERS, account.getId(), false);

        if (supplierSyncJobType == null){
            String message = "You don't have the role to sync suppliers";
            response.put("message", message);
            response.put("success", false);
            return response;
        }

        if (supplierSyncJobType.getConfiguration().inforConfiguration.businessUnit.equals("")){
            String message = "Configure business unit before sync suppliers.";
            response.put("message", message);
            response.put("success", false);
            return response;
        }

        if (supplierSyncJobType.getConfiguration().supplierConfiguration.groups.equals("")){
            String message = "Configure supplier group before sync suppliers.";
            response.put("message", message);
            response.put("success", false);
            return response;
        }

        if (supplierSyncJobType.getConfiguration().supplierConfiguration.vendorTaxes.equals("")){
            String message = "Configure supplier tax before sync suppliers.";
            response.put("message", message);
            response.put("success", false);
            return response;
        }

        SyncJob syncJob = new SyncJob(Constants.RUNNING, "",  new Date(), null, userId,
                account.getId(), supplierSyncJobType.getId(), 0);
        syncJobRepo.save(syncJob);

        Response supplierResponse = new Response();

        try {

            HashMap<String, Object> data = supplierService.getSuppliersData(supplierSyncJobType, account);

            if (data.get("status").equals(Constants.SUCCESS)) {
                ArrayList<Supplier> suppliers = (ArrayList<Supplier>) data.get("suppliers");

                if (suppliers.size() > 0){
                    supplierResponse = supplierService.saveSuppliersData(suppliers, syncJob, supplierSyncJobType);

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

                    if (supplierResponse.getAddedSuppliers().size() != 0){
                        boolean openDriverFlag = true;
                        boolean closeDriverFlag = true;

                        if (supplierResponse.getUpdatedSuppliers().size() > 0){
                            closeDriverFlag = false;
                        }

                        data  = supplierService.sendSuppliersData(supplierResponse.getAddedSuppliers(), syncJob,
                                supplierSyncJobType, account, true, closeDriverFlag, openDriverFlag,
                                driver);
                        if (data.get("status").equals(Constants.SUCCESS)){
                            // check if there is suppliers need update
                            ArrayList<SyncJobData> updatedSuppliers = (ArrayList<SyncJobData>) data.get("updatedSuppliers");
                            supplierResponse.getUpdatedSuppliers().addAll(updatedSuppliers);
                            if (supplierResponse.getUpdatedSuppliers().size() > 0){
                                data  = supplierService.sendSuppliersData(supplierResponse.getUpdatedSuppliers(), syncJob,
                                        supplierSyncJobType, account, false, true,
                                        false, driver);

                                if (data.get("status").equals(Constants.SUCCESS)){
                                    syncJob.setStatus(Constants.SUCCESS);
                                    syncJob.setReason("");
                                    syncJob.setEndDate(new Date());
                                    syncJob.setRowsFetched(supplierResponse.getAddedSuppliers().size());
                                    syncJobRepo.save(syncJob);

                                    response.put("message", data.get("message"));
                                    response.put("success", true);
                                }else{
                                    syncJob.setStatus(Constants.FAILED);
                                    syncJob.setReason((String) data.get("message"));
                                    syncJob.setEndDate(new Date());
                                    syncJob.setRowsFetched(supplierResponse.getAddedSuppliers().size());
                                    syncJobRepo.save(syncJob);

                                    response.put("message", data.get("message"));
                                    response.put("success", false);
                                }
                            }
                            else{
                                syncJob.setStatus(Constants.SUCCESS);
                                syncJob.setReason("Sync suppliers to Oracle hospitality successfully.");
                                syncJob.setEndDate(new Date());
                                syncJob.setRowsFetched(supplierResponse.getAddedSuppliers().size());
                                syncJobRepo.save(syncJob);

                                response.put("message", data.get("message"));
                                response.put("success", true);
                            }
                        }
                        else {
                            syncJob.setStatus(Constants.FAILED);
                            syncJob.setReason((String) data.get("message"));
                            syncJob.setEndDate(new Date());
                            syncJob.setRowsFetched(supplierResponse.getAddedSuppliers().size());
                            syncJobRepo.save(syncJob);

                            response.put("message", data.get("message"));
                            response.put("success", false);

                        }
                    }
                    else {
                        if (supplierResponse.getUpdatedSuppliers().size() > 0){
                            data  = supplierService.sendSuppliersData(supplierResponse.getUpdatedSuppliers(), syncJob,
                                    supplierSyncJobType, account, false, true,
                                    true, driver);

                            if (data.get("status").equals(Constants.SUCCESS)){
                                syncJob.setStatus(Constants.SUCCESS);
                                syncJob.setReason("");
                                syncJob.setEndDate(new Date());
                                syncJob.setRowsFetched(supplierResponse.getAddedSuppliers().size());
                                syncJobRepo.save(syncJob);

                                response.put("message", data.get("message"));
                                response.put("success", true);
                            }else{
                                syncJob.setStatus(Constants.FAILED);
                                syncJob.setReason((String) data.get("message"));
                                syncJob.setEndDate(new Date());
                                syncJob.setRowsFetched(supplierResponse.getAddedSuppliers().size());
                                syncJobRepo.save(syncJob);

                                response.put("message", data.get("message"));
                                response.put("success", false);
                            }
                        }

                        else {
                            syncJob.setStatus(Constants.SUCCESS);
                            syncJob.setReason("No new suppliers to add in middleware.");
                            syncJob.setEndDate(new Date());
                            syncJob.setRowsFetched(supplierResponse.getAddedSuppliers().size());
                            syncJobRepo.save(syncJob);

                            response.put("message", "No new suppliers to add in middleware.");
                            response.put("success", true);
                        }
                    }
                    try{
                        driver.quit();
                    }
                    catch (Exception ex){
                        System.out.println("Already Closed!!");
                    }
                }
                else {
                    syncJob.setStatus(Constants.SUCCESS);
                    syncJob.setReason("There is no suppliers to get from Sun System.");
                    syncJob.setEndDate(new Date());
                    syncJob.setRowsFetched(supplierResponse.getAddedSuppliers().size());
                    syncJobRepo.save(syncJob);

                    response.put("message", "There is no suppliers to get from Sun System.");
                    response.put("success", true);
                }
                return response;

            }
            else {
                syncJob.setStatus(Constants.FAILED);
                syncJob.setReason((String) data.get("message"));
                syncJob.setEndDate(new Date());
                syncJob.setRowsFetched(supplierResponse.getAddedSuppliers().size());
                syncJobRepo.save(syncJob);

                response.put("message", "Failed to get suppliers from Sun System.");
                response.put("success", false);
                return response;
            }
        }catch (Exception e) {
            syncJob.setStatus(Constants.FAILED);
            syncJob.setReason(e.getMessage());
            syncJob.setEndDate(new Date());
            syncJob.setRowsFetched(supplierResponse.getAddedSuppliers().size());
            syncJobRepo.save(syncJob);

            response.put("message", e);
            response.put("success", false);
            return response;
        }
    }

    @RequestMapping("/getSupplierTaxes")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public HashMap<String, Object> getSupplierTaxes(Principal principal){
        User user = (User)((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();

        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        Account account = accountOptional.get();
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

        ArrayList<HashMap<String, Object>> taxes = new ArrayList<>();

        try {
            if (!setupEnvironment.loginOHIM(driver, Constants.OHIM_LOGIN_LINK, account)){
                driver.quit();

                response.put("status", Constants.FAILED);
                response.put("message", "Invalid username and password.");
                response.put("data", taxes);
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

                if (cols.size() != columns.size()){
                    continue;
                }
                for (int j = 0; j < cols.size(); j++) {
                    tax.put(columns.get(j), cols.get(j).getText().strip());
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
    public HashMap<String, Object> getSupplierGroups(Principal principal){
        User user = (User)((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();

        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        Account account = accountOptional.get();
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
        ArrayList<HashMap<String, Object>> groups = new ArrayList<>();


        try {
            if (!setupEnvironment.loginOHIM(driver, Constants.OHIM_LOGIN_LINK, account)){
                driver.quit();

                response.put("status", Constants.FAILED);
                response.put("message", "Invalid username and password.");
                response.put("journals", groups);
                return response;
            }

            driver.get(Constants.SUPPLIER_GROUPS_URL);

            driver.findElement(By.name("filterPanel_btnRefresh")).click();

            List<WebElement> rows = driver.findElements(By.tagName("tr"));

            ArrayList<String> columns = setupEnvironment.getTableColumns(rows, true, 7);

            for (int i = 8; i < rows.size(); i++) {
                HashMap<String, Object> group = new HashMap<>();

                WebElement row = rows.get(i);
                List<WebElement> cols = row.findElements(By.tagName("td"));

                if (cols.size() != columns.size()){
                    continue;
                }

                for (int j = 0; j < cols.size(); j++) {
                    group.put(columns.get(j), cols.get(j).getText().strip());
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


    @RequestMapping("/getVendors")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity getVendors(Principal principal){
        String supplierName;
        User user = (User)((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();

        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        Account account = accountOptional.get();
        ArrayList<Supplier> suppliers = new ArrayList<>();
        GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(user.getAccountId(), false);
        if (generalSettings != null){
            suppliers = generalSettings.getSuppliers();
        }

        HashMap<String, Object> response = new HashMap<>();

        WebDriver driver;
        try{
            driver = setupEnvironment.setupSeleniumEnv(false);
        }
        catch (Exception ex){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to establish connection with firefox driver.");
        }

        try {
            if (!setupEnvironment.loginOHIM(driver, Constants.OHIM_LOGIN_LINK, account)){
                driver.quit();
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid username and password.");
            }

            driver.get(Constants.SUPPLIER_URL);

            driver.findElement(By.name("filterPanel_btnRefresh")).click();

            List<WebElement> rows = driver.findElements(By.tagName("tr"));

            ArrayList<String> columns = setupEnvironment.getTableColumns(rows, true, 7);

            Supplier supplier;
            for (int i = 12; i < rows.size(); i++) {
                WebElement row = rows.get(i);
                List<WebElement> cols = row.findElements(By.tagName("td"));

                if (cols.size() != 15){
                    continue;
                }
                supplierName = cols.get(columns.indexOf("vendor")).getText().strip();

                supplier = conversions.checkSupplierExistence(suppliers, supplierName);
                if(supplier != null)
                    continue;

                supplier = new Supplier();
                supplier.setSupplierName(supplierName);
                suppliers.add(supplier);
            }

            driver.quit();

            // Save new suppliers
            generalSettingsRepo.save(generalSettings);
            return ResponseEntity.status(HttpStatus.OK).body(suppliers);
        } catch (Exception e) {
            e.printStackTrace();
            driver.quit();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to fetch new suppliers.");
        }

    }

}
