package com.sun.supplierpoc.services;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.models.Journal;
import com.sun.supplierpoc.models.SyncJob;
import com.sun.supplierpoc.models.SyncJobData;
import com.sun.supplierpoc.models.configurations.*;
import com.sun.supplierpoc.repositories.SyncJobDataRepo;
import com.sun.supplierpoc.repositories.SyncJobRepo;
import com.sun.supplierpoc.soapModels.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class SyncJobDataService {
    @Autowired
    private SyncJobRepo syncJobRepo;

    @Autowired
    SyncJobDataRepo syncJobDataRepo;

    public void updateSyncJobDataStatus(SyncJobData syncJobData, String status, String reason) {
        syncJobData.setStatus(status);
        syncJobData.setReason(reason);
        syncJobDataRepo.save(syncJobData);
    }

    public void updateSyncJobDataStatus(List<SyncJobData> syncJobDataArrayList, String status) {
        for (SyncJobData syncJobData : syncJobDataArrayList) {
            syncJobData.setStatus(status);
            syncJobDataRepo.save(syncJobData);
        }
    }

    public ArrayList<SyncJobData> getSyncJobData(String syncJobID){
        return (ArrayList<SyncJobData>) syncJobDataRepo.findBySyncJobIdAndDeleted(syncJobID, false);
    }

    public ArrayList<SyncJobData> getSyncJobDataByBookingNo(String bookingNo){
        return (ArrayList<SyncJobData>) syncJobDataRepo.findByDataByBookingNo(bookingNo);
    }

    public ArrayList<SyncJobData> getSyncJobDataByBookingNoAndType(String bookingNo, String typeId){
        return (ArrayList<SyncJobData>) syncJobDataRepo.findByDataByBookingNoAndSyncJobTypeId(bookingNo, typeId);
    }

    public ArrayList<SyncJobData> getDataByBookingNoAndSyncType(String bookingNo, String syncJobTypeId){
        List<SyncJobData> data;
        ArrayList<SyncJobData> syncJobData = new ArrayList<>();

        List<SyncJob> syncJobs = syncJobRepo.findBySyncJobTypeIdAndDeletedOrderByCreationDateDesc(syncJobTypeId, false);
        for (SyncJob syncJob : syncJobs) {
            if(bookingNo == null || bookingNo.equals("")){
                data = syncJobDataRepo.findBySyncJobIdAndDeleted(syncJob.getId(), false);
            }else {
                data = syncJobDataRepo.findByBookingNoAndSyncJobId(bookingNo, syncJob.getId());
            }
            syncJobData.addAll(data);
        }

        return syncJobData;
    }

    public ArrayList<SyncJobData> getSyncJobDataByTypeId(String syncJobTypeId)  {
        List<SyncJob> syncJobs = syncJobRepo.findBySyncJobTypeIdAndDeletedOrderByCreationDateDesc(syncJobTypeId, false);
        ArrayList<SyncJobData> syncJobsData = new ArrayList<>();
        for (SyncJob syncJob : syncJobs) {
            List<SyncJobData> syncJobData = syncJobDataRepo.findBySyncJobIdAndDeleted(syncJob.getId(), false);
            syncJobsData.addAll(syncJobData);
        }
        return syncJobsData;
    }

    public ArrayList<SyncJobData> getFailedSyncJobData(String syncJobTypeId)  {
        List<SyncJob> syncJobs = syncJobRepo.findBySyncJobTypeIdAndDeletedOrderByCreationDateDesc(syncJobTypeId, false);
        ArrayList<SyncJobData> syncJobsData = new ArrayList<>();
        for (SyncJob syncJob : syncJobs) {
            List<SyncJobData> syncJobData = syncJobDataRepo.findBySyncJobIdAndDeletedAndStatus(syncJob.getId(),
                    false, Constants.FAILED);
            syncJobsData.addAll(syncJobData);
        }
        return syncJobsData;
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

    public void prepareConsumptionJournalAnalysis(HashMap<String, Object> data, Configuration configuration,
                                CostCenter location, String DCMarker){
        ArrayList<Analysis> analysis = configuration.analysis;
        for (int i = 1; i <= analysis.size(); i++) {
            data.put("analysisCodeT" + i, analysis.get(i - 1).getCodeElement());
        }

        String index;
        if(location != null && !location.accountCode.equals("") && !location.locationName.equals("") && DCMarker.equals("D")){
            index = configuration.locationAnalysisCode;
            data.put("analysisCodeT" + index, location.accountCode);
        }
    }

    public void prepareConsumptionAnalysis(RevenueCenter revenueCenter, HashMap<String, Object> data, Configuration configuration,
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

        if(revenueCenter.isRequireAnalysis()){
            if (familyGroup != null && !familyGroup.departmentCode.equals("")) {
                index = configuration.familyGroupAnalysisCode;
                data.put("analysisCodeT" + index, familyGroup.departmentCode);
            }
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

        if(journal != null ){
            index = configuration.taxesCodeAnalysisCode;
            if(journal.getTax() == 14.00){
            data.put("analysisCodeT" + index, "DV114");}
            else{
            data.put("analysisCodeT" + index, "NOTAX");}
        }
    }
}
