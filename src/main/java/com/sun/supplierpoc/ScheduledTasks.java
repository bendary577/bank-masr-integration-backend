package com.sun.supplierpoc;

import com.sun.supplierpoc.controllers.*;
import com.sun.supplierpoc.controllers.simphony.MenuItemsController;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.Feature;
import com.sun.supplierpoc.models.GeneralSettings;
import com.sun.supplierpoc.models.SyncJobType;
import com.sun.supplierpoc.models.applications.ApplicationUser;
import com.sun.supplierpoc.models.applications.BirthdayGift;
import com.sun.supplierpoc.models.applications.Group;
import com.sun.supplierpoc.models.configurations.SimphonyLocation;
import com.sun.supplierpoc.models.roles.Features;
import com.sun.supplierpoc.repositories.GeneralSettingsRepo;
import com.sun.supplierpoc.repositories.SyncJobTypeRepo;
import com.sun.supplierpoc.repositories.applications.GroupRepo;
import com.sun.supplierpoc.services.AccountService;
import com.sun.supplierpoc.services.AppGroupService;
import com.sun.supplierpoc.services.FeatureService;
import com.sun.supplierpoc.services.application.AppUserService;
import com.sun.supplierpoc.services.onlineOrdering.AggregatorIntegratorService;
import com.sun.supplierpoc.services.opera.BookingService;
import com.sun.supplierpoc.services.opera.CancelBookingService;
import com.sun.supplierpoc.services.opera.ExpensesService;
import com.sun.supplierpoc.services.opera.OccupancyService;
import com.systemsunion.ssc.client.ComponentException;
import com.systemsunion.ssc.client.SoapFaultException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
@RestController
public class ScheduledTasks {
    @Autowired
    private GeneralSettingsRepo generalSettingsRepo;
    @Autowired
    private SyncJobTypeRepo syncJobTypeRepo;
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

    @Autowired
    private AccountService accountService;
    @Autowired
    private AppUserService appUserService;
    @Autowired
    private BookingService bookingService;
    @Autowired
    private CancelBookingService cancelBookingService;
    @Autowired
    private OccupancyService occupancyService;
    @Autowired
    private ExpensesService expensesService;
    @Autowired
    private FeatureService featureService;
    @Autowired
    private SalesAPIController salesAPIController;

    @Autowired
    private AggregatorIntegratorService aggregatorIntegratorService;

    @Autowired
    AppGroupService appGroupService;

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    /*
    * Add birthday points daily
    * */
//    @Scheduled(cron = "0 0 1 * * SUN-SAT")
//    @GetMapping("opera/addGuestPoints")
    public void scheduleTaskWithFixedRate() {
        logger.info("Fixed Rate Task :: Execution Time - {}", dateTimeFormatter.format(LocalDateTime.now()));
        logger.info("Current Thread : {}", Thread.currentThread().getName());

        Feature feature = featureService.getFeatureByRef(Features.REWARD_POINTS);
        if(feature == null)
            return;

        Date date;
        int month;
        int dayOfMonth;

        int addedPoints;
        Date userBirthDate;
        int userBirthMonth;
        int userBirthDayOfMonth;

        GeneralSettings generalSettings;
        BirthdayGift birthdayGift;
        ArrayList<ApplicationUser> users;
        ArrayList<Account> accounts = accountService.getActiveAccountsHasFeature(feature);

        Calendar myCalendar = Calendar.getInstance();

        for (Account account : accounts) {
            generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);
            birthdayGift = generalSettings.getBirthdayGift();

            if(!birthdayGift.enabled)
                continue;

            /* Get current date - account birth offset */
            date = new Date();
            myCalendar.setTime(date);
            myCalendar.add(Calendar.DAY_OF_MONTH, birthdayGift.offsetBefore);

            month = myCalendar.get(Calendar.MONTH);
            dayOfMonth = myCalendar.get(Calendar.DAY_OF_MONTH);

            /* Get all Guests */
            users = appUserService.getActiveUsers(account.getId());
            for (ApplicationUser user : users) {
                /* Check user birth date*/
                userBirthDate = user.getBirthDate();
                if(userBirthDate == null)
                    continue;

                myCalendar.setTime(userBirthDate);
                userBirthMonth = myCalendar.get(Calendar.MONTH);
                userBirthDayOfMonth = myCalendar.get(Calendar.DAY_OF_MONTH);
                if(userBirthMonth == month && userBirthDayOfMonth == dayOfMonth){
                    addedPoints = user.getPoints() + birthdayGift.addedPoints;
                    user.setPoints(addedPoints);
                    appUserService.saveUsers(user);
                }
            }
        }

        feature = featureService.getFeatureByRef(Features.CANTEEN);
        if(feature == null)
            return;

        accounts = accountService.getActiveAccountsHasFeature(feature);

        //////////////////////////////////////  Get Current date //////////////////////////////////////
        String[] weekdays = new DateFormatSymbols(Locale.ENGLISH).getWeekdays();

        myCalendar = Calendar.getInstance();
        date = new Date();
        myCalendar.setTime(date);

        int hour = myCalendar.get(Calendar.HOUR_OF_DAY);
        int dayOfWeek = myCalendar.get(Calendar.DAY_OF_WEEK);
        dayOfMonth = myCalendar.get(Calendar.DAY_OF_MONTH);
        String dayOfWeekName = weekdays[dayOfWeek];
        ///////////////////////////////////////////////////////////////////////////////////////////////

        boolean accountHasCanteenFeature = false;

        for (Account account : accounts) {

            //check if account has canteen feature
            for (Feature feature1 : account.getFeatures()) {
                if (feature1.getReference().equals(Features.CANTEEN)) {
                    accountHasCanteenFeature = true;
                    break;
                }
            }

            if (accountHasCanteenFeature) {
                List<Group> groups = appGroupService.getTopGroups(account);
                ArrayList<Group> groupsQueue = new ArrayList<>();
                if (groups.size() == 0) continue;

                for (Group group : groups) {
                    if (group.getCanteenConfiguration().getHour().equals("")) continue;

                    String schedulerHour = group.getCanteenConfiguration().getHour();
                    String[] arrOfStr = schedulerHour.split(":");

                    // check hours
                    if (group.getCanteenConfiguration().getDuration().equals(Constants.DAILY)) {
                        if (Integer.parseInt(arrOfStr[0]) == hour) {
                            groupsQueue.add(group);
                            System.out.println(Constants.DAILY);
                            System.out.println(group.getName());
                        }
                    }

                    // check hours and day_name
                    if (group.getCanteenConfiguration().getDuration().equals(Constants.WEEKLY)) {
                        if (group.getCanteenConfiguration().getDayName() != null) {
                            if (Integer.parseInt(arrOfStr[0]) == hour) {
                                String schedulerDay = (group.getCanteenConfiguration().getDayName()).toLowerCase();
                                if (dayOfWeekName.equals(schedulerDay)) {
                                    groupsQueue.add(group);
                                    System.out.println(Constants.WEEKLY);
                                    System.out.println(group.getName());
                                }
                            }
                        }
                    }

                    // check hours and day
                    if (group.getCanteenConfiguration().getDuration().equals(Constants.MONTHLY)) {
                        if (group.getCanteenConfiguration().getDay() != null) {
                            if (Integer.parseInt(arrOfStr[0]) == hour) {
                                int schedulerDay = Integer.parseInt(group.getCanteenConfiguration().getDay());
                                if (dayOfMonth == schedulerDay) {
                                    groupsQueue.add(group);
                                    System.out.println(Constants.MONTHLY);
                                    System.out.println(group.getName());
                                }
                            }
                        }
                    }
                }
                for (Group group : groupsQueue) {
                    appGroupService.resetGroupWallet(account, group.getId());
                }
            } else {
                continue;
            }
        }
    }

    // run every 60 min
    // @Scheduled(cron = "[Seconds] [Minutes] [Hours] [Day of month] [Month] [Day of week] [Year]")
//    @Scheduled(cron = "0 0/60 * * * SUN-SAT")
//    @GetMapping("opera/checkUsers")
    public void scheduleTaskWithCronExpression() throws SoapFaultException, ComponentException, ParseException, IOException {
        logger.info("Cron Task :: Execution Time - {}", dateTimeFormatter.format(LocalDateTime.now()));
        logger.info("Current Thread : {}", Thread.currentThread().getName());

        ArrayList<Account> accounts = accountService.getActiveAccounts();

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
                if (syncJobType.getConfiguration().schedulerConfiguration.duration.equals(Constants.DAILY)) {
                    if (Integer.parseInt(arrOfStr[0]) == hour) {
                        syncJobsQueue.add(syncJobType);
                        System.out.println(Constants.DAILY);
                        System.out.println(syncJobType.getName());
                    }
                }
                // check hours and day_name
                if (syncJobType.getConfiguration().schedulerConfiguration.duration.equals(Constants.WEEKLY)) {
                    if (syncJobType.getConfiguration().schedulerConfiguration.dayName != null) {
                        if (Integer.parseInt(arrOfStr[0]) == hour) {
                            String schedulerDay = (syncJobType.getConfiguration().schedulerConfiguration.dayName).toLowerCase();
                            if (dayOfWeekName.equals(schedulerDay)) {
                                syncJobsQueue.add(syncJobType);
                                System.out.println(Constants.WEEKLY);
                                System.out.println(syncJobType.getName());
                            }
                        }
                    }
                }
                // check hours and day
                if (syncJobType.getConfiguration().schedulerConfiguration.duration.equals(Constants.MONTHLY)
                        || syncJobType.getConfiguration().schedulerConfiguration.duration.equals(Constants.DAILY_PER_MONTH)) {
                    if (syncJobType.getConfiguration().schedulerConfiguration.day != null) {
                        if (Integer.parseInt(arrOfStr[0]) == hour) {
                            int schedulerDay = Integer.parseInt(syncJobType.getConfiguration().schedulerConfiguration.day);
                            if (dayOfMonth == schedulerDay) {
                                syncJobsQueue.add(syncJobType);
                                System.out.println(Constants.MONTHLY);
                                System.out.println(syncJobType.getName());
                            }
                        }
                    }
                }
            }

            for (SyncJobType syncJobType : syncJobsQueue) {
                if (syncJobType.getName().equals(Constants.SUPPLIERS)) {
                    supplierController.getSuppliers("Automated User", account);
                } else if (syncJobType.getName().equals(Constants.APPROVED_INVOICES)) {
                    invoiceController.getApprovedInvoices("Automated User", account);
                } else if (syncJobType.getName().equals(Constants.CREDIT_NOTES)) {
                    creditNoteController.getCreditNotes("Automated User", account);
                } else if (syncJobType.getName().equals(Constants.TRANSFERS)) {
                    transferController.getBookedTransfer("Automated User", account);
                } else if (syncJobType.getName().equals(Constants.CONSUMPTION)) {
                    journalController.getJournals("Automated User", account);
                } else if (syncJobType.getName().equals(Constants.COST_OF_GOODS)) {
                    costOfGoodsController.syncCostOfGoodsInDayRange("Automated User", account);
                } else if (syncJobType.getName().equals(Constants.SALES)) {
                    salesController.syncPOSSalesInDayRange("Automated User", account);
                } else if (syncJobType.getName().equals(Constants.WASTAGE)) {
                    wastageController.getWastage("Automated User", account);
                } else if (syncJobType.getName().equals(Constants.BOOKED_PRODUCTION)) {
                    bookedProductionController.getBookedProduction("Automated User", account);
                } else if (syncJobType.getName().equals(Constants.MENU_ITEMS)) {
                    // sync per revenue center
                    GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);

                    ArrayList<SimphonyLocation> locations = generalSettings.getSimphonyLocations();
                    for (SimphonyLocation location : locations) {
                        if (location.isChecked()) {
                            menuItemsController.SyncSimphonyMenuItems("Automated User", account, location.getRevenueCenterID());
                        }
                    }
                } else if (syncJobType.getName().equals(Constants.NEW_BOOKING_REPORT)) {
                    bookingService.fetchNewBookingFromReport("Automated User", account, null);
                } else if (syncJobType.getName().equals(Constants.CANCEL_BOOKING_REPORT)) {
                    cancelBookingService.fetchCancelBookingFromReport("Automated User", account, null);
                } else if (syncJobType.getName().equals(Constants.OCCUPANCY_UPDATE_REPORT)) {
                    occupancyService.fetchOccupancyFromReport("Automated User", account, null);
                } else if (syncJobType.getName().equals(Constants.EXPENSES_DETAILS_REPORT)) {
                    expensesService.fetchExpensesDetailsFromReport("Automated User", account);
                } else if (syncJobType.getName().equals(Constants.SALES_API_Daily)) {
                    salesAPIController.syncPOSSalesInDayRange("Automated User", account, "Daily");
                } else if (syncJobType.getName().equals(Constants.SALES_API_Monthly)) {
                    salesAPIController.syncPOSSalesInDayRange("Automated User", account, "monthly");
                }

            }

        }

    }

    /*
    * Delivery aggregator scheduler that run every 1 min to check new orders
    * */
//    @Scheduled(cron = "0 * * * * SUN-SAT")
    public void aggregatorScheduler() {

        logger.info("Cron Task :: Execution Time - {}", dateTimeFormatter.format(LocalDateTime.now()));
        logger.info("Current Thread : {}", Thread.currentThread().getName());

        Feature feature = featureService.getFeatureByRef(Features.DELIVERY_AGGREGATORS);
        if(feature == null)
            return;

        ArrayList<Account> accounts = accountService.getActiveAccountsHasFeature(feature);
        ///////////////////////////////////////////////////////////////////////////////////////////////

        for (Account account : accounts) {
            aggregatorIntegratorService.sendTalabatOrdersToFoodics(account);
        }
    }



//    // run every 60 min
//    // @Scheduled(cron = "[Seconds] [Minutes] [Hours] [Day of month] [Month] [Day of week] [Year]")
//    //    @Scheduled(cron = "0 0/52 * * * SUN-SAT")
//    //    @Scheduled(cron = "0/20 * * * * ?")
//    //    @Scheduled(cron = "*/1 * * * * SUN-SAT")
//    @Scheduled(cron = "0 * * * * SUN-SAT")
//    public void canteenSchedular() {
//        logger.info("Canteen Schedular Task :: Execution Time - {}", dateTimeFormatter.format(LocalDateTime.now()));
//        logger.info("Current Thread : {}", Thread.currentThread().getName());
//
//
//    }



}
