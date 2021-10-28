package com.sun.supplierpoc.excelExporters;

import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.models.GeneralSettings;
import com.sun.supplierpoc.models.JournalBatch;
import com.sun.supplierpoc.models.SyncJobData;
import com.sun.supplierpoc.models.SyncJobType;
import com.sun.supplierpoc.models.configurations.Item;
import com.sun.supplierpoc.models.configurations.ItemGroup;
import com.sun.supplierpoc.models.configurations.OverGroup;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class ConsumptionExcelExporter {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<SyncJobData> listSyncJobData;

    private CommonFunctions commonFunctions;
    private Conversions conversions = new Conversions();
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public ConsumptionExcelExporter() {
        workbook = new XSSFWorkbook();
        this.commonFunctions = new CommonFunctions();
    }

    public ConsumptionExcelExporter(List<SyncJobData> listUsers) {
        this.listSyncJobData = listUsers;
        workbook = new XSSFWorkbook();
        this.commonFunctions = new CommonFunctions();
    }

    private void writeHeaderLine() {
        sheet = workbook.createSheet("Consumption");
        commonFunctions.setSheet(sheet);

        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(16);
        style.setFont(font);

        commonFunctions.createCell(row, 0, "Status", style);
        commonFunctions.createCell(row, 1, "Reason", style);
        commonFunctions.createCell(row, 2, "Description", style);

        commonFunctions.createCell(row, 3, "Consumption Total Credit", style);
        commonFunctions.createCell(row, 4, "Consumption Total Debit", style);
        commonFunctions.createCell(row, 5, "Cost Center", style);
        commonFunctions.createCell(row, 6, "Account Code", style);
        commonFunctions.createCell(row, 7, "Inventory Account", style);
        commonFunctions.createCell(row, 8, "Expenses Account", style);
        commonFunctions.createCell(row, 9, "Transaction Date", style);
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

    private void monthlyHeaderLine(List<JournalBatch> journalBatches, SyncJobType syncJobType) {
        Row row;
        sheet = workbook.createSheet("ConsumptionMonthlyReport");
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
        commonFunctions.createCell(row, 0, "Consumption per Cost Centers", titleStyle);
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
        commonFunctions.createCell(row, 2, "Total Net Receipts", style);
        commonFunctions.createCell(row, 3, "Total Net Transfers", style);

        int colCounter = 4;
        String locationName;
        for (int i = 0; i < journalBatches.size(); i++) {
            locationName = journalBatches.get(i).getLocation().locationName;
            if(locationName.equals(""))
                locationName = journalBatches.get(i).getCostCenter().locationName;
            commonFunctions.createCell(row, i+colCounter, locationName + " Receipts", style);
            commonFunctions.createCell(row, i+colCounter+1, locationName + " Transfers", style);
            colCounter ++;
        }
    }

    private void monthlyDataLinesPerItem(GeneralSettings generalSettings, SyncJobType syncJobType,
                                  List<JournalBatch> journalBatches) {
        /* Row Style */
        Row row;
        int rowCount = 4;
        List<SyncJobData> journalList;

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
        ArrayList<ItemGroup> itemGroups = generalSettings.getItemGroups();
        ArrayList<Item> items = generalSettings.getItems();

        for (ItemGroup itemGroup : itemGroups) {
            row = sheet.createRow(rowCount++);
            commonFunctions.createCell(row, 0, itemGroup.getItemGroup(), style);
            for (int i = 1; i < (journalBatches.size() * 2) + 4; i++) {
                commonFunctions.createCell(row, i, "", style);
            }

            String unit;

            String netReceipts;
            float totalNetReceipts ;

            String netTranfers;
            float totalNetTranfers ;

            for (Item item : items) {
                /* Pick items under this item group */
                if(item.getItemGroup().equals(itemGroup.getItemGroup())){
                    unit = item.getUnit();
                    totalNetTranfers = 0;
                    totalNetReceipts = 0;
                    int colCounter = 4;
                    row = sheet.createRow(rowCount++);
                    commonFunctions.createCell(row, 0, item.getItem(), itemStyle); // Item Name

                    /* List items synced */
                    for (int i = 0; i < journalBatches.size(); i++) {
                        netReceipts = "0";
                        netTranfers = "0";
                        JournalBatch locationBatch = journalBatches.get(i);
                        journalList = new ArrayList<>(locationBatch.getConsumptionData());
                        /* Get location data */
                        for (int j = 0; j < journalList.size(); j++) {
                            SyncJobData data = journalList.get(j);
                            if(data.getData().get("overGroup").equals(item.getItem())){
                                netReceipts = data.getData().get("netReceipts").toString();
                                netTranfers = data.getData().get("netTransfers").toString();

                                unit = data.getData().get("unit").toString();
                                totalNetReceipts += Float.parseFloat(data.getData().get("netReceipts").toString());
                                totalNetTranfers += Float.parseFloat(data.getData().get("netTransfers").toString());

                                journalBatches.get(i).getWasteData().remove(j);
                                break;
                            }
                        }

                        commonFunctions.createCell(row, i+colCounter, netTranfers, itemStyle);
                        if(netReceipts.equals("0"))
                            commonFunctions.createCell(row, i+colCounter+1, ".", itemStyle);
                        else
                            commonFunctions.createCell(row, i+colCounter+1, netReceipts, itemStyle);

                        colCounter++;
                    }

                    commonFunctions.createCell(row, 1, unit, itemStyle); // Item Unit
                    commonFunctions.createCell(row, 2, Float.toString(totalNetTranfers), itemStyle); // Total Qty
                    commonFunctions.createCell(row, 3, Float.toString(totalNetReceipts), itemStyle); // Total Amount
                }
            }
        }
    }

    private void monthlyDataLinesPerItemGroup(GeneralSettings generalSettings, SyncJobType syncJobType,
                                         List<JournalBatch> journalBatches) {
        /* Row Style */
        Row row;
        int rowCount = 4;
        List<SyncJobData> journalList;

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
        ArrayList<ItemGroup> itemGroups = generalSettings.getItemGroups();

        for (ItemGroup itemGroup : itemGroups) {
            int colCounter = 4;
            String unit = "";
            String netReceipts;
            float totalNetReceipts = 0;
            String netTranfers;
            float totalNetTranfers = 0;

            row = sheet.createRow(rowCount++);
            commonFunctions.createCell(row, 0, itemGroup.getItemGroup(), itemStyle); // Item Name

            /* List item groups synced */
            for (int i = 0; i < journalBatches.size(); i++) {
                netReceipts = "0";
                netTranfers = "0";
                JournalBatch locationBatch = journalBatches.get(i);
                journalList = new ArrayList<>(locationBatch.getConsumptionData());

                /* Get location data */
                for (int j = 0; j < journalList.size(); j++) {
                    SyncJobData data = journalList.get(j);
                    if(data.getData().get("overGroup").equals(itemGroup.getItemGroup())){
                        netReceipts = data.getData().get("netReceipts").toString();
                        netTranfers = data.getData().get("netTransfers").toString();

                        unit = "";
                        totalNetReceipts += Float.parseFloat(data.getData().get("netReceipts").toString());
                        totalNetTranfers += Float.parseFloat(data.getData().get("netTransfers").toString());

                        journalBatches.get(i).getConsumptionData().remove(j);
                        break;
                    }
                }

                commonFunctions.createCell(row, i+colCounter, netTranfers, itemStyle);
                if(netReceipts.equals("0"))
                    commonFunctions.createCell(row, i+colCounter+1, ".", itemStyle);
                else
                    commonFunctions.createCell(row, i+colCounter+1, netReceipts, itemStyle);

                colCounter++;
            }

            commonFunctions.createCell(row, 1, unit, itemStyle); // Item Unit
            commonFunctions.createCell(row, 2, Float.toString(totalNetTranfers), itemStyle); // Total Qty
            commonFunctions.createCell(row, 3, Float.toString(totalNetReceipts), itemStyle); // Total Amount
        }
    }

    public void exportMonthlyReport(String accountName, GeneralSettings generalSettings,
                                    SyncJobType syncJobType, List<JournalBatch> journalBatchesBatches) throws IOException {
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

        monthlyHeaderLine(journalBatchesBatches, syncJobType);
        monthlyDataLinesPerItemGroup(generalSettings, syncJobType, journalBatchesBatches);

        // write operation workbook using file out object
        workbook.write(out);
        out.close();
    }
}
