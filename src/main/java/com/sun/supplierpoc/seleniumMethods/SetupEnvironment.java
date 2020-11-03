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
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;


import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class SetupEnvironment {

    private Conversions conversions = new Conversions();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public WebDriver setupSeleniumEnv(boolean driverFlag) {
        if (driverFlag) {
            String chromePath = "chromedriver.exe";
            System.setProperty("webdriver.chrome.driver", chromePath);
            ChromeOptions options = new ChromeOptions();
            options.addArguments(
                    "--headless",
                    "--disable-gpu",
                    "--window-size=1920,1200",
                    "--ignore-certificate-errors");
            return new ChromeDriver(options);
        } else {

            FirefoxBinary firefoxBinary = new FirefoxBinary();
            firefoxBinary.addCommandLineOptions("--headless");
            FirefoxOptions firefoxOptions = new FirefoxOptions();

            firefoxOptions.setBinary(firefoxBinary);
            firefoxOptions.setCapability("marionette", true);
            return new FirefoxDriver(firefoxOptions);
        }
    }

    public boolean loginOHIM(WebDriver driver, String url, Account account) {

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

    public boolean loginOHRA(WebDriver driver, String url, Account account) {
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

    public ArrayList<String> getTableColumns(List<WebElement> rows, boolean rowType, int rowNumber) {
        ArrayList<String> columns = new ArrayList<>();
        WebElement row = rows.get(rowNumber);
        List<WebElement> cols;
        if (rowType) {
            cols = row.findElements(By.tagName("th"));
        } else {
            cols = row.findElements(By.tagName("td"));
        }

        for (WebElement col : cols) {
            if (!conversions.transformColName(col.getText().strip()).equals("")){
                columns.add(conversions.transformColName(col.getText().strip()));
            }else {
                columns.add(conversions.transformColName(col.getText().strip()));
            }
        }

        return columns;
    }

    public HashMap<String, Object> selectTimePeriodOHIM(String timePeriod, String syncFromDate, String syncToDate,
                                                        Select select, WebDriver driver) {
        HashMap<String, Object> response = new HashMap<>();

        try {
            if (timePeriod.equals("Today") || timePeriod.equals("Yesterday") || timePeriod.equals("UserDefined")) {
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
                String fromDateFormatted = "";
                String toDateFormatted = "";

                if (timePeriod.equals("Yesterday")) {
                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.DATE, -1);
                    toDateFormatted = dateFormat.format(cal.getTime());
                    fromDateFormatted = dateFormat.format(cal.getTime());
                } else if (timePeriod.equals("Today")) {
                    Date date = new Date();
                    toDateFormatted = dateFormat.format(date);
                    fromDateFormatted = dateFormat.format(date);
                } else {
                    Date toDate = new SimpleDateFormat("yyyy-MM-dd").parse(syncToDate);
                    Date fromDate = new SimpleDateFormat("yyyy-MM-dd").parse(syncFromDate);

                    if(toDate.compareTo(fromDate) < 0){
                        Date tempDate = toDate;
                        toDate = fromDate;
                        fromDate = tempDate;
                    }

                    fromDateFormatted = dateFormat.format(fromDate);
                    toDateFormatted = dateFormat.format(toDate);
                }

                driver.findElement(By.id("_ctl7_input")).clear();
                driver.findElement(By.id("_ctl7_input")).sendKeys(fromDateFormatted);
                driver.findElement(By.id("_ctl7_input")).sendKeys(Keys.ENTER);

                driver.findElement(By.id("_ctl9_input")).clear();
                driver.findElement(By.id("_ctl9_input")).sendKeys(toDateFormatted);
                driver.findElement(By.id("_ctl9_input")).sendKeys(Keys.ENTER);

                String startDateValue = driver.findElement(By.id("_ctl7_input")).getAttribute("value");
                Date startDate = dateFormat.parse(startDateValue);

                String endDateValue = driver.findElement(By.id("_ctl9_input")).getAttribute("value");
                Date endDate = dateFormat.parse(endDateValue);

                if (!dateFormat.format(startDate).equals(fromDateFormatted)) {
                    driver.quit();

                    response.put("status", Constants.FAILED);
                    response.put("message", "Failed to get invoices of this time period, please try again or contact support team.");
                    response.put("invoices", new ArrayList<>());
                    return response;
                }

                if (!dateFormat.format(endDate).equals(toDateFormatted)) {
                    driver.quit();

                    response.put("status", Constants.FAILED);
                    response.put("message", "Failed to get invoices of this time period, please try again or contact support team.");
                    response.put("invoices", new ArrayList<>());
                    return response;
                }

                response.put("status", Constants.SUCCESS);
                response.put("message", "");
                response.put("invoices", new ArrayList<>());
                return response;

            }
            else {
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
        } catch (Exception e) {
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

    public Response selectTimePeriodOHRA(String timePeriod, String syncFromDate, String syncToDate,
                                         String location, String revenueCenter, WebDriver driver) {
        Response response = new Response();
        try {
            if (!revenueCenter.equals("")) {
                Select selectLocation = new Select(driver.findElement(By.id("revenueCenterData")));

                try {
                    selectLocation.selectByVisibleText(revenueCenter);
                } catch (Exception e) {
                    response.setStatus(false);
                    response.setMessage(Constants.INVALID_REVENUE_CENTER);
                    response.setEntries(new ArrayList<>());
                    return response;
                }

                String selectLocationOption = selectLocation.getFirstSelectedOption().getText().strip();
                while (!selectLocationOption.equals(revenueCenter)) {
                    selectLocationOption = selectLocation.getFirstSelectedOption().getText().strip();
                }
            }

            if (!location.equals("")) {
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

            if (timePeriod.equals(Constants.USER_DEFINED)) {
                try {
                    WebDriverWait wait = new WebDriverWait(driver, 20);

                    wait.until(ExpectedConditions.elementToBeClickable(By.id("calendarBtn")));
                    driver.findElement(By.id("calendarBtn")).click();

                    wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.id("calendarFrame")));
                    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("selectQuick")));

                    if (syncFromDate.equals(syncToDate)){
                        response = chooseDaysDateOHRA(syncFromDate, syncToDate, driver);
                    }else {
                        response = chooseMonthsDateOHRA(syncFromDate, syncToDate, driver);
                    }

                    if (!response.isStatus()){
                        return response;
                    }

                    driver.switchTo().defaultContent();

                } catch (Exception e) {
                    driver.quit();

                    response.setStatus(false);
                    response.setMessage(Constants.INVALID_BUSINESS_DATE);
                    response.setEntries(new ArrayList<>());
                    return response;
                }
            } else if (timePeriod.equals(Constants.MOST_RECENT) || timePeriod.equals(Constants.FINANCIAL_WEEK_TO_DATE)
                    || timePeriod.equals(Constants.PAST_7_DAYES) || timePeriod.equals(Constants.TODAY)
                    || timePeriod.equals(Constants.YESTERDAY) || timePeriod.equals(Constants.MONTH_TO_DATE)
                    || timePeriod.equals(Constants.FINANCIAL_PERIOD_TO_DATE)) {
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
            } else if (timePeriod.equals(Constants.LAST_MONTH) || timePeriod.equals(Constants.LAST_QUARTER)
                    || timePeriod.equals(Constants.YEAR_TO_DATE) || timePeriod.equals(Constants.LAST_YEAR_YTD)) {
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
                    while (!selectedOption.equals(timePeriod)) {
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
        } catch (Exception e) {
            response.setStatus(false);
            response.setMessage("Failed to choose business date");
        }
        return response;
    }

    private Response chooseDaysDateOHRA(String syncFromDate, String syncToDate, WebDriver driver) throws ParseException {
        driver.findElement(By.id("clear0")).click();
        Response response = new Response();

        DateFormat Date = DateFormat.getDateInstance();
        Date fromDate = new SimpleDateFormat("yyyy-MM-dd").parse(syncFromDate);
        Date toDate = new SimpleDateFormat("yyyy-MM-dd").parse(syncToDate);

        SimpleDateFormat format = new SimpleDateFormat("EEEE");
        Calendar cals = Calendar.getInstance();

        cals.setTime(fromDate);
        String fromDateFormatted = Date.format(cals.getTime());
        String fromDayName = format.format(fromDate);

        cals.setTime(toDate);
        String toDateFormatted = Date.format(cals.getTime());
        String toDayName = format.format(toDate);

        List<WebElement> fromDateElements = driver.findElements(By.cssSelector("*[title='Select "
                + fromDayName + ", " + fromDateFormatted + "']"));

        fromDateElements.get(0).click();

        if (!toDateFormatted.equals(fromDateFormatted)) {
            List<WebElement> toDateElements = driver.findElements(By.cssSelector("*[title='Select "
                    + toDayName + ", " + toDateFormatted + "']"));
            Actions actions = new Actions(driver);
            actions.keyDown(Keys.LEFT_CONTROL)
                    .click(toDateElements.get(0))
                    .keyUp(Keys.LEFT_CONTROL)
                    .build()
                    .perform();
        }

        response.setStatus(true);
        return response;
    }

    private Response chooseMonthsDateOHRA(String syncFromDate, String syncToDate, WebDriver driver) throws ParseException {
        driver.findElement(By.id("clear0")).click();
        Response response = new Response();
        try {
            Select businessDate = new Select(driver.findElement(By.id("selectYear")));
            businessDate.selectByVisibleText(syncFromDate.split("-")[0]);
        } catch (Exception e) {
            String message = "Chosen year out of range";
            response.setStatus(false);
            response.setMessage(message);
        }

        Date fromDate = new SimpleDateFormat("yyyy-MM-dd").parse(syncFromDate);

        int startMonth = Integer.parseInt(syncFromDate.split("-")[1]);
        int endMonth = Integer.parseInt(syncToDate.split("-")[1]);

        if(endMonth < startMonth){
            int tempDate = startMonth;
            startMonth = endMonth;
            endMonth = tempDate;
        }

        Calendar cals = Calendar.getInstance();
        cals.setTime(fromDate);

        String fromMonthName = new SimpleDateFormat("MMMM").format(cals.getTime());
        WebElement fromDateElement = driver.findElement(By.linkText(fromMonthName + " " +
                syncFromDate.split("-")[0]));
        fromDateElement.click();

        DateFormatSymbols dfs = new DateFormatSymbols();
        String[] months = dfs.getMonths();
        String toMonthName;

        while(startMonth != endMonth){
            toMonthName = months[startMonth];
            startMonth++;

            WebElement toDateElements = driver.findElement(By.linkText(toMonthName + " " +
                    syncToDate.split("-")[0]));

            if(toDateElements.isDisplayed()){
                Actions actions = new Actions(driver);
                actions.keyDown(Keys.LEFT_CONTROL)
                        .click(toDateElements)
                        .keyUp(Keys.LEFT_CONTROL)
                        .build()
                        .perform();
            }else {
                response.setStatus(false);
                return response;
            }
        }

        response.setStatus(true);
        return response;
    }
}


