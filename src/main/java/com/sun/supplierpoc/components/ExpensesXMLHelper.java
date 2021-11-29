package com.sun.supplierpoc.components;

import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.models.GeneralSettings;
import com.sun.supplierpoc.models.SyncJob;
import com.sun.supplierpoc.models.SyncJobData;
import com.sun.supplierpoc.models.configurations.BookingConfiguration;
import com.sun.supplierpoc.models.opera.booking.*;
import com.sun.supplierpoc.services.SyncJobDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class ExpensesXMLHelper {
    @Autowired
    SyncJobDataService syncJobDataService;

    private Conversions conversions = new Conversions();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /* Expenses Exported from OPERA Reports*/
    public List<SyncJobData> getExpensesUpdateFromXLS(SyncJob syncJob, String filePath, GeneralSettings generalSettings,
                                                          BookingConfiguration configuration) {
        List<SyncJobData> syncJobDataList = new ArrayList<>();

        double unitPrice = 0;
        double vat = 0;
        double vatPercent = 0;
        double serviceCharge = 0;
        double serviceChargePercent = 0;
        double municipalityTax= 0;
        double municipalityTaxPercent= 0;

        String[] generatesArray;

        BookingType paymentType = new BookingType();

        ExpenseObject expenseObject = new ExpenseObject();
        ExpenseItem expenseItem = new ExpenseItem();

        ArrayList<String> neglectedGroupCodes = configuration.neglectedGroupCodes;

        try {
            File file = new File(filePath);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            doc.getDocumentElement().normalize();

            NodeList list = doc.getElementsByTagName("G_C18");

            for (int temp = 0; temp < list.getLength(); temp++) {
                serviceChargePercent = 0; vatPercent = 0; municipalityTaxPercent = 0;

                // Read Expenses row
                ExpenseRow expenseRow = readExpensesRow(list, temp, generalSettings);

                ArrayList<SyncJobData> dataList = syncJobDataService.getSyncJobDataByBookingNo(expenseRow.bookingNo.strip());
//                if (dataList.size() > 0) {
//                    expenseObject.transactionId = (String) dataList.get(0).getData().get("transactionId");
//                } else {
//                    continue;
//                }

                // Skip neglected group code
                if(neglectedGroupCodes.contains(expenseRow.codeGroup))
                    continue;

                unitPrice = conversions.roundUpDouble(expenseRow.transactionAmount);

                generatesArray = expenseRow.generates.split("%");
                if(generatesArray.length >= 1 && !generatesArray[generatesArray.length -1].equals("")){
                    try {
                        vatPercent = Integer.parseInt(generatesArray[generatesArray.length -1]);
                    } catch (Exception e) {
                        vatPercent = 0;
                    }
                }
                if(generatesArray.length >= 2 && !generatesArray[generatesArray.length -2].equals(""))
                    municipalityTaxPercent = Integer.parseInt(generatesArray[generatesArray.length -2]);
                if(generatesArray.length >= 3 && !generatesArray[generatesArray.length -3].equals(""))
                    serviceChargePercent = Integer.parseInt(generatesArray[generatesArray.length -3]);

                // Round up to nearest 2 digests
                serviceCharge = conversions.roundUpDouble(expenseRow.transactionAmount * serviceChargePercent)/100;
                municipalityTax = conversions.roundUpDouble(expenseRow.transactionAmount * municipalityTaxPercent)/100;
                vat = conversions.roundUpDouble((expenseRow.transactionAmount + municipalityTax) * vatPercent)/100;

                expenseItem.expenseDate = expenseRow.transactionDate;
                expenseItem.cuFlag = "1";
                expenseItem.discount = "0";

                expenseObject.roomNo = expenseRow.roomNo;
                expenseItem.paymentType = paymentType.getTypeId();
                expenseItem.expenseTypeId = expenseRow.transactionCode;

                expenseItem.vat = String.valueOf(vat);
                expenseItem.municipalityTax = String.valueOf(municipalityTax);
                expenseItem.unitPrice = String.valueOf(expenseRow.transactionAmount);
                expenseItem.grandTotal = String.valueOf(conversions.roundUpDouble(unitPrice + vat + municipalityTax + serviceCharge));

                if (expenseItem.expenseTypeId != 0 && !expenseItem.unitPrice.equals("0.0")) {
                    expenseObject.items.add(expenseItem);

                    HashMap<String, Object> data = new HashMap<>();
                    Field[] allFields = expenseObject.getClass().getDeclaredFields();
                    for (Field field : allFields) {
                        field.setAccessible(true);
                        Object value = field.get(expenseObject);
                        if (value != null && !value.equals("null")) {
                            data.put(field.getName(), value);
                        } else {
                            data.put(field.getName(), "");
                        }
                    }

                    SyncJobData syncJobData = new SyncJobData(data, "success", "", new Date(), syncJob.getId());
                    syncJobDataList.add(syncJobData);
                }

                expenseObject = new ExpenseObject();
                expenseItem = new ExpenseItem();

            }

            return syncJobDataList;
        } catch (Exception e) {
            throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
        }
    }

    private ExpenseRow readExpensesRow(NodeList list, int rowIndex , GeneralSettings generalSettings){
        ExpenseRow row = new ExpenseRow();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

        String generates = "";
        String typeName = "";

        ArrayList<BookingType> paymentTypes = generalSettings.getPaymentTypes();
        ArrayList<BookingType> expenseTypes = generalSettings.getExpenseTypes();

        Node node = list.item(rowIndex);

        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element element = (Element) node;
            row.bookingNo = element.getElementsByTagName("C18").item(0).getTextContent();

            try{
                row.roomNo = Integer.parseInt(element.getElementsByTagName("C42").item(0).getTextContent());
            } catch (NumberFormatException e) {
                if(!element.getElementsByTagName("C42").item(0).getTextContent().equals(""))
                    row.roomNo = -1;
            }

            row.codeGroup = element.getElementsByTagName("C48").item(0).getTextContent();

            typeName = element.getElementsByTagName("C54").item(0).getTextContent();
            row.transactionCode = conversions.checkExpenseTypeExistence(expenseTypes, typeName).getTypeId();

            row.transactionCodeDesc = element.getElementsByTagName("C57").item(0).getTextContent();

            row.transactionAmount = conversions.roundUpDouble(Double.parseDouble(element.getElementsByTagName("C69").item(0).getTextContent()));

            generates = element.getElementsByTagName("C78").item(0).getTextContent();
            row.generates = generates.replaceAll(",", "").replaceAll("\\s", "");

            typeName = element.getElementsByTagName("C96").item(0).getTextContent();
            row.paymentMethod = conversions.checkBookingTypeExistence(paymentTypes, typeName).getTypeId();

            String tempDate = element.getElementsByTagName("C87").item(0).getTextContent();
            if (!tempDate.equals("")){
                try{
                    if(tempDate.contains(".")){
                        Date updateDate = new SimpleDateFormat("dd.MM.yy").parse(tempDate);
                        if (updateDate != null)
                            row.transactionDate = dateFormat.format(updateDate);
                    }else if (tempDate.contains("-")){
                        Date updateDate = new SimpleDateFormat("dd-MMMM-yy").parse(tempDate);
                        if (updateDate != null)
                            row.transactionDate = dateFormat.format(updateDate);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return row;
    }

    /* Expenses Exported from OPERA Database */
    public List<SyncJobData> getExpensesUpdateFromDB(SyncJob syncJob, String filePath, GeneralSettings generalSettings,
                                                      BookingConfiguration configuration) {
        ExpenseItem expenseItem;
        ExpenseItem expenseItemTax;
        List<SyncJobData> syncJobDataList = new ArrayList<>();
        ArrayList<String> neglectedGroupCodes = configuration.neglectedGroupCodes;

        try {
            File file = new File(filePath);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            doc.getDocumentElement().normalize();

            NodeList list = doc.getElementsByTagName("G_RESV_NAME_ID");

            double unitPrice, vat, municipalityTax;

            String currentBookingNumber = "";
            String[] generatesArray;
            ArrayList<SyncJobData> dataList = new ArrayList<>();
            for (int temp = 0; temp < list.getLength(); temp++) {
                vat = 0; municipalityTax = 0;
                // Read Expenses row
                expenseItem = readExpensesRowDB(list, temp, generalSettings);

                // Loop over taxes
                if(!expenseItem.generates.equals("")){
                    generatesArray = expenseItem.generates.split("%");
                    for (int i = 0; i < generatesArray.length; i++) {
                        temp++;
                        expenseItemTax = readExpensesRowDB(list, temp, generalSettings);

                        if(expenseItemTax.description.toLowerCase().contains("muncipality")){
                            expenseItem.municipalityTax = expenseItemTax.unitPrice;
                        }else if(expenseItemTax.description.toLowerCase().contains("vat")){
                            expenseItem.vat = expenseItemTax.unitPrice;
                        }
                    }
                }

                // Calculate Grand Total
                unitPrice = Double.parseDouble(expenseItem.unitPrice);
                if(!expenseItem.vat.equals(""))
                    vat = Double.parseDouble(expenseItem.vat);
                if(!expenseItem.municipalityTax.equals(""))
                    municipalityTax = Double.parseDouble(expenseItem.municipalityTax);

                expenseItem.grandTotal = String.valueOf(conversions.roundUpDouble(unitPrice + vat + municipalityTax));

                if(currentBookingNumber.equals("") || !currentBookingNumber.equals(expenseItem.bookingNo.strip())){
                    currentBookingNumber = expenseItem.bookingNo.strip();
                    dataList = syncJobDataService.getSyncJobDataByBookingNo(currentBookingNumber);
                }

                if (dataList.size() > 0) {
                    expenseItem.transactionId = (String) dataList.get(0).getData().get("transactionId");
                } else {
                    expenseItem.transactionId = "";
                }

                // Skip neglected group code
                if(neglectedGroupCodes.contains(expenseItem.itemNumber))
                    continue;

                if (!expenseItem.unitPrice.equals("0.0")) {
                    HashMap<String, Object> data = new HashMap<>();
                    Field[] allFields = expenseItem.getClass().getDeclaredFields();
                    for (Field field : allFields) {
                        field.setAccessible(true);
                        Object value = field.get(expenseItem);
                        if (value != null && !value.equals("null")) {
                            data.put(field.getName(), value);
                        } else {
                            data.put(field.getName(), "");
                        }
                    }

                    SyncJobData syncJobData = new SyncJobData(data, "success", "", new Date(), syncJob.getId());
                    syncJobDataList.add(syncJobData);
                }
            }

            return syncJobDataList;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse expenses details from XML file: " + e.getMessage());
        }
    }

    private ExpenseItem readExpensesRowDB(NodeList list, int rowIndex , GeneralSettings generalSettings){
        ExpenseItem item = new ExpenseItem();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

        String generates = "";
        String typeName = "";

        ArrayList<BookingType> paymentTypes = generalSettings.getPaymentTypes();
        ArrayList<BookingType> expenseTypes = generalSettings.getExpenseTypes();

        Node node = list.item(rowIndex);

        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element element = (Element) node;
            item.bookingNo = element.getElementsByTagName("RESV_NAME_ID").item(0).getTextContent();

            try{
                item.roomNo = Integer.parseInt(element.getElementsByTagName("ROOM").item(0).getTextContent());
            } catch (NumberFormatException e) {
                if(!element.getElementsByTagName("ROOM").item(0).getTextContent().equals(""))
                    item.roomNo = -1;
            }

            item.unitPrice = String.valueOf(conversions.roundUpDouble(Double.parseDouble(element.getElementsByTagName("NET_AMOUNT").item(0).getTextContent())));
            item.itemNumber = element.getElementsByTagName("TRX_CODE").item(0).getTextContent();
            item.description = element.getElementsByTagName("DESCRIPTION").item(0).getTextContent();

            typeName = element.getElementsByTagName("TC_GROUP").item(0).getTextContent();
            item.expenseTypeId = conversions.checkBookingTypeExistence(expenseTypes, typeName).getTypeId();

            generates = element.getElementsByTagName("TAX_ELEMENTS").item(0).getTextContent();
            item.generates = generates.replaceAll(",", "").replaceAll("\\s", "");

            typeName = element.getElementsByTagName("PAYMENT_METHOD").item(0).getTextContent();
            item.paymentType = conversions.checkBookingTypeExistence(paymentTypes, typeName).getTypeId();

            String tempDate = element.getElementsByTagName("TRX_DATE").item(0).getTextContent();
            if (!tempDate.equals("")){
                try{
                    if(tempDate.contains(".")){
                        Date updateDate = new SimpleDateFormat("dd.MM.yy").parse(tempDate);
                        if (updateDate != null)
                            item.expenseDate = dateFormat.format(updateDate);
                    }else if (tempDate.contains("-")){
                        Date updateDate = new SimpleDateFormat("dd-MMMM-yy").parse(tempDate);
                        if (updateDate != null)
                            item.expenseDate = dateFormat.format(updateDate);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return item;
    }

}

