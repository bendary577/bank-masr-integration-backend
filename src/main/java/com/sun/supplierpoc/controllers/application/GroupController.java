package com.sun.supplierpoc.controllers.application;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.applications.ApplicationUser;
import com.sun.supplierpoc.models.applications.Group;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.repositories.AccountRepo;
import com.sun.supplierpoc.repositories.ApplicationRepo;
import com.sun.supplierpoc.repositories.applications.ApplicationUserRepo;
import com.sun.supplierpoc.repositories.applications.GroupRepo;
import com.sun.supplierpoc.services.AppGroupService;
import com.sun.supplierpoc.services.ImageService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
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

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @RequestMapping("/getAllApplicationGroups")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity getAllApplicationCompanies(Principal principal) {

        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        if (accountOptional.isPresent()) {

            Account account = accountOptional.get();
            ArrayList<Group> groups = groupRepo.findAllByAccountId(account.getId());

            return ResponseEntity.status(HttpStatus.OK).body(groups);
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
                                                   @RequestPart(name = "discountRate", required = false) String discountRate,
                                                   @RequestPart(name = "discountId", required = false) String discountId,
                                                   @RequestPart(name = "parentGroupId", required = false) String parentGroupId,
                                                   @RequestPart(name = "groupId", required = false) String groupId,
                                                   @RequestPart(name = "image", required = false) MultipartFile image,
                                                   Principal principal) {

        HashMap response = new HashMap();
        try {

            User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
            Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());

            if (accountOptional.isPresent()) {
                Account account = accountOptional.get();
                Group group;
                Group parentGroup ;

                if (addFlag) {

                    group = new Group();

                    if (parentGroupId != null) {

                        Optional<Group> parentGroupOptional = groupRepo.findById(parentGroupId);
                        parentGroup = parentGroupOptional.get();

                        if (parentGroupOptional.get().getParentGroupId() != null) {
                            response.put("message", "Parent group is already child for another group," +
                                    "\n Please select valid parent group.");
                            return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
                        }
                        group.setParentGroupId(parentGroup.getId());
                    }

                    Optional<Group> testNameGroupOptional = groupRepo.findByName(name);

                    if (testNameGroupOptional.isPresent()) {
                        response.put("message", "Group is already exist with this name.");
                        return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
                    }

                    String logoUrl = Constants.GROUP_IMAGE_URL;
//                    if (image != null) {
//                        try {
//                            logoUrl = imageService.store(image);
//                        } catch (Exception e) {
//                            LoggerFactory.getLogger(GroupController.class).info(e.getMessage());
//                        }
//                    }

                    group.setName(name);
                    group.setDescription(description);
                    group.setDiscountRate(Float.parseFloat(discountRate));
                    group.setDiscountId(Integer.parseInt(discountId));
                    group.setLogoUrl(logoUrl);
                    group.setAccountId(account.getId());
                    group.setCreationDate(new Date());
                    group.setLastUpdate(new Date());
                    group.setDeleted(false);
                    groupRepo.save(group);
                } else {
                    Optional<Group> groupOptional = groupRepo.findById(groupId);

                    if (groupOptional.isPresent()) {

                        group = groupOptional.get();

                        Optional<Group> testNameGroupOptional = groupRepo.findByName(name);

                        if (testNameGroupOptional.isPresent() && !group.getName().equals(name)) {
                            response.put("message", "Group is already exist with this name.");
                            return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
                        }

                        String logoUrl = Constants.GROUP_IMAGE_URL;
//                        if (image != null) {
//                            try {
//                                logoUrl = imageService.store(image);
//                            } catch (Exception e) {
//                                LoggerFactory.getLogger(GroupController.class).info(e.getMessage());
//                            }
//                        }

                        if (parentGroupId != null) {
                            Optional<Group> parentGroupOptional = groupRepo.findById(parentGroupId);
                            parentGroup = parentGroupOptional.get();
                            if (parentGroupOptional.get().getParentGroupId() != null) {
                                response.put("message", "Parent group is already child for another group," +
                                        "\n Please select valid parent group.");
                                return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
                            }
                            group.setParentGroupId(parentGroup.getId());
                        }

                        group.setName(name);
                        group.setDescription(description);
                        group.setDiscountRate(Float.parseFloat(discountRate));
                        group.setDiscountId(Integer.parseInt(discountId));
                        group.setLogoUrl(logoUrl);
                        group.setAccountId(account.getId());
                        group.setLastUpdate(new Date());
                        groupRepo.save(group);
                    }
                }
                response.put("message", "Group saved successfully.");
                return ResponseEntity.status(HttpStatus.OK).body(response);
            } else {
                response.put("message", "Invalid user.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
        }catch (Exception e){
            response.put("message", "Something went wrong.");
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
                                                     @RequestParam(name = "addFlag") boolean addFlag) {
        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        if (accountOptional.isPresent()) {
            for (Group group : groups) {

                Account account = accountOptional.get();

                if (addFlag) {
                    group.setDeleted(true);
                    groupRepo.save(group);

                    List<ApplicationUser> applicationUsers = userRepo.findAllByAccountIdAndGroupAndDeleted(account.getId(), group, false);

                    for (ApplicationUser applicationUser : applicationUsers) {
                        applicationUser.setDeleted(true);
                        userRepo.save(applicationUser);
                    }
                } else {
                    group.setDeleted(false);
                    groupRepo.save(group);

                    List<ApplicationUser> applicationUsers = userRepo.findAllByAccountIdAndGroupAndDeleted(account.getId(), group, false);

                    for (ApplicationUser applicationUser : applicationUsers) {
                        applicationUser.setDeleted(false);
                        userRepo.save(applicationUser);
                    }
                }
            }
            return ResponseEntity.status(HttpStatus.OK).body(groups);
        }
        return new ResponseEntity(HttpStatus.FORBIDDEN);
    }

    @RequestMapping("/getTopGroups")
    public List getTransactionByType(Principal principal) {

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
}
