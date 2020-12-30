package com.sun.supplierpoc;

import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.models.configurations.*;

import java.nio.charset.StandardCharsets;
import java.text.*;
import java.util.*;
import java.text.DecimalFormat;

public class Conversions {
    public Conversions() {
    }

    public String transformColName(String columnName){
        return columnName.strip().toLowerCase().replace(' ', '_');
    }

    public Tax checkTaxExistence(ArrayList<Tax> taxes, String taxName) {
        for (Tax tax : taxes) {
            if (tax.getTax().toLowerCase().equals(taxName.toLowerCase())) {
                return tax;
            }
        }
        return new Tax();
    }

    public Tender checkTenderExistence(ArrayList<Tender> tenders, String tenderName, float amount) {
        for (Tender tender : tenders) {
            if (tender.getTender().toLowerCase().equals(tenderName.toLowerCase())
            || tender.getChildren().contains(tenderName)) {
                tender.setTotal(tender.getTotal() + amount);
                return tender;
            }
        }
        return new Tender();
    }

    public OverGroup checkOverGroupExistence(ArrayList<OverGroup> overGroups, String overGroupName) {
        for (OverGroup overGroup : overGroups) {
            if (overGroup.getOverGroup().toLowerCase().equals(overGroupName.toLowerCase())) {
                return overGroup;
            }
        }
        return new OverGroup();
    }

    public MajorGroup checkMajorGroupExistence(ArrayList<MajorGroup> majorGroups, String majorGroupName){
        for (MajorGroup majorGroup : majorGroups) {
            if (majorGroup.getMajorGroup().toLowerCase().equals(majorGroupName.toLowerCase())) {
                return majorGroup;
            }
        }

        return new MajorGroup();
    }

    public Discount checkDiscountExistence(ArrayList<Discount> discounts, String discountName){
        for (Discount discount : discounts) {
            if (discount.getDiscount().toLowerCase().equals(discountName.toLowerCase())) {
                return discount;
            }
        }

        return new Discount();
    }

    public ServiceCharge checkServiceChargeExistence(ArrayList<ServiceCharge> serviceCharges, String serviceChargeName){
        for (ServiceCharge serviceCharge : serviceCharges) {
            if (serviceCharge.getServiceCharge().toLowerCase().equals(serviceChargeName.toLowerCase())) {
                return serviceCharge;
            }
        }

        return new ServiceCharge();
    }

    public ItemGroup checkItemGroupExistence(ArrayList<ItemGroup> itemGroups, String itemGroupName){
        for (ItemGroup itemGroup : itemGroups) {
            if (itemGroup.getItemGroup().toLowerCase().equals(itemGroupName.toLowerCase())) {
                return itemGroup;
            }
        }
        return new ItemGroup();
    }

    public Item checkItemExistence(ArrayList<Item> items, String itemName){
        for (Item item : items) {
            if (item.getItem().toLowerCase().equals(itemName.toLowerCase())) {
                return item;
            }
        }
        return new Item();
    }

    public WasteGroup checkWasteTypeExistence(ArrayList<WasteGroup> wasteTypes, String wasteTypeName){
        for (WasteGroup wasteType : wasteTypes) {
            if (wasteType.getWasteGroup().toLowerCase().equals(wasteTypeName.toLowerCase())) {
                return wasteType;
            }
        }
        return new WasteGroup();
    }

    public CostCenter checkCostCenterExistence(ArrayList<CostCenter> costCenters, String costCenterName,
                                               boolean getOrUseFlag){
        for (CostCenter costCenter : costCenters) {
            String savedCostCenterName = costCenter.costCenter;
            if (!getOrUseFlag){ // True in case of getting and False in case of use
                if (savedCostCenterName.indexOf('(') != -1){
                    savedCostCenterName = savedCostCenterName.substring(0, savedCostCenterName.indexOf('(') - 1);
                }
            }
            if (savedCostCenterName.toLowerCase().equals(costCenterName.toLowerCase())) {
                return costCenter;
            }
        }
        return new CostCenter();
    }

    public SyncJobData checkSupplierExistence(ArrayList<SyncJobData> suppliers, String vendorName){
        for (SyncJobData supplier : suppliers) {
            if (supplier.getData().containsKey("supplier") && supplier.getData().get("supplier").toLowerCase().equals(vendorName.toLowerCase())
            || supplier.getData().get("description").toLowerCase().equals(vendorName.toLowerCase())) {
                return supplier;
            }
        }
        return null;
    }

    public SyncJobData checkInvoiceExistence(ArrayList<SyncJobData> invoices, String invoiceNumber, String overGroup){
        for (SyncJobData invoice : invoices) {
            if (invoice.getData().containsKey("invoiceNo") && invoice.getData().get("invoiceNo") != null
                    && invoice.getData().get("invoiceNo").equals(invoiceNumber) &&
                    invoice.getData().get("overGroup").equals(overGroup)) {
                return invoice;
            }
        }
        return null;
    }

    public HashMap<String, Object> checkSunDefaultConfiguration(SyncJobType syncJobType){
        HashMap<String, Object> response = new HashMap<>();

        if (syncJobType.getConfiguration().getBusinessUnit().equals("")){
            String message = "Configure business unit before sync " + syncJobType.getName();
            response.put("message", message);
            response.put("success", false);
            return response;
        }

        if (syncJobType.getConfiguration().getJournalType().equals("")){
            String message = "Configure journal type before sync invoices " + syncJobType.getName().toLowerCase();
            response.put("message", message);
            response.put("success", false);
            return response;
        }

        if (syncJobType.getConfiguration().getCurrencyCode().equals("")){
            String message = "Configure currency code before sync invoices " + syncJobType.getName().toLowerCase();
            response.put("message", message);
            response.put("success", false);
            return response;
        }

        if (syncJobType.getConfiguration().getPostingType().equals("")){
            String message = "Configure posting type before sync invoices " + syncJobType.getName().toLowerCase();
            response.put("message", message);
            response.put("success", false);
            return response;
        }

        if (syncJobType.getConfiguration().getAnalysis().size() == 0){
            String message = "Configure analysis before sync invoices " + syncJobType.getName().toLowerCase();
            response.put("message", message);
            response.put("success", false);
            return response;
        }

        if (syncJobType.getConfiguration().getLocationAnalysis().equals("")){
            String message = "Configure location ledger analysis before sync invoices " + syncJobType.getName().toLowerCase();
            response.put("message", message);
            response.put("success", false);
            return response;
        }

        if (syncJobType.getConfiguration().getSuspenseAccount().equals("")){
            String message = "Configure suspense account before sync invoices " + syncJobType.getName().toLowerCase();
            response.put("message", message);
            response.put("success", false);
            return response;
        }

        return null;
    }

    public float convertStringToFloat(String value){
        if (value == null){
            return 0;
        }
        try{
            value = value.toLowerCase().replaceAll(",", "");
            if (value.contains("(")){
                value = value.replace("(", "").replace(")", "");
                value = "-" + value;
            }

            DecimalFormat df = new DecimalFormat("###.###");
            String temp = df.format(Float.parseFloat(value));
            temp = temp.toLowerCase().replaceAll(",", "");
            return Float.parseFloat(temp);
        } catch (NumberFormatException e) {
            System.out.println(e.getMessage());
            return 0;
        }
    }

    public float roundUpFloat(float value){
        DecimalFormat df = new DecimalFormat("###.###");
        String temp = df.format(value);
        temp = temp.toLowerCase().replaceAll(",", "");
        return Float.parseFloat(temp);
    }

    public String getTransactionDate(String businessDate, String fromDate){
        SimpleDateFormat inMonthFormatter = new SimpleDateFormat("M");
        SimpleDateFormat outMonthFormatter = new SimpleDateFormat("MM");

        String transactionDate = "";
        Calendar cal = Calendar.getInstance();
        DateFormat dayFormat = new SimpleDateFormat("dd");
        DateFormat monthFormat = new SimpleDateFormat("MM");
        DateFormat dayMonthFormat = new SimpleDateFormat("ddMM");

        String lastMonth = String.valueOf(Calendar.getInstance().get(Calendar.MONTH));
        if (lastMonth.equals("0")){
            lastMonth = "12";
        }

        Date date;
        try {
            date = inMonthFormatter.parse(lastMonth);
            lastMonth = outMonthFormatter.format(date);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        String today = dayFormat.format(cal.getTime());
        String currentMonth = monthFormat.format(cal.getTime());
        String currentYear = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));

        if (businessDate.equals(Constants.TODAY)){
            transactionDate += today + currentMonth ;

        }
        else if (businessDate.equals(Constants.YESTERDAY)){
            cal.add(Calendar.DATE, -1);
            String yesterday = dayMonthFormat.format(cal.getTime());
            cal.add(Calendar.DATE, +1);

            transactionDate += yesterday;
        }
        else if (businessDate.equals(Constants.PAST_7_DAYES)){
            cal.add(Calendar.DATE, -7);
            String last7Days = dayMonthFormat.format(cal.getTime());
            cal.add(Calendar.DATE, +7);

            transactionDate += last7Days;

        }
        else if (businessDate.equals(Constants.LAST_MONTH) || businessDate.equals(Constants.MONTH_TO_DATE)){
            transactionDate += "01" + lastMonth ;

        }
        else if(businessDate.equals(Constants.CURRENT_MONTH)){
            transactionDate += "01" + currentMonth ;

        }
        else if(businessDate.equals(Constants.USER_DEFINED)){
            String[] fromDateArr = fromDate.split("-");
            transactionDate += fromDateArr[2] + fromDateArr[1] ;
        }
        else{
            transactionDate += today + currentMonth ;
        }

        transactionDate += currentYear;

        return transactionDate;
    }

    public String[] convertBasicAuth(String authorization){
        String[] values = {};
        if (authorization != null && authorization.toLowerCase().startsWith("basic")) {
            String base64Credentials = authorization.substring("Basic".length()).trim();
            byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
            String credentials = new String(credDecoded, StandardCharsets.UTF_8);
            values = credentials.split(":", 2);
            return values;
        }else{
            return values;
        }
    }
}
