package com.sun.supplierpoc.services.simphony;

import com.sun.supplierpoc.repositories.*;
import com.sun.supplierpoc.services.AccountService;
import com.sun.supplierpoc.services.InvokerUserService;
import com.sun.supplierpoc.services.SyncJobDataService;
import com.sun.supplierpoc.services.SyncJobService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

@Service
public class CreateOrderService {

    @Autowired
    private CallRestService callRestService;
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
    private OperationTypeRepo operationTypeRepo;
    @Autowired
    private SyncJobDataRepo syncJobDataRepo;
    @Autowired
    private OperationRepo operationRepo;
    @Autowired
    private OperationDataRepo operationDataRepo;
    @Autowired
    private SyncJobTypeRepo syncJobTypeRepo;


//    public ArrayList<OperationData> saveOrderCreation(PostTransactionEx2 checkDetails, Operation operation) {
//
//        ArrayList<OperationData> savedMenuItems = new ArrayList<>();
//        HashMap<String, Object> zealPaymentData = new HashMap<>();
//        List<HashMap<String, Object>> menuItemsMap = getItem(checkDetails);
//
//        zealPaymentData.put("checkOrderType", checkDetails.getpGuestCheck().getCheckOrderType());
//        zealPaymentData.put("checkInfoLines", checkDetails.getpGuestCheck().getPCheckInfoLines());
//        zealPaymentData.put("status", "received");
//        zealPaymentData.put("menuItems", menuItemsMap);
//
//        LoggerFactory.getLogger(CreateOrderService.class).info(zealPaymentData.toString());
//        OperationData operationData = new OperationData(zealPaymentData, new Date(),
//                operation.getId());
//
//        operationDataRepo.save(operationData);
//        savedMenuItems.add(operationData);
//        return savedMenuItems;
//    }

//    public List<HashMap<String, Object>> getItem(PostTransactionEx2 checkDetails) {
//
//        List<SyncJob> syncJobs = syncJobRepo.findBySyncJobTypeIdAndDeletedOrderByCreationDateDesc("5fe34649283cde246c2d7736", false);
//        SyncJob syncJob = syncJobs.get(0);
//        System.out.println(syncJob.getEndDate());
//        ArrayList<SyncJobData> syncJobData = new ArrayList<>(syncJobDataRepo.findBySyncJobIdAndDeleted(syncJob.getId(), false));
//
//        ArrayList<HashMap<String, Object>> menuItems = menuItemService.simplifyMenuItemData(syncJobData);
//        List<HashMap<String, Object>> menuItemsMap = new ArrayList<>();
//
//        List<MenuItem> listMenuItem = checkDetails.getPpMenuItemsEx().getSimphonyPosApi_MenuItemEx().get;
//
//        for (MenuItem menuItem : listMenuItem) {
//            for (HashMap<String, Object> tempItem : menuItems) {
//
//                if (tempItem.get("miObjectNum").equals(menuItem.getMiObjectNum())) {
//                    menuItemsMap.add(tempItem);
//                }
//            }
//        }
//        return menuItemsMap;
//    }
}