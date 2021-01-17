package com.sun.supplierpoc.fileDelimiterExporters;

import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.sun.supplierpoc.models.SyncJobData;
import com.sun.supplierpoc.models.SyncJobType;
import com.sun.supplierpoc.models.util.SyncJobDataCSV;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class SalesFileDelimiterExporter {
    private String fileName;
    private SyncJobType syncJobType;
    private List<SyncJobData> listSyncJobData;
    private List<SyncJobDataCSV> syncJobDataCSVList = new ArrayList<>();
    private StringBuilder fileContent = new StringBuilder();

    public SalesFileDelimiterExporter() {
    }

    public SalesFileDelimiterExporter(String fileName) {
        this.fileName = fileName;
    }

    public SalesFileDelimiterExporter(String fileName, SyncJobType syncJobType, List<SyncJobData> listSyncJobData) {
        this.fileName = fileName;
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

        this.createFileContent();
        writer.print(this.fileContent);
    }

    public File createNDFFile() throws IOException {
        this.createFileContent();
        File file = new File(fileName);
        boolean status= file.getParentFile().mkdirs();
        if(status)
            file.createNewFile();

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

        fileContent.append("VERSION                         ")
                .append(this.syncJobType.getConfiguration().getVersionCode()).append("\n");

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
                    .append(syncJobDataCSV.analysisCode5).append(syncJobDataCSV.analysisCode6)
                    .append(syncJobDataCSV.analysisCode7)
                    .append(syncJobDataCSV.analysisCode8).append(syncJobDataCSV.analysisCode9);

            if(i != this.syncJobDataCSVList.size()-1){
                fileContent.append("\r\n");
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
                    decimalPart = decimalPart.substring(0, 3);
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
            }
            else {
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

            syncJobDataCSV.recordType = syncJobType.getConfiguration().getRecordType();
            syncJobDataCSV.versionCode = syncJobType.getConfiguration().getVersionCode();
            syncJobDataCSV.conversionCode = syncJobType.getConfiguration().getConversionCode();
            syncJobDataCSV.conversionRate = syncJobType.getConfiguration().getConversionRate();

            syncJobDataCSV.journalType = syncJobType.getConfiguration().getJournalType();
            if(syncJobDataCSV.journalType.length() > 5){
                syncJobDataCSV.journalType = syncJobDataCSV.journalType.substring(0, 5);
            }else if(syncJobDataCSV.journalType.length() < 5) {
                syncJobDataCSV.journalType = String.format("%-5s", syncJobDataCSV.journalType);
            }

            // 15 char
            syncJobDataCSV.analysisCode0 = fillTCode(0, syncJobData, false);
            syncJobDataCSV.analysisCode1 = fillTCode(1, syncJobData, false);
            syncJobDataCSV.analysisCode2 = fillTCode(2, syncJobData, false);
            syncJobDataCSV.analysisCode3 = fillTCode(3, syncJobData, false);
            syncJobDataCSV.analysisCode4 = fillTCode(4, syncJobData, false);
            syncJobDataCSV.analysisCode5 = fillTCode(5, syncJobData, false);
            syncJobDataCSV.analysisCode6 = fillTCode(6, syncJobData, false);
            syncJobDataCSV.analysisCode7 = fillTCode(7, syncJobData, false);
            syncJobDataCSV.analysisCode8 = fillTCode(8, syncJobData, false);
            syncJobDataCSV.analysisCode9 = fillTCode(9, syncJobData, false);

            if(syncJobType.getConfiguration().getLocationAnalysis() != null){
                int index = Integer.parseInt(syncJobType.getConfiguration().getLocationAnalysis()) -1;
                if(index == 1){
                    syncJobDataCSV.analysisCode0 = fillTCode(index, syncJobData, true);
                }else if(index == 2){
                    syncJobDataCSV.analysisCode1 = fillTCode(index, syncJobData, true);
                }else if(index == 3){
                    syncJobDataCSV.analysisCode2 = fillTCode(index, syncJobData, true);
                }else if(index == 4){
                    syncJobDataCSV.analysisCode3 = fillTCode(index, syncJobData, true);
                }else if(index == 5){
                    syncJobDataCSV.analysisCode4 = fillTCode(index, syncJobData, true);
                }else if(index == 6){
                    syncJobDataCSV.analysisCode5 = fillTCode(index, syncJobData, true);
                }
            }

            this.syncJobDataCSVList.add(syncJobDataCSV);
        }
    }

    private String fillTCode(int index, SyncJobData syncJobData, boolean locationFlag){
        String analysisTCode = "";

        if (!locationFlag && syncJobData.getData().containsKey("analysisCodeT" + (index + 1))){
            analysisTCode = syncJobData.getData().get("analysisCodeT" + (index + 1));
        }else if(locationFlag && syncJobData.getData().containsKey("fromLocation") && !syncJobData.getData().get("fromLocation").equals("")){
            analysisTCode = syncJobData.getData().get("fromLocation");
        }else {
            analysisTCode = syncJobType.getConfiguration().getAnalysis().get(index).getCodeElement();
        }

        if(analysisTCode.length() > 15){
            analysisTCode = analysisTCode.substring(0, 15);
        }else if(analysisTCode.length() < 15) {
            analysisTCode = String.format("%-15s", analysisTCode);
        }
        return analysisTCode;
    }

    public void generateSingleFile(PrintWriter printWriter, String path, String FileName, boolean perLocation) throws IOException {
        File folder = new File(path);
        File generalSyncFile = new File(path + "/" + FileName);
        Files.deleteIfExists(generalSyncFile.toPath());

        boolean status= generalSyncFile.getParentFile().mkdirs();
        if(status)
            generalSyncFile.createNewFile();

        String[] syncFileNames;
        BufferedReader reader;

        /*
         * Loop over all locations
         * */
        if (perLocation){
            String[] syncLocations = folder.list();
            if(syncLocations == null)
                syncLocations = new String[]{};

            for (String location : syncLocations) {
                if(new File(path, location).isDirectory()){
                    File locationFolder = new File(path + "/" + location);
                    syncFileNames = locationFolder.list();

                    assert syncFileNames != null;
                    for (int i = 0; i < syncFileNames.length; i++) {
                        String syncFileName = syncFileNames[i];
                        reader = new BufferedReader(new FileReader(path + "/"+ location + "/" + syncFileName));

                        String line;
                        String ls = System.getProperty("line.separator");
                        while ((line = reader.readLine()) != null) {
                            if(i != 0 && line.contains("VERSION")){
                                continue;
                            }

                            this.fileContent.append(line);
                            this.fileContent.append(ls);
                        }

                        // delete the last new line separator
                        this.fileContent.deleteCharAt(this.fileContent.length() - 2);
                        reader.close();
                    }
                }
            }
        }else{
            syncFileNames = folder.list();
            if(syncFileNames == null)
                syncFileNames = new String[]{};

            for (int i = 0; i < syncFileNames.length; i++) {
                String pathname = syncFileNames[i];
                reader = new BufferedReader(new FileReader(path + pathname));

                String line;
                String ls = System.getProperty("line.separator");
                while ((line = reader.readLine()) != null) {
                    if(i != 0 && line.contains("VERSION")){
                        continue;
                    }

                    this.fileContent.append(line);
                    this.fileContent.append(ls);
                }

                // delete the last new line separator
                this.fileContent.deleteCharAt(this.fileContent.length() - 2);
                reader.close();
            }
        }

        try (Writer writer = new BufferedWriter(new FileWriter(generalSyncFile))) {
            writer.write(String.valueOf(this.fileContent));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (printWriter != null){
            printWriter.flush();
            printWriter.print(this.fileContent);
        }
    }

    public ArrayList<String> ListSyncFiles(String path){
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();
        ArrayList<String> fileName = new ArrayList<>();

        assert listOfFiles != null;
        for (File listOfFile : listOfFiles) {
            if (listOfFile.isFile()) {
                fileName.add(path + "/" + listOfFile.getName());
            } else if (listOfFile.isDirectory()) {
                fileName.addAll(ListSyncFiles(path + "/" + listOfFile.getName()));
            }
        }
        return fileName;
    }
}
