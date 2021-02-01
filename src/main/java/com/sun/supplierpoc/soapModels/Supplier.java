package com.sun.supplierpoc.soapModels;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Objects;

@XmlRootElement(name="Supplier")
public class Supplier {
    @XmlElement
    String AccountCode = "";
    @XmlElement
    String SupplierName = "";
    @XmlElement
    String SupplierCode = "";
    @XmlElement
    String Status = "";
    @XmlElement
    String CustomerNumber = "";
    @XmlElement
    String PaymentTermsGroupCode = "";
    @XmlElement
    SupplierAddress SupplierAddress = new SupplierAddress();
    @XmlElement
    Address_Contact Address_Contact = new Address_Contact();
    @XmlElement
    Accounts Accounts = new Accounts();

    public String getAccountCode() {
        return AccountCode;
    }

    public void setAccountCode(String accountCode) {
        AccountCode = accountCode;
    }

    public String getSupplierName() {
        if(SupplierName != null){
            return SupplierName;
        }
        else return "";
    }

    public void setSupplierName(String supplierName) {
        SupplierName = supplierName;
    }

    public String getSupplierCode() {
        if(SupplierCode != null){
            return SupplierCode;
        }
        else return "";
    }

    public void setSupplierCode(String supplierCode) {
        SupplierCode = supplierCode;
    }

    public String getStatus() {
        if(Status != null){
            return Status;
        }
        else return "";
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getPaymentTermsGroupCode() {
        if(PaymentTermsGroupCode != null){
            return PaymentTermsGroupCode;
        }
        else return "";

    }

    public void setPaymentTermsGroupCode(String paymentTermsGroupCode) {
        PaymentTermsGroupCode = paymentTermsGroupCode;
    }

    public SupplierAddress getSupplierAddress() {
        if(SupplierAddress != null){
            return SupplierAddress;
        }
        else return new SupplierAddress();
    }

    public void setSupplierAddress(SupplierAddress supplierAddress) {
        SupplierAddress = supplierAddress;
    }

    public Address_Contact getAddress_Contact() {
        if(Address_Contact != null){
            return Address_Contact;
        }
        else return new Address_Contact();
    }

    public void setAddress_Contact(Address_Contact address_Contact) {
        Address_Contact = address_Contact;
    }

    public String getCustomerNumber() {
        return Objects.requireNonNullElse(CustomerNumber, "");
    }

    public void setCustomerNumber(String customerNumber) {
        CustomerNumber = customerNumber;
    }

    public com.sun.supplierpoc.soapModels.Accounts getAccounts() {
        return Accounts;
    }

    public void setAccounts(com.sun.supplierpoc.soapModels.Accounts accounts) {
        Accounts = accounts;
    }
}
