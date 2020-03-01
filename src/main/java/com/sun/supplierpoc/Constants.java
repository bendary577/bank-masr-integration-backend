package com.sun.supplierpoc;

public class Constants {
    ///////////////////////////////////////// Sync Job Data Status//////////////////////////////////////////////////////
    public static String SUCCESS = "Success";
    public static String FAILED = "Failed";
    public static String RECEIVED = "Received";

    ///////////////////////////////////////// Sync Job Status//////////////////////////////////////////////////////
    public static String RUNNING = "Running";

    ///////////////////////////////////////// Sync Job Types///////////////////////////////////////////////////////////
    public static String SUPPLIERS = "Suppliers";
    public static String APPROVED_INVOICES = "Approved Invoices";
    public static String CREDIT_NOTES = "Credit Notes";
    public static String TRANSFERS = "Booked Transfers";
    public static String JOURNALS = "Journals";
    public static String SALES = "Running";


    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static int PORT = 8080;
    public static String HOST = "192.168.1.3";

    public Constants() {}
}
