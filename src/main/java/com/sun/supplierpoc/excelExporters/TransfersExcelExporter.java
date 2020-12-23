package com.sun.supplierpoc.excelExporters;
import com.sun.supplierpoc.models.SyncJobData;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class TransfersExcelExporter {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<SyncJobData> listSyncJobData;

    private CommonFunctions commonFunctions;

    public TransfersExcelExporter(List<SyncJobData> listUsers) {
        this.listSyncJobData = listUsers;
        workbook = new XSSFWorkbook();
        this.commonFunctions = new CommonFunctions();
    }

    private void writeHeaderLine() {
        sheet = workbook.createSheet("Transfers");
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

        commonFunctions.createCell(row, 3, "Transfer Total Credit", style);
        commonFunctions.createCell(row, 4, "Transfer Total Debit", style);
        commonFunctions.createCell(row, 5, "From Cost Center", style);
        commonFunctions.createCell(row, 6, "Account Code", style);
        commonFunctions.createCell(row, 7, "To Cost Center", style);
        commonFunctions.createCell(row, 8, "Account Code", style);
        commonFunctions.createCell(row, 9, "Inventory Account", style);
        commonFunctions.createCell(row, 10, "Expenses Account", style);
        commonFunctions.createCell(row, 11, "Delivery Date", style);
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
            commonFunctions.createCell(row, columnCount++, syncJobData.getData().get("toCostCenter"), style);
            commonFunctions.createCell(row, columnCount++, syncJobData.getData().get("toAccountCode"), style);
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
}
