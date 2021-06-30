package com.sun.supplierpoc.excelExporters;

import com.sun.supplierpoc.models.Transactions;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class TransactionExcelExport {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<Transactions> transactions;

    private CommonFunctions commonFunctions;

    public TransactionExcelExport(List<Transactions> transactions) {
        this.transactions = transactions;
        workbook = new XSSFWorkbook();
        this.commonFunctions = new CommonFunctions();
    }

    private void writeHeaderLine() {
        sheet = workbook.createSheet("Transactions");
        commonFunctions.setSheet(sheet);

        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(16);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.CORAL.getIndex());

        commonFunctions.createCell(row, 0, "User Name", style);
        commonFunctions.createCell(row, 1, "Transaction Date", style);
        commonFunctions.createCell(row, 2, "Group", style);
        commonFunctions.createCell(row, 3, "Total Payment", style);
        commonFunctions.createCell(row, 4, "Discount Rate", style);
        commonFunctions.createCell(row, 5, "After Discount", style);

    }

    private void writeDataLines() {
        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(14);
        style.setFont(font);

        for (Transactions transaction : transactions) {
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            commonFunctions.createCell(row, columnCount++, transaction.getUser().getName(), style);
            commonFunctions.createCell(row, columnCount++, transaction.getTransactionDate().toString(), style);
            commonFunctions.createCell(row, columnCount++, transaction.getGroup().getName(), style);
            commonFunctions.createCell(row, columnCount++, String.valueOf(transaction.getTotalPayment()), style);
            commonFunctions.createCell(row, columnCount++,  String.valueOf(transaction.getDiscountRate()) + " %", style);
            commonFunctions.createCell(row, columnCount++,  String.valueOf(transaction.getAfterDiscount()) , style);

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
