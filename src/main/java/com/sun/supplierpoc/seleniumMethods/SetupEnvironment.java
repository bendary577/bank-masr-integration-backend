package com.sun.supplierpoc.seleniumMethods;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class SetupEnvironment {

    public static String chromePath = "chromedriver.exe";

    public WebDriver setupSeleniumEnv() {
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
}
