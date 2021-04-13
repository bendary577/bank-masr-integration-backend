package com.sun.supplierpoc.controllers.simphony;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.models.auth.InvokerUser;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.models.configurations.SimphonyLocation;
import com.sun.supplierpoc.models.simphony.response.MenuItemResponse;
import com.sun.supplierpoc.repositories.AccountRepo;
import com.sun.supplierpoc.repositories.GeneralSettingsRepo;
import com.sun.supplierpoc.repositories.SyncJobRepo;
import com.sun.supplierpoc.repositories.SyncJobTypeRepo;
import com.sun.supplierpoc.services.AccountService;
import com.sun.supplierpoc.services.InvokerUserService;
import com.sun.supplierpoc.services.SyncJobDataService;
import com.sun.supplierpoc.services.SyncJobService;
import com.sun.supplierpoc.services.simphony.MenuItemService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;

@RestController()
public class MenuItemsController {
    @Autowired
    private AccountRepo accountRepo;
    @Autowired
    private SyncJobTypeRepo syncJobTypeRepo;
    @Autowired
    private SyncJobRepo syncJobRepo;
    @Autowired
    private GeneralSettingsRepo generalSettingsRepo;
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
    private Conversions conversions = new Conversions();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @RequestMapping("/SyncSimphonyMenuItems")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity<Response> SyncSimphonyMenuItemsRequest(Principal principal) {
        Response response = new Response();
        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();
            GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);
            ArrayList<SimphonyLocation> locations = generalSettings.getSimphonyLocations();
            for (SimphonyLocation location : locations){
                if(location.isChecked()){
                    response = SyncSimphonyMenuItems(user.getId(), account, location.getRevenueCenterID());
                    if(!response.isStatus()){
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                    }
                }
            }

            return ResponseEntity.status(HttpStatus.OK).body(response);
        }

        String message = "Invalid Credentials";
        response.setMessage(message);
        response.setStatus(false);

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    public Response SyncSimphonyMenuItems(String userId, Account account, int revenueCenterID){
        Response response = new Response();
        SyncJob syncJob = null;
        try {
            SyncJobType syncJobType = syncJobTypeRepo.findByNameAndAccountIdAndDeleted(Constants.MENU_ITEMS, account.getId(), false);
            GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);

            //////////////////////////////////////// Validation ////////////////////////////////////////////////////////
            int startIndex = syncJobType.getConfiguration().menuItemConfiguration.startIndex;
            int maxCount = syncJobType.getConfiguration().menuItemConfiguration.maxCount;

            SimphonyLocation simphonyLocation = generalSettings.getSimphonyLocationsByID(revenueCenterID);
            if(simphonyLocation == null){
                String message = "Please configure revenue center before sync menu items.";
                response.setMessage(message);
                response.setStatus(false);

                return response;
            }

            int empNum = simphonyLocation.getEmployeeNumber();
            String simphonyPosApiWeb = simphonyLocation.getSimphonyServer();

            if (simphonyPosApiWeb.equals("")){
                String message = "Please configure simphony server IP before sync menu items.";
                response.setMessage(message);
                response.setStatus(false);

                return response;
            }

            if (maxCount == 0){
                String message = "Please configure menu items count before sync menu items.";
                response.setMessage(message);
                response.setStatus(false);

                return response;
            }

            //////////////////////////////////////// End of Validation //////////////////////////////////////////////////

            syncJob = new SyncJob(Constants.RUNNING, "", new Date(), null, userId,
                    account.getId(), syncJobType.getId(), 0);
            syncJob.setRevenueCenter(revenueCenterID);

            syncJobRepo.save(syncJob);

            response = this.menuItemService.GetConfigurationInfoEx(empNum, revenueCenterID, simphonyPosApiWeb,
                    startIndex, maxCount);
            if(response.isStatus()){
                // Save menu items
                ArrayList<SyncJobData> savedMenuItems = this.menuItemService.saveMenuItemData(response.getMenuItems(),
                        syncJob, response.getMenuItemClasses());
                response.setMenuItemClasses(null);
                syncJob.setStatus(Constants.SUCCESS);
                syncJob.setEndDate(new Date());
                syncJob.setRowsFetched(response.getMenuItems().size());
                syncJobRepo.save(syncJob);

                response.setAddedSyncJobData(savedMenuItems);
            }else {
                syncJob.setStatus(Constants.FAILED);
                syncJob.setReason(response.getMessage());
                syncJob.setEndDate(new Date());
                syncJob.setRowsFetched(0);
                syncJobRepo.save(syncJob);
            }


            return response;
        }catch (Exception e){
            LoggerFactory.getLogger(MenuItemsController.class).info("");
            e.printStackTrace();
            if (syncJob != null){
                syncJob.setStatus(Constants.FAILED);
                syncJob.setReason(e.getMessage());
                syncJob.setEndDate(new Date());
                syncJob.setRowsFetched(0);
                syncJobRepo.save(syncJob);
            }
            response.setMessage(e.getMessage());
            response.setStatus(false);

            return response;
        }
    }

    @RequestMapping("/Simphony/GetSimphonyMenuItems")
    public ResponseEntity GetSimphonyMenuItemsRequest(@RequestParam(name = "revenueCenterID") int revenueCenterID,
                                                      @RequestHeader("Authorization") String authorization) {
        String username, password;

        final String[] values = conversions.convertBasicAuth(authorization);
        if (values.length != 0) {
            username = values[0];
            password = values[1];

            ArrayList<SyncJobData> syncJobData;

            InvokerUser invokerUser = invokerUserService.getInvokerUser(username, password);
            if (invokerUser != null){
                Optional<Account> accountOptional = accountService.getAccountOptional(invokerUser.getAccountId());

                if (accountOptional.isPresent()) {
                    Account account = accountOptional.get();

                    SyncJobType syncJobType = syncJobTypeRepo.findByNameAndAccountIdAndDeleted(Constants.MENU_ITEMS, account.getId(), false);

                    if (!invokerUser.getTypeId().contains(syncJobType.getId())){
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You don't have role to get menu items!");
                    }

                    /*
                     * Get last success sync job with revenue center ID
                     * */

                    GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);
                    SimphonyLocation location = generalSettings.getSimphonyLocationsByID(revenueCenterID);

                    if (location == null) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                                new HashMap<String, Object>() {{
                                    put("error", "Revenue center not found.");
                                    put("Date", LocalDateTime.now());
                                }});
                    }

                    SyncJob syncJob = syncJobService.getSyncJobByRevenueCenterID(revenueCenterID, syncJobType.getId());

                    if (syncJob != null){
                        syncJobData = syncJobDataService.getSyncJobData(syncJob.getId());
                        ArrayList<MenuItemResponse> menuItems = menuItemService.simplifyMenuItemData(syncJobData);
                        return new ResponseEntity<>(menuItems, HttpStatus.OK);
                    }else{
                        // Sync menu items
                        Response syncResponse = SyncSimphonyMenuItems(username, account, revenueCenterID);

                        if(syncResponse.isStatus()){
                            syncJobData = syncResponse.getAddedSyncJobData();
                            ArrayList<MenuItemResponse> menuItems = menuItemService.simplifyMenuItemData(syncJobData);
                            return new ResponseEntity<>(menuItems, HttpStatus.OK);
                        }else {
                            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(syncResponse.getMessage());
                        }
                    }
                }else {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                            new HashMap<String, Object>() {{
                                put("error", "Wrong username or password.");
                                put("Date", LocalDateTime.now());
                            }});
                }
            }else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                        new HashMap<String, Object>() {{
                            put("error", "Wrong username or password.");
                            put("Date", LocalDateTime.now());
                        }});
            }
        }else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    new HashMap<String, Object>() {{
                        put("error", "Wrong username or password.");
                        put("Date", LocalDateTime.now());
                    }});
        }
    }

    @RequestMapping("/addSimphonyLocation")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity<Response> addSimphonyLocation(@RequestBody ArrayList<SimphonyLocation> locations,
                                              Principal principal) {
        Response response = new Response();
        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();
            GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);
            // Check account quota first
            if (locations.size() > account.getLocationQuota()){
                response.setStatus(false);
                response.setMessage("Exceed account quota, Please contact support team to raise it. ");

                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            else if (generalSettings != null) {
                generalSettings.setSimphonyLocations(locations);
                generalSettingsRepo.save(generalSettings);

                response.setStatus(true);
                response.setMessage("Update simphony locations successfully.");
            } else {
                response.setStatus(false);
                response.setMessage("Failed to update simphony locations.");

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
