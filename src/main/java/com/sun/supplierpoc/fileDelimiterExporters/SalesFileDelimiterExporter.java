package com.sun.supplierpoc.fileDelimiterExporters;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.models.SyncJobData;
import com.sun.supplierpoc.models.SyncJobType;
import com.sun.supplierpoc.models.util.SyncJobDataCSV;

import java.io.*;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class SalesFileDelimiterExporter {
    private String fileName;
    private SyncJobType syncJobType;
    private List<SyncJobData> listSyncJobData;
    private List<SyncJobDataCSV> syncJobDataCSVList = new ArrayList<>();
    private StringBuilder fileContent = new StringBuilder();
    private Conversions conversions = new Conversions();

    public SalesFileDelimiterExporter(SyncJobType syncJobType, List<SyncJobData> listSyncJobData) {
        this.syncJobType = syncJobType;
        this.listSyncJobData = listSyncJobData;
    }

    public SalesFileDelimiterExporter(String fileName, SyncJobType syncJobType, List<SyncJobData> listSyncJobData) {
        this.fileName = fileName;
        this.syncJobType = syncJobType;
        this.listSyncJobData = listSyncJobData;
    }

    public void writeSyncData(PrintWriter writer) {
        /*
         * Check sync job type here
         * */
        if (syncJobType.getName().equals(Constants.SALES) || syncJobType.getName().equals(Constants.COST_OF_GOODS)
                || syncJobType.getName().equals(Constants.CONSUMPTION))
            this.extractSalesSyncJobData();
        else if (syncJobType.getName().equals(Constants.APPROVED_INVOICES))
            this.extractInvoicesSyncJobData();
        else if (syncJobType.getName().equals(Constants.WASTAGE))
            this.extractWastageSyncJobData();
        this.createFileContent();
        writer.print(this.fileContent);
    }

    public File prepareNDFFile(List<SyncJobData> jobData, SyncJobType syncJobType, String accountName, String location) throws ParseException {
        if(jobData.size() <= 0){
            return null;
        }
        try {
            String transactionDate = jobData.get(0).getData().get("transactionDate").toString();
            Date date = new SimpleDateFormat("ddMMyyyy").parse(transactionDate);

            DateFormatSymbols dfs = new DateFormatSymbols();
            String[] weekdays = dfs.getWeekdays();

            Calendar cal = Calendar.getInstance();
            cal.setTime(date);

            int day = cal.get(Calendar.DAY_OF_WEEK);
            int year = cal.get(Calendar.YEAR);
            int Month = cal.get(Calendar.MONTH) + 1;
            String dayName = weekdays[day];
            String fileExtension = ".ndf";

            File file;
            String fileDirectory;
            if (location.equals("")) {
                fileDirectory = accountName + "/" + syncJobType.getName() + "/" + year + "/" +Month + "/";
                this.fileName = fileDirectory + transactionDate + dayName.substring(0, 3) + fileExtension;
            } else {
                fileDirectory = accountName + "/" + syncJobType.getName() + "/" + year + "/" + Month + "/" + location + "/";
                this.fileName = fileDirectory + transactionDate + dayName.substring(0, 3) + " - " + location + fileExtension;
            }

            /*
             * Check sync job type here
             * */
            if (syncJobType.getName().equals(Constants.SALES) || syncJobType.getName().equals(Constants.COST_OF_GOODS)
            || syncJobType.getName().equals(Constants.CONSUMPTION))
                this.extractSalesSyncJobData();
            else if (syncJobType.getName().equals(Constants.APPROVED_INVOICES))
                this.extractInvoicesSyncJobData();
            else if (syncJobType.getName().equals(Constants.WASTAGE))
                this.extractWastageSyncJobData();

            file = createNDFFile();
            System.out.println(file.getName());

            return file;
        } catch (Exception e) {
            return new File(this.fileName);
        }
    }

    private File createNDFFile() throws IOException {
        this.createFileContent();

        File file = new File(this.fileName);
        boolean status = file.getParentFile().mkdirs();
        if (status)
            file.createNewFile();

        try (Writer writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(String.valueOf(this.fileContent));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return file;
    }

    private void createFileContent() {
        String journalNumberSpaces = "       "; // 7 Length
        String lineSpaces = "       "; // 7 Length
        String allocationIndicatorSpace = " "; // 1 Length
        String journalSource = "     "; // 5 Length
        String entryDate = "        "; // 8 Length
        String entryPeriod = "       "; // 7 Length
        String dueDate = "        "; // 8 Length
        String filler6 = "      ";
        String paymentRef = "         ";  // 9 Length
        String paymentDate = "        ";  // 8 Length
        String paymentPeriod = "       "; // 7 Length
        String asssetIndicator = " "; // 1 Length
        String asssetCode = "          "; // 10 Length
        String assetSubCode = "     ";  // 5 Length
        String conversionCode = "     "; // 5 Length
        String conversionRate = "                  "; // 18 Length
        String otherAmount = "                  "; // 18 Length
        String amountDec = " ";  // 1 Length
        String cleardown = "     "; // 5 Length
        String filler4 = "    "; // 4 Length
        String nextPeriodReversal = " "; // 1 Length
        String textLinked = " ";  // 1 Length
        String roughBookFlag = " "; // 1 Length
        String inUseFlag = " "; // 1 Length

        fileContent.append("VERSION                         ")
                .append(this.syncJobType.getConfiguration().versionCode).append("\r\n");

        for (int i = 0; i < this.syncJobDataCSVList.size(); i++) {
            SyncJobDataCSV syncJobDataCSV = this.syncJobDataCSVList.get(i);
            fileContent.append(syncJobDataCSV.accountCode).append("     ")
                    .append(syncJobDataCSV.accountingPeriod).append(syncJobDataCSV.transactionDate).append("  ")
                    .append(syncJobDataCSV.recordType).append(journalNumberSpaces).append(lineSpaces)
                    .append(syncJobDataCSV.amount).append(syncJobDataCSV.DCMarker).append(allocationIndicatorSpace)
                    .append(syncJobDataCSV.journalType).append(journalSource).append(syncJobDataCSV.transactionReference)
                    .append(syncJobDataCSV.description).append(entryDate).append(entryPeriod).append(dueDate)
                    .append(filler6).append(paymentRef).append(paymentDate).append(paymentPeriod).append(asssetIndicator)
                    .append(asssetCode).append(assetSubCode).append(conversionCode).append(conversionRate)
                    .append(otherAmount).append(amountDec).append(cleardown).append(filler4).append(nextPeriodReversal)
                    .append(textLinked).append(roughBookFlag).append(inUseFlag)
                    .append(syncJobDataCSV.analysisCode1).append(syncJobDataCSV.analysisCode2)
                    .append(syncJobDataCSV.analysisCode3).append(syncJobDataCSV.analysisCode4)
                    .append(syncJobDataCSV.analysisCode5).append(syncJobDataCSV.analysisCode6)
                    .append(syncJobDataCSV.analysisCode7).append(syncJobDataCSV.analysisCode8)
                    .append(syncJobDataCSV.analysisCode9).append(syncJobDataCSV.analysisCode10);

            if (i != this.syncJobDataCSVList.size() - 1) {
                fileContent.append("\r\n");
            }
        }
    }

    private void extractSalesSyncJobData() {
        SyncJobDataCSV syncJobDataCSV;
        for (SyncJobData syncJobData : listSyncJobData) {
            if (syncJobData.getData().containsKey("totalDr")) {
                syncJobDataCSV = createSyncJobDataObject(syncJobType, syncJobData, "D");
                if (syncJobDataCSV != null)
                    this.syncJobDataCSVList.add(syncJobDataCSV);
            } else {
                syncJobDataCSV = createSyncJobDataObject(syncJobType, syncJobData, "C");
                if (syncJobDataCSV != null)
                    this.syncJobDataCSVList.add(syncJobDataCSV);
            }

        }
    }

    private void extractInvoicesSyncJobData() {
        SyncJobDataCSV syncJobDataCSV;
        SyncJobData tempSyncJobData = new SyncJobData();
        String invoiceNumber = "";
        float vat = 0;
        int counter = 1;
        for (SyncJobData syncJobData : listSyncJobData) {

            if (invoiceNumber.equals("")) {
                invoiceNumber = syncJobData.getData().get("invoiceNo").toString();
                tempSyncJobData = syncJobData;
            }

            if (!syncJobData.getData().get("invoiceNo").toString().equals(invoiceNumber)) {
                tempSyncJobData.getData().put("vat", vat);
                syncJobDataCSV = createSyncJobDataObject(syncJobType, tempSyncJobData, "DV");
                if (syncJobDataCSV != null)
                    this.syncJobDataCSVList.add(syncJobDataCSV);
                tempSyncJobData = syncJobData;
                invoiceNumber = syncJobData.getData().get("invoiceNo").toString();
                vat = conversions.convertStringToFloat(syncJobData.getData().get("vat").toString());
            } else {
                vat = vat + conversions.convertStringToFloat(syncJobData.getData().get("vat").toString());
            }

            syncJobDataCSV = createSyncJobDataObject(syncJobType, syncJobData, "D");
            if (syncJobDataCSV != null)
                this.syncJobDataCSVList.add(syncJobDataCSV);

            syncJobDataCSV = createSyncJobDataObject(syncJobType, syncJobData, "C");
            if (syncJobDataCSV != null)
                this.syncJobDataCSVList.add(syncJobDataCSV);

            if (counter == listSyncJobData.size()) {
                syncJobDataCSV = createSyncJobDataObject(syncJobType, tempSyncJobData, "DV");
                if (syncJobDataCSV != null)
                    this.syncJobDataCSVList.add(syncJobDataCSV);
            }
            counter++;
        }
    }

    private void extractWastageSyncJobData() {
        SyncJobDataCSV syncJobDataCSV;

        for (SyncJobData syncJobData : listSyncJobData) {

            syncJobDataCSV = createSyncJobDataObject(syncJobType, syncJobData, "D");
            if (syncJobDataCSV != null)
                this.syncJobDataCSVList.add(syncJobDataCSV);

            syncJobDataCSV = createSyncJobDataObject(syncJobType, syncJobData, "C");
            if (syncJobDataCSV != null)
                this.syncJobDataCSVList.add(syncJobDataCSV);
        }
    }

    private SyncJobDataCSV createSyncJobDataObject(SyncJobType syncJobType, SyncJobData syncJobData, String CDMaker) {

        SyncJobDataCSV syncJobDataCSV = new SyncJobDataCSV();
        syncJobDataCSV.description = syncJobData.getData().get("description").toString();

        if (syncJobDataCSV.description.length() > 25) {
            syncJobDataCSV.description = syncJobDataCSV.description.substring(0, 25);
        } else if (syncJobDataCSV.description.length() < 25) {
            syncJobDataCSV.description = String.format("%-25s", syncJobDataCSV.description);
        }

        syncJobDataCSV.transactionReference = syncJobData.getData().get("transactionReference").toString();
        if (syncJobDataCSV.transactionReference.length() > 15) {
            syncJobDataCSV.transactionReference = syncJobDataCSV.transactionReference.substring(0, 15);
        } else if (syncJobDataCSV.transactionReference.length() < 15) {
            syncJobDataCSV.transactionReference = String.format("%-15s", syncJobDataCSV.transactionReference);
        }

        String year = syncJobData.getData().get("transactionDate").toString().substring(4);
        String month = syncJobData.getData().get("transactionDate").toString().substring(2, 4);
        String day = syncJobData.getData().get("transactionDate").toString().substring(0, 2);

        syncJobDataCSV.transactionDate = year + month + day;
        syncJobDataCSV.accountingPeriod = year + "0" + month;

        String totalDr;

        if (CDMaker.equals("D")) {
            syncJobDataCSV.DCMarker = "D";

            if (syncJobData.getData().containsKey("totalDr")) {
                totalDr = syncJobData.getData().get("totalDr").toString();
            } else {
                totalDr = syncJobData.getData().get("net").toString();
            }


            if (totalDr.substring(0, 1).equals("-")) {
                totalDr = totalDr.substring(1);
            }

            splitTotalAmount(syncJobDataCSV, totalDr);

            String accountCode = "";

            if (syncJobData.getData().containsKey("expensesAccount")) {
                accountCode = syncJobData.getData().get("expensesAccount").toString();
            } else {
                accountCode = syncJobData.getData().get("toLocation").toString();
            }

            if (accountCode.length() < 10) {
                accountCode = String.format("%-10s", accountCode);
            }
            syncJobDataCSV.accountCode = accountCode;

        } else if (CDMaker.equals("DV")) {
            syncJobDataCSV.DCMarker = "D";
            totalDr = syncJobData.getData().get("vat").toString();

            if (totalDr.substring(0, 1).equals("-")) {
                totalDr = totalDr.substring(1);
            }

            splitTotalAmount(syncJobDataCSV, totalDr);

            String accountCode = "9230002";

            if (accountCode.length() < 10) {
                accountCode = String.format("%-10s", accountCode);
            }
            syncJobDataCSV.accountCode = accountCode;
            syncJobDataCSV.description = "TAX" + syncJobDataCSV.description.substring(3, 25);
        } else {
            syncJobDataCSV.DCMarker = "C";

            // 18 char --> 15 char + 3 decimals
            String totalCr = syncJobData.getData().get("totalCr").toString();
            if (totalCr.substring(0, 1).equals("-")) {
                totalCr = totalCr.substring(1);
            }
            String[] amountArray = totalCr.split("\\.");

            String amountPart = amountArray[0];
            String decimalPart = "0";

            if (amountArray.length > 1) {
                decimalPart = amountArray[1];
            }
            if (amountPart.length() < 15) {
                amountPart = String.format("%0" + 15 + "d", Integer.parseInt(amountPart));
            }

            if (decimalPart.length() > 3) {
                decimalPart = decimalPart.substring(0, 4);
            } else if (decimalPart.length() < 3) {
                decimalPart = decimalPart +
                        String.format("%0" + (3 - decimalPart.length()) + "d", 0);
            }

            syncJobDataCSV.amount = amountPart + decimalPart;


            String accountCode = syncJobData.getData().get("inventoryAccount").toString();

            if (syncJobType.getName().equals(Constants.WASTAGE)) {
                accountCode = syncJobData.getData().get("fromLocation").toString();
            }

            if (accountCode.length() < 10) {
                accountCode = String.format("%-10s", accountCode);
            }
            syncJobDataCSV.accountCode = accountCode;
        }

        syncJobDataCSV.recordType = this.syncJobType.getConfiguration().recordType;
        syncJobDataCSV.conversionCode = this.syncJobType.getConfiguration().conversionCode;
        syncJobDataCSV.conversionRate = this.syncJobType.getConfiguration().conversionRate;

        syncJobDataCSV.journalType = this.syncJobType.getConfiguration().inforConfiguration.journalType;
        if (syncJobDataCSV.journalType.length() > 5) {
            syncJobDataCSV.journalType = syncJobDataCSV.journalType.substring(0, 5);
        } else if (syncJobDataCSV.journalType.length() < 5) {
            syncJobDataCSV.journalType = String.format("%-5s", syncJobDataCSV.journalType);
        }

        /* 15 char */
        syncJobDataCSV.analysisCode1 = fillTCode(1, syncJobData, CDMaker);
        syncJobDataCSV.analysisCode2 = fillTCode(2, syncJobData, CDMaker);
        syncJobDataCSV.analysisCode3 = fillTCode(3, syncJobData, CDMaker);
        syncJobDataCSV.analysisCode4 = fillTCode(4, syncJobData, CDMaker);
        syncJobDataCSV.analysisCode5 = fillTCode(5, syncJobData, CDMaker);
        syncJobDataCSV.analysisCode6 = fillTCode(6, syncJobData, CDMaker);
        syncJobDataCSV.analysisCode7 = fillTCode(7, syncJobData, CDMaker);
        syncJobDataCSV.analysisCode8 = fillTCode(8, syncJobData, CDMaker);
        syncJobDataCSV.analysisCode9 = fillTCode(9, syncJobData, CDMaker);
        syncJobDataCSV.analysisCode10 = fillTCode(10, syncJobData, CDMaker);

        if (syncJobDataCSV.amount.equals("000000000000000000")) {
            return null;
        }
        return syncJobDataCSV;
    }

    private void splitTotalAmount(SyncJobDataCSV syncJobDataCSV, String totalDr) {
        String[] amountArray = totalDr.split("\\.");

        String amountPart = amountArray[0];
        String decimalPart = amountArray[1];
        if (amountPart.equals(""))
            amountPart = "0";
        if (decimalPart.equals(""))
            decimalPart = "0";
        if (amountPart.length() < 15) {
            amountPart = String.format("%0" + 15 + "d", Integer.parseInt(amountPart));
        }
        if (decimalPart.length() > 3) {
            decimalPart = decimalPart.substring(0, 3);
        } else if (decimalPart.length() < 3) {
            decimalPart = decimalPart +
                    String.format("%0" + (3 - decimalPart.length()) + "d", 0);
        }

        syncJobDataCSV.amount = amountPart + decimalPart;
    }

    private String fillTCode(int index, SyncJobData syncJobData, String CDMaker) {
        String analysisTCode = "";
        if (syncJobType.getConfiguration().analysis.get(index - 1).getChecked())
            analysisTCode = "#";

        if (syncJobData.getData().containsKey("analysisCodeT" + index) && !syncJobData.getData().get("analysisCodeT" + index).equals("")) {

            if (!syncJobType.getAccountId().equals("600424f292be3d32dfe0208b") && !syncJobType.getName().equals(Constants.APPROVED_INVOICES)) {

                analysisTCode = syncJobData.getData().get("analysisCodeT" + index).toString();

            } else if (syncJobType.getAccountId().equals("600424f292be3d32dfe0208b") && syncJobType.getName().equals(Constants.WASTAGE)) {
                if (("analysisCodeT" + index).equals("analysisCodeT1") && CDMaker.equals("D")) {
                    analysisTCode = syncJobData.getData().get("analysisCodeT" + index).toString();
                }
            } else {
                if (CDMaker.equals("D") || CDMaker.equals("DV")) {
                    if (!("analysisCodeT" + index).equals("analysisCodeT9") &&
                            !("analysisCodeT" + index).equals("analysisCodeT10")) {
                        analysisTCode = syncJobData.getData().get("analysisCodeT" + index).toString();
                    }
                } else {
                    analysisTCode = syncJobData.getData().get("analysisCodeT" + index).toString();
                }
            }
        }

        if (analysisTCode.length() > 15) {
            analysisTCode = analysisTCode.substring(0, 15);
        } else if (analysisTCode.length() < 15) {
            analysisTCode = String.format("%-15s", analysisTCode);
        }
        return analysisTCode;
    }

    private String fillConsumptionTCode(int index, SyncJobData syncJobData, String CDMaker) {
        String analysisTCode = "";
        if (syncJobType.getConfiguration().analysis.get(index - 1).getChecked())
            analysisTCode = "#";

        if (syncJobData.getData().containsKey("analysisCodeT" + index) && !syncJobData.getData().get("analysisCodeT" + index).equals("")) {
            if (CDMaker.equals("D")) {
                analysisTCode = syncJobData.getData().get("analysisCodeT" + index).toString();
            }
        }

        if (analysisTCode.length() > 15) {
            analysisTCode = analysisTCode.substring(0, 15);
        } else if (analysisTCode.length() < 15) {
            analysisTCode = String.format("%-15s", analysisTCode);
        }
        return analysisTCode;
    }
}
