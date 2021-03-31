package com.sun.supplierpoc;

import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.models.configurations.*;
import com.sun.supplierpoc.models.opera.booking.CancelReason;
import com.sun.supplierpoc.models.opera.booking.PaymentType;
import com.sun.supplierpoc.soapModels.Supplier;

import java.nio.charset.StandardCharsets;
import java.text.*;
import java.util.*;
import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

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

    public Tender checkTenderExistence(ArrayList<Tender> tenders, String tenderName, String location, float amount) {
        for (Tender tender : tenders) {
            if ((tender.getCostCenter().locationName.equals("General") || tender.getCostCenter().locationName.equals(location))
                    && (tender.getTender().toLowerCase().equals(tenderName.toLowerCase()) || tender.getChildren().contains(tenderName))) {
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

    public MajorGroup checkMajorGroupExistence(ArrayList<MajorGroup> majorGroups, String majorGroupName) throws CloneNotSupportedException {
        for (MajorGroup majorGroup : majorGroups) {
            if (majorGroup.getMajorGroup().toLowerCase().equals(majorGroupName.toLowerCase())
                    || majorGroup.getChildren().contains(majorGroupName)) {
                /*
                * Return copy of object
                * */
                MajorGroup mj = new MajorGroup();
                mj.setChecked(majorGroup.getChecked());
                mj.setOverGroup(majorGroup.getOverGroup());
                mj.setMajorGroup(majorGroup.getMajorGroup());
                mj.setFamilyGroups(majorGroup.getFamilyGroups());
                mj.setRevenueCenters(majorGroup.getRevenueCenters());
                mj.setChildren(majorGroup.getChildren());
                mj.setAccount(majorGroup.getAccount());
                mj.setDiscountAccount(majorGroup.getDiscountAccount());
                return mj;
            }
        }

        return new MajorGroup();
    }

    public RevenueCenter checkRevenueCenterExistence(ArrayList<RevenueCenter> revenueCenters, String revenueCenterName){
        for (RevenueCenter rc : revenueCenters) {
            if (rc.getRevenueCenter().toLowerCase().equals(revenueCenterName.toLowerCase())) {
                return rc;
            }
        }
        return new RevenueCenter();
    }

    public FamilyGroup checkFamilyGroupExistence(ArrayList<FamilyGroup> familyGroups, String familyGroupName){
        for (FamilyGroup fg : familyGroups) {
            if (fg.familyGroup.toLowerCase().equals(familyGroupName.toLowerCase())) {
                return fg;
            }
        }
        return new FamilyGroup();
    }

    public Discount checkDiscountExistence(ArrayList<Discount> discounts, String discountName){
        for (Discount discount : discounts) {
            if (discount.getDiscount().toLowerCase().equals(discountName.toLowerCase())) {
                return discount;
            }
        }

        return new Discount();
    }

    public ServiceCharge checkServiceChargeExistence(ArrayList<ServiceCharge> serviceCharges, String serviceChargeName,
                                                     String location){
        for (ServiceCharge serviceCharge : serviceCharges) {
            if ((serviceCharge.getCostCenter().locationName.equals("General") || serviceCharge.getCostCenter().locationName.equals(location))
                && (serviceCharge.getServiceCharge().toLowerCase().equals(serviceChargeName.toLowerCase()))) {
                return serviceCharge;
            }
        }

        return new ServiceCharge();
    }

    public SalesStatistics checkSalesStatisticsExistence(String location, ArrayList<SalesStatistics> statisticsList){
        for (SalesStatistics statistics : statisticsList) {
            if (statistics.location.toLowerCase().equals(location.toLowerCase())) {
                return statistics;
            }
        }
        return new SalesStatistics();
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

    public Supplier checkSupplierExistence(ArrayList<Supplier> suppliers, String vendorName){
        for (Supplier supplier : suppliers) {
            if (supplier.getSupplierName().toLowerCase().equals(vendorName.toLowerCase())) {
                return supplier;
            }
        }
        return null;
    }

    public SyncJobData checkSupplierDataExistence(ArrayList<SyncJobData> suppliers, String vendorName){
        for (SyncJobData supplier : suppliers) {
            if (supplier.getData().containsKey("supplier") && supplier.getData().get("supplier").toString().toLowerCase().equals(vendorName.toLowerCase())
                    || supplier.getData().get("description").toString().toLowerCase().equals(vendorName.toLowerCase())) {
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

    public HashMap<String, Object> checkSunDefaultConfiguration(SyncJobType syncJobType, String ERD){
        HashMap<String, Object> response = new HashMap<>();

        if (syncJobType.getConfiguration().inforConfiguration.businessUnit.equals("")){
            String message = "Configure business unit before sync " + syncJobType.getName();
            response.put("message", message);
            response.put("success", false);
            return response;
        }

        if (syncJobType.getConfiguration().inforConfiguration.journalType.equals("")){
            String message = "Configure journal type before sync invoices " + syncJobType.getName().toLowerCase();
            response.put("message", message);
            response.put("success", false);
            return response;
        }

        if (syncJobType.getConfiguration().inforConfiguration.currencyCode.equals("")){
            String message = "Configure currency code before sync invoices " + syncJobType.getName().toLowerCase();
            response.put("message", message);
            response.put("success", false);
            return response;
        }

        if (syncJobType.getConfiguration().inforConfiguration.postingType.equals("")){
            String message = "Configure posting type before sync invoices " + syncJobType.getName().toLowerCase();
            response.put("message", message);
            response.put("success", false);
            return response;
        }

        if (syncJobType.getConfiguration().analysis.size() == 0){
            String message = "Configure analysis before sync invoices " + syncJobType.getName().toLowerCase();
            response.put("message", message);
            response.put("success", false);
            return response;
        }

        if (syncJobType.getConfiguration().inforConfiguration.suspenseAccount.equals("")){
            String message = "Configure suspense account before sync invoices " + syncJobType.getName().toLowerCase();
            response.put("message", message);
            response.put("success", false);
            return response;
        }

        /*
        * Validate exported file configuration
        * */
        if(ERD.equals(Constants.EXPORT_TO_SUN_ERD)){
            if (syncJobType.getConfiguration().recordType.equals("")){
                String message = "Configure record type before sync invoices " + syncJobType.getName().toLowerCase();
                response.put("message", message);
                response.put("success", false);
                return response;
            }

            if (syncJobType.getConfiguration().conversionCode.equals("")){
                String message = "Configure conversion code before sync invoices " + syncJobType.getName().toLowerCase();
                response.put("message", message);
                response.put("success", false);
                return response;
            }

            if (syncJobType.getConfiguration().conversionRate.equals("")){
                String message = "Configure conversion rate before sync invoices " + syncJobType.getName().toLowerCase();
                response.put("message", message);
                response.put("success", false);
                return response;
            }

            if (syncJobType.getConfiguration().versionCode.equals("")){
                String message = "Configure version code before sync invoices " + syncJobType.getName().toLowerCase();
                response.put("message", message);
                response.put("success", false);
                return response;
            }
        }
        return null;
    }


    // ==> OPERA Report Functions

    public PaymentType checkPaymentTypeExistence(ArrayList<PaymentType> paymentTypes, String paymentTypeName){
        for (PaymentType paymentType : paymentTypes) {
            if (paymentType.getPaymentType().toLowerCase().equals(paymentTypeName.toLowerCase())) {
                return paymentType;
            }
        }
        return new PaymentType("0");
    }

    public CancelReason checkCancelReasonExistence(ArrayList<CancelReason> cancelReasons, String cancelReasonName){
        if(cancelReasonName.equals("")){
            return new CancelReason("0"); // Not applicable
        }
        for (CancelReason cancelReason : cancelReasons) {
            if (cancelReason.getReason().toLowerCase().equals(cancelReasonName.toLowerCase())) {
                return cancelReason;
            }
        }
        return new CancelReason("0");
    }

    public String checkRoomRentType(Date arrivalDate, Date departureDate){
        long diff = departureDate.getTime() - arrivalDate.getTime();
        long numberOfDays = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
        System.out.println ("Days: " + numberOfDays);

        if(numberOfDays % 7 == 0){
            return "3"; //Weekly
        }else if(numberOfDays % 30 == 0){
            return "4"; // Monthly
        }

        return "1"; //Daily
    }

    // ==> END of OPERA Report Functions

    public String filterString(String value){
        value = value.toLowerCase().replaceAll(",", "");
        return value;
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

//            DecimalFormat df = new DecimalFormat("###.####");
//            String temp = df.format(Float.parseFloat(value));
//            temp = temp.toLowerCase().replaceAll(",", "");
//            return Float.parseFloat(temp);
            return Float.parseFloat(value);
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
            currentYear = fromDateArr[0];

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
