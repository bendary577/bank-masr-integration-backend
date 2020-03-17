package com.sun.supplierpoc.controllers;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.models.SyncJobType;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.models.configurations.Analysis;
import com.sun.supplierpoc.repositories.SyncJobTypeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@RestController
// @RequestMapping(path = "server")


public class SyncJobTypeController {

    @Autowired
    private SyncJobTypeRepo syncJobTypeRepo;

    @GetMapping("/getAccSyncJobTypesByName")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public SyncJobType getSyncJobType(@RequestParam(name = "typeName") String syncJobTypeName, Principal principal)  {
        User user = (User)((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        SyncJobType syncJobType =  syncJobTypeRepo.findByNameAndAccountId(syncJobTypeName, user.getAccountId());
        if (syncJobType.getConfiguration().getAnalysis().size() == 0){
            ArrayList<Analysis> analysis = new ArrayList<>();
            analysis.add(new Analysis(false, "1", "", ""));
            analysis.add(new Analysis(false, "2", "", ""));
            analysis.add(new Analysis(false, "3", "", ""));
            analysis.add(new Analysis(false, "4", "", ""));
            analysis.add(new Analysis(false, "5", "", ""));
            analysis.add(new Analysis(false, "6", "", ""));
            analysis.add(new Analysis(false, "7", "", ""));
            analysis.add(new Analysis(false, "8", "", ""));
            analysis.add(new Analysis(false, "9", "", ""));
            analysis.add(new Analysis(false, "10", "", ""));
            syncJobType.getConfiguration().setAnalysis(analysis);
        }

        return syncJobType;
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
