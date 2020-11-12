package com.sun.supplierpoc.fileDelimiterExporters;

import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.sun.supplierpoc.models.SyncJobData;
import com.sun.supplierpoc.models.SyncJobType;
import com.sun.supplierpoc.models.util.SyncJobDataCSV;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SalesFileDelimiterExporter {
    private SyncJobType syncJobType;
    private List<SyncJobData> listSyncJobData;
    private List<SyncJobDataCSV> syncJobDataCSVList = new ArrayList<>();
    private StringBuilder fileContent = new StringBuilder();

    public SalesFileDelimiterExporter( SyncJobType syncJobType, List<SyncJobData> listSyncJobData) {
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

//        StatefulBeanToCsv<SyncJobDataCSV> csvWriter = new StatefulBeanToCsvBuilder<SyncJobDataCSV>(writer)
//                .withQuotechar(CSVWriter.NO_QUOTE_CHARACTER)
//                .withMappingStrategy(mapStrategy)
//                .withSeparator('\t')
//                .withOrderedResults(false)
//                .build();
//            csvWriter.write(this.syncJobDataCSVList);

        this.createFileContent();
        writer.print(this.fileContent);
    }

    public File createNDFFile(){
        this.createFileContent();
        File file = new File("sales.ndf");
        try (Writer writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(String.valueOf(this.fileContent));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return file;
    }

    private void createFileContent(){
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

        fileContent.append("VERSION                         ").append(this.syncJobDataCSVList.get(0).versionCode).append("\n");

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
                    .append(textLinked).append(roughBookFlag).append(inUseFlag).append(syncJobDataCSV.analysisCode0)
                    .append(syncJobDataCSV.analysisCode1).append(syncJobDataCSV.analysisCode2)
                    .append(syncJobDataCSV.analysisCode3).append(syncJobDataCSV.analysisCode4)
                    .append(syncJobDataCSV.analysisCode5);
            if(i != this.syncJobDataCSVList.size()-1){
                fileContent.append("\n");
            }
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
                String totalDr = syncJobData.getData().get("totalDr");
                if (totalDr.substring(0, 1).equals("-")){
                    totalDr = totalDr.substring(1);
                }
                String[] amountArray = totalDr.split("\\.");

                String amountPart = amountArray[0];
                String decimalPart = amountArray[1];
                if (amountPart.equals(""))
                    amountPart = "0";
                if (decimalPart.equals(""))
                    decimalPart = "0";

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
                String totalCr = syncJobData.getData().get("totalCr");
                if (totalCr.substring(0, 1).equals("-")){
                    totalCr = totalCr.substring(1);
                }
                String[] amountArray = totalCr.split("\\.");

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

            syncJobDataCSV.analysisCode3 = syncJobType.getConfiguration().getAnalysis().get(3).getCodeElement();
            if(syncJobDataCSV.analysisCode3.length() > 15){
                syncJobDataCSV.analysisCode3 = syncJobDataCSV.analysisCode3.substring(0, 15);
            }else if(syncJobDataCSV.analysisCode3.length() < 15) {
                syncJobDataCSV.analysisCode3 = String.format("%-15s", syncJobDataCSV.analysisCode3);
            }

            if (syncJobData.getData().containsKey("analysisCodeT5")){
                syncJobDataCSV.analysisCode4 = syncJobData.getData().get("analysisCodeT5");
            }else {
                syncJobDataCSV.analysisCode4 = syncJobType.getConfiguration().getAnalysis().get(4).getCodeElement();
            }
            if(syncJobDataCSV.analysisCode4.length() > 15){
                syncJobDataCSV.analysisCode4 = syncJobDataCSV.analysisCode4.substring(0, 15);
            }else if(syncJobDataCSV.analysisCode4.length() < 15) {
                syncJobDataCSV.analysisCode4 = String.format("%-15s", syncJobDataCSV.analysisCode4);
            }

            if (syncJobData.getData().containsKey("analysisCodeT6")){
                syncJobDataCSV.analysisCode5 = syncJobData.getData().get("analysisCodeT6");
            }else {
                syncJobDataCSV.analysisCode5 = syncJobType.getConfiguration().getAnalysis().get(5).getCodeElement();
            }
            if(syncJobDataCSV.analysisCode5.length() > 15){
                syncJobDataCSV.analysisCode5 = syncJobDataCSV.analysisCode5.substring(0, 15);
            }else if(syncJobDataCSV.analysisCode5.length() < 15) {
                syncJobDataCSV.analysisCode5 = String.format("%-15s", syncJobDataCSV.analysisCode5);
            }

            this.syncJobDataCSVList.add(syncJobDataCSV);
        }
    }

}
