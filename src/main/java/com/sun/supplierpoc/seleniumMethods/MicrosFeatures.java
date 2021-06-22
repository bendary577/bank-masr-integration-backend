package com.sun.supplierpoc.seleniumMethods;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.Response;
import com.sun.supplierpoc.models.configurations.AccountCredential;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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

                    driver.findElement(By.xpath("/html/body/div[1]/div[2]/div/div/div/div/input")).sendKeys(Keys.ARROW_DOWN);
                    driver.findElement(By.xpath("/html/body/div[1]/div[2]/div/div/div/div/input")).sendKeys(Keys.ENTER);
                } catch (Exception e) {
                    driver.quit();

                    response.setStatus(false);
                    response.setMessage(Constants.INVALID_BUSINESS_DATE);
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
