package com.sun.supplierpoc.seleniumMethods;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.Response;
import com.sun.supplierpoc.models.configurations.AccountCredential;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Component
public class MicrosFeatures {
    SetupEnvironment setupEnvironment = new SetupEnvironment();
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public boolean loginMicrosOHRA(WebDriver driver, String url, Account account) {
        try {
            ArrayList<AccountCredential> accountCredentials = account.getAccountCredentials();
            AccountCredential hospitalityOHRACredentials = account.getAccountCredentialByAccount("HospitalityOHRA", accountCredentials);

            driver.get(url);
            try {
                Alert al = driver.switchTo().alert();
                al.accept();
            } catch (NoAlertPresentException Ex) {
                System.out.println("No alert exits");
            }

            // Wait until elements be ready
            WebDriverWait wait = new WebDriverWait(driver, 30);
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name-input|input")));

            driver.findElement(By.id("user-name-input|input")).sendKeys(hospitalityOHRACredentials.getUsername());
            driver.findElement(By.id("org-name-input|input")).sendKeys(hospitalityOHRACredentials.getCompany());
            driver.findElement(By.id("password-input|input")).sendKeys(hospitalityOHRACredentials.getPassword());

            String previous_url = driver.getCurrentUrl();
            driver.findElement(By.id("signinBtn")).click();


            return !driver.getCurrentUrl().equals(previous_url);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public Response selectDateRangeMicros(String timePeriod, String dayDate,
                                         String location, String revenueCenter, String orderType, WebDriver driver){
        Response response = new Response();
        try {
            WebDriverWait wait = new WebDriverWait(driver, 30);
            WebDriverWait shortWait = new WebDriverWait(driver, 5);

            // Edit Parameters
            try {
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("edit_filter_button")));
                driver.findElement(By.id("edit_filter_button")).click();
            }catch (Exception e){
                response.setStatus(false);
                response.setMessage(e.getMessage());
                return response;
            }

            if (timePeriod.equals(Constants.USER_DEFINED)) {
                try {
                    // Advanced
                    try {
                        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("businessDateFilter_href_link")));
                        driver.findElement(By.id("businessDateFilter_href_link")).click();
                    } catch (Exception e){
                        response.setStatus(false);
                        response.setMessage(e.getMessage());
                        return response;
                    }

                    // Choose Date
                    response = setupEnvironment.chooseDayDateOHRA(dayDate,driver);

                    if (!response.isStatus()){
                        return response;
                    }

                    // Apply
                    driver.findElement(By.xpath("//*[@id=\"advance_filter_busDates_apply\"]/button")).click();

                } catch (Exception e) {
                    driver.quit();
                    e.printStackTrace();
                    response.setStatus(false);
                    response.setMessage(Constants.INVALID_BUSINESS_DATE);
                    response.setEntries(new ArrayList<>());
                    return response;
                }
            }

            else if (timePeriod.equals(Constants.YESTERDAY) || timePeriod.equals(Constants.LAST_MONTH)
                    || timePeriod.equals(Constants.TODAY)) {
                try {
                    // Business Date
                    wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("oj-select-choice-search_businessdates_select")));
                    driver.findElement(By.id("oj-select-choice-search_businessdates_select")).click();

                    // Filter by range
                    WebElement input = driver.findElement(By.xpath("//*[@id=\"oj-listbox-drop\"]/div[2]/div/input"));
                    input.sendKeys(timePeriod);
                    Thread.sleep(500);
                    input.sendKeys(Keys.ARROW_DOWN);
                    input.sendKeys(Keys.ENTER);
                } catch (Exception e) {
                    driver.quit();

                    response.setStatus(false);
                    response.setMessage(Constants.INVALID_BUSINESS_DATE);
                    response.setEntries(new ArrayList<>());
                    return response;
                }
            }

            if (driver.findElements(By.id("location_label")).size() != 0){
                try {
                    // Business Location
                    wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("oj-select-choice-search_locations_select")));
                    wait.until(ExpectedConditions.elementToBeClickable(By.id("oj-select-choice-search_locations_select")));
                    driver.findElement(By.id("oj-select-choice-search_locations_select")).click();

                    // Filter by range
                    WebElement input = driver.findElement(By.xpath("/html/body/div[1]/div[2]/div/div/div/div/input"));

                    if (location == null || location.equals("")){
                        input.sendKeys("all");
                    }else{
                        input.sendKeys(location);
                    }

                    Thread.sleep(500);
                    input.sendKeys(Keys.ARROW_DOWN);
                    input.sendKeys(Keys.ENTER);
                } catch (Exception e) {
                    response.setStatus(false);
                    response.setMessage(Constants.INVALID_LOCATION);
                    response.setEntries(new ArrayList<>());
                    return response;
                }
            }

            if (driver.findElements(By.id("rvc_filter_label")).size() != 0){
                try {
                    // Business Revenue Center
                    wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("oj-select-choice-search_rvc_select")));
                    wait.until(ExpectedConditions.elementToBeClickable(By.id("oj-select-choice-search_rvc_select")));

                    try {
                        WebDriverWait newWait = new WebDriverWait(driver, 3);
                        newWait.until(ExpectedConditions.alertIsPresent());
                    } catch (Exception ex) {
                        System.out.println(ex.getMessage());
                    }

                    driver.findElement(By.id("oj-select-choice-search_rvc_select")).click();

                    // Filter by range
//                    try {
//                        shortWait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/div[1]/div[2]/div/div/div/div/input")));
//                    } catch (Exception e) {
//                        driver.findElement(By.id("oj-select-choice-search_rvc_select")).click();
//                    }

                    WebElement input = driver.findElement(By.xpath("/html/body/div[1]/div[2]/div/div/div/div/input"));

                    if (revenueCenter == null || revenueCenter.equals("")){
                        input.sendKeys("all");
                    }else{
                        input.sendKeys(revenueCenter);
                    }

                    Thread.sleep(500);
                    input.sendKeys(Keys.ARROW_DOWN);
                    input.sendKeys(Keys.ENTER);
                } catch (Exception e) {
                    response.setStatus(false);
                    response.setMessage(Constants.INVALID_REVENUE_CENTER);
                    response.setEntries(new ArrayList<>());
                    return response;
                }
            }

            if (driver.findElements(By.id("ordery_type_filter_label")).size() != 0){
                try {
                    // Business Revenue Center
                    wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("oj-select-choice-search_orderType_select")));
                    wait.until(ExpectedConditions.elementToBeClickable(By.id("oj-select-choice-search_orderType_select")));
                    driver.findElement(By.id("oj-select-choice-search_orderType_select")).click();

                    // Filter by range
                    try {
                        shortWait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"oj-listbox-drop\"]/div[2]/div/input")));
                    } catch (Exception e) {
                        driver.findElement(By.id("oj-select-choice-search_orderType_select")).click();
                    }
                    WebElement input = driver.findElement(By.xpath("//*[@id=\"oj-listbox-drop\"]/div[2]/div/input"));

                    if (orderType == null || orderType.equals("")){
                        input.sendKeys("all");
                    }else{
                        input.sendKeys(orderType);
                    }

                    Thread.sleep(500);
                    input.sendKeys(Keys.ARROW_DOWN);
                    input.sendKeys(Keys.ENTER);
                } catch (Exception e) {
                    response.setStatus(false);
                    response.setMessage(Constants.INVALID_REVENUE_CENTER);
                    response.setEntries(new ArrayList<>());
                    return response;
                }
            }

            response.setStatus(true);
            response.setMessage("");
        } catch (Exception e) {
            response.setStatus(false);
            response.setMessage("Failed to choose business date");
        }
        return response;
    }


}
