package com.sun.supplierpoc.controllers;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.models.SyncJob;
import com.sun.supplierpoc.models.SyncJobData;
import com.sun.supplierpoc.models.SyncJobType;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.repositories.SyncJobDataRepo;
import com.sun.supplierpoc.repositories.SyncJobRepo;
import com.sun.supplierpoc.repositories.SyncJobTypeRepo;
import com.sun.supplierpoc.seleniumMethods.SetupEnvironment;
import com.sun.supplierpoc.services.TransferService;
import com.systemsunion.ssc.client.ComponentException;
import com.systemsunion.ssc.client.SoapFaultException;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.Key;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


@RestController

public class TransferController {
    @Autowired
    private SyncJobRepo syncJobRepo;
    @Autowired
    private SyncJobTypeRepo syncJobTypeRepo;
    @Autowired
    private SyncJobDataRepo syncJobDataRepo;
    @Autowired
    private TransferService transferService;

    private Conversions conversions = new Conversions();
    private SetupEnvironment setupEnvironment = new SetupEnvironment();


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @RequestMapping("/getBookedTransfer")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public HashMap<String, Object> getBookedTransfer(Principal principal) {
        HashMap<String, Object> response = new HashMap<>();

        try {
            User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();

            SyncJobType syncJobType = syncJobTypeRepo.findByNameAndAccountId("Booked Transfers", user.getAccountId());
            SyncJobType syncJobTypeJournal = syncJobTypeRepo.findByNameAndAccountId("Journals", user.getAccountId());

            SyncJob syncJob = new SyncJob(Constants.RUNNING, "", new Date(), null, user.getId(),
                    user.getAccountId(), syncJobType.getId());

            syncJobRepo.save(syncJob);

            HashMap<String, Object> data = transferService.getTransferData(syncJobTypeJournal);

            if (data.get("status").equals(Constants.SUCCESS)) {
                ArrayList<HashMap<String, String>> transfers = (ArrayList<HashMap<String, String>>) data.get("transfers");
                if (transfers.size() > 0) {
                    ArrayList<SyncJobData> addedTransfers = transferService.saveTransferSunData(transfers, syncJob);
                    if (addedTransfers.size() != 0) {
                        for (SyncJobData addedTransfer : addedTransfers) {
                            try {
                                transferService.sendTransferData(addedTransfer, syncJobType);

                            } catch (SoapFaultException | ComponentException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    syncJob.setStatus(Constants.SUCCESS);
                    syncJob.setEndDate(new Date());
                    syncJobRepo.save(syncJob);

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
            response.put("message", e);
            response.put("success", false);
            return response;
        }
    }

    @RequestMapping("/getOverGroups")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public HashMap<String, Object> getOverGroups(Principal principal) {
        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        SyncJobType syncJobType = syncJobTypeRepo.findByNameAndAccountId("Journals", user.getAccountId());

        ArrayList<HashMap<String, String>> oldOverGroups = (ArrayList<HashMap<String, String>>) syncJobType.getConfiguration().get("overGroups");


        WebDriver driver = setupEnvironment.setupSeleniumEnv(false);
        HashMap<String, Object> response = new HashMap<>();
        ArrayList<HashMap<String, Object>> overGroups = new ArrayList<>();


        try {
            String url = "https://mte03-ohim-prod.hospitality.oracleindustry.com/Webclient/FormLogin.aspx";

            if (!setupEnvironment.loginOHIM(driver, url)) {
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
                HashMap<String, Object> overGroup = new HashMap<>();

                WebElement row = rows.get(i);
                List<WebElement> cols = row.findElements(By.tagName("td"));

                if (cols.size() != columns.size()) {
                    continue;
                }
                // check existence of over group
                WebElement td = cols.get(columns.indexOf("over_group"));
                HashMap<String, Object> oldOverGroupData = conversions.checkOverGroupExistence(oldOverGroups, td.getText().strip());

                if ((boolean) oldOverGroupData.get("status")) {
                    overGroup.put("checked", true);
                } else {
                    overGroup.put("checked", false);
                }

                for (int j = 0; j < cols.size(); j++) {
                    overGroup.put(columns.get(j), cols.get(j).getText());
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
        SyncJobType syncJobType = syncJobTypeRepo.findByNameAndAccountId("Journals", user.getAccountId());

        ArrayList<HashMap<String, String>> oldOverGroups = (ArrayList<HashMap<String, String>>) syncJobType.getConfiguration().get("overGroups");
        ArrayList<HashMap<String, String>> majorGroups = new ArrayList<>();
        ArrayList<HashMap<String, String>> itemGroups = new ArrayList<>();
        ArrayList<HashMap<String, String>> items = new ArrayList<>();

        WebDriver driver = setupEnvironment.setupSeleniumEnv(false);
        HashMap<String, Object> response = new HashMap<>();

        try {
            String url = "https://mte03-ohim-prod.hospitality.oracleindustry.com/Webclient/FormLogin.aspx";

            if (checkLogin(items, driver, response, url)) return response;

            String majorGroupsURL = "https://mte03-ohim-prod.hospitality.oracleindustry.com/Webclient/MasterData/MajorGroups/OverviewMajorGroup.aspx";
            driver.get(majorGroupsURL);

            driver.findElement(By.name("filterPanel_btnRefresh")).click();
            List<WebElement> rows = driver.findElements(By.tagName("tr"));

            // Getting MajorGroups
            ArrayList<String> columns = setupEnvironment.getTableColumns(rows, true, 13);
            for (int i = 14; i < rows.size(); i++) {
                HashMap<String, String> majorGroup = new HashMap<>();

                WebElement row = rows.get(i);
                List<WebElement> cols = row.findElements(By.tagName("td"));

                if (cols.size() != columns.size()) {
                    continue;
                }

                for (int j = 0; j < columns.indexOf("over_group")+1; j++) {
                    majorGroup.put(columns.get(j), cols.get(j).getText());
                }

                // check if this major group belong to chosen over group
                HashMap<String, Object> overGroupData = conversions.checkOverGroupExistence(oldOverGroups, majorGroup.get("over_group"));
                if ((Boolean) overGroupData.get("status")) {
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
            syncJobType.getConfiguration().put("majorGroups", majorGroups);

            // Get Items Group
            String mainMenuURL = "https://mte03-ohim-prod.hospitality.oracleindustry.com/Webclient/Common/Menu/MainMenu.aspx";
            driver.get(mainMenuURL);
            String itemGroupsURL = "https://mte03-ohim-prod.hospitality.oracleindustry.com/Webclient/MasterData/ItemGroups/OverviewItemGroup.aspx";
            driver.get(itemGroupsURL);

            driver.findElement(By.name("filterPanel_btnRefresh")).click();
            rows = driver.findElements(By.tagName("tr"));

            columns = setupEnvironment.getTableColumns(rows, true, 10);
            for (int i = 11; i < rows.size(); i++) {
                HashMap<String, String> itemGroup = getTableData(rows, columns, i);
                if (itemGroup == null) continue;
                // check if this item group belong to chosen major group
                HashMap<String, Object> majorGroupData = conversions.checkMajorGroupExistence(majorGroups,itemGroup.get("major_group"));
                if ((Boolean) majorGroupData.get("status")) {
                    itemGroup.put("over_group", (String) ((HashMap<String, Object>)majorGroupData.get("majorGroup")).get("over_group"));
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
            // save new item groups
            syncJobType.getConfiguration().put("itemGroups", itemGroups);

            // Get Items Group
            driver.get(mainMenuURL);
            String itemsURL = "https://mte03-ohim-prod.hospitality.oracleindustry.com/Webclient/MasterData/Items/OverviewItem.aspx";
            driver.get(itemsURL);

            // search by item group
            for (HashMap<String, String> itemGroup : itemGroups) {
                driver.findElement(By.id("cfItemGroup_Text")).clear();
                driver.findElement(By.id("cfItemGroup_Text")).sendKeys(itemGroup.get("item_group"));
                driver.findElement(By.id("cfItemGroup_Text")).sendKeys(Keys.ARROW_DOWN);
                driver.findElement(By.id("cfItemGroup_Text")).sendKeys(Keys.ENTER);

                try {
                    WebDriverWait wait = new WebDriverWait(driver, 10);
                    WebElement itemGroupValue = driver.findElement(By.id("cfItemGroup_Value"));
                    wait.until(ExpectedConditions.textToBePresentInElementValue(itemGroupValue, itemGroup.get("item_group")));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                driver.findElement(By.name("filterPanel_btnRefresh")).click();
                rows = driver.findElements(By.tagName("tr"));

                columns = setupEnvironment.getTableColumns(rows, true, 17);

                for (int i = 11; i < rows.size(); i++) {
                    HashMap<String, String> item = getTableData(rows, columns, i);
                    if (item == null) continue;
                    HashMap<String, Object> itemGroupData = conversions.checkItemGroupExistence(itemGroups,itemGroup.get("item_group"));
                    // check if this item group belong to chosen major group
                    if ((Boolean) itemGroupData.get("status")) {
                        item.put("over_group", (String) ((HashMap<String, Object>)itemGroupData.get("itemGroup")).get("over_group"));
                        item.put("major_group", (String) ((HashMap<String, Object>)itemGroupData.get("itemGroup")).get("major_group"));
                        items.add(item);
                    }
                }
            }
            // save new items
            syncJobType.getConfiguration().put("items", items);
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

    private boolean checkLogin(ArrayList<HashMap<String, String>> items, WebDriver driver, HashMap<String, Object> response, String url) {
        if (!setupEnvironment.loginOHIM(driver, url)) {
            driver.quit();

            response.put("status", Constants.FAILED);
            response.put("message", "Invalid username and password.");
            response.put("data", items);
            return true;
        }
        return false;
    }

    private HashMap<String, String> getTableData(List<WebElement> rows, ArrayList<String> columns, int i) {
        HashMap<String, String> itemGroup = new HashMap<>();

        WebElement row = rows.get(i);
        List<WebElement> cols = row.findElements(By.tagName("td"));

        if (cols.size() != columns.size()) {
            return null;
        }

        for (int j = 0; j < cols.size(); j++) {
            itemGroup.put(columns.get(j), cols.get(j).getText());
        }
        return itemGroup;
    }


}
