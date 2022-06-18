package com.sun.supplierpoc.controllers.application;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.GeneralSettings;
import com.sun.supplierpoc.models.Response;
import com.sun.supplierpoc.models.applications.ApplicationUser;
import com.sun.supplierpoc.models.applications.Group;
import com.sun.supplierpoc.models.applications.SimphonyDiscount;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.repositories.AccountRepo;
import com.sun.supplierpoc.repositories.GeneralSettingsRepo;
import com.sun.supplierpoc.repositories.applications.ApplicationUserRepo;
import com.sun.supplierpoc.repositories.applications.GroupRepo;
import com.sun.supplierpoc.services.AppGroupService;
import com.sun.supplierpoc.services.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.*;

@RestController
public class GroupController {

    @Autowired
    AccountRepo accountRepo;
    @Autowired
    GroupRepo groupRepo;
    @Autowired
    private AppGroupService appGroupService;
    @Autowired
    private ImageService imageService;

    @Autowired
    private ApplicationUserRepo userRepo;

    @Autowired
    private GeneralSettingsRepo generalSettingsRepo;

    private Conversions conversions = new Conversions();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @RequestMapping("/getAllApplicationGroups")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity getAllApplicationCompanies(Principal principal, @RequestParam("status") int status) {

        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        if (accountOptional.isPresent()) {

            Account account = accountOptional.get();
            ArrayList<Group> groups;
            if(status == 1)
                groups = groupRepo.findAllByAccountIdAndDeleted(account.getId(), false);
            else
                groups = groupRepo.findAllByAccountId(account.getId());

            return ResponseEntity.status(HttpStatus.OK).body(groups);
        }
        return new ResponseEntity(HttpStatus.FORBIDDEN);
    }

    @RequestMapping("/getGenericGroup")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity getGenericGroup(Principal principal) {

        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        if (accountOptional.isPresent()) {

            Account account = accountOptional.get();

            Group group = groupRepo.findByAccountIdAndDeletedAndName(account.getId(),false, Constants.GENERIC);

            // Create if not exists
            if(group == null){
                Group newGenericGroup = new Group();
                SimphonyDiscount simphonyDiscount = new SimphonyDiscount();

                newGenericGroup.setName(Constants.GENERIC);
                newGenericGroup.setDescription(Constants.GENERIC);
                newGenericGroup.setLogoUrl("");
                newGenericGroup.setAccountId(account.getId());
                newGenericGroup.setSimphonyDiscount(simphonyDiscount);
                newGenericGroup.setCreationDate(new Date());
                newGenericGroup.setLastUpdate(new Date());
                newGenericGroup.setDeleted(false);

                group = groupRepo.save(newGenericGroup);
            }

            return ResponseEntity.status(HttpStatus.OK).body(group);
        }
        return new ResponseEntity(HttpStatus.FORBIDDEN);
    }


    @RequestMapping("/getApplicationGroups")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity getApplicationCompanies(Principal principal, @RequestParam("parentId") String parentId,
                                                  @RequestParam("isParent") boolean isParent,
                                                  @RequestParam("status") int status) {

        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();
            ArrayList<Group> groups;

            if(isParent){
                if(status == 1)
                    groups = groupRepo.findAllByAccountIdAndParentGroupIdAndDeleted(account.getId(), null, false);
                else
                    groups = groupRepo.findAllByAccountIdAndParentGroupId(account.getId(), null);
            }else {
                if(status == 1)
                    groups = groupRepo.findAllByAccountIdAndParentGroupIdAndDeleted(account.getId(), parentId, false);
                else
                    groups = groupRepo.findAllByAccountIdAndParentGroupId(account.getId(), parentId);
            }

            return ResponseEntity.status(HttpStatus.OK).body(groups);
        }
        return new ResponseEntity(HttpStatus.FORBIDDEN);
    }

    @RequestMapping("/addApplicationGroup")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity addApplicationGroupImage(@RequestParam(name = "addFlag") boolean addFlag,
                                                   @RequestPart(name = "name", required = false) String name,
                                                   @RequestPart(name = "description", required = false) String description,
                                                   @RequestPart(name = "discountId", required = false) String discountId,
                                                   @RequestPart(name = "parentGroupId", required = false) String parentGroupId,
                                                   @RequestPart(name = "groupId", required = false) String groupId,
                                                   @RequestPart(name = "image", required = false) MultipartFile image,
                                                   Principal principal) {

            Response response = new Response();
        try {

            User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
            Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());

            if (accountOptional.isPresent()) {

                Account account = accountOptional.get();
                Group group;
                Group parentGroup ;

                GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);
                ArrayList<SimphonyDiscount> discounts = generalSettings.getDiscountRates();

                if (addFlag) {
                    group = new Group();
                    if (parentGroupId != null) {
                        Optional<Group> parentGroupOptional = groupRepo.findById(parentGroupId);
                        parentGroup = parentGroupOptional.get();

                        if (parentGroup.getParentGroupId() != null) {
                            response.setStatus(false);
                            response.setMessage("Parent group is already child for another group, Please select valid parent group.");

                            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                        }else if(parentGroup.isDeleted()){
                            response.setStatus(false);
                            response.setMessage("Parent group is inactive, Please select valid parent group.");

                            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                        }
                        group.setParentGroupId(parentGroup.getId());
                    }

                    Optional<Group> testNameGroupOptional = groupRepo.findByNameAndAccountId(name, account.getId());

                    if (testNameGroupOptional.isPresent()) {
                        response.setMessage("Group is already exist with this name.");
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                    }

                    String logoUrl = Constants.GROUP_IMAGE_URL;
                    if (image != null) {
                        try {
                            logoUrl = imageService.store(image);
                        } catch (Exception e) {
                            response.setMessage("Group is already exist with this name.");
                            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
                        }
                    }

                    SimphonyDiscount simphonyDiscount = new SimphonyDiscount();
                    if(discountId != null)
                        simphonyDiscount = conversions.checkSimphonyDiscountExistence(discounts, Integer.parseInt(discountId));

                    group.setName(name);
                    group.setDescription(description);
                    group.setLogoUrl(logoUrl);
                    group.setSimphonyDiscount(simphonyDiscount);
                    group.setAccountId(account.getId());
                    group.setCreationDate(new Date());
                    group.setLastUpdate(new Date());
                    group.setDeleted(false);

                    groupRepo.save(group);
                }else {
                    Optional<Group> groupOptional = groupRepo.findById(groupId);

                    if (groupOptional.isPresent()) {

                        group = groupOptional.get();

                        Optional<Group> testNameGroupOptional = groupRepo.findByNameAndAccountId(name, account.getId());

                        if (testNameGroupOptional.isPresent() && !group.getName().equals(name)) {
                            response.setStatus(false);
                            response.setMessage("Group is already exist with this name.");
                            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                        }

                        if(image != null) {
                            String logoUrl = Constants.GROUP_IMAGE_URL;
                            if (image != null) {
                                try {
                                    logoUrl = imageService.store(image);
                                } catch (Exception e) {
                                    response.setMessage("Group is already exist with this name.");
                                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
                                }
                            }
                            group.setLogoUrl(logoUrl);
                        }

                        if (parentGroupId != null) {
                            Optional<Group> parentGroupOptional = groupRepo.findById(parentGroupId);
                            parentGroup = parentGroupOptional.get();
                            if (parentGroup.getParentGroupId() != null) {
                                response.setStatus(false);
                                response.setMessage("Parent group is already child for another group, Please select valid parent group.");

                                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                            }else if(parentGroup.isDeleted()){
                                response.setStatus(false);
                                response.setMessage("Parent group is inactive, Please select valid parent group.");

                                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                            }else if(group.getId().equals(parentGroupId)){
                                response.setStatus(false);
                                response.setMessage("Invalid parent group.");
                                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                            }
                            group.setParentGroupId(parentGroup.getId());
                        }

                        SimphonyDiscount simphonyDiscount = new SimphonyDiscount();
                        if(discountId != null)
                            simphonyDiscount = conversions.checkSimphonyDiscountExistence(discounts, Integer.parseInt(discountId));

                        group.setName(name);
                        group.setDescription(description);
                        group.setSimphonyDiscount(simphonyDiscount);
                        group.setAccountId(account.getId());
                        group.setLastUpdate(new Date());
                        groupRepo.save(group);

                        List<ApplicationUser> applicationUsers = userRepo.findAllByAccountIdAndGroupId(account.getId(), group.getId());
                        for (ApplicationUser applicationUser : applicationUsers) {
                            applicationUser.setGroup(group);
                            userRepo.save(applicationUser);
                        }
                    }
                }

                response.setMessage("Group saved successfully.");
                return ResponseEntity.status(HttpStatus.OK).body(response);
            } else {
                response.setMessage("Invalid user.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
        }catch (Exception e){
            response.setMessage("Something went wrong.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @RequestMapping("/deleteAllApplicationGroupsDeeply")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity deleteAllApplicationGroupsDeeply(Principal principal) {

        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        if (accountOptional.isPresent()) {

            groupRepo.deleteAll();

            return ResponseEntity.status(HttpStatus.OK).body("Deleted");
        }
        return new ResponseEntity(HttpStatus.FORBIDDEN);
    }


    @RequestMapping("/deleteApplicationGroups")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity deleteApplicationCompanies(@RequestBody List<Group> groups, Principal principal,
                                                     @RequestParam(name = "addFlag") boolean addFlag,
                                                     @RequestParam(name = "withUsers") boolean withUsers,
                                                     @RequestParam(name = "parentGroupId") String parentGroupId) {

        User user = (User) ((OAuth2Authentication) principal    ).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        if (accountOptional.isPresent()) {
            for (Group group : groups) {

                Account account = accountOptional.get();

                if (addFlag) {
                    group.setDeleted(true);
                    groupRepo.save(group);

                    if(withUsers) {

                        List<ApplicationUser> applicationUsers = userRepo.findAllByAccountIdAndGroupId(account.getId(), group.getId());

                        for (ApplicationUser applicationUser : applicationUsers) {
                            applicationUser.setDeleted(true);
                            applicationUser.setGroup(group);
                            userRepo.save(applicationUser);
                        }

                        List<Group> childGroups = groupRepo.findAllByAccountIdAndParentGroupId(account.getId(), group.getId());

                        for(Group childGroup: childGroups){
                            childGroup.setDeleted(true);

                            List<ApplicationUser> childGroupApplicationUsers = userRepo.findAllByAccountIdAndGroupId(account.getId(), childGroup.getId());

                            for (ApplicationUser applicationUser : childGroupApplicationUsers) {
                                applicationUser.setDeleted(true);
                                applicationUser.setGroup(childGroup);
                                userRepo.save(applicationUser);
                            }
                        }

                        groupRepo.saveAll(childGroups);

                    }else{
                        List<ApplicationUser> applicationUsers = userRepo.findAllByAccountIdAndGroupId(account.getId(), group.getId());

                        Optional<Group> newGroupOptional = groupRepo.findById(parentGroupId);

                        if(newGroupOptional.isPresent()) {
                            Group newGroup = newGroupOptional.get();
                            for (ApplicationUser applicationUser : applicationUsers) {
                                applicationUser.setGroup(newGroup);
                            }

                            List<Group> childGroups = groupRepo.findAllByAccountIdAndParentGroupId(account.getId(), group.getId());

                            for(Group childGroup: childGroups){
                                childGroup.setParentGroupId(newGroup.getId());

                                List<ApplicationUser> childGroupApplicationUsers = userRepo.findAllByAccountIdAndGroupId(account.getId(), childGroup.getId());

                                for (ApplicationUser applicationUser : childGroupApplicationUsers) {
                                    applicationUser.setGroup(childGroup);
                                    userRepo.save(applicationUser);
                                }
                            }

                            groupRepo.saveAll(childGroups);

                        }else{ }
                        userRepo.saveAll(applicationUsers);
                    }
                } else {
                    group.setDeleted(false);
                    groupRepo.save(group);

                    if(withUsers) {
                        List<ApplicationUser> applicationUsers = userRepo.findAllByAccountIdAndGroupId(account.getId(), group.getId());
                        for (ApplicationUser applicationUser : applicationUsers) {
                            applicationUser.setDeleted(false);
                            applicationUser.setGroup(group);
                            userRepo.save(applicationUser);
                        }

                        List<Group> childGroups = groupRepo.findAllByAccountIdAndParentGroupId(account.getId(), group.getId());

                        for(Group childGroup: childGroups){
                            childGroup.setDeleted(false);

                            List<ApplicationUser> childGroupApplicationUsers = userRepo.findAllByAccountIdAndGroupId(account.getId(), childGroup.getId());

                            for (ApplicationUser applicationUser : childGroupApplicationUsers) {
                                applicationUser.setDeleted(false);
                                applicationUser.setGroup(childGroup);
                                userRepo.save(applicationUser);
                            }
                        }
                        groupRepo.saveAll(childGroups);

                    }
                }
            }
            return ResponseEntity.status(HttpStatus.OK).body(groups);
        }
        return new ResponseEntity(HttpStatus.FORBIDDEN);
    }

    @RequestMapping("/getTopGroups")
    public List getTopGroups(Principal principal) {

        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();

        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());

        if (accountOptional.isPresent()) {

            Account account = accountOptional.get();

            List<Group> groups = appGroupService.getTopGroups(account);

            return groups;
        } else {
            return new ArrayList<>();
        }
    }


    @RequestMapping("/saveGroup")
    public Group saveGroup(Principal principal, @RequestBody Group group) {
        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Group savedGroup = appGroupService.saveGroup(group);
        return savedGroup;
    }

}
