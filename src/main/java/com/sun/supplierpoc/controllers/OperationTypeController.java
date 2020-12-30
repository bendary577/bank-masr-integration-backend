package com.sun.supplierpoc.controllers;

import com.sun.supplierpoc.models.OperationTypes;
import com.sun.supplierpoc.models.SyncJobType;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.repositories.OperationTypeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;

@RestController
public class OperationTypeController {
    @Autowired
    OperationTypeRepo operationTypeRepo;

    @GetMapping("/getOperationTypes")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ArrayList<OperationTypes> getOperationTypes(Principal principal)  {
        User user = (User)((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        return operationTypeRepo.findAllByAccountIdAndDeletedOrderByIndexAsc(user.getAccountId(), false);
    }

    @GetMapping("/getOperationTypeByName")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public OperationTypes getOperationTypeByName(@RequestParam(name = "operationName") String operationName, Principal principal)  {
        User user = (User)((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        return operationTypeRepo.findAllByNameAndAccountIdAndDeleted(operationName, user.getAccountId(), false);
    }

    @RequestMapping("/createOperationType")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public OperationTypes createOperationType(Principal principal)  {
        User user = (User)((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        OperationTypes operation = new OperationTypes(1, "Create Check", "/createCheck", new Date(), user.getAccountId());
        operationTypeRepo.save(operation);
        return operation;
    }

    @PutMapping("/updateOperationTypeConfiguration")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public Boolean updateSyncJobTypesConfiguration(@RequestBody OperationTypes operationTypes)  {
        operationTypeRepo.save(operationTypes);
        return true;
    }
}
