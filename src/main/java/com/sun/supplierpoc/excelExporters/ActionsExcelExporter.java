package com.sun.supplierpoc.excelExporters;

import com.sun.supplierpoc.models.applications.Action;
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

public class ActionsExcelExporter {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<Action> actions;

    private CommonFunctions commonFunctions;

    public ActionsExcelExporter(List<Action> actions) {
        this.workbook = new XSSFWorkbook();
        this.actions = actions;
        this.commonFunctions = new CommonFunctions();
    }

    private void writeHeaderLine() {
        sheet = workbook.createSheet("Agent_Actions");
        commonFunctions.setSheet(sheet);

        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(16);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.CORAL.getIndex());

        commonFunctions.createCell(row, 0, "Agent Name", style);
        commonFunctions.createCell(row, 1, "Action", style);
        commonFunctions.createCell(row, 2, "Amount", style);
        commonFunctions.createCell(row, 3, "Date", style);
    }

    private void writeDataLines() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(14);
        style.setFont(font);

        for (Action action : actions) {
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            commonFunctions.createCell(row, columnCount++, action.getUser().getName(), style);
            commonFunctions.createCell(row, columnCount++, action.getActionType(), style);
            commonFunctions.createCell(row, columnCount++, String.valueOf(action.getAmount()), style);
            commonFunctions.createCell(row, columnCount, (dateFormat.format(action.getDate())), style);

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
