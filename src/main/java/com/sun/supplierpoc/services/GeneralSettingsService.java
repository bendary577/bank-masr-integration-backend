package com.sun.supplierpoc.services;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.SyncJob;
import com.sun.supplierpoc.models.SyncJobType;
import com.sun.supplierpoc.models.configurations.OverGroup;
import com.sun.supplierpoc.models.configurations.WasteGroup;
import com.sun.supplierpoc.repositories.GeneralSettingsRepo;
import com.sun.supplierpoc.seleniumMethods.MicrosFeatures;
import com.sun.supplierpoc.seleniumMethods.SetupEnvironment;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class GeneralSettingsService {
    @Autowired
    private GeneralSettingsRepo generalSettingsRepo;

    private final Conversions conversions = new Conversions();
    private final SetupEnvironment setupEnvironment = new SetupEnvironment();
    private MicrosFeatures microsFeatures = new MicrosFeatures();
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public HashMap<String, Object> getWastageGroups(Account account, ArrayList<OverGroup> oldOverGroups){
        WebDriver driver;
        HashMap<String, Object> response = new HashMap<>();
        ArrayList<OverGroup> overGroups = new ArrayList<>();

        try{
            driver = setupEnvironment.setupSeleniumEnv(false);
        }
        catch (Exception ex){
            response.put("status", Constants.FAILED);
            response.put("message", "Failed to establish connection with firefox driver.");
            response.put("invoices", new ArrayList<>());
            return response;
        }
        ArrayList<WasteGroup> wasteTypes = new ArrayList<>();
        WebDriverWait wait = new WebDriverWait(driver, 20);

        try {
            String URL = Constants.OHIM_LOGIN_LINK;

            if(account.getMicrosVersion().equals("version1")){
                if (!setupEnvironment.loginOHIM(driver, URL, account)) {
                    driver.quit();

                    response.put("status", Constants.FAILED);
                    response.put("message", "Invalid username and password.");
                    response.put("data", wasteTypes);
                    return response;
                }
                driver.get(Constants.OVER_GROUPS_LINK);
            }
            else if(account.getMicrosVersion().equals("version2")){
                URL = Constants.MICROS_V2_LINK;

                if (!microsFeatures.loginMicrosOHRA(driver, URL, account)) {
                    driver.quit();

                    response.put("status", Constants.FAILED);
                    response.put("message", "Invalid username and password.");
                    response.put("data", wasteTypes);
                    return response;
                }

                try{
                    wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("drawerToggleButton")));
                    wait.until(ExpectedConditions.elementToBeClickable(By.id("drawerToggleButton")));
                    driver.findElement(By.id("drawerToggleButton")).click();

                    wait.until(ExpectedConditions.visibilityOfElementLocated(By.partialLinkText("Inventory Management")));
                    driver.findElement(By.partialLinkText("Inventory Management")).click();
                    List<WebElement> elements = driver.findElements(By.partialLinkText("Inventory Management"));
                    if(elements.size() >= 2){
                        driver.findElements(By.partialLinkText("Inventory Management")).get(1).click();

                        ArrayList<String> tabs2 = new ArrayList<String> (driver.getWindowHandles());
                        driver.switchTo().window(tabs2.get(1));
                    //    wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("_ctl32")));
                        driver.get(Constants.MICROS_OVER_GROUPS_LINK);
                    }else {
                        throw new Exception();
                    }
                } catch (Exception e) {
                    throw e;
                }
            }

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
                } else {
                    overGroup.setChecked(false);
                    overGroup.setOverGroup(td.getText().strip());
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

            response.put("data", wasteTypes);
            response.put("message", "Failed to get over groups.");
            response.put("success", false);

            return response;
        }
    }

}
