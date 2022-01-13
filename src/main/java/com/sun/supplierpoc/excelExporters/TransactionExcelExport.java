package com.sun.supplierpoc.excelExporters;

import com.sun.supplierpoc.Conversions;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
        commonFunctions.createCell(row, 1, "Group", style);
        commonFunctions.createCell(row, 2, "Total Payment", style);
        commonFunctions.createCell(row, 3, "Discount Rate", style);
        commonFunctions.createCell(row, 4, "After Discount", style);
        commonFunctions.createCell(row, 5, "Transaction Date", style);


    }

    private void writeDataLines() {

        Conversions conversion = new Conversions();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(14);
        style.setFont(font);

        for (Transactions transaction : transactions) {
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            commonFunctions.createCell(row, columnCount++, transaction.getUser().getName(), style);
            commonFunctions.createCell(row, columnCount++, transaction.getGroup().getName(), style);
            commonFunctions.createCell(row, columnCount++, String.valueOf(conversion.roundUpDouble(transaction.getTotalPayment())), style);
            commonFunctions.createCell(row, columnCount++,  transaction.getDiscountRate() + " %", style);
            commonFunctions.createCell(row, columnCount++,  String.valueOf(conversion.roundUpDouble(transaction.getAfterDiscount())) , style);
            commonFunctions.createCell(row, columnCount, (dateFormat.format(transaction.getTransactionDate())), style);

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

    /* Reward Points */
    private void writeRPHeaderLine() {
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
        commonFunctions.createCell(row, 1, "QR Code", style);
        commonFunctions.createCell(row, 2, "Group", style);
        commonFunctions.createCell(row, 3, "Check #", style);
        commonFunctions.createCell(row, 4, "Operation", style);
        commonFunctions.createCell(row, 5, "Points Redeemed", style);
        commonFunctions.createCell(row, 6, "Points Collected", style);
        commonFunctions.createCell(row, 7, "Total Payment", style);
        commonFunctions.createCell(row, 8, "Transaction Date", style);
    }

    private void writeRPDataLines() {

        Conversions conversion = new Conversions();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(14);
        style.setFont(font);

        for (Transactions transaction : transactions) {
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            commonFunctions.createCell(row, columnCount++, transaction.getUser().getName(), style);
            commonFunctions.createCell(row, columnCount++, transaction.getUser().getCode(), style);
            commonFunctions.createCell(row, columnCount++, transaction.getGroup().getName(), style);
            commonFunctions.createCell(row, columnCount++, transaction.getCheckNumber(), style);
            commonFunctions.createCell(row, columnCount++, transaction.getTransactionType().getName(), style);
            commonFunctions.createCell(row, columnCount++, transaction.getPointsRedeemed(), style);
            commonFunctions.createCell(row, columnCount++, transaction.getPointsReward(), style);
            commonFunctions.createCell(row, columnCount++, String.valueOf(conversion.roundUpDouble(transaction.getTotalPayment())), style);
            commonFunctions.createCell(row, columnCount, (dateFormat.format(transaction.getTransactionDate())), style);
        }
    }

    public void exportRewardPointsExcel(HttpServletResponse response) throws IOException {
        writeRPHeaderLine();
        writeRPDataLines();

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }
}
