package com.sun.supplierpoc.seleniumMethods;

import com.sun.supplierpoc.Conversions;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;


import java.util.ArrayList;
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
            System.setProperty("webdriver.gecko.driver", "geckodriver.exe");
            FirefoxOptions firefoxOptions = new FirefoxOptions();
            firefoxOptions.setCapability("marionette", true);
            return new FirefoxDriver(firefoxOptions);
        }
    }

    public boolean loginOHIM(WebDriver driver, String url){
        driver.get(url);

        driver.findElement(By.id("igtxtdfUsername")).sendKeys("Fusion");
        driver.findElement(By.id("igtxtdfPassword")).sendKeys("Gcs@3000");
        driver.findElement(By.id("igtxtdfCompany")).sendKeys("gcs");

        String previous_url = driver.getCurrentUrl();
        driver.findElement(By.name("Login")).click();

        return !driver.getCurrentUrl().equals(previous_url);
    }

    public boolean loginOHRA(WebDriver driver, String url){
        driver.get(url);

        driver.findElement(By.id("usr")).sendKeys("Fusion");
        driver.findElement(By.id("pwd")).sendKeys("Gcs@3000");
        driver.findElement(By.id("cpny")).sendKeys("gcs");

        String previous_url = driver.getCurrentUrl();
        driver.findElement(By.id("Login")).click();

        return !driver.getCurrentUrl().equals(previous_url);
    }

    public ArrayList<String> getTableColumns(List<WebElement> rows, int rowNumber){
        ArrayList<String> columns = new ArrayList<>();
        WebElement row = rows.get(rowNumber);
        List<WebElement> cols = row.findElements(By.tagName("th"));

        for (WebElement col : cols) columns.add(conversions.transformColName(col.getText()));

        return columns;
    }
}
