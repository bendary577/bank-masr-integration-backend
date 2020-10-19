package com.sun.supplierpoc.excelExporters;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import com.sun.supplierpoc.models.SyncJobData;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class InvoicesExcelExporter {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<SyncJobData> listSyncJobData;

    public InvoicesExcelExporter(List<SyncJobData> listUsers) {
        this.listSyncJobData = listUsers;
        workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine() {
        sheet = workbook.createSheet("Approved Invoices");

        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(16);
        style.setFont(font);

        createCell(row, 0, "Status", style);
        createCell(row, 1, "Reason", style);
        createCell(row, 2, "Description", style);
        createCell(row, 3, "Invoice No", style);
        createCell(row, 4, "Reference", style);
        createCell(row, 7, "Gross", style);
        createCell(row, 5, "Inventory Account", style);
        createCell(row, 6, "Expenses Account", style);
        createCell(row, 8, "Supplier", style);
        createCell(row, 9, "Supplier Code", style);
        createCell(row, 10, "Cost Center", style);
        createCell(row, 11, "Account Code", style);
        createCell(row, 12, "status", style);
        createCell(row, 13, "Invoice Date", style);
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

            createCell(row, columnCount++, syncJobData.getStatus(), style);
            createCell(row, columnCount++, syncJobData.getReason(), style);
            createCell(row, columnCount++, syncJobData.getData().get("description"), style);
            createCell(row, columnCount++, syncJobData.getData().get("invoiceNo"), style);
            createCell(row, columnCount++, syncJobData.getData().get("reference"), style);
            createCell(row, columnCount++, syncJobData.getData().get("totalCr"), style);
            createCell(row, columnCount++, syncJobData.getData().get("inventoryAccount"), style);
            createCell(row, columnCount++, syncJobData.getData().get("expensesAccount"), style);
            createCell(row, columnCount++, syncJobData.getData().get("fromCostCenter"), style);
            createCell(row, columnCount++, syncJobData.getData().get("fromAccountCode"), style);
            createCell(row, columnCount++, syncJobData.getData().get("toCostCenter"), style);
            createCell(row, columnCount++, syncJobData.getData().get("toAccountCode"), style);
            createCell(row, columnCount++, syncJobData.getData().get("status"), style);
            createCell(row, columnCount, syncJobData.getData().get("transactionDate"), style);
        }
    }

    private void createCell(Row row, int columnCount, Object value, CellStyle style) {
        sheet.autoSizeColumn(columnCount);
        Cell cell = row.createCell(columnCount);
        if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        }else {
            cell.setCellValue((String) value);
        }
        cell.setCellStyle(style);
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
