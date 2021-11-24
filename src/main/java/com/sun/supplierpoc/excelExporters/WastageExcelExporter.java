package com.sun.supplierpoc.excelExporters;

import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.models.GeneralSettings;
import com.sun.supplierpoc.models.JournalBatch;
import com.sun.supplierpoc.models.SyncJobData;
import com.sun.supplierpoc.models.SyncJobType;
import com.sun.supplierpoc.models.configurations.CostCenter;
import com.sun.supplierpoc.models.configurations.Item;
import com.sun.supplierpoc.models.configurations.ItemGroup;
import com.sun.supplierpoc.models.configurations.OverGroup;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class WastageExcelExporter {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<SyncJobData> listSyncJobData;

    private CommonFunctions commonFunctions;
    private Conversions conversions = new Conversions();
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public WastageExcelExporter() {
        workbook = new XSSFWorkbook();
        this.commonFunctions = new CommonFunctions();
    }

    public WastageExcelExporter(List<SyncJobData> listUsers) {
        this.listSyncJobData = listUsers;
        workbook = new XSSFWorkbook();
        this.commonFunctions = new CommonFunctions();
    }

    private void writeHeaderLine() {
        sheet = workbook.createSheet("Wastage");
        commonFunctions.setSheet(sheet);

        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(16);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.CORAL.getIndex());

        commonFunctions.createCell(row, 0, "Status", style);
        commonFunctions.createCell(row, 1, "Reason", style);
        commonFunctions.createCell(row, 2, "Description", style);
        commonFunctions.createCell(row, 3, "Reference", style);
        commonFunctions.createCell(row, 4, "Wastage Total Credit", style);
        commonFunctions.createCell(row, 5, "Wastage Total Debit", style);
        commonFunctions.createCell(row, 6, "Cost Center", style);
        commonFunctions.createCell(row, 7, "Account Code", style);
        commonFunctions.createCell(row, 8, "Inventory Account", style);
        commonFunctions.createCell(row, 9, "Expenses Account", style);
        commonFunctions.createCell(row, 10, "Transaction Date", style);
    }

    private void writeDataLines() {
        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(14);
        style.setFont(font);

        for (SyncJobData syncJobData : listSyncJobData) {
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            commonFunctions.createCell(row, columnCount++, syncJobData.getStatus(), style);
            commonFunctions.createCell(row, columnCount++, syncJobData.getReason(), style);
            commonFunctions.createCell(row, columnCount++, syncJobData.getData().get("description"), style);
            commonFunctions.createCell(row, columnCount++, syncJobData.getData().get("transactionReference"), style);
            commonFunctions.createCell(row, columnCount++, syncJobData.getData().get("totalCr"), style);
            commonFunctions.createCell(row, columnCount++, syncJobData.getData().get("totalDr"), style);
            commonFunctions.createCell(row, columnCount++, syncJobData.getData().get("fromCostCenter"), style);
            commonFunctions.createCell(row, columnCount++, syncJobData.getData().get("fromAccountCode"), style);
            commonFunctions.createCell(row, columnCount++, syncJobData.getData().get("inventoryAccount"), style);
            commonFunctions.createCell(row, columnCount++, syncJobData.getData().get("expensesAccount"), style);
            commonFunctions.createCell(row, columnCount, syncJobData.getData().get("transactionDate"), style);
        }
    }

    public void export(HttpServletResponse response) throws IOException {
        writeHeaderLine();
        writeDataLines();

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }

    /////////////////////////////////////////// Custom Report //////////////////////////////////////////////////////////

    private void monthlyHeaderLine(List<JournalBatch> wasteBatches, SyncJobType syncJobType) {
        Row row;
        sheet = workbook.createSheet("WastageMonthlyReport");
//        sheet.setColumnWidth(2, 17);
        sheet.setDefaultColumnWidth(20);
        commonFunctions.setSheet(sheet);

        /* Title Header Style */
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(16);

        CellStyle titleStyle = workbook.createCellStyle();
        titleStyle.setFont(font);

        /* Header Style */
        CellStyle style = workbook.createCellStyle();
        style.setFont(font);

        XSSFColor myColor = new XSSFColor(new Color(211,219,227));
        ((XSSFCellStyle) style).setFillForegroundColor(myColor);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);


        row = sheet.createRow(0);
        commonFunctions.createCell(row, 0, "Raw Materials Wastage Cost Centers", titleStyle);
        row = sheet.createRow(1);
        commonFunctions.createCell(row, 0, "All Issues", titleStyle);

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        DateFormat BusinessDateFormat = new SimpleDateFormat("d/M/yyyy");

        Date fromDate = null;
        try {
            fromDate = dateFormat.parse(syncJobType.getConfiguration().fromDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Date toDate = null;
        try {
            toDate = dateFormat.parse(syncJobType.getConfiguration().toDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        String dateFormatted = BusinessDateFormat.format(fromDate) + " - " + BusinessDateFormat.format(toDate);
        row = sheet.createRow(2);
        commonFunctions.createCell(row, 0, dateFormatted, titleStyle);

        row = sheet.createRow(3);
        commonFunctions.createCell(row, 0, "Item Name", style);
        commonFunctions.createCell(row, 1, "Unit", style);
        commonFunctions.createCell(row, 2, "Total Qty", style);
        commonFunctions.createCell(row, 3, "Total Amount", style);

        int colCounter = 4;
        for (int i = 0; i < wasteBatches.size(); i++) {
            commonFunctions.createCell(row, i+colCounter, wasteBatches.get(i).getLocation().locationName + " Qty", style);
            commonFunctions.createCell(row, i+colCounter+1, wasteBatches.get(i).getLocation().locationName + " Amount", style);
            colCounter ++;
        }
    }

    private void monthlyDataLines(GeneralSettings generalSettings, SyncJobType syncJobType,
                                  List<JournalBatch> wasteBatches) {
        /* Row Style */
        Row row;
        int rowCount = 4;
        List<SyncJobData> wasteList;

        CellStyle style = workbook.createCellStyle();
        CellStyle itemStyle = workbook.createCellStyle();

        XSSFFont font = workbook.createFont();
        font.setFontHeight(14);

        itemStyle.setFont(font);
        style.setFont(font);

        XSSFColor myColor = new XSSFColor(new Color(231,230,230));
        ((XSSFCellStyle) style).setFillForegroundColor(myColor);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        /* Get sub headers */
        ArrayList<OverGroup> overGroups = syncJobType.getConfiguration().overGroups;
        ArrayList<ItemGroup> itemGroups = generalSettings.getItemGroups();
        ArrayList<Item> items = generalSettings.getItems();

        for (ItemGroup itemGroup : itemGroups) {
            /* Check if this item group included in this account */
            OverGroup overGroup = conversions.checkOverGroupExistence(overGroups, itemGroup.getOverGroup());
            if (!overGroup.getChecked())
                continue;

            row = sheet.createRow(rowCount++);
            commonFunctions.createCell(row, 0, itemGroup.getItemGroup(), style);
            for (int i = 1; i < (wasteBatches.size() * 2) + 4; i++) {
                commonFunctions.createCell(row, i, "", style);
            }

            String amount;
            float totalAmount ;
            String unit;
            String locationQuantity;
            float totalQuantity ;

            for (Item item : items) {
                /* Pick items under this item group */
                if(item.getItemGroup().equals(itemGroup.getItemGroup())){
                    unit = item.getUnit();
                    totalQuantity = 0;
                    totalAmount = 0;
                    int colCounter = 4;
                    row = sheet.createRow(rowCount++);
                    commonFunctions.createCell(row, 0, item.getItem(), itemStyle); // Item Name

                    /* List items synced */
                    for (int i = 0; i < wasteBatches.size(); i++) {
                        amount = "0";
                        locationQuantity = "0";
                        JournalBatch locationBatch = wasteBatches.get(i);
                        wasteList = new ArrayList<>(locationBatch.getWasteData());
                        /* Get location data */
                        for (int j = 0; j < wasteList.size(); j++) {
                            SyncJobData data = wasteList.get(j);
                            if(data.getData().get("overGroup").equals(item.getItem())){
                                amount = data.getData().get("totalCr").toString();
                                locationQuantity = data.getData().get("quantity").toString();
                                unit = data.getData().get("unit").toString();
                                totalQuantity += (float)data.getData().get("quantity");
                                totalAmount += Float.parseFloat(data.getData().get("totalCr").toString());
                                wasteBatches.get(i).getWasteData().remove(j);
                                break;
                            }
                        }

                        commonFunctions.createCell(row, i+colCounter, locationQuantity, itemStyle);
                        if(amount.equals("0"))
                            commonFunctions.createCell(row, i+colCounter+1, ".", itemStyle);
                        else
                            commonFunctions.createCell(row, i+colCounter+1, amount, itemStyle);

                        colCounter++;
                    }

                    commonFunctions.createCell(row, 1, unit, itemStyle); // Item Unit
                    commonFunctions.createCell(row, 2, Float.toString(totalQuantity), itemStyle); // Total Qty
                    commonFunctions.createCell(row, 3, Float.toString(totalAmount), itemStyle); // Total Amount
                }
            }
        }
    }

    public void exportMonthlyReport(String accountName, GeneralSettings generalSettings,
                                    SyncJobType syncJobType, List<JournalBatch> wasteBatches) throws IOException {
        String dateFormatted = syncJobType.getConfiguration().fromDate.replaceAll("-", "")
                + syncJobType.getConfiguration().toDate.replaceAll("-", "");

        String fileDirectory = accountName + "/" + syncJobType.getName() + "/CustomReports/";
        String fileName = fileDirectory + syncJobType.getName() + dateFormatted + ".xlsx";
        File directory = new File(fileDirectory);
        if (!directory.exists()){
            directory.getParentFile().mkdirs();
            directory.mkdir();
        }

        // Rename old file with same range
        directory = new File(fileName);
        Random random = new Random();
        int rand = random.nextInt(100);
        File cpFile = new File(fileDirectory + syncJobType.getName() + dateFormatted + rand + ".xlsx");
        if (directory.exists()){
            directory.renameTo(cpFile);
        }
        FileOutputStream out = new FileOutputStream(new File(fileName));

        monthlyHeaderLine(wasteBatches, syncJobType);
        monthlyDataLines(generalSettings, syncJobType, wasteBatches);

        // write operation workbook using file out object
        workbook.write(out);
        out.close();
    }
}
