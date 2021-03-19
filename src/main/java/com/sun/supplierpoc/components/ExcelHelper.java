package com.sun.supplierpoc.components;

import com.sun.supplierpoc.models.SyncJob;
import com.sun.supplierpoc.models.SyncJobData;
import com.sun.supplierpoc.models.SyncJobType;
import com.sun.supplierpoc.models.opera.Reservation;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;


public class ExcelHelper {

    public static List<SyncJobData> excelToTutorials(SyncJob syncJob, InputStream is) {

        try {
            XSSFWorkbook workbook = new XSSFWorkbook(is);

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            List<SyncJobData> syncJobDataList = new ArrayList<>();

            int rowNumber = 0;
            while (rows.hasNext()) {
                Row currentRow = rows.next();

                // skip header
                if (rowNumber == 0) {
                    rowNumber++;
                    continue;
                }

                Iterator<Cell> cellsInRow = currentRow.iterator();

                Reservation reservation = new Reservation();

                int cellIdx = 0;
                while (cellsInRow.hasNext()) {
                    Cell currentCell = cellsInRow.next();

                    switch (cellIdx) {
                        case 0:
                            reservation.setRoom((int) currentCell.getNumericCellValue());
                            break;
                        case 1:
                            reservation.setGuestId((int) currentCell.getNumericCellValue());
                            break;
                        case 2:
                            reservation.setName(currentCell.getStringCellValue());
                            break;
                        case 3:
                            reservation.setPersonsNumber((int) currentCell.getNumericCellValue());
                            break;
                        case 4:
                            reservation.setChildrenNumber((int) currentCell.getNumericCellValue());
                            break;
                        case 5:
                            reservation.setFromDate(String.valueOf(currentCell.getNumericCellValue()));
                            break;
                        case 6:
                            reservation.setToDate(String.valueOf(currentCell.getNumericCellValue()));
                            break;
                        case 7:
                            reservation.setWebsite(String.valueOf(currentCell.getStringCellValue()));
                            break;
                        case 8:
                            reservation.setEmail(String.valueOf(currentCell.getStringCellValue()));
                            break;
                        case 9:
                            try {
                                reservation.setPhone(String.valueOf(currentCell.getStringCellValue()));
                            }catch (Exception e){
                                reservation.setPhone(String.valueOf(currentCell.getNumericCellValue()));
                            }
                            break;
                        case 10:
                            reservation.setNationality(currentCell.getStringCellValue());
                            break;
                        case 11:
                            reservation.setLastRoom(String.valueOf(currentCell.getNumericCellValue()));
                            break;
                        case 12:
                            reservation.setLastVist(String.valueOf(currentCell.getNumericCellValue()));
                            break;
                        case 13:
                            reservation.setArr(String.valueOf(currentCell.getNumericCellValue()));
                            break;
                        case 14:
                            reservation.setNts(String.valueOf(currentCell.getNumericCellValue()));
                            break;
                        default:
                            break;
                    }
                    cellIdx++;
                }

                SyncJobData syncJobData = new SyncJobData(reservation, "success", "",  new Date(), syncJob.getId(), false);
                syncJobDataList.add(syncJobData);
            }
            workbook.close();

            return syncJobDataList;
        } catch (IOException e) {
            throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
        }
    }
}
