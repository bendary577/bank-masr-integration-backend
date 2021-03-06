package com.sun.supplierpoc.controllers;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.models.Feature;
import com.sun.supplierpoc.models.Response;
import com.sun.supplierpoc.models.Role;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.repositories.FeatureRepository;
import com.sun.supplierpoc.repositories.RoleRepository;
import com.sun.supplierpoc.services.FeatureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/feature")
public class FeatureController {

    @Autowired
    private FeatureService featureService;

    @Autowired
    private FeatureRepository featureRepository;

    @Autowired
    private RoleRepository roleRepo;

    @PostMapping("/test/addFeature")
    @CrossOrigin(origins = "*")
    public ResponseEntity<?> addFeature(@RequestBody Feature featureRequest) {
        Response response = new Response();

        try {
            response = featureService.addFeature(featureRequest);
            if (response.isStatus()) {
                return new ResponseEntity<>(response.getData(), HttpStatus.OK);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response.getMessage());
            }
        } catch (Exception e) {
            response.setStatus(false);
            response.setMessage(e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/getFeatures")
    public ResponseEntity<?> featureRequest(Principal principal,
                                            @RequestParam(required = false, name = "accountId", defaultValue = "") String accountId){
        Response response = new Response();
        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        if(user != null){
            if(accountId == null){
                response.setMessage(Constants.INVALID_ACCOUNT);
                response.setStatus(false);
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            List<Feature> features = featureService.findAllFeature(accountId);
            response.setStatus(true);
            response.setData(features);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }else{
            response.setMessage(Constants.INVALID_USER);
            response.setStatus(false);
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }


    @PostMapping("/test/addAccountFeatures")
    public ResponseEntity<?> addAccountFeatures(@RequestParam("accountId") String accountId,
                                               @RequestParam("featuresIds") List<String> featuresIds){
        Response response ;

        response = featureService.setAccountFeatures(accountId, featuresIds);
        if(response.isStatus()){
            return new ResponseEntity<>(response.getData(), HttpStatus.OK);
        }else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response.getMessage());
        }
    }


    @RequestMapping("/test/addRolesToFeature")
    public ResponseEntity<?> addRolesToFeature(@RequestParam("featureId") String featureId
            ,@RequestParam("rolesIds") List<String> rolesIds){

        Response response = new Response();

        Optional<Feature> featureOptional =  featureRepository.findById(featureId);

        if (featureOptional.isPresent()){
            Feature feature = featureOptional.get();

            for(String roleId : rolesIds){
                Optional<Role> role = roleRepo.findById(roleId);
                if(role.isPresent()){
                    /* Check if role already exists */
                    if(!featureService.checkRoleExistence(feature.getRoles(), roleId)){
                        feature.getRoles().add(role.get());
                    }
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
