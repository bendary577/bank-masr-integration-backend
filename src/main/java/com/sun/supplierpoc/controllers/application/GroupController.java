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
                                                  @RequestParam("isParent") boolean isParent) {

        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();
            ArrayList<Group> groups;

            if (isParent) {
                groups = groupRepo.findAllByAccountIdAndParentGroup(account.getId(), null);
            } else {
                Optional<Group> groupOptional = groupRepo.findById(parentId);
                if (groupOptional.isPresent()) {
                    Group group = groupOptional.get();
                    groups = groupRepo.findAllByAccountIdAndParentGroup(account.getId(), group);

                } else {
                    return new ResponseEntity(HttpStatus.BAD_REQUEST);
                }            }

            return ResponseEntity.status(HttpStatus.OK).body(groups);
        }
        return new ResponseEntity(HttpStatus.FORBIDDEN);
    }

    @RequestMapping("/addApplicationGroup")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity addApplicationCompany(@RequestParam(name = "addFlag") boolean addFlag,
                                                @RequestBody Group group, Principal principal) {

        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();

            if (addFlag) {

                if (group.getParentGroup() != null) {

                    if (group.getParentGroup().getParentGroup() != null) {
                        return new ResponseEntity("Parent group is already child for another group," +
                                "\n Please select valid parent group.",
                                HttpStatus.BAD_REQUEST);
                    }
                }

                Optional<Group> testNameGroupOptional = groupRepo.findByName(group.getName());

                if(testNameGroupOptional.isPresent()){
                    return new ResponseEntity("Group is already exist with this name.", HttpStatus.BAD_REQUEST);
                }

                group.setAccountId(account.getId());
                group.setCreationDate(new Date());
                group.setLastUpdate(new Date());
                group.setDeleted(false);
            } else {

                if (group.getParentGroup() != null) {
                    if (group.getParentGroup().getParentGroup() != null) {
                        return new ResponseEntity("Parent group is already child for another group," +
                                "\n Please select valid parent group.",
                                HttpStatus.BAD_REQUEST);
                    }
                }

                group.setLastUpdate(new Date());

            }

            groupRepo.save(group);
            return ResponseEntity.status(HttpStatus.OK).body(group);
        }
        return new ResponseEntity(HttpStatus.FORBIDDEN);
    }

    @RequestMapping("/addApplicationGroupImage")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity addApplicationGroupImage(@RequestPart(name = "groupId", required = false) String groupId,
                                                   @RequestPart(name = "image", required = false) MultipartFile image,
                                                   Principal principal) {

        HashMap response = new HashMap();

        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());

        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();

            Optional<Group> groupOptional = groupRepo.findById(groupId);

            if (groupOptional.isPresent()) {

                Group group = groupOptional.get();

                String logoUrl = Constants.USER_IMAGE_URL;
                if(image != null) {
                    try {
                        logoUrl = imageService.store(image);
                    } catch (Exception e) {
                        LoggerFactory.getLogger(GroupController.class).info(e.getMessage());
                    }
                }
                group.setLogoUrl(logoUrl);
                groupRepo.save(group);

                response.put("message", "Group updated successfully.");
                return ResponseEntity.status(HttpStatus.OK).body(response);
            }else{
                response.put("message", "Can't save group.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        }
        return new ResponseEntity(HttpStatus.FORBIDDEN);
    }


    @RequestMapping("/deleteApplicationGroups")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity deleteApplicationCompanies(@RequestBody List<Group> groups,Principal principal,
                                                     @RequestParam(name = "addFlag") boolean addFlag) {
        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        if (accountOptional.isPresent()) {
            for (Group group : groups) {

                Account account = accountOptional.get();

                if(addFlag) {
                    group.setDeleted(true);
                    groupRepo.save(group);

                    List<ApplicationUser> applicationUsers = userRepo.findAllByAccountIdAndGroupAndDeleted(account.getId(), group, false);

                    for (ApplicationUser applicationUser : applicationUsers) {
                        applicationUser.setDeleted(true);
                        userRepo.save(applicationUser);
                    }
                }else{
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
