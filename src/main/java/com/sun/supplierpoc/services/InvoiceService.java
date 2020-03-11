package com.sun.supplierpoc.services;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.controllers.InvoiceController;
import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.models.configurations.CostCenter;
import com.sun.supplierpoc.repositories.SyncJobDataRepo;
import com.sun.supplierpoc.seleniumMethods.SetupEnvironment;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Service
public class InvoiceService {

    @Autowired
    private SyncJobDataRepo syncJobDataRepo;
    @Autowired
    private InvoiceController invoiceController;

    private Conversions conversions = new Conversions();
    private SetupEnvironment setupEnvironment = new SetupEnvironment();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public HashMap<String, Object> getInvoicesData(Boolean typeFlag, SyncJobType syncJobType, Account account){
        HashMap<String, Object> data = new HashMap<>();

        WebDriver driver = setupEnvironment.setupSeleniumEnv(false);
        if (driver == null){
            data.put("status", Constants.FAILED);
            data.put("message", "Failed to establish connection with firefox driver.");
            data.put("invoices", new ArrayList<>());
            return data;
        }

        ArrayList<HashMap<String, Object>> invoices = new ArrayList<>();
        ArrayList<CostCenter> costCenters = syncJobType.getConfiguration().getCostCenters();

        try {
            String url = "https://mte03-ohim-prod.hospitality.oracleindustry.com/Webclient/FormLogin.aspx";

            if (!setupEnvironment.loginOHIM(driver, url, account)){
                driver.quit();

                data.put("status", Constants.FAILED);
                data.put("message", "Invalid username and password.");
                data.put("invoices", invoices);
                return data;
            }

            String approvedInvoices = "https://mte03-ohim-prod.hospitality.oracleindustry.com/Webclient/Purchase/Invoicing/IvcOverviewView.aspx?type=1";
            driver.get(approvedInvoices);

            Select select = new Select(driver.findElement(By.id("_ctl5")));
            select.selectByVisibleText(syncJobType.getConfiguration().getTimePeriod());

            if (typeFlag){
                driver.findElement(By.id("igtxttbxInvoiceFilter")).sendKeys("RTV");
            }

            driver.findElement(By.name("filterPanel_btnRefresh")).click();

            // wait until loading finished "tableLoadingBar"
            try{
                WebDriverWait wait = new WebDriverWait(driver, 60);
                wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("tableLoadingBar")));

            } catch (Exception e) {
                driver.quit();

                data.put("status", Constants.FAILED);
                data.put("message", "Oracle Hospitality takes long time to load, Please try agin after few minutes.");
                data.put("invoices", invoices);
                return data;
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

                    // check if cost center choosen
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

                    // Mock supplier of nw
                    HashMap<String, String> supplierData = new HashMap<>();
                    supplierData.put("accountCode", "64101");
                    supplierData.put("supplier", "PKP France S.A.");

                    SyncJobData supplier = new SyncJobData(supplierData, Constants.RECEIVED, "", new Date(), "");

                    invoice.put(columns.get(columns.indexOf("vendor")), supplier);

                    for (int j = 0; j < cols.size(); j++) {
                        if (j == columns.indexOf("cost_center") || j == columns.indexOf("vendor"))
                            continue;
                        invoice.put(columns.get(j), cols.get(j).getText());
                    }
                    invoices.add(invoice);
                }

                // check if there is other pages
                if (driver.findElements(By.linkText("Next")).size() == 0){
                    break;
                }
                else {
                    TransferService.checkPagination(driver);
                    bodyTable = driver.findElement(By.id("G_dg"));
                    rows = bodyTable.findElements(By.tagName("tr"));
                }
            }

            driver.quit();

            data.put("status", Constants.SUCCESS);
            data.put("message", "");
            data.put("invoices", invoices);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            driver.quit();

            data.put("status", Constants.FAILED);
            data.put("message", e.getMessage());
            data.put("invoices", invoices);
            return data;
        }

    }

    public ArrayList<SyncJobData> saveInvoicesData(ArrayList<HashMap<String, Object>> invoices, SyncJob syncJob,
                                                   Boolean flag){
        ArrayList<SyncJobData> addedInvoices = new ArrayList<>();

        for (HashMap<String, Object> invoice : invoices) {
            HashMap<String, String> costCenter = (HashMap<String, String>) invoice.get("cost_center");
            SyncJobData supplier = (SyncJobData) invoice.get("vendor");

            // Invoice Part
            if (!flag) {
                if (((String)invoice.get("invoice_no.")).substring(0, 3).equals("RTV")) {
                    continue;
                }
            }

            HashMap<String, String> data = new HashMap<>();

            data.put("invoiceNo", invoice.get("invoice_no.") == null? "0": (String)invoice.get("invoice_no."));

            data.put("from_cost_center", supplier.getData().get("supplier") == null? "0":supplier.getData().get("supplier"));
            data.put("from_account_code", supplier.getData().get("accountCode") == null? "0":supplier.getData().get("accountCode"));

            data.put("to_cost_center", costCenter.get("costCenter") == null? "0":costCenter.get("costCenter"));
            data.put("to_account_code", costCenter.get("costCenter") == null? "0":costCenter.get("accountCode"));

            data.put("status", invoice.get("status") == null? "0":(String)invoice.get("status"));
            data.put("invoiceDate", invoice.get("invoice_date") == null? "0":(String)invoice.get("invoice_date"));
            data.put("net", invoice.get("net") == null? "0":(String)invoice.get("net"));
            data.put("vat", invoice.get("vat") == null? "0":(String)invoice.get("vat"));
            data.put("total", invoice.get("gross") == null? "0": String.valueOf(conversions.convertStringToFloat((String)invoice.get("gross"))));
            data.put("createdBy", invoice.get("created_by") == null? "0":(String)invoice.get("created_by"));
            data.put("createdAt", invoice.get("created_at") == null? "0":(String)invoice.get("created_at"));
            // Fixed for now
            data.put("overGroup", "FOOD");


            if (!flag)
                data.put("description", "Invoice From "+ supplier.getData().get("supplier") + " to " + costCenter.get("costCenter"));
            else
                data.put("description", "Credit Note From "+ supplier.getData().get("supplier") + " to " + costCenter.get("costCenter"));

            data.put("transactionReference", invoice.get("invoice_no.") == null? "0": (String)invoice.get("invoice_no."));


            SyncJobData syncJobData = new SyncJobData(data, Constants.RECEIVED, "", new Date(),
                    syncJob.getId());
            syncJobDataRepo.save(syncJobData);

            addedInvoices.add(syncJobData);
        }
        return addedInvoices;

    }

}
