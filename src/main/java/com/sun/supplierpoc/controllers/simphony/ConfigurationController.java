package com.sun.supplierpoc.controllers.simphony;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.Response;
import com.sun.supplierpoc.models.SyncJob;
import com.sun.supplierpoc.models.SyncJobType;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.models.configurations.SimphonyLocation;
import com.sun.supplierpoc.models.configurations.Tender;
import com.sun.supplierpoc.repositories.AccountRepo;
import com.sun.supplierpoc.repositories.SyncJobRepo;
import com.sun.supplierpoc.repositories.SyncJobTypeRepo;
import com.sun.supplierpoc.repositories.UserRepo;
import com.sun.supplierpoc.services.simphony.MenuItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

@RestController()
public class ConfigurationController {
    @Autowired
    private AccountRepo accountRepo;
    @Autowired
    private SyncJobTypeRepo syncJobTypeRepo;
    @Autowired
    private SyncJobRepo syncJobRepo;
    @Autowired
    MenuItemService menuItemService;
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @RequestMapping("/GetSimphonyMenuItems")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity<Response> GetSimphonyCheckDetailRequest(Principal principal,
                                                                  @RequestParam(name = "revenueCenterID") int revenueCenterID) {
        Response response = new Response();
        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();
            response = GetSimphonyMenuItems(user.getId(), account, revenueCenterID);
            if(!response.isStatus()){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }else {
                return ResponseEntity.status(HttpStatus.OK).body(response);
            }
        }

        String message = "Invalid Credentials";
        response.setMessage(message);
        response.setStatus(false);

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    public Response GetSimphonyMenuItems(String userId, Account account, int revenueCenterID){
        Response response = new Response();
        SyncJob syncJob = null;
        try {
            SyncJobType syncJobType = syncJobTypeRepo.findByNameAndAccountIdAndDeleted(Constants.MENU_ITEMS, account.getId(), false);

            //////////////////////////////////////// Validation ////////////////////////////////////////////////////////
            SimphonyLocation simphonyLocation = syncJobType.getConfiguration().getSimphonyLocationsByID(revenueCenterID);
            int empNum = simphonyLocation.getEmployeeNumber();
            String simphonyPosApiWeb = simphonyLocation.getSimphonyServer();

            if (simphonyPosApiWeb.equals("")){
                String message = "Please configure simphony server IP before sync credit notes.";
                response.setMessage(message);
                response.setStatus(false);

                return response;
            }

            //////////////////////////////////////// En of Validation //////////////////////////////////////////////////

            syncJob = new SyncJob(Constants.RUNNING, "", new Date(), null, userId,
                    account.getId(), syncJobType.getId(), 0);

            syncJobRepo.save(syncJob);

            response = this.menuItemService.GetConfigurationInfoEx(empNum, revenueCenterID, simphonyPosApiWeb);
            if(response.isStatus()){
                // Save menu items
                this.menuItemService.saveMenuItemData(response.getMenuItems(), syncJob);
                syncJob.setStatus(Constants.SUCCESS);
                syncJob.setEndDate(new Date());
                syncJob.setRowsFetched(response.getMenuItems().size());
                syncJobRepo.save(syncJob);
            }else {
                syncJob.setStatus(Constants.FAILED);
                syncJob.setReason(response.getMessage());
                syncJob.setEndDate(new Date());
                syncJob.setRowsFetched(0);
                syncJobRepo.save(syncJob);
            }

            return response;
        }catch (Exception e){
            if (syncJob != null){
                syncJob.setStatus(Constants.FAILED);
                syncJob.setReason(e.getMessage());
                syncJob.setEndDate(new Date());
                syncJob.setRowsFetched(0);
                syncJobRepo.save(syncJob);
            }

            return response;
        }
    }


    @RequestMapping("/addSimphonyLocation")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity<Response> addSimphonyLocation(@RequestBody ArrayList<SimphonyLocation> locations,
                                              @RequestParam(name = "syncJobTypeId") String syncJobTypeId,
                                              Principal principal) {
        Response response = new Response();
        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();
            SyncJobType syncJobType = syncJobTypeRepo.findByIdAndDeleted(syncJobTypeId, false);

            // Check account quota first
            if (locations.size() > account.getLocationQuota()){
                response.setStatus(false);
                response.setMessage("Exceed account quota, Please contact support team to raise it. ");

                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            else if (syncJobType != null) {
                syncJobType.getConfiguration().setSimphonyLocations(locations);
                syncJobTypeRepo.save(syncJobType);

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
