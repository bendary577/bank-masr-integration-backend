package com.sun.supplierpoc.components;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.models.GeneralSettings;
import com.sun.supplierpoc.models.SyncJob;
import com.sun.supplierpoc.models.SyncJobData;
import com.sun.supplierpoc.models.SyncJobType;
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
    public List<SyncJobData> getExpensesUpdateFromDB(SyncJob syncJob, SyncJobType syncJobType,
                                                     SyncJobType bookingSyncJobType, String filePath,
                                                     GeneralSettings generalSettings, BookingConfiguration configuration) {
        double unitPrice, vat, municipalityTax, grandTotal;

        String currentBookingNumber = "";
        ExpenseObject expenseObject = new ExpenseObject();
        HashMap<String, Object> expenseObjectData;

        ExpenseItem expenseItem;
        ExpenseItem expenseItemTax;

        String[] generatesArray;
        ArrayList<SyncJobData> dataList = new ArrayList<>();
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

            for (int temp = 0; temp < list.getLength(); temp++) {
                vat = 0; municipalityTax = 0; grandTotal = 0;
                // Read Expenses row
                expenseItem = readExpensesRowDB(list, temp, generalSettings);

                // Loop over taxes
                if(!expenseItem.generates.equals("")){
                    generatesArray = expenseItem.generates.split("%");
                }else{
                    generatesArray = new String[0];
                }

                // Skip neglected group code
                if(neglectedGroupCodes.contains(expenseItem.itemNumber)){
                    temp += generatesArray.length;
                    continue;
                }

                for (int i = 0; i < generatesArray.length; i++) {
                    temp++;
                    expenseItemTax = readExpensesRowDB(list, temp, generalSettings);

                    if(expenseItemTax.description.toLowerCase().contains("municipality")){
                        expenseItem.municipalityTax = expenseItemTax.unitPrice;
                    }else if(expenseItemTax.description.toLowerCase().contains("vat")){
                        expenseItem.vat = expenseItemTax.unitPrice;
                    }
                }

                // Calculate Grand Total
                unitPrice = Double.parseDouble(expenseItem.unitPrice);
                if(!expenseItem.vat.equals(""))
                    vat = Double.parseDouble(expenseItem.vat);
                if(!expenseItem.municipalityTax.equals(""))
                    municipalityTax = Double.parseDouble(expenseItem.municipalityTax);

                grandTotal = unitPrice + vat + municipalityTax;
                expenseItem.grandTotal = String.valueOf(conversions.roundUpDouble(grandTotal));

                /* New Expense Object */
                if(!currentBookingNumber.equals(expenseItem.bookingNo.strip())){
                    /* Save old one */
                    saveExpenseObject(syncJob, syncJobType, expenseObject, syncJobDataList);

                    currentBookingNumber = expenseItem.bookingNo.strip();
                    dataList = syncJobDataService.getSyncJobDataByBookingNoAndType(currentBookingNumber,
                            bookingSyncJobType.getId());

                    expenseObject = new ExpenseObject();

                    expenseObject.roomNo = expenseItem.roomNo;
                    expenseObject.bookingNo = currentBookingNumber;
                    expenseObject.channel = configuration.getChannel();

                    if (dataList.size() > 0) {
                        expenseObject.transactionId = (String) dataList.get(0).getData().get("transactionId");

                        /* Check Existence */
                        dataList = syncJobDataService.getSyncJobDataByBookingNoAndType(currentBookingNumber, syncJobType.getId());
                    } else {
                        expenseObject.transactionId = "";
                        dataList = new ArrayList<>();
                    }
                }

                boolean addItem = true;
                for (ExpenseItem item : expenseObject.items) {
                    if(item.itemNumber.equals(expenseItem.itemNumber)
                            && !item.expenseDate.equals(expenseItem.expenseDate)){
                        item.vat = String.valueOf(vat + Double.parseDouble(item.vat));
                        item.municipalityTax = String.valueOf(municipalityTax + Double.parseDouble(item.municipalityTax));
                        item.unitPrice = String.valueOf(unitPrice + Double.parseDouble(item.unitPrice));
                        item.grandTotal = String.valueOf(grandTotal + Double.parseDouble(item.grandTotal));
                        addItem = false;
                        break;
                    }
                }

                if(dataList.size() > 0 && addItem){
                    expenseObjectData = dataList.get(dataList.size() -1).getData();
                    ArrayList<ExpenseItem> items = (ArrayList<ExpenseItem>) expenseObjectData.get("items");
                    /* Check if this item already exists */
                    for (ExpenseItem item : items) {
                        if(item.itemNumber.equals(expenseItem.itemNumber)){
                            expenseItem.cuFlag = "2"; // UPDATE ITEM
                            if(!item.expenseDate.equals(expenseItem.expenseDate)){
                                expenseItem.vat = String.valueOf(vat + Double.parseDouble(item.vat));
                                expenseItem.municipalityTax = String.valueOf(municipalityTax + Double.parseDouble(item.municipalityTax));
                                expenseItem.unitPrice = String.valueOf(unitPrice + Double.parseDouble(item.unitPrice));
                                expenseItem.grandTotal = String.valueOf(grandTotal + Double.parseDouble(item.grandTotal));
                            }
                            break;
                        }

                    }
                }

                if(addItem)
                    expenseObject.items.add(expenseItem);
            }

            saveExpenseObject(syncJob, syncJobType, expenseObject, syncJobDataList);

            return syncJobDataList;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse expenses details from XML file: " + e.getMessage());
        }
    }

    private void saveExpenseObject(SyncJob syncJob, SyncJobType syncJobType, ExpenseObject expenseObject, List<SyncJobData> syncJobDataList) throws IllegalAccessException {
        if (!expenseObject.bookingNo.equals("")) {
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

            SyncJobData syncJobData = new SyncJobData(data, Constants.RECEIVED, "",
                    new Date(), syncJob.getId(), syncJobType.getId());
            syncJobDataList.add(syncJobData);
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

            item.cuFlag = "1"; // NEW ITEM
        }
        return item;
    }



}

