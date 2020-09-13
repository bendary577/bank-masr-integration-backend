package com.sun.supplierpoc.seleniumMethods;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.Response;
import com.sun.supplierpoc.models.configurations.AccountCredential;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class SetupEnvironment {

    private Conversions conversions = new Conversions();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public WebDriver setupSeleniumEnv(boolean driverFlag) {
        if (driverFlag){
            String chromePath = "chromedriver.exe";
            System.setProperty("webdriver.chrome.driver", chromePath);
            ChromeOptions options = new ChromeOptions();
            options.addArguments(
                    "--headless",
                    "--disable-gpu",
                    "--window-size=1920,1200",
                    "--ignore-certificate-errors");
            return new ChromeDriver(options);
        }
        else {

            FirefoxBinary firefoxBinary = new FirefoxBinary();
            firefoxBinary.addCommandLineOptions("--headless");
            FirefoxOptions firefoxOptions = new FirefoxOptions();

            firefoxOptions.setBinary(firefoxBinary);
            firefoxOptions.setCapability("marionette", true);
            return new FirefoxDriver(firefoxOptions);
        }
    }

    public boolean loginOHIM(WebDriver driver, String url, Account account){

        ArrayList<AccountCredential> accountCredentials = account.getAccountCredentials();
        AccountCredential hospitalityOHIMCredentials = account.getAccountCredentialByAccount("HospitalityOHIM", accountCredentials);

        driver.get(url);

        try {
            Alert al = driver.switchTo().alert();
            al.accept();
        } catch (NoAlertPresentException Ex) {
            System.out.println("No alert exits");
        }
        driver.findElement(By.id("igtxtdfUsername")).sendKeys(hospitalityOHIMCredentials.getUsername());
        driver.findElement(By.id("igtxtdfPassword")).sendKeys(hospitalityOHIMCredentials.getPassword());
        driver.findElement(By.id("igtxtdfCompany")).sendKeys(hospitalityOHIMCredentials.getCompany());

        String previous_url = driver.getCurrentUrl();
        driver.findElement(By.name("Login")).click();

        return !driver.getCurrentUrl().equals(previous_url);
    }

    public boolean loginOHRA(WebDriver driver, String url, Account account){
        ArrayList<AccountCredential> accountCredentials = account.getAccountCredentials();
        AccountCredential hospitalityOHRACredentials = account.getAccountCredentialByAccount("HospitalityOHRA", accountCredentials);

        driver.get(url);
        try {
            Alert al = driver.switchTo().alert();
            al.accept();
        } catch (NoAlertPresentException Ex) {
            System.out.println("No alert exits");
        }
        driver.findElement(By.id("usr")).sendKeys(hospitalityOHRACredentials.getUsername());
        driver.findElement(By.id("pwd")).sendKeys(hospitalityOHRACredentials.getPassword());
        driver.findElement(By.id("cpny")).sendKeys(hospitalityOHRACredentials.getCompany());

        String previous_url = driver.getCurrentUrl();
        driver.findElement(By.id("Login")).click();

        try {
            // card is wrong
            Alert al = driver.switchTo().alert();
            al.accept();

            return false;
        } catch (NoAlertPresentException Ex) {
            System.out.println("No alert exits");
        }

        return !driver.getCurrentUrl().equals(previous_url);
    }

    public ArrayList<String> getTableColumns(List<WebElement> rows, boolean rowType, int rowNumber){
        ArrayList<String> columns = new ArrayList<>();
        WebElement row = rows.get(rowNumber);
        List<WebElement> cols;
        if (rowType){
            cols = row.findElements(By.tagName("th"));
        }
        else {
            cols = row.findElements(By.tagName("td"));
        }

        for (WebElement col : cols){
            columns.add(conversions.transformColName(col.getText().strip()));
        }

        return columns;
    }

    public HashMap<String, Object> selectTimePeriodOHIM(String timePeriod, Select select, WebDriver driver){
        HashMap<String, Object> response = new HashMap<>();

        try {
            if (timePeriod.equals("Today") || timePeriod.equals("Yesterday")) {
                try {
                    select.selectByVisibleText("User-defined");
                } catch (Exception e) {
                    driver.quit();

                    response.put("status", Constants.FAILED);
                    response.put("message", "Invalid time period.");
                    response.put("invoices", new ArrayList<>());
                    return response;
                }

                DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
                String targetDate = "";

                if (timePeriod.equals("Yesterday")) {
                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.DATE, -1);
                    targetDate = dateFormat.format(cal.getTime());
                } else {
                    Date date = new Date();
                    targetDate = dateFormat.format(date);
                }

                driver.findElement(By.id("_ctl7_input")).clear();
                driver.findElement(By.id("_ctl7_input")).sendKeys(targetDate);
//                driver.findElement(By.id("_ctl7_input")).sendKeys("6/1/2020");
                driver.findElement(By.id("_ctl7_input")).sendKeys(Keys.ENTER);

                driver.findElement(By.id("_ctl9_input")).clear();
                driver.findElement(By.id("_ctl9_input")).sendKeys(targetDate);
//                driver.findElement(By.id("_ctl9_input")).sendKeys("6/30/2020");
                driver.findElement(By.id("_ctl9_input")).sendKeys(Keys.ENTER);

                String startDateValue = driver.findElement(By.id("_ctl7_input")).getAttribute("value");
                Date startDate = dateFormat.parse(startDateValue);
//                Date startDate = dateFormat.parse("6/1/2020");

                String endDateValue = driver.findElement(By.id("_ctl9_input")).getAttribute("value");
                Date endDate = dateFormat.parse(endDateValue);
//                Date endDate = dateFormat.parse("6/30/2020");

//                if (!dateFormat.format(startDate).equals("06/01/2020")) {
                if (!dateFormat.format(startDate).equals(targetDate)) {

                    driver.quit();

                    response.put("status", Constants.FAILED);
                    response.put("message", "Failed to get invoices of today, please try again or contact support team.");
                    response.put("invoices", new ArrayList<>());
                    return response;
                }

//                if (!dateFormat.format(endDate).equals("06/30/2020")) {
                if (!dateFormat.format(endDate).equals(targetDate)) {
                    driver.quit();

                    response.put("status", Constants.FAILED);
                    response.put("message", "Failed to get invoices of today, please try again or contact support team.");
                    response.put("invoices", new ArrayList<>());
                    return response;
                }

                response.put("status", Constants.SUCCESS);
                response.put("message", "");
                response.put("invoices", new ArrayList<>());
                return response;

            } else {
                try {
                    select.selectByVisibleText(timePeriod);
                } catch (Exception e) {
                    driver.quit();

                    response.put("status", Constants.FAILED);
                    response.put("message", "Invalid time period.");
                    response.put("invoices", new ArrayList<>());
                    return response;
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
            driver.quit();

            response.put("status", Constants.FAILED);
            response.put("message", e.getMessage());
            response.put("invoices", new ArrayList<>());
            return response;
        }

        response.put("status", Constants.SUCCESS);
        response.put("message", "");
        response.put("invoices", new ArrayList<>());
        return response;
    }

    public Response selectTimePeriodOHRA(String timePeriod, String location, WebDriver driver){
        Response response = new Response();
        try {
            if (timePeriod.equals(Constants.MOST_RECENT) || timePeriod.equals(Constants.FINANCIAL_WEEK_TO_DATE)
            || timePeriod.equals(Constants.PAST_7_DAYES) || timePeriod.equals(Constants.TODAY)
            || timePeriod.equals(Constants.YESTERDAY) || timePeriod.equals(Constants.MONTH_TO_DATE)
            || timePeriod.equals(Constants.FINANCIAL_PERIOD_TO_DATE) ){
                try {
                    Select businessDate = new Select(driver.findElement(By.id("calendarData")));
                    businessDate.selectByVisibleText(timePeriod);
                } catch (Exception e) {
                    driver.quit();

                    response.setStatus(false);
                    response.setMessage(Constants.INVALID_BUSINESS_DATE);
                    response.setEntries(new ArrayList<>());
                    return response;
                }
            }
            else if (timePeriod.equals(Constants.LAST_MONTH) || timePeriod.equals(Constants.LAST_QUARTER)
            || timePeriod.equals(Constants.YEAR_TO_DATE) || timePeriod.equals(Constants.LAST_YEAR_YTD)){
                try {
                    WebDriverWait wait = new WebDriverWait(driver, 20);

                    wait.until(ExpectedConditions.elementToBeClickable(By.id("calendarBtn")));
                    driver.findElement(By.id("calendarBtn")).click();

                    wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.id("calendarFrame")));
                    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("selectQuick")));

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
                    while (!selectedOption.equals(timePeriod)){
                        selectedOption = businessDate.getFirstSelectedOption().getText().strip();
                    }

                    driver.switchTo().defaultContent();
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


            if (!location.equals("")){
                Select selectLocation = new Select(driver.findElement(By.id("locationData")));

                try {
                    selectLocation.selectByVisibleText(location);
                } catch (Exception e) {
                    response.setStatus(false);
                    response.setMessage(Constants.INVALID_LOCATION);
                    response.setEntries(new ArrayList<>());
                    return response;
                }

                String selectLocationOption = selectLocation.getFirstSelectedOption().getText().strip();
                while (!selectLocationOption.equals(location)) {
                    selectLocationOption = selectLocation.getFirstSelectedOption().getText().strip();
                }
            }
        }catch (Exception e) {
            response.setStatus(false);
            response.setMessage(e.getMessage());
        }
        return response;
    }
}
