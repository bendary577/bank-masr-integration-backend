package com.sun.supplierpoc.seleniumMethods;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.configurations.AccountCredential;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.Select;


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
//                    "--headless",
                    "--disable-gpu",
                    "--window-size=1920,1200",
                    "--ignore-certificate-errors");
            return new ChromeDriver(options);
        }
        else {

            FirefoxBinary firefoxBinary = new FirefoxBinary();
//            firefoxBinary.addCommandLineOptions("--headless");
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

    public HashMap<String, Object> selectTimePeriod(String timePeriod, Select select, WebDriver driver){
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
                driver.findElement(By.id("_ctl7_input")).sendKeys(Keys.ENTER);

                driver.findElement(By.id("_ctl9_input")).clear();
                driver.findElement(By.id("_ctl9_input")).sendKeys(targetDate);
                driver.findElement(By.id("_ctl9_input")).sendKeys(Keys.ENTER);

                String startDateValue = driver.findElement(By.id("_ctl7_input")).getAttribute("value");
                Date startDate = dateFormat.parse(startDateValue);

                String endDateValue = driver.findElement(By.id("_ctl9_input")).getAttribute("value");
                Date endDate = dateFormat.parse(endDateValue);

                if (!dateFormat.format(startDate).equals(targetDate)) {
                    driver.quit();

                    response.put("status", Constants.FAILED);
                    response.put("message", "Failed to get invoices of today, please try again or contact support team.");
                    response.put("invoices", new ArrayList<>());
                    return response;
                }

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
}
