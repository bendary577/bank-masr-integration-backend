package com.sun.supplierpoc.services;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.controllers.InvoiceController;
import com.sun.supplierpoc.controllers.SyncJobDataController;
import com.sun.supplierpoc.controllers.SyncJobTypeController;
import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.models.configurations.CostCenter;
import com.sun.supplierpoc.repositories.SyncJobDataRepo;
import com.sun.supplierpoc.seleniumMethods.SetupEnvironment;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Service
public class InvoiceService {
    @Autowired
    private SyncJobDataRepo syncJobDataRepo;
    @Autowired
    private SyncJobDataController syncJobTypeController;

    private Conversions conversions = new Conversions();
    private SetupEnvironment setupEnvironment = new SetupEnvironment();
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public HashMap<String, Object> getInvoicesData(Boolean typeFlag, SyncJobType syncJobType, ArrayList<CostCenter> costCenters,
                                                   Account account){
        HashMap<String, Object> response = new HashMap<>();

        WebDriver driver;
        try{
            driver = setupEnvironment.setupSeleniumEnv(false);
        }
        catch (Exception ex){
            response.put("status", Constants.FAILED);
            response.put("message", "Failed to establish connection with firefox driver.");
            response.put("invoices", new ArrayList<>());
            return response;
        }

        ArrayList<HashMap<String, Object>> invoices = new ArrayList<>();

        try {
            String url = "https://mte03-ohim-prod.hospitality.oracleindustry.com/Webclient/FormLogin.aspx";

            if (!setupEnvironment.loginOHIM(driver, url, account)){
                driver.quit();

                response.put("status", Constants.FAILED);
                response.put("message", "Invalid username and password.");
                response.put("invoices", invoices);
                return response;
            }

            String approvedInvoices = "https://mte03-ohim-prod.hospitality.oracleindustry.com/Webclient/Purchase/Invoicing/IvcOverviewView.aspx?type=1";
            driver.get(approvedInvoices);

            Select select = new Select(driver.findElement(By.id("_ctl5")));
            String timePeriod = syncJobType.getConfiguration().getTimePeriod();
            select.selectByVisibleText(timePeriod);

            if (timePeriod.equals("User-defined")){
                DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
                Date date = new Date();

                driver.findElement(By.id("_ctl7_input")).clear();
                driver.findElement(By.id("_ctl7_input")).sendKeys(dateFormat.format(date));
                driver.findElement(By.id("_ctl7_input")).sendKeys(Keys.ENTER);

                driver.findElement(By.id("_ctl9_input")).clear();
                driver.findElement(By.id("_ctl9_input")).sendKeys(dateFormat.format(date));
                driver.findElement(By.id("_ctl9_input")).sendKeys(Keys.ENTER);

                String startDateValue = driver.findElement(By.id("_ctl7_input")).getAttribute("value");
                Date startDate = dateFormat.parse(startDateValue);

                String endDateValue = driver.findElement(By.id("_ctl9_input")).getAttribute("value");
                Date endDate = dateFormat.parse(endDateValue);

                if (!dateFormat.format(startDate).equals(dateFormat.format(date))){
                    driver.quit();

                    response.put("status", Constants.FAILED);
                    response.put("message", "Failed to get invoices of today, please try again or contact support team.");
                    response.put("invoices", invoices);
                    return response;
                }

                if (!dateFormat.format(endDate).equals(dateFormat.format(date))){
                    driver.quit();

                    response.put("status", Constants.FAILED);
                    response.put("message", "Failed to get invoices of today, please try again or contact support team.");
                    response.put("invoices", invoices);
                    return response;
                }

            }

            if (typeFlag){
                driver.findElement(By.id("igtxttbxInvoiceFilter")).sendKeys("RTV");
            }

            driver.findElement(By.name("filterPanel_btnRefresh")).click();

            try{
                WebDriverWait wait = new WebDriverWait(driver, 60);
                wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("tableLoadingBar")));

            } catch (Exception e) {
                driver.quit();

                response.put("status", Constants.FAILED);
                response.put("message", "Oracle Hospitality takes long time to load, Please try again after few minutes.");
                response.put("invoices", invoices);
                return response;
            }
            try{
                WebDriverWait wait = new WebDriverWait(driver, 60);
                wait.until(ExpectedConditions.presenceOfElementLocated(By.id("G_dg")));

            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

            WebElement bodyTable = driver.findElement(By.id("G_dg"));
            WebElement headerTable = driver.findElement(By.xpath("/html/body/form/table/tbody/tr[4]/td/table/tbody/tr[1]/td/div/table"));

            List<WebElement> headerRows = headerTable.findElements(By.tagName("tr"));
            List<WebElement> rows = bodyTable.findElements(By.tagName("tr"));

            ArrayList<String> columns = setupEnvironment.getTableColumns(headerRows, true, 0);

            while (true){
                for (int i = 1; i < rows.size(); i++) {
                    HashMap<String, Object> invoice = new HashMap<>();

                    WebElement row = rows.get(i);
                    List<WebElement> cols = row.findElements(By.tagName("td"));
                    if (cols.size() !=  columns.size()){
                        continue;
                    }

                    // check if cost center chosen
                    WebElement td = cols.get(columns.indexOf("cost_center"));
                    CostCenter oldCostCenterData = conversions.checkCostCenterExistence(costCenters, td.getText().strip(), false);

                    if (!oldCostCenterData.checked) {
                        continue;
                    }

                    invoice.put(columns.get(columns.indexOf("cost_center")), oldCostCenterData);

                    // check if vendor exits in middleware
    //                    td = cols.get(columns.indexOf("vendor"));
    //                    ArrayList<SyncJobData> suppliers = syncJobDataController.getSyncJobData(syncJobType.getId());
    //                    HashMap<String, Object> oldSupplierData = conversions.checkSupplierExistence(suppliers, td.getText().strip());
    //
    //                    if (!(boolean) oldSupplierData.get("status")) {
    //                        continue;
    //                    }
    //                    invoice.put(columns.get(columns.indexOf("vendor")), oldSupplierData.get("supplier"));

                    // Mock supplier of now
                    HashMap<String, String> supplierData = new HashMap<>();
                    supplierData.put("accountCode", "64101");
                    supplierData.put("supplier", "PKP France S.A.");

                    SyncJobData supplier = new SyncJobData(supplierData, Constants.RECEIVED, "", new Date(), "");

                    invoice.put(columns.get(columns.indexOf("vendor")), supplier);

                    String link = cols.get(columns.indexOf("invoice_no.")).findElement(By.tagName("a")).getAttribute("href");
                    link = link.substring(link.indexOf('(') + 1, link.indexOf(','));
                    String fullLink = "https://mte03-ohim-prod.hospitality.oracleindustry.com/Webclient/Purchase/Invoicing/InvoiceDetail.aspx?RNG_ID=" + link;
                    invoice.put("reference_link", fullLink);

                    for (int j = 0; j < cols.size(); j++) {
                        if (j == columns.indexOf("cost_center") || j == columns.indexOf("vendor"))
                            continue;
                        invoice.put(columns.get(j), cols.get(j).getText().strip());
                    }
                    invoices.add(invoice);
                }

                // check if there is other pages
                if (driver.findElements(By.linkText("Next")).size() == 0){
                    break;
                }
                else {
                    TransferService.checkPagination(driver, "dg_rc_0_1");
                    bodyTable = driver.findElement(By.id("G_dg"));
                    rows = bodyTable.findElements(By.tagName("tr"));
                }
            }

            for (HashMap<String, Object> invoice:invoices) {
                String reference = getInvoicesReceipt(driver, (String) invoice.get("reference_link"));
                invoice.put("reference", reference);
            }

            driver.quit();

            response.put("status", Constants.SUCCESS);
            response.put("message", "");
            response.put("invoices", invoices);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            driver.quit();

            response.put("status", Constants.FAILED);
            response.put("message", e.getMessage());
            response.put("invoices", invoices);
            return response;
        }

    }

    public String getInvoicesReceipt(WebDriver driver, String link){
        String reference = "";
        try{
            driver.get(link);

            WebDriverWait wait = new WebDriverWait(driver, 20);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("igtxtdfReference")));

            reference = driver.findElement(By.id("igtxtdfReference")).getAttribute("value");
            return reference;
        }
        catch (Exception ex){
            return reference;
        }
    }

    public ArrayList<SyncJobData> saveInvoicesData(ArrayList<HashMap<String, Object>> invoices, SyncJob syncJob,
                                                   SyncJobType syncJobType, Boolean flag){
        ArrayList<SyncJobData> addedInvoices = new ArrayList<>();
        ArrayList<SyncJobData> savedInvoices = syncJobTypeController.getSyncJobData(syncJobType.getId());

        for (HashMap<String, Object> invoice : invoices) {
            // check existence of invoice in middleware (UNIQUE: invoiceNo)
            SyncJobData oldInvoice = conversions.checkInvoiceExistence(savedInvoices, (String)invoice.get("invoice_no."));
            if (oldInvoice != null){
                if (!oldInvoice.getStatus().equals(Constants.FAILED)){
                    continue;
                }
            }

            CostCenter costCenter = (CostCenter) invoice.get("cost_center");
            SyncJobData supplier = (SyncJobData) invoice.get("vendor");

            if (costCenter.costCenterReference.equals("")){
                costCenter.costCenterReference = costCenter.costCenter;
            }

            // Invoice Part
            if (!flag) {
                if (((String)invoice.get("invoice_no.")).substring(0, 3).equals("RTV")) {
                    continue;
                }
            }

            HashMap<String, String> data = new HashMap<>();

            data.put("invoiceNo", invoice.get("invoice_no.") == null? "0": (String)invoice.get("invoice_no."));
            if (((String)invoice.get("reference")).equals("")){
                data.put("reference", invoice.get("invoice_no.") == null? "0": (String)invoice.get("invoice_no."));
                data.put("transactionReference", invoice.get("invoice_no.") == null? "0": (String)invoice.get("invoice_no."));
            }
            else {
                data.put("reference", invoice.get("reference") == null? "0": (String)invoice.get("reference"));
                data.put("transactionReference", invoice.get("reference") == null? "0": (String)invoice.get("reference"));
            }

            data.put("from_cost_center", supplier.getData().get("supplier") == null? "0":supplier.getData().get("supplier"));
            data.put("from_account_code", supplier.getData().get("accountCode") == null? "0":supplier.getData().get("accountCode"));

            data.put("to_cost_center", costCenter.costCenter == null? "0":costCenter.costCenterReference);
            data.put("to_account_code", costCenter.costCenter == null? "0":costCenter.accountCode);

            data.put("status", invoice.get("status") == null? "0":(String)invoice.get("status"));
            data.put("invoiceDate", invoice.get("invoice_date") == null? "0":(String)invoice.get("invoice_date"));
            data.put("net", invoice.get("net") == null? "0": String.valueOf(Math.round(conversions.convertStringToFloat((String)invoice.get("net")))));
            data.put("vat", invoice.get("vat") == null? "0": String.valueOf(Math.round(conversions.convertStringToFloat((String)invoice.get("vat")))));
            data.put("totalCr", invoice.get("gross") == null? "0": String.valueOf(Math.round(conversions.convertStringToFloat((String)invoice.get("gross")))));
            data.put("totalDr", invoice.get("gross") == null? "0": String.valueOf(Math.round(conversions.convertStringToFloat((String)invoice.get("gross")) * -1)));
            data.put("createdBy", invoice.get("created_by") == null? "0":(String)invoice.get("created_by"));
            data.put("createdAt", invoice.get("created_at") == null? "0":(String)invoice.get("created_at"));

            data.put("overGroup", "General");

            data.put("inventoryAccount", syncJobType.getConfiguration().getInventoryAccount());
            data.put("expensesAccount", syncJobType.getConfiguration().getExpensesAccount());

            if (!flag)
                data.put("description", "Invoice From "+ supplier.getData().get("supplier") + " to " + costCenter.costCenterReference);
            else
                data.put("description", "Credit Note From "+ supplier.getData().get("supplier") + " to " + costCenter.costCenterReference);

            SyncJobData syncJobData = new SyncJobData(data, Constants.RECEIVED, "", new Date(),
                    syncJob.getId());
            syncJobDataRepo.save(syncJobData);

            addedInvoices.add(syncJobData);
        }
        return addedInvoices;

    }

}
