package com.sun.supplierpoc.controllers;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.models.SyncJobType;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.repositories.SyncJobTypeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@RestController


public class SyncJobTypeController {

    @Autowired
    private SyncJobTypeRepo syncJobTypeRepo;

    @GetMapping("/getAccSyncJobTypesByName")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public SyncJobType getSyncJobType(@RequestParam(name = "typeName") String syncJobTypeName, Principal principal)  {
        User user = (User)((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        return  syncJobTypeRepo.findByNameAndAccountId(syncJobTypeName, user.getAccountId());
    }

    @GetMapping("/getSyncJobTypes")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ArrayList<SyncJobType> getSyncJobTypesRequest(Principal principal)  {
        return getSyncJobTypes(principal);
    }


    public ArrayList<SyncJobType> getSyncJobTypes(Principal principal)  {
        User user = (User)((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        return (ArrayList<SyncJobType>) syncJobTypeRepo.findByAccountId(user.getAccountId());
    }


    @PutMapping("/updateSyncJobTypesConfiguration")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public Boolean updateSyncJobTypesConfiguration(@RequestBody SyncJobType syncJobType)  {
        syncJobTypeRepo.save(syncJobType);
        return true;
    }

}
