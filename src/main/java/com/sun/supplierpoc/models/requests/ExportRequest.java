package com.sun.supplierpoc.models.requests;

import java.util.Date;
import java.util.List;

public class ExportRequest {

//    private DateRange dateRange;
    private Date startDate;
    private Date endDate;
    private String email;
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

