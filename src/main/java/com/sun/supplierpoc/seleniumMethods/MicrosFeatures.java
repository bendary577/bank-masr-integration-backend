package com.sun.supplierpoc.seleniumMethods;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.Response;
import com.sun.supplierpoc.models.configurations.AccountCredential;
import com.sun.supplierpoc.models.configurations.CostCenter;
import com.sun.supplierpoc.models.configurations.RevenueCenter;
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
import java.util.concurrent.TimeUnit;

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

    public Response selectDateRangeMicros(String timePeriod, String fromDate, String toDate,
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
//                        TimeUnit.SECONDS.sleep(1);
                        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("businessDateFilter_href_link")));
                        driver.findElement(By.id("businessDateFilter_href_link")).click();
                        TimeUnit.SECONDS.sleep(1);
                    } catch (Exception e){
                        response.setStatus(false);
                        response.setMessage(e.getMessage());
                        return response;
                    }
                    if(!driver.findElements(By.id("clear0")).isEmpty()){
                        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("clear0")));
                    }else{
                        //while element is not possible .. cancel and start again
                        do{
                            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("advance_filter_busDates_cancel")));
                            driver.findElement(By.id("advance_filter_busDates_cancel")).click();
                            try {
                                wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("businessDateFilter_href_link")));
                                driver.findElement(By.id("businessDateFilter_href_link")).click();
                                TimeUnit.SECONDS.sleep(1);
                            }catch (Exception e){
                                response.setStatus(false);
                                response.setMessage(e.getMessage());
                                return response;
                            }

                        }while(driver.findElements(By.id("clear0")).isEmpty());
                    }
                    driver.findElement(By.id("clear0")).click();

                    // Choose Date Range
                    if (fromDate.equals(toDate)){
                        response = setupEnvironment.chooseDayDateOHRA(fromDate,driver);
                    }else{
                        response = setupEnvironment.chooseRangeDaysDateOHRA(fromDate, toDate, driver);
                    }

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
                    WebElement input = driver.findElement(By.xpath("/html/body/div[1]/div[2]/div/div/div/div/input"));
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

//            if (driver.findElements(By.id("location_label")).size() != 0){
//                try {
//                    // Business Location
//                    wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("oj-select-choice-search_locations_select")));
//                    wait.until(ExpectedConditions.elementToBeClickable(By.id("oj-select-choice-search_locations_select")));
//                    driver.findElement(By.id("oj-select-choice-search_locations_select")).click();
//
//                    // Filter by range
//                    WebElement input = driver.findElement(By.xpath("/html/body/div[1]/div[2]/div/div/div/div/input"));
//
//                    if (location == null || location.equals("")){
//                        input.sendKeys("all");
//                    }else{
//                        input.sendKeys(location);
//                    }
//
//                    Thread.sleep(500);
//                    input.sendKeys(Keys.ARROW_DOWN);
//                    input.sendKeys(Keys.ENTER);
//                } catch (Exception e) {
//                    response.setStatus(false);
//                    response.setMessage(Constants.INVALID_LOCATION);
//                    response.setEntries(new ArrayList<>());
//                    return response;
//                }
//            }

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
                    WebElement input = driver.findElement(By.xpath("//*[@id=\"oj-listbox-drop\"]/div/div/input"));

                    if (revenueCenter == null || revenueCenter.equals("")){
                        revenueCenter = "all";
                        input.sendKeys("all");
                    }else{
                        input.sendKeys(revenueCenter);
                    }

                    Thread.sleep(500);
                    input.sendKeys(Keys.ARROW_DOWN);
                    input.sendKeys(Keys.ENTER);

                    if(driver.findElements(By.xpath("/html/body/div[1]/div[2]/div/div/div[1]/div[1]")).size() > 0 ){
                        WebElement checker = driver.findElement(By.xpath("/html/body/div[1]/div[2]/div/div/div[1]/div[1]"));
                        if(checker != null && checker.getText().strip().equals("No matches found")){
                            driver.findElement(By.xpath("/html/body/div[1]/div[2]/oj-dialog/div[1]/oj-button/button")).click();
                            response.setStatus(false);
                            response.setMessage(Constants.INVALID_REVENUE_CENTER);
                            response.setEntries(new ArrayList<>());
                            return response;
                        }
                    }

                    // Check choosen value
                    if(!revenueCenter.equalsIgnoreCase(driver.findElement(By.id("oj-select-choice-search_rvc_select")).getText())){
                        driver.findElement(By.xpath("/html/body/div[1]/div[2]/oj-dialog/div[1]/oj-button/button")).click();
                        response.setStatus(false);
                        response.setMessage(Constants.INVALID_REVENUE_CENTER);
                        response.setEntries(new ArrayList<>());
                        return response;
                    }

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

    public String getActualBusinessDate(WebDriver driver){
      return    driver.findElement(By.id("dateTime_input")).getText();
    }
    public String getActualLocation(WebDriver driver){
        return driver.findElement(By.id("search_locations_input")).getText();
    }
    public String getActualRevenueCenter(WebDriver driver){
      return driver.findElement(By.id("search-rvc-input")).getText();
    }


    public Response checkReportParameters(WebDriver driver,String fromDate,String toDate,String businessDate,String locationName ) throws Exception{

        Response response = new Response();
        String actualBusinessDate= driver.findElement(By.id("dateTime_input")).getText();;
        String actaulLocation = driver.findElement(By.id("search_locations_input")).getText();
       // String revenueCenter = driver.findElement(By.id("search-rvc-input")).getText();


        if(actualBusinessDate.equals(Constants.POWER_SELECT)){
            driver.findElement(By.xpath("/html/body/div[2]/section/div[1]/div[2]/div/div/div[2]/div/my-reports-cca/report-group-cca/div[1]/div[5]/div[2]/div[2]/div[1]/div/oj-button")).click();
            String rangeDate = driver.findElement(By.className("oj-fbgbu-popup-list")).getText();

            DateFormat formater = new SimpleDateFormat("yyyy-MM-dd");
            Date formatedFromDate = formater.parse(fromDate);
            Date formatedToDate = formater.parse(toDate);
            DateFormat BusinessDateFormatV2 = new SimpleDateFormat("MM/dd/yyyy");
            String modifiedFromDate = BusinessDateFormatV2.format(formatedFromDate);
            String modifiedToDate = BusinessDateFormatV2.format(formatedToDate);
            String modifiedBusinessDate = modifiedFromDate +" - "+ modifiedToDate;
            if(!rangeDate.equals(modifiedBusinessDate) && !rangeDate.equals(modifiedFromDate)){
                response.setStatus(false);
                response.setMessage(Constants.INVALID_BUSINESS_DATE);
                return response;
            }
        }

        else if(!actualBusinessDate.equals(Constants.LAST_MONTH) && businessDate.equals(Constants.LAST_MONTH)){
            response.setStatus(false);
            response.setMessage(Constants.INVALID_BUSINESS_DATE);
            return response;
        }

        if(!actaulLocation.equals(locationName)){
            response.setStatus(false);
            response.setMessage(Constants.INVALID_LOCATION);
            return response;
        }
        response.setStatus(true);
        return response;
    }

//    public Response reRunFilters(Response response, WebDriver driver, String timePeriod, String fromDate, String toDate){
//        try {
//            WebDriverWait wait = new WebDriverWait(driver, 30);
//            WebDriverWait shortWait = new WebDriverWait(driver, 5);
//
//            // Edit Parameters
//            try {
//                wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("edit_filter_button")));
//                driver.findElement(By.id("edit_filter_button")).click();
//            }catch (Exception e){
//                response.setStatus(false);
//                response.setMessage(e.getMessage());
//                return response;
//            }
//
//            if (timePeriod.equals(Constants.USER_DEFINED)) {
//                try {
//                    // Advanced
//                    try {
////                        TimeUnit.SECONDS.sleep(1);
//                        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("businessDateFilter_href_link")));
//                        driver.findElement(By.id("businessDateFilter_href_link")).click();
//                        TimeUnit.SECONDS.sleep(1);
//                    } catch (Exception e){
//                        response.setStatus(false);
//                        response.setMessage(e.getMessage());
//                        return response;
//                    }
//
////                    wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("clear0")));
//                    TimeUnit.SECONDS.sleep(1);
//                    if(!driver.findElements(By.id("clear0")).isEmpty()){
//                        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("clear0")));
//                        driver.findElement(By.id("clear0")).click();
//                    }else{
//                        System.out.println("no element");
//                        driver.findElement(By.id("advance_filter_busDates_cancel")).click();
//                    }
//
//
//                    // Choose Date Range
//                    if (fromDate.equals(toDate)){
//                        response = setupEnvironment.chooseDayDateOHRA(fromDate,driver);
//                    }else{
//                        response = setupEnvironment.chooseRangeDaysDateOHRA(fromDate, toDate, driver);
//                    }
//
//                    if (!response.isStatus()){
//                        return response;
//                    }
//
//                    // Apply
//                    driver.findElement(By.xpath("//*[@id=\"advance_filter_busDates_apply\"]/button")).click();
//
//                } catch (Exception e) {
//                    driver.quit();
//                    e.printStackTrace();
//                    response.setStatus(false);
//                    response.setMessage(Constants.INVALID_BUSINESS_DATE);
//                    response.setEntries(new ArrayList<>());
//                    return response;
//                }
//            }
//
//            response.setStatus(true);
//            response.setMessage("");
//        } catch (Exception e) {
//            response.setStatus(false);
//            response.setMessage("Failed to choose business date");
//        }
//    }
}
