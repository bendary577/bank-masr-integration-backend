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

public class BasicFeatures {
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
            if (timePeriod.equals(Constants.USER_DEFINED)) {
                try {
                    // Edit Parameters
                    driver.findElement(By.id("[['edit_filter_button']]")).click();

                    // Advanced
                    driver.findElement(By.id("businessDateFilter_href_link")).click();

                    // Choose Date
                    response = setupEnvironment.chooseDayDateOHRA(dayDate,driver);

                    // Apply
                    driver.findElement(By.xpath("//*[@id=\"advance_filter_busDates_apply\"]/button"));

                    // Run
                    driver.findElement(By.xpath("//*[@id=\"save-close-button\"]/button"));

                    if (!response.isStatus()){
                        return response;
                    }
                } catch (Exception e) {
                    driver.quit();

                    response.setStatus(false);
                    response.setMessage(Constants.INVALID_BUSINESS_DATE);
                    response.setEntries(new ArrayList<>());
                    return response;
                }
            }

            else if (timePeriod.equals(Constants.YESTERDAY) || timePeriod.equals(Constants.LAST_MONTH)
                    || timePeriod.equals(Constants.TODAY)) {
                try {
                    Select businessDate = new Select(driver.findElement(By.id("selectQuick")));
                    try {
                        businessDate.selectByVisibleText(timePeriod);
                    } catch (Exception e) {
                        driver.quit();

                        response.setStatus(false);
                        response.setMessage(Constants.INVALID_BUSINESS_DATE);
                        response.setEntries(new ArrayList<>());
                        return response;
                    }

                    String selectedOption = businessDate.getFirstSelectedOption().getText().strip();
                    while (!selectedOption.equals(timePeriod)) {
                        selectedOption = businessDate.getFirstSelectedOption().getText().strip();
                    }
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
