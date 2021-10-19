package com.sun.supplierpoc.excelExporters;

import com.sun.supplierpoc.models.SyncJobData;
import com.sun.supplierpoc.models.configurations.CostCenter;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WastageExcelExporter {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<SyncJobData> listSyncJobData;

    private CommonFunctions commonFunctions;

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

    private void monthlyHeaderLine(ArrayList<CostCenter> locations) {
        Row row;
        sheet = workbook.createSheet("WastageMonthlyReport");
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
        style.setFillForegroundColor(IndexedColors.CORAL.getIndex());

        row = sheet.createRow(0);
        commonFunctions.createCell(row, 0, "Raw Materials Wastage Cost Centers", titleStyle);
        row = sheet.createRow(1);
        commonFunctions.createCell(row, 0, "All Issues", titleStyle);
        row = sheet.createRow(2);
        commonFunctions.createCell(row, 0, "01/07/2021 - 31/07/2021", titleStyle);

        row = sheet.createRow(3);
        commonFunctions.createCell(row, 0, "Item Name", style);
        commonFunctions.createCell(row, 1, "Unit", style);
        commonFunctions.createCell(row, 2, "Total Qty", style);
        for (int i = 0; i < locations.size(); i++) {
            commonFunctions.createCell(row, i+3, locations.get(i).costCenterReference, style);
        }
    }

    public void exportMonthlyReport(HttpServletResponse response, ArrayList<CostCenter> locations) throws IOException {
        monthlyHeaderLine(locations);

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }
}
