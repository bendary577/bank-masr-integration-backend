package com.sun.supplierpoc.fileDelimiterExporters;

import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvException;
import com.sun.supplierpoc.models.SyncJobData;
import com.opencsv.CSVWriter;
import com.sun.supplierpoc.models.SyncJobType;
import com.sun.supplierpoc.models.util.SyncJobDataCSV;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class SalesFileDelimiterExporter {
    private SyncJobType syncJobType;
    private List<SyncJobData> listSyncJobData;
    private List<SyncJobDataCSV> syncJobDataCSVList = new ArrayList<>();

    public SalesFileDelimiterExporter(SyncJobType syncJobType, List<SyncJobData> listSyncJobData) {
        this.syncJobType = syncJobType;
        this.listSyncJobData = listSyncJobData;
    }

    public void writeSyncData(PrintWriter writer) {
        ColumnPositionMappingStrategy<SyncJobDataCSV> mapStrategy
                = new ColumnPositionMappingStrategy<>();

        mapStrategy.setType(SyncJobDataCSV.class);

        String[] columns = new String[]{"accountCode", "accountingPeriod", "transactionDate",
        "recordType", "amount", "DCMarker", "journalType", "transactionReference", "description",
        "conversionCode", "conversionRate", "analysisCode0", "analysisCode1"};
        mapStrategy.setColumnMapping(columns);

        StatefulBeanToCsv<SyncJobDataCSV> csvWriter = new StatefulBeanToCsvBuilder<SyncJobDataCSV>(writer)
                .withQuotechar(CSVWriter.NO_QUOTE_CHARACTER)
                .withMappingStrategy(mapStrategy)
                .withSeparator('\t')
                .withOrderedResults(false)
                .build();
//            csvWriter.write(this.syncJobDataCSVList);

        this.extractSyncJobData();
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
        String asssetIndicator  = " "; // 1 Length
        String asssetCode = "          "; // 10 Length
        String assetSubCode = "     ";  // 5 Length
        String conversionCode = "     "; // 5 Length
        String conversionRate  = "                  "; // 18 Length
        String otherAmount = "                  "; // 18 Length
        String amountDec = " ";  // 1 Length
        String cleardown = "     "; // 5 Length
        String filler4  = "    "; // 4 Length
        String nextPeriodReversal = " "; // 1 Length
        String textLinked = " ";  // 1 Length
        String roughBookFlag = " "; // 1 Length
        String inUseFlag  = " "; // 1 Length


        writer.println("VERSION                         " + this.syncJobDataCSVList.get(0).versionCode);
        for (SyncJobDataCSV syncJobDataCSV : this.syncJobDataCSVList) {
            writer.println(syncJobDataCSV.accountCode + "     " + syncJobDataCSV.accountingPeriod +
                    syncJobDataCSV.transactionDate + "  " + syncJobDataCSV.recordType +
                    journalNumberSpaces + lineSpaces +
                    syncJobDataCSV.amount + syncJobDataCSV.DCMarker +
                    allocationIndicatorSpace + syncJobDataCSV.journalType +
                    journalSource + syncJobDataCSV.transactionReference +
                    syncJobDataCSV.description + entryDate + entryPeriod + dueDate + filler6 + paymentRef +
                    paymentDate + paymentPeriod + asssetIndicator + asssetCode +
                    assetSubCode + conversionCode + conversionRate + otherAmount + amountDec + cleardown +
                            filler4 + nextPeriodReversal + textLinked + roughBookFlag + inUseFlag +
                            syncJobDataCSV.analysisCode0 + syncJobDataCSV.analysisCode1 + syncJobDataCSV.analysisCode2
                    );
        }

    }

    private void extractSyncJobData(){
        for (SyncJobData syncJobData : listSyncJobData) {
            SyncJobDataCSV syncJobDataCSV = new SyncJobDataCSV();
            syncJobDataCSV.fromCostCenter = syncJobData.getData().get("fromCostCenter");
            syncJobDataCSV.fromAccountCode = syncJobData.getData().get("fromAccountCode");
            syncJobDataCSV.toCostCenter = syncJobData.getData().get("toCostCenter");
            syncJobDataCSV.toAccountCode = syncJobData.getData().get("toAccountCode");
            syncJobDataCSV.fromLocation = syncJobData.getData().get("fromLocation");
            syncJobDataCSV.toLocation = syncJobData.getData().get("toLocation");
            syncJobDataCSV.DCMarker = syncJobData.getData().get("DCMarker");
            syncJobDataCSV.recordType = syncJobData.getData().get("recordType");
            syncJobDataCSV.journalType = syncJobData.getData().get("journalType");
            syncJobDataCSV.conversionCode = syncJobData.getData().get("conversionCode");
            syncJobDataCSV.conversionRate = syncJobData.getData().get("conversionRate");

            syncJobDataCSV.analysisCode0 = syncJobData.getData().get("analysisCode0");
            syncJobDataCSV.analysisCode1 = syncJobData.getData().get("analysisCode1");

            syncJobDataCSV.recordType = syncJobType.getConfiguration().getRecordType();

            syncJobDataCSV.description = syncJobData.getData().get("description");
            if(syncJobDataCSV.description.length() > 25){
                syncJobDataCSV.description = syncJobDataCSV.description.substring(0, 25);
            }else if(syncJobDataCSV.description.length() < 25) {
                syncJobDataCSV.description = String.format("%-25s", syncJobDataCSV.description);
            }

            syncJobDataCSV.transactionReference = syncJobData.getData().get("transactionReference");
            if(syncJobDataCSV.transactionReference.length() > 15){
                syncJobDataCSV.transactionReference = syncJobDataCSV.transactionReference.substring(0, 15);
            }else if(syncJobDataCSV.transactionReference.length() < 15) {
                syncJobDataCSV.transactionReference = String.format("%-15s", syncJobDataCSV.transactionReference);
            }

            String year = syncJobData.getData().get("transactionDate").substring(4);
            String month = syncJobData.getData().get("transactionDate").substring(2,4);
            String day = syncJobData.getData().get("transactionDate").substring(0,2);

            syncJobDataCSV.transactionDate = year + month + day;
            syncJobDataCSV.accountingPeriod = year + "0" + month;

            if(syncJobData.getData().containsKey("totalDr")){
                syncJobDataCSV.DCMarker = "D";
                String[] amountArray = syncJobData.getData().get("totalDr").substring(1).split("\\.");

                String amountPart = amountArray[0];
                String decimalPart = amountArray[1];

                if(amountPart.length() < 15){
                    amountPart = String.format("%0"+ 15 + "d", Integer.parseInt(amountPart));
                }

                if(decimalPart.length() > 3){
                    decimalPart = decimalPart.substring(0, 4);
                }else if (decimalPart.length() < 3){
                    decimalPart = decimalPart +
                            String.format("%0"+ (3 - decimalPart.length()) +"d", 0);
                }

                syncJobDataCSV.amount = amountPart + decimalPart;

                String accountCode = syncJobData.getData().get("expensesAccount");
                if(accountCode.length() < 10){
                    accountCode = String.format("%-10s", accountCode);
                }
                syncJobDataCSV.accountCode = accountCode;
            }else {
                syncJobDataCSV.DCMarker = "C";
                // 18 char --> 15 char + 3 decimals
                String[] amountArray = syncJobData.getData().get("totalCr").split("\\.");

                String amountPart = amountArray[0];
                String decimalPart = amountArray[1];

                if(amountPart.length() < 15){
                    amountPart = String.format("%0"+ 15 + "d", Integer.parseInt(amountPart));
                }

                if(decimalPart.length() > 3){
                    decimalPart = decimalPart.substring(0, 4);
                }else if (decimalPart.length() < 3){
                    decimalPart = decimalPart +
                            String.format("%0"+ (3 - decimalPart.length()) +"d", 0);
                }

                syncJobDataCSV.amount = amountPart + decimalPart;

                String accountCode = syncJobData.getData().get("inventoryAccount");
                if(accountCode.length() < 10){
                    accountCode = String.format("%-10s", accountCode);
                }
                syncJobDataCSV.accountCode = accountCode;
            }

            syncJobDataCSV.versionCode = syncJobType.getConfiguration().getVersionCode();

            syncJobDataCSV.conversionCode = syncJobType.getConfiguration().getConversionCode();
            syncJobDataCSV.conversionRate = syncJobType.getConfiguration().getConversionRate();

            syncJobDataCSV.journalType = syncJobType.getConfiguration().getJournalType();
            // 15 char
            syncJobDataCSV.analysisCode0 = syncJobType.getConfiguration().getAnalysis().get(0).getCodeElement();
            if(syncJobDataCSV.analysisCode0.length() > 15){
                syncJobDataCSV.analysisCode0 = syncJobDataCSV.analysisCode0.substring(0, 15);
            }else if(syncJobDataCSV.analysisCode0.length() < 15) {
                syncJobDataCSV.analysisCode0 = String.format("%-15s", syncJobDataCSV.analysisCode0);
            }

            syncJobDataCSV.analysisCode1 = syncJobType.getConfiguration().getAnalysis().get(1).getCodeElement();
            if(syncJobDataCSV.analysisCode1.length() > 15){
                syncJobDataCSV.analysisCode1 = syncJobDataCSV.analysisCode1.substring(0, 15);
            }else if(syncJobDataCSV.analysisCode1.length() < 15) {
                syncJobDataCSV.analysisCode1 = String.format("%-15s", syncJobDataCSV.analysisCode1);
            }

            syncJobDataCSV.analysisCode2 = syncJobType.getConfiguration().getAnalysis().get(2).getCodeElement();
            if(syncJobDataCSV.analysisCode2.length() > 15){
                syncJobDataCSV.analysisCode2 = syncJobDataCSV.analysisCode2.substring(0, 15);
            }else if(syncJobDataCSV.analysisCode2.length() < 15) {
                syncJobDataCSV.analysisCode2 = String.format("%-15s", syncJobDataCSV.analysisCode2);
            }

            this.syncJobDataCSVList.add(syncJobDataCSV);
        }
    }

}
