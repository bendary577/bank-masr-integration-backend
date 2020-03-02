package com.sun.supplierpoc.seleniumMethods;

import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.models.Account;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
//            firefoxBinary.addCommandLineOptions("--headless");

            System.setProperty("webdriver.gecko.driver", "geckodriver.exe");
            FirefoxOptions firefoxOptions = new FirefoxOptions();

            firefoxOptions.setBinary(firefoxBinary);
//            firefoxOptions.setCapability("marionette", true);
            return new FirefoxDriver(firefoxOptions);
        }
    }


    public boolean loginOHIM(WebDriver driver, String url, Account account){

        HashMap<String, HashMap<String, String>> accountCredentials = account.getAccountCredentials();
        HashMap<String, String> hospitalityOHIMCredentials = accountCredentials.get("HospitalityOHIM");

        driver.get(url);

        try {
            Alert al = driver.switchTo().alert();
            al.accept();
        } catch (NoAlertPresentException Ex) {
            System.out.println("No alert exits");
        }
        driver.findElement(By.id("igtxtdfUsername")).sendKeys(hospitalityOHIMCredentials.get("username"));
        driver.findElement(By.id("igtxtdfPassword")).sendKeys(hospitalityOHIMCredentials.get("password"));
        driver.findElement(By.id("igtxtdfCompany")).sendKeys(hospitalityOHIMCredentials.get("company"));

        String previous_url = driver.getCurrentUrl();
        driver.findElement(By.name("Login")).click();

        return !driver.getCurrentUrl().equals(previous_url);
    }

    public boolean loginOHRA(WebDriver driver, String url, Account account){
        HashMap<String, HashMap<String, String>> accountCredentials = account.getAccountCredentials();
        HashMap<String, String> hospitalityOHRACredentials = accountCredentials.get("HospitalityOHRA");

        driver.get(url);
        try {
            Alert al = driver.switchTo().alert();
            al.accept();
        } catch (NoAlertPresentException Ex) {
            System.out.println("No alert exits");
        }
        driver.findElement(By.id("usr")).sendKeys(hospitalityOHRACredentials.get("username"));
        driver.findElement(By.id("pwd")).sendKeys(hospitalityOHRACredentials.get("password"));
        driver.findElement(By.id("cpny")).sendKeys(hospitalityOHRACredentials.get("company"));

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
            columns.add(conversions.transformColName(col.getText()));
        }

        return columns;
    }
}
