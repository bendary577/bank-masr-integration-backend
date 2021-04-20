package com.sun.supplierpoc;

import com.sun.supplierpoc.controllers.*;
import com.sun.supplierpoc.controllers.simphony.MenuItemsController;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.GeneralSettings;
import com.sun.supplierpoc.models.SyncJobType;
import com.sun.supplierpoc.models.configurations.SimphonyLocation;
import com.sun.supplierpoc.repositories.GeneralSettingsRepo;
import com.sun.supplierpoc.repositories.SyncJobTypeRepo;
import com.systemsunion.ssc.client.ComponentException;
import com.systemsunion.ssc.client.SoapFaultException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class ScheduledTasks {
    @Autowired
    private GeneralSettingsRepo generalSettingsRepo;
    @Autowired
    private SyncJobTypeRepo syncJobTypeRepo;
    @Autowired
    private AccountController accountController;
    @Autowired
    private SupplierController supplierController;
    @Autowired
    private InvoiceController invoiceController;
    @Autowired
    private CreditNoteController creditNoteController;
    @Autowired
    private TransferController transferController;
    @Autowired
    private JournalController journalController;
    @Autowired
    private CostOfGoodsController costOfGoodsController;
    @Autowired
    private WastageController wastageController;
    @Autowired
    private SalesController salesController;
    @Autowired
    private BookedProductionController bookedProductionController;
    @Autowired
    private MenuItemsController menuItemsController;

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

//    @Scheduled(fixedRate = 2000)  // 2 sec
    public void scheduleTaskWithFixedRate() {
        logger.info("Fixed Rate Task :: Execution Time - {}", dateTimeFormatter.format(LocalDateTime.now()) );
        logger.info("Current Thread : {}", Thread.currentThread().getName());
    }

    // run every 60 min
//    @Scheduled(cron = "[Seconds] [Minutes] [Hours] [Day of month] [Month] [Day of week] [Year]")
    @Scheduled(cron="0 0/60 * * * SUN-SAT")
    public void scheduleTaskWithCronExpression() throws SoapFaultException, ComponentException, ParseException, IOException {
        logger.info("Cron Task :: Execution Time - {}", dateTimeFormatter.format(LocalDateTime.now()));
        logger.info("Current Thread : {}", Thread.currentThread().getName());

        ArrayList<Account> accounts = accountController.getAccounts();

        HashMap<String, Object> response;

        //////////////////////////////////////  Get Current date //////////////////////////////////////
        String[] weekdays = new DateFormatSymbols(Locale.ENGLISH).getWeekdays();

        Calendar myCalendar = Calendar.getInstance();
        Date date = new Date();
        myCalendar.setTime(date);

        int hour = myCalendar.get(Calendar.HOUR_OF_DAY);
        int dayOfWeek = myCalendar.get(Calendar.DAY_OF_WEEK);
        int dayOfMonth = myCalendar.get(Calendar.DAY_OF_MONTH);
        String dayOfWeekName = weekdays[dayOfWeek];
        ///////////////////////////////////////////////////////////////////////////////////////////////

        for (Account account : accounts) {
            ArrayList<SyncJobType> syncJobsQueue = new ArrayList<>();

            ArrayList<SyncJobType> syncJobTypes = (ArrayList<SyncJobType>) syncJobTypeRepo.findByAccountIdAndDeleted(account.getId(), false);

            if (syncJobTypes.size() == 0) continue;

            for (SyncJobType syncJobType : syncJobTypes) {
                if (syncJobType.getConfiguration().schedulerConfiguration.hour.equals("")) continue;

                String schedulerHour = syncJobType.getConfiguration().schedulerConfiguration.hour;
                String[] arrOfStr = schedulerHour.split(":");

                // check hours
                if (syncJobType.getConfiguration().schedulerConfiguration.duration.equals(Constants.DAILY)){
                    if (Integer.parseInt(arrOfStr[0]) == hour){
                        syncJobsQueue.add(syncJobType);
                        System.out.println(Constants.DAILY);
                        System.out.println(syncJobType.getName());
                    }
                }
                // check hours and day_name
                if (syncJobType.getConfiguration().schedulerConfiguration.duration.equals(Constants.WEEKLY)){
                    if (syncJobType.getConfiguration().schedulerConfiguration.dayName != null){
                        if (Integer.parseInt(arrOfStr[0]) == hour){
                            String schedulerDay = (syncJobType.getConfiguration().schedulerConfiguration.dayName).toLowerCase();
                            if (dayOfWeekName.equals(schedulerDay)){
                                syncJobsQueue.add(syncJobType);
                                System.out.println(Constants.WEEKLY);
                                System.out.println(syncJobType.getName());
                            }
                        }
                    }
                }
                // check hours and day
                if (syncJobType.getConfiguration().schedulerConfiguration.duration.equals(Constants.MONTHLY)
                        || syncJobType.getConfiguration().schedulerConfiguration.duration.equals(Constants.DAILY_PER_MONTH)){
                    if (syncJobType.getConfiguration().schedulerConfiguration.day != null){
                        if (Integer.parseInt(arrOfStr[0]) == hour){
                            int schedulerDay = Integer.parseInt(syncJobType.getConfiguration().schedulerConfiguration.day);
                            if (dayOfMonth == schedulerDay){
                                syncJobsQueue.add(syncJobType);
                                System.out.println(Constants.MONTHLY);
                                System.out.println(syncJobType.getName());
                            }
                        }
                    }
                }
            }

            for (SyncJobType syncJobType : syncJobsQueue){
                if (syncJobType.getName().equals(Constants.SUPPLIERS)){
                    supplierController.getSuppliers("Automated User", account);
                }
                else if (syncJobType.getName().equals(Constants.APPROVED_INVOICES)){
                    invoiceController.getApprovedInvoices("Automated User", account);
                }
                else if (syncJobType.getName().equals(Constants.CREDIT_NOTES)){
                    creditNoteController.getCreditNotes("Automated User", account);
                }
                else if (syncJobType.getName().equals(Constants.TRANSFERS)){
                    transferController.getBookedTransfer("Automated User", account);
                }
                else if (syncJobType.getName().equals(Constants.CONSUMPTION)){
                    journalController.getJournals("Automated User", account);
                }
                else if (syncJobType.getName().equals(Constants.COST_OF_GOODS)){
                    costOfGoodsController.syncCostOfGoodsInDayRange("Automated User", account);
                }
                else if (syncJobType.getName().equals(Constants.SALES)){
                    salesController.syncPOSSalesInDayRange("Automated User", account);
                }
                else if (syncJobType.getName().equals(Constants.WASTAGE)){
                    wastageController.getWastage("Automated User", account);
                }
                else if (syncJobType.getName().equals(Constants.BOOKED_PRODUCTION)){
                    bookedProductionController.getBookedProduction("Automated User", account);
                }
                else if (syncJobType.getName().equals(Constants.MENU_ITEMS)){
                    // sync per revenue center
                    GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);

                    ArrayList<SimphonyLocation> locations = generalSettings.getSimphonyLocations();
                    for (SimphonyLocation location : locations){
                        if(location.isChecked()){
                            menuItemsController.SyncSimphonyMenuItems("Automated User", account, location.getRevenueCenterID());
                        }
                    }
                }
            }
        }

    }
}