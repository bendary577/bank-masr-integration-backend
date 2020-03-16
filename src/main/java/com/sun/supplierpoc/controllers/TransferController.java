package com.sun.supplierpoc.controllers;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.models.configurations.Item;
import com.sun.supplierpoc.models.configurations.ItemGroup;
import com.sun.supplierpoc.models.configurations.MajorGroup;
import com.sun.supplierpoc.models.configurations.OverGroup;
import com.sun.supplierpoc.repositories.AccountRepo;
import com.sun.supplierpoc.repositories.SyncJobDataRepo;
import com.sun.supplierpoc.repositories.SyncJobRepo;
import com.sun.supplierpoc.repositories.SyncJobTypeRepo;
import com.sun.supplierpoc.seleniumMethods.SetupEnvironment;
import com.sun.supplierpoc.services.TransferService;
import com.systemsunion.security.IAuthenticationVoucher;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;


@RestController


public class TransferController {
    @Autowired
    private SyncJobRepo syncJobRepo;
    @Autowired
    private SyncJobTypeRepo syncJobTypeRepo;
    @Autowired
    private AccountRepo accountRepo;
    @Autowired
    private TransferService transferService;
    @Autowired
    private InvoiceController invoiceController;

    private Conversions conversions = new Conversions();
    private SetupEnvironment setupEnvironment = new SetupEnvironment();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @RequestMapping("/getBookedTransfer")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public HashMap<String, Object> getBookedTransferRequest(Principal principal) {
        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        Account account = accountOptional.get();

        HashMap<String, Object> response = getBookedTransfer(user.getId(), account);

        return response;
    }

    public HashMap<String, Object> getBookedTransfer(String userId, Account account) {
        HashMap<String, Object> response = new HashMap<>();

        SyncJobType transferSyncJobType = syncJobTypeRepo.findByNameAndAccountId(Constants.TRANSFERS, account.getId());
        SyncJobType journalSyncJobType = syncJobTypeRepo.findByNameAndAccountId(Constants.CONSUMPTION, account.getId());
        SyncJobType invoiceSyncJobType = syncJobTypeRepo.findByNameAndAccountId(Constants.APPROVED_INVOICES, account.getId());

        HashMap<String, Object> sunConfigResponse = conversions.checkSunDefaultConfiguration(transferSyncJobType);
        if (sunConfigResponse != null){
            return sunConfigResponse;
        }

        if (invoiceSyncJobType.getConfiguration().getCostCenters().size() == 0){
            String message = "Map cost centers before sync transfers.";
            response.put("message", message);
            response.put("success", false);
            return response;
        }

        if (journalSyncJobType.getConfiguration().getItems().size() == 0){
            String message = "Map items before sync transfers.";
            response.put("message", message);
            response.put("success", false);
            return response;
        }

        SyncJob syncJob = new SyncJob(Constants.RUNNING, "", new Date(), null, userId, account.getId(),
                transferSyncJobType.getId());
        syncJobRepo.save(syncJob);

        try {
            HashMap<String, Object> data = transferService.getTransferData(journalSyncJobType, invoiceSyncJobType, account);

            if (data.get("status").equals(Constants.SUCCESS)) {
                ArrayList<HashMap<String, String>> transfers = (ArrayList<HashMap<String, String>>) data.get("transfers");
                if (transfers.size() > 0) {
                    ArrayList<SyncJobData> addedTransfers = transferService.saveTransferSunData(transfers, syncJob);
                    IAuthenticationVoucher voucher = transferService.connectToSunSystem(account);
                    if (voucher != null){
                        invoiceController.handleSendJournal(transferSyncJobType, journalSyncJobType, syncJob, addedTransfers, account, voucher);
                        syncJob.setReason("");
                        syncJob.setEndDate(new Date());
                        syncJobRepo.save(syncJob);

                        response.put("message", "Sync Invoices Successfully.");
                    }

                } else {
                    syncJob.setStatus(Constants.SUCCESS);
                    syncJob.setReason("There is no transfers to get from Oracle Hospitality.");
                    syncJob.setEndDate(new Date());
                    syncJobRepo.save(syncJob);

                    response.put("message", "There is no transfers to get from Oracle Hospitality.");
                    response.put("success", true);

                }
            } else {
                syncJob.setStatus(Constants.FAILED);
                syncJob.setReason("Failed to get transfers from Oracle Hospitality.");
                syncJob.setEndDate(new Date());
                syncJobRepo.save(syncJob);

                response.put("message", data.get("message"));
                response.put("success", false);
            }
            return response;

        } catch (Exception e) {
            syncJob.setStatus(Constants.FAILED);
            syncJob.setReason(e.getMessage());
            syncJob.setEndDate(new Date());
            syncJobRepo.save(syncJob);

            response.put("message", e);
            response.put("success", false);
            return response;
        }
    }

    @RequestMapping("/getOverGroups")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public HashMap<String, Object> getOverGroups(@RequestParam(name = "syncJobType") String syncTypeName, Principal principal) {
        HashMap<String, Object> response = new HashMap<>();

        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        Account account = accountOptional.get();

        SyncJobType syncJobType = syncJobTypeRepo.findByNameAndAccountId(syncTypeName, user.getAccountId());

        ArrayList<OverGroup> oldOverGroups = syncJobType.getConfiguration().getOverGroups();
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
        ArrayList<OverGroup> overGroups = new ArrayList<>();


        try {
            String url = "https://mte03-ohim-prod.hospitality.oracleindustry.com/Webclient/FormLogin.aspx";

            if (!setupEnvironment.loginOHIM(driver, url, account)) {
                driver.quit();

                response.put("status", Constants.FAILED);
                response.put("message", "Invalid username and password.");
                response.put("data", overGroups);
                return response;
            }
            String overGroupsURL = "https://mte03-ohim-prod.hospitality.oracleindustry.com/Webclient/MasterData/OverGroups/OverviewOverGroup.aspx";

            driver.get(overGroupsURL);

            driver.findElement(By.name("filterPanel_btnRefresh")).click();

            List<WebElement> rows = driver.findElements(By.tagName("tr"));

            ArrayList<String> columns = setupEnvironment.getTableColumns(rows, true, 13);

            for (int i = 14; i < rows.size(); i++) {
                OverGroup overGroup = new OverGroup();

                WebElement row = rows.get(i);
                List<WebElement> cols = row.findElements(By.tagName("td"));

                if (cols.size() != columns.size()) {
                    continue;
                }
                // check existence of over group
                WebElement td = cols.get(columns.indexOf("over_group"));
                OverGroup oldOverGroupData = conversions.checkOverGroupExistence(oldOverGroups, td.getText().strip());

                if (oldOverGroupData.getChecked()) {
                    overGroup = oldOverGroupData;
                }
                else {
                    overGroup.setChecked(false);
                    overGroup.setOverGroup(td.getText());
                    overGroup.setProduct("1100");
                    overGroup.setExpensesAccount("511101");
                    overGroup.setInventoryAccount("114101");
                    overGroup.setWasteAccountCredit("511101");
                    overGroup.setWasteAccountDebit("114101");
                }

                overGroups.add(overGroup);
            }

            driver.quit();

            response.put("cols", columns);
            response.put("data", overGroups);
            response.put("message", "Get over groups successfully.");
            response.put("success", true);

            return response;
        } catch (Exception e) {
            e.printStackTrace();
            driver.quit();

            response.put("data", overGroups);
            response.put("message", "Failed to get over groups.");
            response.put("success", false);

            return response;
        }
    }

    @RequestMapping("/mapItems")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public HashMap<String, Object> mapItems(Principal principal) {
        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        Account account = accountOptional.get();

        SyncJobType syncJobType = syncJobTypeRepo.findByNameAndAccountId(Constants.CONSUMPTION, user.getAccountId());

        ArrayList<OverGroup> oldOverGroups = syncJobType.getConfiguration().getOverGroups();
        ArrayList<MajorGroup> majorGroups = new ArrayList<>();
        ArrayList<ItemGroup> itemGroups = new ArrayList<>();
        ArrayList<Item> items = new ArrayList<>();
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

            if (checkLogin(items, driver, response, url, account)) return response;

            String majorGroupsURL = "https://mte03-ohim-prod.hospitality.oracleindustry.com/Webclient/MasterData/MajorGroups/OverviewMajorGroup.aspx";
            driver.get(majorGroupsURL);

            driver.findElement(By.name("filterPanel_btnRefresh")).click();
            List<WebElement> rows = driver.findElements(By.tagName("tr"));

            // Getting MajorGroups
            ArrayList<String> columns = setupEnvironment.getTableColumns(rows, true, 13);
            for (int i = 14; i < rows.size(); i++) {
                MajorGroup majorGroup = new MajorGroup();

                WebElement row = rows.get(i);
                List<WebElement> cols = row.findElements(By.tagName("td"));

                if (cols.size() != columns.size()) {
                    continue;
                }

                majorGroup.setOverGroup(cols.get(columns.indexOf("over_group")).getText());
                majorGroup.setMajorGroup(cols.get(columns.indexOf("major_group")).getText());

                // check if this major group belong to chosen over group
                OverGroup overGroupData = conversions.checkOverGroupExistence(oldOverGroups, majorGroup.getOverGroup());
                if (overGroupData.getChecked()) {
                    majorGroup.setChecked(true);
                    majorGroups.add(majorGroup);
                }
            }

            if (majorGroups.size() == 0) {
                driver.quit();

                response.put("data", items);
                response.put("message", "There is no major groups in over group selected.");
                response.put("success", true);

                return response;
            }
            // save new major groups
            syncJobType.getConfiguration().setMajorGroups(majorGroups);

            // Get Items Group
            String mainMenuURL = "https://mte03-ohim-prod.hospitality.oracleindustry.com/Webclient/Common/Menu/MainMenu.aspx";
            driver.get(mainMenuURL);
            String itemGroupsURL = "https://mte03-ohim-prod.hospitality.oracleindustry.com/Webclient/MasterData/ItemGroups/OverviewItemGroup.aspx";
            driver.get(itemGroupsURL);

            driver.findElement(By.name("filterPanel_btnRefresh")).click();
            rows = driver.findElements(By.tagName("tr"));

            columns = setupEnvironment.getTableColumns(rows, true, 10);
            for (int i = 11; i < rows.size(); i++) {
                ItemGroup itemGroup = new ItemGroup();

                WebElement row = rows.get(i);
                List<WebElement> cols = row.findElements(By.tagName("td"));

                if (cols.size() != columns.size()) {
                    continue;
                }
                itemGroup.setItemGroup(cols.get(columns.indexOf("item_group")).getText());
                itemGroup.setMajorGroup(cols.get(columns.indexOf("major_group")).getText());

                // check if this Item group belong to chosen major group
                MajorGroup majorGroupData = conversions.checkMajorGroupExistence(majorGroups, itemGroup.getMajorGroup());
//                System.out.println(majorGroupData.getOverGroup());

                if (majorGroupData.getChecked()) {
                    itemGroup.setChecked(true);
                    itemGroup.setOverGroup(majorGroupData.getOverGroup());
                    itemGroups.add(itemGroup);
                }
            }

            if (itemGroups.size() == 0) {
                driver.quit();

                response.put("data", items);
                response.put("message", "There is no item groups in over group selected.");
                response.put("success", true);

                return response;
            }
            // save new Item groups
            syncJobType.getConfiguration().setItemGroups(itemGroups);

            // Get Items Group
            driver.get(mainMenuURL);
            String itemsURL = "https://mte03-ohim-prod.hospitality.oracleindustry.com/Webclient/MasterData/Items/OverviewItem.aspx";
            driver.get(itemsURL);

            driver.findElement(By.name("filterPanel_btnRefresh")).click();
            rows = driver.findElements(By.tagName("tr"));
            columns = setupEnvironment.getTableColumns(rows, true, 17);

            while (true){
                for (int i = 11; i < rows.size(); i++) {
                    Item item = new Item();

                    WebElement row = rows.get(i);
                    List<WebElement> cols = row.findElements(By.tagName("td"));

                    if (cols.size() != columns.size()) {
                        continue;
                    }

                    item.setItem(cols.get(columns.indexOf("item")).getText());
                    String itemGroupText = cols.get(columns.indexOf("item_group")).getText();
                    ItemGroup itemGroup = conversions.checkItemGroupExistence(itemGroups, itemGroupText);
                    item.setItemGroup(itemGroupText);
                    item.setMajorGroup(itemGroup.getMajorGroup());
                    item.setOverGroup(itemGroup.getOverGroup());
                    item.setChecked(true);

                    items.add(item);
                }
                // check if there is other pages
                if (driver.findElements(By.linkText("Next")).size() == 0){
                    break;
                }
                else {
                    boolean paginationFlag = TransferService.checkPagination(driver, "dg_rc_0_2");
                    if (!paginationFlag){
                        driver.quit();

                        response.put("data", items);
                        response.put("message", "Failed to map over groups to items.");
                        response.put("success", false);

                        return response;
                    }
                    rows = driver.findElements(By.tagName("tr"));
                }
            }

            // save new items
            syncJobType.getConfiguration().setItems(items);
            syncJobTypeRepo.save(syncJobType);

            driver.quit();

            response.put("cols", columns);
            response.put("data", items);
            response.put("message", "Get map over groups to items successfully.");
            response.put("success", true);

            return response;
        } catch (Exception e) {
            e.printStackTrace();
            driver.quit();

            response.put("data", items);
            response.put("message", "Failed to map over groups to items.");
            response.put("success", false);

            return response;
        }
    }

    private boolean checkLogin(ArrayList<Item> items, WebDriver driver, HashMap<String, Object> response,
                               String url, Account account) {
        if (!setupEnvironment.loginOHIM(driver, url, account)) {
            driver.quit();

            response.put("status", Constants.FAILED);
            response.put("message", "Invalid username and password.");
            response.put("data", items);
            return true;
        }
        return false;
    }



}
