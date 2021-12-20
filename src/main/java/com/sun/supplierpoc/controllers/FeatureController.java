package com.sun.supplierpoc.controllers;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.Feature;
import com.sun.supplierpoc.models.Response;
import com.sun.supplierpoc.models.Role;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.repositories.FeatureRepository;
import com.sun.supplierpoc.repositories.RoleRepository;
import com.sun.supplierpoc.services.AccountService;
import com.sun.supplierpoc.services.FeatureService;
import com.sun.supplierpoc.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(("/feature"))
public class FeatureController {

    @Autowired
    private FeatureService featureService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private UserService userService;

    @Autowired
    private FeatureRepository featureRepository;

    @Autowired
    private RoleRepository roleRepo;

    //, Principal principal
    @PostMapping("/addFeature")
    @CrossOrigin(origins = "*")
    public ResponseEntity<?> addFeature(@RequestBody Feature featureRequest) {

        Response response = new Response();
//        try {
//            User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
//
//            if(userService.eligibleForRole(user, Constants.ADD_FEATURE)) {
//                Account account = accountService.getAccount(user.getAccountId());
//                if (account != null) {
                    try {
                        Feature feature = featureService.save(featureRequest);
                        response.setData(feature);
                        response.setStatus(true);
                        return new ResponseEntity<>(response, HttpStatus.OK);
                    } catch (Exception e) {
                        response.setStatus(false);
                        response.setMessage(e.getMessage());
                        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                    }
//                } else {
//                    response.setStatus(false);
//                    response.setMessage(Constants.ACCOUNT_NOT_EXIST);
//                    return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
//                }
//            }else{
//                response.setStatus(false);
//                response.setMessage(Constants.NOT_ELIGIBLE_USER);
//                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
//            }
//        } catch (Exception e) {
//            response.setStatus(false);
//            response.setMessage(e.getMessage());
//            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
//        }
    }

    //, Principal principal
    @GetMapping("/getFeatures")
    public ResponseEntity<?> featureRequest(@RequestParam(required = false, name = "accountId", defaultValue = "") String accountId){
        Response response = new Response();
//        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
//        if(user != null){
            List<Feature> features = featureService.findAllFeature(accountId);
            response.setStatus(true);
            response.setData(features);
            return new ResponseEntity<>(response, HttpStatus.OK);
//        }else{
//            response.setMessage(Constants.INVALID_USER);
//            response.setStatus(false);
//            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
//        }
    }
    //,Principal principal
    @PostMapping("addAccountFeatures")
    public ResponseEntity<?> addAccountFeatures(@RequestParam("accountId") String accountId,
                                               @RequestParam("featuresIds") List<String> featuresIds){
        Response response = new Response();
//        User authedUser = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
//        if(authedUser != null){
//            if(userService.eligibleForRole(authedUser, Constants.ADD_USER_FEATURE)){
                response = featureService.setAccountFeatures(accountId, featuresIds);
                if(response.isStatus()){
                    return new ResponseEntity<>(response, HttpStatus.OK);
                }else{
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                }
//            }else{
//                response.setStatus(false);
//                response.setMessage(Constants.NOT_ELIGIBLE_USER);
//                return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
//            }
//
//        }else{
//            response.setMessage(Constants.INVALID_USER);
//            response.setStatus(false);
//            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
//        }
    }


    @RequestMapping("/addRolesToFeature")
    public ResponseEntity<?> addRolesToFeature(@RequestPart("featureId") String featureId
            ,@RequestPart("rolesIds") List<String> rolesIds){

        Response response = new Response();

        Optional<Feature> featureOptional =  featureRepository.findById(featureId);

        if (featureOptional.isPresent()){
            Feature feature = featureOptional.get();

            for(String roleId : rolesIds){
                Optional<Role> role = roleRepo.findById(roleId);
                if(role.isPresent()){
                    feature.getRoles().add(role.get());
                }else{
                    response.setStatus(false);
                    response.setMessage("There isn't role with id " + roleId);
                }
            }

            featureRepository.save(feature);
            response.setStatus(true);
            response.setMessage("Roles have been added to the feature successfully.");
        }else{
            response.setStatus(false);
            response.setMessage("There isn't feature with id " + featureId);
        }

        if(response.isStatus()){
            return new ResponseEntity<>(response, HttpStatus.OK);
        }else{
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }
}
