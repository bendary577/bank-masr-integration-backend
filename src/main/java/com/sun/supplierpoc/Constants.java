package com.sun.supplierpoc;

public class Constants {
    ///////////////////////////////////////// Sync Job Data Status//////////////////////////////////////////////////////
    public static String SUCCESS = "Success";
    public static String FAILED = "Failed";
    public static String RECEIVED = "Received";

    ///////////////////////////////////////// Sync Job Status///////////////////////////////////////////////////////////
    public static String RUNNING = "Running";

    ///////////////////////////////////////// Sync Job Types////////////////////////////////////////////////////////////
    public static String SUPPLIERS = "Suppliers";
    public static String APPROVED_INVOICES = "Approved Invoices";
    public static String CREDIT_NOTES = "Credit Notes";
    public static String TRANSFERS = "Booked Transfers";
    public static String JOURNALS = "Consumptions";
    public static String SALES = "POS Sales";
    public static String WASTAGE = "Wastage";

    ////////////////////////////////////////// Sync Job Scheduler //////////////////////////////////////////////////////
    public static String DAILY = "Daily";
    public static String WEEKLY = "Weekly";
    public static String MONTHLY = "Monthly";

    ////////////////////////////////////////// Accounts //////////////////////////////////////////////////////
    public static String SUN = "Sun";
    public static String FUSION = "Fusion";
    public static String ORACLE_OHIM = "HospitalityOHIM";
    public static String ORACLE_OHRA = "HospitalityOHRA";

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static int PORT = 8080;
//    public static String HOST = "192.168.1.15";
    public static String HOST = "41.33.13.24:81";

    public Constants() {}
}
