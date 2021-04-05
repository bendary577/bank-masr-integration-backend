package com.sun.supplierpoc.controllers.application;

import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.applications.Group;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.repositories.AccountRepo;
import com.sun.supplierpoc.repositories.applications.GroupRepo;
import com.sun.supplierpoc.services.AppGroupService;
import com.sun.supplierpoc.services.ImageService;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
public class GroupController {

    @Autowired
    AccountRepo accountRepo;
    @Autowired
    GroupRepo groupRepo;
    @Autowired
    private AppGroupService appGroupService;
    @Autowired
    private HttpServletRequest request;

    @Autowired
    private ImageService imageService;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @RequestMapping("/getAllApplicationGroups")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity getAllApplicationCompanies(Principal principal) {

        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        if (accountOptional.isPresent()) {

            Account account = accountOptional.get();
            ArrayList<Group> groups = groupRepo.findAllByAccountID(account.getId());

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
                Optional<Group> groupOptional = groupRepo.findById(parentId);
                if (groupOptional.isPresent()) {
                    Group group = groupOptional.get();
                    groups = groupRepo.findAllByAccountIDAndParentGroup(account.getId(), group);

                } else {
                    return new ResponseEntity(HttpStatus.BAD_REQUEST);
                }
            } else {
                groups = groupRepo.findAllByAccountIDAndParentGroup(account.getId(), null);
            }

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

                group.setAccountID(account.getId());
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

        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();

            Optional<Group> groupOptional = groupRepo.findById(groupId);


            if (groupOptional.isPresent()) {

                Group group = groupOptional.get();

                imageService.store(image);

                String filePath = "F:\\oracle-hospitality-frontend\\src\\assets\\" + group.getName() + ".jpg";

                try {
                    image.transferTo(new File(filePath));
                } catch (IOException e) {}

                group.setLogoUrl("../../../assets/" + group.getName() + ".jpg");
                groupRepo.save(group);

            }
            return ResponseEntity.status(HttpStatus.OK).body(new Group());
        }
        return new ResponseEntity(HttpStatus.FORBIDDEN);
    }

//    @RequestMapping("/addApplicationGroupImage")
//    @CrossOrigin(origins = "*")
//    @ResponseBody
//    public ResponseEntity addApplicationGroupImage(@RequestPart(name = "groupId", required = false) String groupId,
//                                                   @RequestPart("groupName") String groupName,
//                                                   @RequestPart("description") String description,
//                                                   @RequestPart("discountRate") float discountRate,
//                                                   @RequestPart("discountId") String discountId,
//                                                   @RequestPart("parentGroupId") String parentGroupId,
//                                                   @RequestPart(name = "image", required = false) MultipartFile image,
//                                                   Principal principal){
//
//        User user = (User)((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
//        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
//        if (accountOptional.isPresent()) {
//            Account account = accountOptional.get();
//
//            String filePath = "D:\\1.Bassel\\simphony\\infor-sun-poc\\src\\main\\resources\\"+groupName+".jpg";
//            try {
//                image.transferTo(new File(filePath));
//            } catch (IOException e) {
//            }
//
//            Group group = new Group();
//            Optional<Group> parentGroupOptional = groupRepo.findById(parentGroupId);
//            Group parentGroup = new Group();
//
//            if(group.getParentGroup() != null) {
//
//                if (groupId.equals(null)) {
//
//
//                    if (parentGroupOptional.isPresent()) {
//                        parentGroup = parentGroupOptional.get();
//                        if (group.getParentGroup().getParentGroup() != null) {
//                            return new ResponseEntity("Parent group is already child for another group," +
//                                    "\n Please select valid parent group.",
//                                    HttpStatus.BAD_REQUEST);
//                        }
//                    }
//                    group.setName(groupName);
//                    group.setDescription(description);
//                    group.setDiscountRate(discountRate);
//                    group.setAccountID(discountId);
//                    group.setParentGroup(parentGroup);
//                    group.setAccountID(account.getId());
//                    group.setLogoUrl(filePath);
//                    group.setCreationDate(new Date());
//                    group.setLastUpdate(new Date());
//                    group.setDeleted(false);
//                } else {
//                    group = groupRepo.findById(groupId).get();
//                    if (parentGroupOptional.isPresent()) {
//                        parentGroup = parentGroupOptional.get();
//                        if (group.getParentGroup().getParentGroup() != null) {
//                            return new ResponseEntity("Parent group is already child for another group," +
//                                    "\n Please select valid parent group.",
//                                    HttpStatus.BAD_REQUEST);
//                        }
//                    }
//                    group.setName(groupName);
//                    group.setDescription(description);
//                    group.setDiscountRate(discountRate);
//                    group.setAccountID(discountId);
//                    group.setParentGroup(parentGroup);
//                    group.setAccountID(account.getId());
//                    group.setLastUpdate(new Date());
//
//                }
//            }
//
//            groupRepo.save(new Group());
//            return ResponseEntity.status(HttpStatus.OK).body(new Group());
//        }
//        return new ResponseEntity(HttpStatus.FORBIDDEN);
//    }

    @RequestMapping("/deleteApplicationGroups")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity deleteApplicationCompanies(@RequestBody List<Group> groups, Principal principal) {
        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        if (accountOptional.isPresent()) {
            for (Group group : groups) {
                group.setDeleted(true);
                groupRepo.save(group);
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

            List<Group> groups = appGroupService.getTopGroups();

            return groups;
        } else {
            return new ArrayList<>();
        }
    }
}
