package com.sun.supplierpoc.services;


import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.controllers.InvoiceController;
import com.sun.supplierpoc.models.Journal;
import com.sun.supplierpoc.models.SyncJob;
import com.sun.supplierpoc.models.SyncJobData;
import com.sun.supplierpoc.models.SyncJobType;
import com.sun.supplierpoc.repositories.SyncJobDataRepo;
import com.sun.supplierpoc.seleniumMethods.SetupEnvironment;
import com.sun.supplierpoc.soapModels.JournalSSC;
import com.systemsunion.security.IAuthenticationVoucher;
import com.systemsunion.ssc.client.ComponentException;
import com.systemsunion.ssc.client.SecurityProvider;
import com.systemsunion.ssc.client.SoapComponent;
import com.systemsunion.ssc.client.SoapFaultException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class JournalService {
    static int PORT = 8080;
    static String HOST= "192.168.1.21";

    @Autowired
    SyncJobDataRepo syncJobDataRepo;
    @Autowired
    InvoiceController invoiceController;

    public SetupEnvironment setupEnvironment = new SetupEnvironment();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public HashMap<String, Object> getJournalData(SyncJobType syncJobType){
        HashMap<String, Object> data = new HashMap<>();

        WebDriver driver = setupEnvironment.setupSeleniumEnv(false);
        ArrayList<HashMap<String, String>> journals = new ArrayList<>();

        ArrayList<HashMap<String, String>> costCenters = (ArrayList<HashMap<String, String>>) syncJobType.getConfiguration().get("costCenters");

        try {
            String url = "https://mte03-ohra-prod.hospitality.oracleindustry.com/servlet/PortalLogIn/";

            if (!setupEnvironment.loginOHRA(driver, url)){
                driver.quit();

                data.put("status", Constants.FAILED);
                data.put("message", "Invalid username and password.");
                data.put("journals", journals);
                return data;
            }

            String journalUrl = "https://mte03-ohra-prod.hospitality.oracleindustry.com/finengine/reportRunAction.do?rptroot=499&method=run&reportID=myInvenCOSByCC";
            driver.get(journalUrl);

            List<WebElement> rows = driver.findElements(By.tagName("tr"));

            ArrayList<String> columns = setupEnvironment.getTableColumns(rows, false, 5);

            ArrayList<HashMap<String, String>> selectedCostCenters = new ArrayList<>();

            for (int i = 6; i < rows.size(); i++) {
                HashMap<String, String> journal = new HashMap<>();

                WebElement row = rows.get(i);
                List<WebElement> cols = row.findElements(By.tagName("td"));
                if (cols.size() != columns.size()) {
                    continue;
                }

                WebElement td = cols.get(columns.indexOf("cost_center"));
                HashMap<String, Object> oldCostCenterData = invoiceController.checkExistence(costCenters, td.getText().strip());

                if (!(boolean) oldCostCenterData.get("status")) {
                    continue;
                }

                journal.put("extensions", cols.get(0).getAttribute("onclick"));
                journal.put(columns.get(0), cols.get(0).getText());

                selectedCostCenters.add(journal);
            }

            String baseURL = "https://mte03-ohra-prod.hospitality.oracleindustry.com";

            for (HashMap<String, String> costCenter : selectedCostCenters) {
                try {
                    driver.get(baseURL + costCenter.get("extensions"));

                    rows = driver.findElements(By.tagName("tr"));

                    columns = setupEnvironment.getTableColumns(rows, true, 5);

                    for (int i = 6; i < rows.size(); i++) {
                        Journal journal = new Journal();
                        // check if the item belone to selected items
                        // lw belong agyb al over group bt3ha


                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


            driver.quit();

            data.put("status", Constants.SUCCESS);
            data.put("message", "");
            data.put("journals", journals);
            return data;

        } catch (Exception e) {
            e.printStackTrace();
            driver.quit();

            data.put("status", Constants.FAILED);
            data.put("message", e);
            data.put("journals", journals);
            return data;
        }
    }

    public ArrayList<SyncJobData> saveJournalData(ArrayList<HashMap<String, String>> journals, SyncJob syncJob){
        ArrayList<SyncJobData> addedJournals = new ArrayList<>();

        for (HashMap<String, String> journal : journals) {
            HashMap<String, String> data = new HashMap<>();

            data.put("journal", journal.get("journal"));

            SyncJobData syncJobData = new SyncJobData(data, Constants.RECEIVED, "", new Date(),
                    syncJob.getId());
            syncJobDataRepo.save(syncJobData);

            addedJournals.add(syncJobData);
        }
        return addedJournals;

    }

}
