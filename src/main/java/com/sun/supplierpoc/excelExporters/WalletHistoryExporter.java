package com.sun.supplierpoc.excelExporters;

import com.sun.supplierpoc.models.applications.Action;
import com.sun.supplierpoc.models.applications.WalletHistory;
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

public class WalletHistoryExporter {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<WalletHistory> historyList;

    private CommonFunctions commonFunctions;

    public WalletHistoryExporter(List<WalletHistory> historyList) {
        this.workbook = new XSSFWorkbook();
        this.historyList = historyList;
        this.commonFunctions = new CommonFunctions();
    }

    private void writeHeaderLine() {
        sheet = workbook.createSheet("Wallet_History");
        commonFunctions.setSheet(sheet);

        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(16);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.CORAL.getIndex());

        commonFunctions.createCell(row, 0, "Opreation Type", style);
        commonFunctions.createCell(row, 1, "Operation Amount", style);
        commonFunctions.createCell(row, 2, "Previous Balance", style);
        commonFunctions.createCell(row, 3, "New Balance", style);
        commonFunctions.createCell(row, 4, "Agent Name", style);
        commonFunctions.createCell(row, 5, "Opreation Time", style);
    }

    private void writeDataLines() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(14);
        style.setFont(font);

        for (WalletHistory history : historyList) {
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            commonFunctions.createCell(row, columnCount++, history.getOperation(), style);
            commonFunctions.createCell(row, columnCount++, String.valueOf(history.getAmount()), style);
            commonFunctions.createCell(row, columnCount++, String.valueOf(history.getPreviousBalance()), style);
            commonFunctions.createCell(row, columnCount++, String.valueOf(history.getNewBalance()), style);
            if(history.getUser() != null)
                commonFunctions.createCell(row, columnCount++, history.getUser().name, style);
            else
                commonFunctions.createCell(row, columnCount++, "", style);

            commonFunctions.createCell(row, columnCount, (dateFormat.format(history.getDate())), style);

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
