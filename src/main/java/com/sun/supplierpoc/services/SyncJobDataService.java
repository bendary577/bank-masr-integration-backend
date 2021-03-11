package com.sun.supplierpoc.services;

import com.sun.supplierpoc.models.Journal;
import com.sun.supplierpoc.models.SyncJobData;
import com.sun.supplierpoc.models.configurations.*;
import com.sun.supplierpoc.repositories.SyncJobDataRepo;
import com.sun.supplierpoc.soapModels.Supplier;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class SyncJobDataService {

    @Autowired
    SyncJobDataRepo syncJobDataRepo;

    public void updateSyncJobDataStatus(List<SyncJobData> syncJobDataArrayList, String status) {
        for (SyncJobData syncJobData : syncJobDataArrayList) {
            syncJobData.setStatus(status);
            syncJobDataRepo.save(syncJobData);
        }
    }

    public ArrayList<SyncJobData> getSyncJobData(String syncJobID){
        return (ArrayList<SyncJobData>) syncJobDataRepo.findBySyncJobIdAndDeleted(syncJobID, false);
    }

    public void prepareAnalysis(HashMap<String, Object> data, Configuration configuration,
                                CostCenter location, FamilyGroup familyGroup, Tender tender){
        ArrayList<Analysis> analysis = configuration.analysis;
        for (int i = 1; i <= analysis.size(); i++) {
            data.put("analysisCodeT" + i, analysis.get(i - 1).getCodeElement());
        }

        String index;
        if(location != null && !location.accountCode.equals("")){
            index = configuration.locationAnalysisCode;
            data.put("analysisCodeT" + index, location.accountCode);
        }

        if(tender != null && !tender.getAnalysisCodeT5().equals("")){
            index = configuration.tenderAnalysisCode;
            data.put("analysisCodeT" + index, tender.getAnalysisCodeT5());
        }

        if(familyGroup != null && !familyGroup.departmentCode.equals("")){
            index = configuration.familyGroupAnalysisCode;
            data.put("analysisCodeT" + index, familyGroup.departmentCode);
        }
    }

    public void prepareAnalysisForInvoices(HashMap<String, Object> data, Configuration configuration,
                                           CostCenter location, FamilyGroup familyGroup, Tender tender, Supplier supplier, Journal journal){
        ArrayList<Analysis> analysis = configuration.analysis;
        for (int i = 1; i <= analysis.size(); i++) {
            data.put("analysisCodeT" + i, analysis.get(i - 1).getCodeElement());
        }

        String index;
        if(location != null && !location.accountCode.equals("")){
            index = configuration.locationAnalysisCode;
            data.put("analysisCodeT" + index, location.accountCode);
        }

        if(tender != null && !tender.getAnalysisCodeT5().equals("")){
            index = configuration.tenderAnalysisCode;
            data.put("analysisCodeT" + index, tender.getAnalysisCodeT5());
        }

        if(familyGroup != null && !familyGroup.departmentCode.equals("")){
            index = configuration.familyGroupAnalysisCode;
            data.put("analysisCodeT" + index, familyGroup.departmentCode);
        }

        if(supplier != null && !supplier.getAccountCode().equals("")){
            index = configuration.supplierCodeAnalysisCode;
            data.put("analysisCodeT" + index, supplier.getAccountCode());
        }

//        LoggerFactory.getLogger(SyncJobDataService.class).info(journal.get);
        if(journal != null && !journal.getTax().equals("")){
            index = configuration.taxesCodeAnalysisCode;
            if(journal.getTax().equals("14.00")){
            data.put("analysisCodeT" + index, "DV114");}
            else{
            data.put("analysisCodeT" + index, "NOTAX");}
        }
    }
}
