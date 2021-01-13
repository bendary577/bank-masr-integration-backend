package com.sun.supplierpoc;

public class Constants {
    ///////////////////////////////////////// Sync Job Data Status//////////////////////////////////////////////////////
    public static String SUCCESS = "Success";
    public static String FAILED = "Failed";
    public static String RECEIVED = "Received";
    public static String RETRY_TO_SEND = "Retry to Send";

    public static String INVALID_LOCATION = "Invalid location";
    public static String EMPTY_LOCATION = "Locations parameter is empty. Please make a selection.";
    public static String INVALID_REVENUE_CENTER = "Invalid revenue center";
    public static String EMPTY_REVENUE_CENTER = "Revenue Centers parameter is empty. Please make a selection.";
    public static String INVALID_BUSINESS_DATE = "Invalid business Date";
    public static String EMPTY_BUSINESS_DATE = "Business Dates parameter is empty. Please make a selection.";

    ///////////////////////////////////////// Sync Job Status///////////////////////////////////////////////////////////
    public static String RUNNING = "Running";

    ///////////////////////////////////////// Sync Job Types////////////////////////////////////////////////////////////
    public static String SUPPLIERS = "Suppliers";
    public static String APPROVED_INVOICES = "Approved Invoices";
    public static String CREDIT_NOTES = "Credit Notes";
    public static String TRANSFERS = "Booked Transfers";
    public static String CONSUMPTION = "Consumption";
    public static String SALES = "POS Sales";
    public static String WASTAGE = "Wastage";
    public static String BOOKED_PRODUCTION = "Booked Production";
    public static String MENU_ITEMS = "Menu Items";

    ///////////////////////////////////////// Operation Types///////////////////////////////////////////////////////////
    public static String CREATE_CHECK = "Create Check";

    ////////////////////////////////////////// Sync Job Scheduler //////////////////////////////////////////////////////
    public static String DAILY = "Daily";
    public static String WEEKLY = "Weekly";
    public static String MONTHLY = "Monthly";

    ////////////////////////////////////////// Accounts ////////////////////////////////////////////////////////////////
    public static String SUN = "Sun";
    public static String FUSION = "Fusion";
    public static String ORACLE_OHIM = "HospitalityOHIM";
    public static String ORACLE_OHRA = "HospitalityOHRA";

    //////////////////////////////////////////////// ERD //////////////////////////////////////////////////////////////

    public static String EXPORT_TO_SUN_ERD = "ExportSun";
    public static String SUN_ERD = "Sun";
    public static String FUSION_ERD = "Fusion";
    public static String SIMPHONY_ERD = "Simphony";

    ///////////////////////////////////////// Sun Server ///////////////////////////////////////////////////////////////

//    public static int PORT = 8080;
//    public static String HOST = "41.33.13.24";

    ///////////////////////////////////////// Links ////////////////////////////////////////////////////////////////////

    public static String OHRA_LINK  = "https://mte03-ohra-prod.hospitality.oracleindustry.com";
    public static String OHIM_LINK = "https://mte03-ohim-prod.hospitality.oracleindustry.com";

    public static String OHIM_LOGIN_LINK = OHIM_LINK + "/Webclient/FormLogin.aspx";
    public static String OHRA_LOGIN_LINK =  "https://mte03-ohra-prod.hospitality.oracleindustry.com/";

    public static String APPROVED_INVOICES_LINK =  OHIM_LINK + "/Webclient/Purchase/Invoicing/IvcOverviewView.aspx?type=1";
    public static String ACCOUNT_PAYABLE_LINK =  OHIM_LINK + "/Webclient/Purchase/Invoicing/IvcOverviewView.aspx?type=2";

    public static String RECEIPTS_LINK = OHIM_LINK + "/Webclient/Purchase/Receiving/RcvOverviewView.aspx?type=2";

    public static String BOOKED_PRODUCTION_LINK =  OHIM_LINK + "/Webclient/Production/ProductionD/PrView.aspx?type=20";

    public static String SUPPLIER_LINK =  OHIM_LINK +  "/Webclient/Purchase/Invoicing/IvcOverviewView.aspx?type=2";

    public static String COST_CENTERS_LINK = OHIM_LINK + "/Webclient/MasterData/CostCenters/OverviewCC.aspx";

    // SALES LINKS
    // Daily Detail >> Tenders
    public static String TENDERS_REPORT_LINK = OHRA_LOGIN_LINK + "finengine/reportAction.do?method=run&reportID=19";
    public static String TENDERS_TABLE_LINK = OHRA_LOGIN_LINK + "/finengine/reportRunAction.do?rptroot=19&reportID=TendersDailyDetail&method=run";

    // Daily Detail >> Tax
    public static String TAXES_REPORT_LINK = OHRA_LOGIN_LINK + "finengine/reportAction.do?method=run&reportID=18";
    public static String TAX_INCLUDED_REPORT_LINK = OHRA_LOGIN_LINK + "finengine/reportRunAction.do?method=run&reportID=EAME_TaxesDailyDetail_VAT&rptroot=1191";

    // Menu Engineering >> Sales Mix Summary
    public static String ITEM_GROSS_REPORT_LINK = OHRA_LOGIN_LINK + "finengine/reportAction.do?method=run&reportID=46";

    // Daily Detail >> Sales Mix
    public static String OVER_GROUP_GROSS_REPORT_LINK = OHRA_LOGIN_LINK + "finengine/reportAction.do?method=run&reportID=15";
    public static String SYSTEM_SALES_REPORT_LINK = OHRA_LOGIN_LINK + "/finengine/reportAction.do?method=run&reportID=1191";

    // Daily Detail >> Discount
    public static String DISCOUNT_REPORT_LINK = OHRA_LOGIN_LINK + "/finengine/reportAction.do?method=run&reportID=13";
    public static String DISCOUNT_TABLE_LINK = OHRA_LOGIN_LINK + "/finengine/reportRunAction.do?rptroot=13&reportID=DiscDailyDetail&method=run";

    // Daily Detail >> Service Charge
    public static String SERVICE_CHARGE_REPORT_LINK = OHRA_LOGIN_LINK + "/finengine/reportAction.do?method=run&reportID=16";
    public static String SERVICE_CHARGE_TABLE_LINK = OHRA_LOGIN_LINK + "/finengine/reportRunAction.do?rptroot=16&reportID=SrvcChrgDailyDetail&method=run";

    // myInventory Reports >> COS by Cost Center
    public static String CONSUMPTION_REPORT_LINK = OHRA_LOGIN_LINK + "finengine/reportAction.do?method=run&reportID=499";
    public static String CONSUMPTION_TABLE_LINK = OHRA_LOGIN_LINK + "finengine/reportRunAction.do?rptroot=499&method=run&reportID=myInvenCOSByCC";

    ///////////////////////////////////////// Invoice Types ////////////////////////////////////////////////////////////

    public static String APPROVED_INVOICE = "Approved Invoices";
    public static String ACCOUNT_PAYABLE = "Account Payable";

    public static String APPROVED_INVOICE_Status = "Approved Invoice";
    public static String ACCOUNT_PAYABLE_RTV_Status = "Invoice sent to A/P (RTV)";
    public static String ACCOUNT_PAYABLE_Status = "Invoice sent to A/P";

    ///////////////////////////////////////// Sales over group gross types /////////////////////////////////////////////

    public static String SALES_GROSS = "Gross";
    public static String SALES_GROSS_LESS_DISCOUNT = "Gross Less Discount";

    ///////////////////////////////////////// Business Date ////////////////////////////////////////////////////////////

    public static String USER_DEFINED = "UserDefined";
    public static String MOST_RECENT = "Most Recent";
    public static String PAST_7_DAYES = "Past 7 Days";
    public static String TODAY = "Today";
    public static String YESTERDAY = "Yesterday";
    public static String CURRENT_MONTH = "Current Month";
    public static String LAST_MONTH = "Last Month";
    public static String MONTH_TO_DATE = "Month to Date";
    public static String LAST_QUARTER = "Last Quarter";
    public static String YEAR_TO_DATE = "Year to Date";
    public static String LAST_YEAR_YTD = "Last Year YTD";
    public static String FINANCIAL_PERIOD_TO_DATE = "Financial Period to Date";
    public static String FINANCIAL_WEEK_TO_DATE = "Financial Week to Date";


    public Constants() {}
}
