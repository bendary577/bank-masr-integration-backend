package com.sun.supplierpoc.models.requests;

import com.sun.supplierpoc.models.SyncJobType;
import com.sun.supplierpoc.models.configurations.CostCenter;

import java.util.Date;
import java.util.List;

public class ExportRequest {

    private List<SyncJobType> syncJobTypes;
    private List<CostCenter> costCenters;
//    private DateRange dateRange;
    private Date startDate;
    private Date endDate;
    private String email;

    public List<SyncJobType> getSyncJobTypes() {
        return syncJobTypes;
    }

    public void setSyncJobTypes(List<SyncJobType> syncJobTypes) {
        this.syncJobTypes = syncJobTypes;
    }

    public List<CostCenter> getCostCenters() {
        return costCenters;
    }

    public void setCostCenters(List<CostCenter> costCenters) {
        this.costCenters = costCenters;
    }

//    public DateRange getDateRange() {
//        return dateRange;
//    }
//
//    public void setDateRange(DateRange dateRange) {
//        this.dateRange = dateRange;
//    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public class DateRange{
        private Date startDate;
        private Date endDate;

        public Date getStartDate() {
            return startDate;
        }

        public void setStartDate(Date startDate) {
            this.startDate = startDate;
        }

        public Date getEndDate() {
            return endDate;
        }

        public void setEndDate(Date endDate) {
            this.endDate = endDate;
        }
    }

}

