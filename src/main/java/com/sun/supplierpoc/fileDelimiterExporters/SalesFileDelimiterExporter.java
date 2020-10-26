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

    public void writeSalesSyncData(PrintWriter writer) {
        try {
            ColumnPositionMappingStrategy<SyncJobDataCSV> mapStrategy
                    = new ColumnPositionMappingStrategy<>();

            mapStrategy.setType(SyncJobDataCSV.class);

            String[] columns = new String[]{"accountCode", "accountingPeriod", "transactionDate",
            "recordType", "amount", "DCMarker", "journalType", "transactionReference", "description",
            "conversionCode", "conversionRate", "otherAmount", "analysisCode0", "analysisCode1"};
            mapStrategy.setColumnMapping(columns);

            StatefulBeanToCsv<SyncJobDataCSV> btcsv = new StatefulBeanToCsvBuilder<SyncJobDataCSV>(writer)
                    .withQuotechar(CSVWriter.NO_QUOTE_CHARACTER)
                    .withMappingStrategy(mapStrategy)
                    .withSeparator('\t')
                    .build();

            this.extractSyncJobData();
            btcsv.write(this.syncJobDataCSVList);

        } catch (CsvException ex) {

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
            syncJobDataCSV.transactionReference = syncJobData.getData().get("transactionReference");
            syncJobDataCSV.DCMarker = syncJobData.getData().get("DCMarker");
            syncJobDataCSV.recordType = syncJobData.getData().get("recordType");
            syncJobDataCSV.journalType = syncJobData.getData().get("journalType");
            syncJobDataCSV.conversionCode = syncJobData.getData().get("conversionCode");
            syncJobDataCSV.conversionRate = syncJobData.getData().get("conversionRate");

            syncJobDataCSV.analysisCode0 = syncJobData.getData().get("analysisCode0");
            syncJobDataCSV.analysisCode1 = syncJobData.getData().get("analysisCode1");

            syncJobDataCSV.recordType = "L";

            syncJobDataCSV.transactionDate = syncJobData.getData().get("transactionDate");
            if (!syncJobData.getData().containsKey("accountingPeriod") ||
                    !syncJobData.getData().get("accountingPeriod").equals("")){
                syncJobDataCSV.accountingPeriod = syncJobData.getData().get("transactionDate").substring(2,6);
            }else {
                syncJobDataCSV.accountingPeriod = syncJobData.getData().get("accountingPeriod");
            }

            if(syncJobData.getData().containsKey("totalDr")){
                syncJobDataCSV.DCMarker = "D";
                syncJobDataCSV.amount = syncJobData.getData().get("totalDr");
                syncJobDataCSV.otherAmount = syncJobData.getData().get("totalDr");
                syncJobDataCSV.accountCode = syncJobData.getData().get("expensesAccount");
            }else {
                syncJobDataCSV.DCMarker = "C";
                syncJobDataCSV.amount = syncJobData.getData().get("totalCr");
                syncJobDataCSV.otherAmount = syncJobData.getData().get("totalCr");
                syncJobDataCSV.accountCode = syncJobData.getData().get("inventoryAccount");
            }
            syncJobDataCSV.conversionCode = "1";
            syncJobDataCSV.conversionRate = "1.0";

            syncJobDataCSV.journalType = syncJobType.getConfiguration().getJournalType();
            syncJobDataCSV.analysisCode0 = syncJobType.getConfiguration().getAnalysis().get(0).getCodeElement();
            syncJobDataCSV.analysisCode1 = syncJobType.getConfiguration().getAnalysis().get(1).getCodeElement();

            this.syncJobDataCSVList.add(syncJobDataCSV);
        }
    }

}
