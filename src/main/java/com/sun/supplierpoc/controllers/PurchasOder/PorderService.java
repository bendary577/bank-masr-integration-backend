package com.sun.supplierpoc.controllers.PurchasOder;

import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.repositories.AccountRepo;
import com.sun.supplierpoc.repositories.UserRepo;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class PorderService {

    Logger log = LoggerFactory.getLogger(PoController.class);

    @Autowired
    AccountRepo accountRepo;

    @Autowired
    UserRepo userRepo;

    @Autowired
    private SetupEnvironment2 setupEnvironment;

    public String CreatePurchase(Account account, PorderRequest porderRequest) throws InterruptedException {

        WebDriver driver = setupEnvironment.setupSeleniumEnv(false);

        WebDriverWait wait = new WebDriverWait(driver, 20);

        setupEnvironment.loginOHIM(driver,"https://mte3-ohim.oracleindustry.com/InventoryManagement/FormLogin.aspx", account);
        log.info("driver opened");

        driver.get("https://mte3-ohim.oracleindustry.com/InventoryManagement/Purchase/Ordering/PoCreate.aspx?type=1");
        log.info("go to PO");

        wait.until(ExpectedConditions.elementToBeClickable(By.id("btnCreate")));

        driver.findElement(By.id("cfVendor_Text")).sendKeys(porderRequest.getVendor());
        wait.until(ExpectedConditions.elementToBeClickable(By.id("CF_ResultContainer")));
        driver.findElement(By.id("cfVendor_Text")).sendKeys(Keys.ENTER);
        wait.until(ExpectedConditions.elementToBeClickable(By.tagName("nobr")));
        log.info("select vendor");
        TimeUnit.SECONDS.sleep(2);

        for(String itemGroup: porderRequest.getItemGroups()) {
            driver.findElement(By.id("igtxttbxSearch")).sendKeys(itemGroup + Keys.ENTER);
            TimeUnit.SECONDS.sleep(2);
            driver.findElement(By.id("igtxttbxSearch")).sendKeys(Keys.ENTER + "");
            driver.findElement(By.id("igtxttbxSearch")).clear();
            TimeUnit.SECONDS.sleep(2);
            driver.findElement(By.id("btnAssign")).click();
            log.info("items");
        }

        driver.findElement(By.id("igtxttbxName")).sendKeys(porderRequest.getReference());
        log.info("Reference");

        driver.findElement(By.id("dtDeliveryDate_input")).sendKeys(porderRequest.getDate());
        log.info("select date");

        driver.findElement(By.id("btnCreate")).click();

        log.info(porderRequest.getItems().get(1));
        log.info(""+ Integer.parseInt(porderRequest.getItemQuantity().get(1)) );

        TimeUnit.SECONDS.sleep(5);

        WebElement name = driver.findElement(By.xpath(".//tr//td[contains(nobr, '"+porderRequest.getItems().get(1)+"')]"));


//        System.out.print(row);
        //        row.findElements(By.cssSelector("ig_6e74c14b_r1")).get(0).click();

//        driver.findElement(row.)
//        driver.close();
        return "done";
    }

}





















//    User user = userRepo.findByUsername("admin");
//
//    Account account = accountRepo.findById(user.getAccountId())
//            .orElseThrow(() -> new RuntimeException("not"));
//
//    WebDriver driver = setupEnvironment.setupSeleniumEnv(false);
//
//    WebDriverWait wait = new WebDriverWait(driver, 20);
//
//        setupEnvironment.loginOHIM(driver, /*Constants.OHRA_LINK*/ "https://mte3-ohim.oracleindustry.com/InventoryManagement/FormLogin.aspx", account);
//
//        log.info("driver opened");
//
////        TimeUnit.SECONDS.sleep(10);
//
////        driver.findElement(By.id("sidemenu")).click();
//
//        driver.get("https://mte3-ohim.oracleindustry.com/InventoryManagement/Purchase/Ordering/PoCreate.aspx?type=1");
////      driver.get("https://mte03-ohim-prod.hospitality.oracleindustry.com/Webclient/Purchase/Receiving/RcvOverviewView.aspx?type=2");
//
//        log.info("go to PO");
//
////        driver.findElement(By.id("6Items")).click();
//        wait.until(ExpectedConditions.elementToBeClickable(By.id("btnCreate")));
//
//        driver.findElement(By.id("cfVendor_Text")).sendKeys("ABO International Supplies");
//        wait.until(ExpectedConditions.elementToBeClickable(By.id("CF_ResultContainer")));
//        driver.findElement(By.id("cfVendor_Text")).sendKeys(Keys.ENTER);
//
//        wait.until(ExpectedConditions.elementToBeClickable(By.tagName("nobr")));
//
//        log.info("select vendor");
//
//        driver.findElement(By.id("dtDeliveryDate")).sendKeys("08/05/2021");
//
//        log.info("select date");
//
////        driver.findElement(By.id("igtxttbxName")).sendKeys("test 5:55");
////
////        log.info("Reference");
////
//////        WebElement selectElement = driver.findElement(By.id("nobr"));
//////        Select selectObject = new Select(selectElement);
//////        selectObject.selectByValue("Beef");
////
////        driver.findElement(By.id("igtxttbxSearch")).sendKeys("Butter" + Keys.ENTER);
////
////        log.info("items");
////
////        driver.findElement(By.id("igtxttbxSearch")).sendKeys( Keys.ENTER);
////
////        driver.findElement(By.id("btnAssign")).click();

//        driver.findElement(By.partialLinkText("abc"));

//        driver.findElement(By.partialLinkText("abc"));

//        WebElement element = driver.findElement(By.id("dg_mtb"));
//        Select select = new Select(element);
//        select.selectByVisibleText("abc");
