package com.sun.supplierpoc;

public class Constants {
    ///////////////////////////////////////// Sync Job Data Status//////////////////////////////////////////////////////
    public static String SUCCESS = "Success";
    public static String FAILED = "Failed";
    public static String RECEIVED = "Received";
    public static String RETRY_TO_SEND = "Retry to Send";

    public static String INVALID_LOCATION = "Invalid location";
    public static String INVALID_USER = "Invalid user.";

    public static String EMPTY_LOCATION = "Locations parameter is empty. Please make a selection.";
    public static String INVALID_REVENUE_CENTER = "Invalid revenue center";
    public static String EMPTY_REVENUE_CENTER = "Revenue Centers parameter is empty. Please make a selection.";
    public static  String WRONG_REVENUE_CENTER = "Wrong revenue center, please configure this RVC";
    public static String INVALID_BUSINESS_DATE = "Invalid business Date";
    public static String EMPTY_BUSINESS_DATE = "Business Dates parameter is empty. Please make a selection.";
    public static String WRONG_BUSINESS_DATE = "Wrong Business Dates Chosen.";
    public static String NO_INFO = "No information is available for the selected range";

    ///////////////////////////////////////// Sync Job Status///////////////////////////////////////////////////////////
    public static String RUNNING = "Running";

    ///////////////////////////////////////// Sync Job Types////////////////////////////////////////////////////////////
    public static String wLsIntegration = "2wLsIntegration";
    public static String NEW_BOOKING_REPORT = "New Booking Report";
    public static String CANCEL_BOOKING_REPORT = "Cancel Booking Report";
    public static String OCCUPANCY_UPDATE_REPORT = "Occupancy Update Report";
    public static String EXPENSES_DETAILS_REPORT = "Expenses Details Report";

    public static String SUPPLIERS = "Suppliers";
    public static String APPROVED_INVOICES = "Approved Invoices";
    public static String CREDIT_NOTES = "Credit Notes";
    public static String TRANSFERS = "Booked Transfers";
    public static String CONSUMPTION = "Consumption";
    public static String COST_OF_GOODS = "Cost of Goods";
    public static String SALES = "POS Sales";
    public static String WASTAGE = "Wastage";
    public static String BOOKED_PRODUCTION = "Booked Production";
    public static String MENU_ITEMS = "Menu Items";

    ///////////////////////////////////////// Operation Types///////////////////////////////////////////////////////////
    public static String CREATE_CHECK = "Create Check";
    public static String OPERA_PAYMENT = "Opera Payment";

    ////////////////////////////////////////// Sync Job Scheduler //////////////////////////////////////////////////////
    public static String DAILY = "Daily";
    public static String WEEKLY = "Weekly";
    public static String MONTHLY = "Monthly";
    public static String DAILY_PER_MONTH = "DailyPerMonth";

    ////////////////////////////////////////// Transaction Type //////////////////////////////////////////////////////
    public static String REDEEM_VOUCHER = "Redeem Voucher";

    ////////////////////////////////////////// Loyalty Source Type //////////////////////////////////////////////////////
    public static String PROJECT_NAME = "oracle-symphony-integrator";
    public static String BUCKET_NAME = "oracle-integrator-bucket";

    public static String USER_IMAGE_URL = "https://storage.googleapis.com/oracle-integrator-bucket/AccourImage/defaultImage.jpg";
    public static String GROUP_IMAGE_URL = "https://storage.googleapis.com/oracle-integrator-bucket/AccourImage/defaultGroup.png";
    public static final String ACCOUNT_IMAGE_URL = "https://storage.googleapis.com/oracle-integrator-bucket/indeex.jpg-1192782010?GoogleAccessId=accour@oracle-symphony-integrator.iam.gserviceaccount.com&Expires=1617883508&Signature=IfLBwoO4X65GOO3yE6deP1W8Ab9rdesZbr9QHy2XJP%2BOdRW0yiGnfFPhiMYX4ukFvqUS5MMroWlgjjzIvGGfOZIaMHLTWttlSOHDL6N8Zwpj3meW3iSkgCBai94PTo1r%2BOaNeP7kFdXJiey5qC8vF2nbXu8KcDJQ%2BTcr64F%2FVzyd3YGYGWpWJPZc0fhZZNs14jgIzIXkaLAnS%2BuQ3LUKUbCE77R%2FaKGNvgggFwbBSJfsqEg4YiQJXph0RJjEfN6Zx7LxgIJqeMwH87y4US%2F2vE08FOGKglN7Jjn7iVfWp4augpUusOuAot3TiocaazGerPefxQ16EKMzC%2FAMMczzww%3D%3D";

    ////////////////////////////////////////// Accounts /////////// /////////////////////////////////////////////////////
    public static String SUN = "Sun";
    public static String FUSION = "Fusion";
    public static String ORACLE_OHIM = "HospitalityOHIM";
    public static String ORACLE_OHRA = "HospitalityOHRA";

    //////////////////////////////////////////////// ERD ///////////////////////////////////////////////////////////////
    public static String EXPORT_TO_SUN_ERD = "ExportSun";
    public static String SUN_ERD = "Sun";
    public static String FUSION_ERD = "Fusion";
    public static String SIMPHONY_ERD = "Simphony";

    ///////////////////////////////////////// Micros Version#1 Links ///////////////////////////////////////////////////

    public static String OHRA_LINK  = "https://mte03-ohra-prod.hospitality.oracleindustry.com";
    public static String OHIM_LINK = "https://mte3-ohim.oracleindustry.com";
    public static String OHIM_LOGIN_LINK = OHIM_LINK + "/InventoryManagement/FormLogin.aspx";

    public static String SUPPLIER_GROUPS_URL = "https://mte03-ohim-prod.hospitality.oracleindustry.com/Webclient/MasterData/VendorGroups/VendorGroupsOverview.aspx";

    public static String APPROVED_INVOICES_LINK =  OHIM_LINK + "/InventoryManagement/Purchase/Invoicing/IvcOverviewView.aspx?type=1";
    public static String ACCOUNT_PAYABLE_LINK =  OHIM_LINK + "/Webclient/Purchase/Invoicing/IvcOverviewView.aspx?type=2";

    /*
    * Old OHIM receipts link
    * public static String RECEIPTS_LINK = OHIM_LINK + "/Webclient/Purchase/Receiving/RcvOverviewView.aspx?type=2";
    * */
    public static String RECEIPTS_LINK = OHIM_LINK + "/InventoryManagement/Purchase/Receiving/RcvOverviewView.aspx?type=1";

    /*
     * Old OHIM booked production link
     * public static String BOOKED_PRODUCTION_LINK =  OHIM_LINK + "/Webclient/Production/ProductionD/PrView.aspx?type=20";
     * */
    public static String BOOKED_PRODUCTION_LINK =  OHIM_LINK + "/InventoryManagement/Production/ProductionD/PrView.aspx?type=20";
    public static String SUPPLIER_LINK =  OHIM_LINK +  "/Webclient/Purchase/Invoicing/IvcOverviewView.aspx?type=2";
    public static String MAIN_MENU_URL = OHRA_LINK + "servlet/PortalIfc/?portlet=InventoryPortlet&action=sidemenu&portletId=3882&options=iNVENTORY";
    public static String COST_CENTERS_LINK = OHIM_LINK + "/InventoryManagement/MasterData/CostCenters/OverviewCC.aspx";
    public static String OVER_GROUPS_LINK = OHIM_LINK + "/InventoryManagement/MasterData/OverGroups/OverviewOverGroup.aspx";
    public static String MAJOR_GROUPS_LINK = OHIM_LINK + "/InventoryManagement/MasterData/MajorGroups/OverviewMajorGroup.aspx";
    public static String ITEMS_GROUPS_LINK = OHIM_LINK + "/InventoryManagement/MasterData/ItemGroups/OverviewItemGroup.aspx";
    public static String ITEMS_LINK = OHIM_LINK + "/InventoryManagement/MasterData/Items/OverviewItem.aspx";
    public static String SUPPLIER_URL = OHIM_LINK + "/InventoryManagement/MasterData/Vendors/OverviewVendor.aspx";


    // SALES LINKS
    // Daily Detail >> Tenders
    public static String TENDERS_REPORT_LINK = OHRA_LINK + "/finengine/reportAction.do?method=run&reportID=19";
    public static String TENDERS_TABLE_LINK = OHRA_LINK + "/finengine/reportRunAction.do?rptroot=19&reportID=TendersDailyDetail&method=run";
    public static String TENDERS_PARAMETERS_XPATH = "/html/body/div[2]/div[1]";
    public static String TENDERS_PARAMETERS_TABLE_XPATH = "/html/body/div[2]/div[2]/table";

    // Daily Detail >> Tax
    public static String TAXES_REPORT_LINK = OHRA_LINK + "/finengine/reportAction.do?method=run&reportID=18";
    public static String TAX_INCLUDED_REPORT_LINK = OHRA_LINK + "/finengine/reportRunAction.do?method=run&reportID=EAME_TaxesDailyDetail_VAT&rptroot=1191";
    public static String ADD_ON_TAX_INCLUDED_REPORT_LINK = OHRA_LINK + "/finengine/reportRunAction.do?rptroot=18&reportID=TaxesDailyDetail&method=run";

    // Menu Engineering >> Sales Mix Summary
    public static String ITEM_GROSS_REPORT_LINK = OHRA_LINK + "/finengine/reportAction.do?method=run&reportID=46";

    // Daily Detail >> Sales Mix
    public static String OVER_GROUP_GROSS_REPORT_LINK = OHRA_LINK + "/finengine/reportAction.do?method=run&reportID=15";

    public static String SYSTEM_SALES_REPORT_LINK = OHRA_LINK + "/finengine/reportAction.do?method=run&reportID=1191";

    // Daily Detail >> Discount
    public static String DISCOUNT_REPORT_LINK = OHRA_LINK + "/finengine/reportAction.do?method=run&reportID=13";
    public static String DISCOUNT_TABLE_LINK = OHRA_LINK + "/finengine/reportRunAction.do?rptroot=13&reportID=DiscDailyDetail&method=run";

    // Daily Detail >> Service Charge
    public static String SERVICE_CHARGE_REPORT_LINK = OHRA_LINK + "/finengine/reportAction.do?method=run&reportID=16";
    public static String SERVICE_CHARGE_TABLE_LINK = OHRA_LINK + "/finengine/reportRunAction.do?rptroot=16&reportID=SrvcChrgDailyDetail&method=run";

    // myInventory Reports >> COS by Cost Center
    public static String CONSUMPTION_REPORT_LINK = OHRA_LINK + "/finengine/reportAction.do?method=run&reportID=499";
    public static String CONSUMPTION_TABLE_LINK = OHRA_LINK + "/finengine/reportRunAction.do?rptroot=499&method=run&reportID=myInvenCOSByCC";

    // Dealy details >> Cost Of Goods (-VAT)
    public static String CONSUMPTION_COSTOFGOODS_REPORT_LINK = OHRA_LINK + "/finengine/reportAction.do?method=run&reportID=12";
    public static String CONSUMPTION_COSTOFGOODS_TABLE_LINK = OHRA_LINK + "/finengine/reportRunAction.do?rptroot=12&method=run&reportID=EAME_COGSDailyDetail_VAT";

    // More Reports >> Summary >> System Sales Summary
    public static String SALES_SUMMARY_LINK = OHRA_LINK + "/finengine/reportRunAction.do?rptroot=1191&reportID=EAME_SystemSalesSummary_VAT&method=run";

    ///////////////////////////////////////// Invoice Types ////////////////////////////////////////////////////////////
    public static String APPROVED_INVOICE = "Approved Invoice";
    public static String ACCOUNT_PAYABLE = "Account Payable";

    public static String APPROVED_INVOICE_Status = "Approved Invoice";
    public static String ACCOUNT_PAYABLE_RTV_Status = "Invoice sent to A/P (RTV)";
    public static String ACCOUNT_PAYABLE_Status = "Invoice sent to A/P";

    ///////////////////////////////////////// Waste Reports ////////////////////////////////////////////////////////////
    public static String WASTE_REPORT = "Waste Reports";
    public static String INVENTORY_WASTE = "Inventory Booked Waste";
    public static String BOOKED_WASTE_REPORT_LINK = OHRA_LINK + "/finengine/reportAction.do?method=run&reportID=497";
    /*
     * Old OHIM waste groups
     * wasteTypesURL = "https://mte03-ohim-prod.hospitality.oracleindustry.com/Webclient/MasterData/WasteGroups/WasteGroup.aspx"
     * */
    public static String WASTE_GROUPS_LINK =  OHIM_LINK + "/InventoryManagement/MasterData/WasteGroups/WasteGroup.aspx";
    public static String WASTE_GROUPS_CONTENT_LINK = OHRA_LINK + "/finengine/reportRunAction.do?rptroot=497&reportID=myInvenItemWasteSummary&method=run";

    ///////////////////////////////////////// Sales over group gross types /////////////////////////////////////////////
    public static String SALES_GROSS = "Gross";
    public static String SALES_GROSS_LESS_DISCOUNT = "Gross Less Discount";


    ///////////////////////////////////////// Micros Version#3 Links ///////////////////////////////////////////////////

    public static String MICROS_V2_LINK  = "https://mte4-ohra-idm.oracleindustry.com/oidc-ui/";
    public static String MICROS_SALES_SUMMARY  = "https://mte4-ohra.oracleindustry.com/portal/?root=reports&reports=myReports&myReports=reportGroup&reportGroup=4";
    public static String MICROS_REPORTS  = "https://mte4-ohra.oracleindustry.com/portal/?root=reports&reports=myReports";

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

    //////////////////////////////////////////////// AMAZON PAYMENT /////////////////////////////////////////////////////

    public static final String SIGNATURE_PHRASE = "68D2fyokjF9UCt2x45V7SD(@";



    public Constants() {}
}
