package com.sun.supplierpoc.models.simphony.tender;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "AccountDataSource",
        "AccountType",
        "IssueNumber",
        "PaymentCommand",
        "StartDate"
})

public class TmedEPayment {
    @XmlElement(name = "AccountDataSource")
    private String AccountDataSource;
    @XmlElement(name = "AccountType")
    private String AccountType;
    @XmlElement(name = "IssueNumber")
    private String IssueNumber;
    @XmlElement(name = "PaymentCommand")
    private String PaymentCommand;
    @XmlElement(name = "StartDate")
    private String StartDate;

    public String getAccountDataSource() {
        return AccountDataSource;
    }

    public void setAccountDataSource(String accountDataSource) {
        AccountDataSource = accountDataSource;
    }

    public String getAccountType() {
        return AccountType;
    }

    public void setAccountType(String accountType) {
        AccountType = accountType;
    }

    public String getIssueNumber() {
        return IssueNumber;
    }

    public void setIssueNumber(String issueNumber) {
        IssueNumber = issueNumber;
    }

    public String getPaymentCommand() {
        return PaymentCommand;
    }

    public void setPaymentCommand(String paymentCommand) {
        PaymentCommand = paymentCommand;
    }

    public String getStartDate() {
        return StartDate;
    }

    public void setStartDate(String startDate) {
        StartDate = startDate;
    }
}
