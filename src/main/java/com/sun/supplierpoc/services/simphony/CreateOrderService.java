package com.sun.supplierpoc.services.simphony;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.models.simphony.MenuItem;
import com.sun.supplierpoc.models.simphony.SimphonyMenuItem;
import com.sun.supplierpoc.models.simphony.transaction.PostTransactionEx2;
import com.sun.supplierpoc.models.simphony.transaction.PostTransactionEx2Response;
import com.sun.supplierpoc.repositories.*;
import com.sun.supplierpoc.services.AccountService;
import com.sun.supplierpoc.services.InvokerUserService;
import com.sun.supplierpoc.services.SyncJobDataService;
import com.sun.supplierpoc.services.SyncJobService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Service
public class CreateOrderService {

    @Autowired
    private SyncJobRepo syncJobRepo;
    @Autowired
    MenuItemService menuItemService;
    @Autowired
    SyncJobService syncJobService;
    @Autowired
    SyncJobDataService syncJobDataService;
    @Autowired
    AccountService accountService;
    @Autowired
    InvokerUserService invokerUserService;
    @Autowired
    private SyncJobDataRepo syncJobDataRepo;

    @Autowired
    private OperationDataRepo operationDataRepo;
    @Autowired
    private SyncJobTypeRepo syncJobTypeRepo;
    private Conversions conversions = new Conversions();

    public ArrayList<OperationData> saveOrderCreation(PostTransactionEx2Response transactionResponse,
                                                      PostTransactionEx2 checkDetails, Operation operation, String accountId) {

        ArrayList<OperationData> savedMenuItems = new ArrayList<>();
        HashMap<String, Object> zealPaymentData = new HashMap<>();
        List<HashMap<String, String>> menuItemsMap = getItem(checkDetails, accountId);

        float checkAmount =  Float.parseFloat(transactionResponse.getpTotalsResponseEx().getTotalsSubTotal()) +
                Float.parseFloat((transactionResponse.getpTotalsResponseEx().getTotalsTaxTotals()));

        zealPaymentData.put("menuItems", menuItemsMap);
        zealPaymentData.put("checkOrderType", checkDetails.getpGuestCheck().getCheckOrderType());
        zealPaymentData.put("checkInfoLines", checkDetails.getpGuestCheck().getPCheckInfoLines().getString());

        zealPaymentData.put("checkId", transactionResponse.getpGuestCheck().getCheckNum());
        zealPaymentData.put("partialAmount", transactionResponse.getpTotalsResponseEx().getTotalsSubTotal());
        zealPaymentData.put("tax", transactionResponse.getpTotalsResponseEx().getTotalsTaxTotals());
        zealPaymentData.put("totalAmount", String.valueOf(conversions.roundUpFloat(checkAmount)));

        OperationData operationData = new OperationData(zealPaymentData, new Date(), operation.getId());

        operationDataRepo.save(operationData);
        savedMenuItems.add(operationData);
        return savedMenuItems;
    }

    public ArrayList<OperationData> saveOrderCreation(PostTransactionEx2 checkDetails, Operation operation, String accountId) {

        ArrayList<OperationData> savedMenuItems = new ArrayList<>();
        HashMap<String, Object> zealPaymentData = new HashMap<>();
        List<HashMap<String, String>> menuItemsMap = getItem(checkDetails, accountId);

        zealPaymentData.put("menuItems", menuItemsMap);
        zealPaymentData.put("checkOrderType", checkDetails.getpGuestCheck().getCheckOrderType());
        zealPaymentData.put("checkInfoLines", checkDetails.getpGuestCheck().getPCheckInfoLines().getString());

        zealPaymentData.put("checkId", 0);
        zealPaymentData.put("partialAmount", 0);
        zealPaymentData.put("tax", 0);
        zealPaymentData.put("totalAmount", 0);

        OperationData operationData = new OperationData(zealPaymentData, new Date(), operation.getId());

        operationDataRepo.save(operationData);
        savedMenuItems.add(operationData);
        return savedMenuItems;
    }

    private List<HashMap<String, String>> getItem(PostTransactionEx2 checkDetails, String accountId) {
        SyncJobType syncJobType = syncJobTypeRepo.findByNameAndAccountIdAndDeleted(Constants.MENU_ITEMS, accountId, false);

        List<SyncJob> syncJobs = syncJobRepo.findBySyncJobTypeIdAndDeletedOrderByCreationDateDesc(syncJobType.getId(), false);
        SyncJob syncJob = syncJobs.get(0);

        ArrayList<SyncJobData> syncJobData = new ArrayList<>(syncJobDataRepo.findBySyncJobIdAndDeleted(syncJob.getId(), false));

        ArrayList<HashMap<String, String>> menuItems = menuItemService.simplifyMenuItemData(syncJobData);
        List<HashMap<String, String>> menuItemsMap = new ArrayList<>();

        List<SimphonyMenuItem> listMenuItem = checkDetails.getPpMenuItemsEx();

        for (SimphonyMenuItem menuItem : listMenuItem) {
            for (HashMap<String, String> tempItem : menuItems) {

                if (tempItem.get("miObjectNum").equals(menuItem.getMenuItem().getMiObjectNum())) {
                    tempItem.put("quantity", menuItem.getMenuItem().getMiQuantity());
                    menuItemsMap.add(tempItem);
                }
            }
        }
        return menuItemsMap;
    }

}