package com.sun.supplierpoc.seleniumMethods;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.ArrayList;
import java.util.List;

public class SetupEnvironment {

    private Conversions conversions = new Conversions();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public WebDriver setupSeleniumEnv() {
        String chromePath = "chromedriver.exe";
        System.setProperty("webdriver.chrome.driver", chromePath);
        ChromeOptions options = new ChromeOptions();
        options.addArguments(
//                "--headless",
                "--disable-gpu",
                "--window-size=1920,1200",
                "--ignore-certificate-errors");
        WebDriver driver = new ChromeDriver(options);
        return driver;
    }

    public boolean loginOHIM(WebDriver driver, String url){
        driver.get(url);

        driver.findElement(By.id("igtxtdfUsername")).sendKeys("Amr");
        driver.findElement(By.id("igtxtdfPassword")).sendKeys("Mic@8000");
        driver.findElement(By.id("igtxtdfCompany")).sendKeys("act");

        String previous_url = driver.getCurrentUrl();
        driver.findElement(By.name("Login")).click();

        return !driver.getCurrentUrl().equals(previous_url);
    }

    public ArrayList<String> getTableColumns(List<WebElement> rows, int rowNumber){
        ArrayList<String> columns = new ArrayList<>();
        WebElement row = rows.get(rowNumber);
        List<WebElement> cols = row.findElements(By.tagName("th"));

        for (int j = 1; j < cols.size(); j++) columns.add(conversions.transformColName(cols.get(j).getText()));

        return columns;
    }
}
