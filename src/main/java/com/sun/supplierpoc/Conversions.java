package com.sun.supplierpoc;

import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.models.applications.Balance;
import com.sun.supplierpoc.models.applications.SimphonyDiscount;
import com.sun.supplierpoc.models.configurations.*;
import com.sun.supplierpoc.models.opera.booking.BookingType;
import com.sun.supplierpoc.models.opera.booking.Package;
import com.sun.supplierpoc.models.opera.booking.RateCode;
import org.springframework.stereotype.Component;

import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.*;
import java.util.*;
import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

@Component
public class Conversions {
    public Conversions() {
    }

    public String transformColName(String columnName){
        return columnName.strip().toLowerCase().replace(' ', '_');
    }

    public RevenueCenter checkRevenueCenterExistence(ArrayList<RevenueCenter> revenueCenters, String revenueCenterName){
        for (RevenueCenter rc : revenueCenters) {
            if (rc.getRevenueCenter().toLowerCase().equals(revenueCenterName.toLowerCase())) {
                return rc;
            }
        }
        return new RevenueCenter();
    }

    public boolean validateRevenueCenter(ArrayList<RevenueCenter> revenueCenters, int revenueCenterId){
        for (RevenueCenter revenueCenter : revenueCenters) {
            if (revenueCenter.getRevenueCenterId() == revenueCenterId) {
                if(revenueCenter.isChecked()) {
                    return true;
                }
            }
        }
        return false;
    }

    public RevenueCenter getRevenueCenter(ArrayList<RevenueCenter> revenueCenters, int revenueCenterId){
        for (RevenueCenter revenueCenter : revenueCenters) {
            if (revenueCenter.getRevenueCenterId() == revenueCenterId) {
                if(revenueCenter.isChecked()) {
                    return revenueCenter;
                }
            }
        }
        return new RevenueCenter();
    }

    // ==> OPERA Report Functions

    public BookingType checkBookingTypeExistence(ArrayList<BookingType> bookingTypes, String typeName){
        if(typeName.equals("") || bookingTypes.size() == 0){
            typeName = "other";
        }
        for (BookingType paymentType : bookingTypes) {
            if (paymentType.getType().toLowerCase().equals(typeName.toLowerCase())) {
                return paymentType;
            }
        }
        return new BookingType(0);
    }

    public BookingType checkExpenseTypeExistence(ArrayList<BookingType> expenseTypes, String typeName){
        if(typeName.equals("")){
            return new BookingType(1);
        }
        for (BookingType paymentType : expenseTypes) {
            if(typeName.toLowerCase().contains(paymentType.getType().toLowerCase())){
                return paymentType;
            }
        }

        return new BookingType(1);
    }

    public RateCode checkRateCodeExistence(ArrayList<RateCode> rateCodes, String code){
        if(code.equals("") || rateCodes.size() == 0){
            return new RateCode();
        }
        for (RateCode rateCode : rateCodes) {
            if (rateCode.code.toLowerCase().equals(code.toLowerCase())) {
                return rateCode;
            }
        }
        return new RateCode();
    }

    public boolean checkPackageExistence(ArrayList<Package> packages, String name, String source){
        for (Package pkg : packages) {
            if (pkg.packageName.toLowerCase().equals(name.toLowerCase()) && pkg.source.toLowerCase().equals(source.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public boolean checkPackageExistence(ArrayList<Package> packages, String name, String source, Date consumptionDate){
        for (Package pkg : packages) {
            if (pkg.packageName.toLowerCase().equals(name.toLowerCase())
                    && pkg.source.toLowerCase().equals(source.toLowerCase())
                    && pkg.consumptionDate.compareTo(consumptionDate) == 0
            ) {
                return true;
            }
        }
        return false;
    }

    public String checkRoomRentType(Date arrivalDate, Date departureDate){
        long diff = departureDate.getTime() - arrivalDate.getTime();
        long numberOfDays = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
        if(numberOfDays < 30 && numberOfDays % 7 == 0){
            return "3"; //Weekly
        }else if(numberOfDays >= 29 && (numberOfDays % 30 == 0 || numberOfDays % 30 == 1)){
            return "4"; // Monthly
        }

        return "1"; //Daily
    }

    public int getNights(Date arrivalDate, Date departureDate){
        long diff =  (departureDate.getTime() - arrivalDate.getTime());
        return (int)TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
    }

    // ==> END of OPERA Report Functions

    // ==> OPERA Report Functions

    public SimphonyDiscount checkSimphonyDiscountExistence(ArrayList<SimphonyDiscount> discounts, int discountId){
        for (SimphonyDiscount discount : discounts) {
            if (discount.getDiscountId() == discountId) {
                return discount;
            }
        }
        return new SimphonyDiscount();
    }

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
        DecimalFormat df = new DecimalFormat("###.##");
        String temp = df.format(value);
        temp = temp.toLowerCase().replaceAll(",", "");
        return Float.parseFloat(temp);
    }

    public float roundUpFloat2Digest(float value){
        DecimalFormat df = new DecimalFormat("###.##");
        String temp = df.format(value);
        temp = temp.toLowerCase().replaceAll(",", "");
        return Float.parseFloat(temp);
    }

    public float roundUpFloatTwoDigitsRounded(float value){
        DecimalFormat df = new DecimalFormat("###.##");
//        df.setRoundingMode(RoundingMode.UP);
        String temp = df.format(value);
        temp = temp.toLowerCase().replaceAll(",", "");
        return Float.parseFloat(temp);
    }

    public Double roundUpDoubleTowDigits(Double value){
        DecimalFormat df = new DecimalFormat("###.##");
        String temp = df.format(value);
        temp = temp.toLowerCase().replaceAll(",", "");
        return Double.parseDouble(temp);
    }

    public float roundUpFloat1Digest(float value){
        DecimalFormat df = new DecimalFormat("###.#");
        String temp = df.format(value);
        temp = temp.toLowerCase().replaceAll(",", "");
        return Float.parseFloat(temp);
    }

    public double roundUpDouble(double value){
        return Math.round(value * 100.0)/100.0;
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

    public String toCamelCase(String s){
        String[] parts = s.split(" ");
        String camelCaseString = "";
        for (int i = 0; i < parts.length; i++){
            String part = "";
            if(i==0) {
                part = parts[i];
                camelCaseString = camelCaseString + toProperCase(part);
            }else{
                part = parts[i];
                camelCaseString = camelCaseString + toUpperCase(part);
            }
        }
        return camelCaseString;
    }

    static String toProperCase(String s) {
        return s.substring(0, 1).toLowerCase() +
                s.substring(1).toLowerCase();
    }

    static String toUpperCase(String s) {
        return s.substring(0, 1).toUpperCase() +
                s.substring(1).toLowerCase();
    }

    public boolean checkIfMajorGroup(String majorGroup) {

        String[] majors = {"food", "beverage", "condiment"};

        if(Arrays.stream(majors).anyMatch(majorGroup::equals)){
            return true;
        }

        return false;

    }

    public boolean checkIfUserHasRole(List<Role> roles, String role) {

        for(Role tempRole : roles){
            if(tempRole.getReference().equals(role)){
                return true;
            }
        }
        return false;
    }

    public boolean checkIfAccountHasFeature(List<Feature> features, String feature) {

        for (Feature tempFeature : features){
            if(tempFeature.getId().equals(feature)){
                return true;
            }
        }

        return false;
    }

    public boolean containRevenueCenter(Balance balance, RevenueCenter revenueCenter) {
        List<RevenueCenter> revenueCenters = balance.getRevenueCenters();
        for (RevenueCenter tempRevenueCenter: revenueCenters){
            if(tempRevenueCenter.getRevenueCenter().equals(revenueCenter.getRevenueCenter())) {
                return true;
            }
        }
        return false;
    }

    public boolean hasBalance(List<Balance> balance) {
        double totalAmount = 0;
        for (Balance tempBalance: balance){
            totalAmount += tempBalance.getAmount();
        }
        if(totalAmount > 0){
            return true;
        }
        return false;
    }

}
