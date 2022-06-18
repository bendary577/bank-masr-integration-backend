package com.sun.supplierpoc.controllers;

import com.sun.supplierpoc.models.OperationType;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.repositories.OperationTypeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

@RestController
public class OperationTypeController {

    @Autowired
    OperationTypeRepo operationTypeRepo;

    @GetMapping("/getOperationTypes")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ArrayList<OperationType> getOperationTypes(Principal principal)  {
        User user = (User)((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        ArrayList<OperationType> operationTypes = operationTypeRepo.findAllByAccountIdAndDeletedOrderByIndexAsc(user.getAccountId(), false);
        return operationTypes;
    }

    @GetMapping("/getOperationTypeByName")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public OperationType getOperationTypeByName(@RequestParam(name = "operationName") String operationName, Principal principal)  {
        User user = (User)((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        return operationTypeRepo.findAllByNameAndAccountIdAndDeleted(operationName, user.getAccountId(), false);
    }

    @RequestMapping("/createOperationType")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public OperationType createOperationType(Principal principal)  {
        User user = (User)((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        //      OperationType operation = new OperationType(1, "Create Check", "/createCheck", new Date(), user.getAccountId());
        OperationType operation = new OperationType(1, "Simphony Payment", "/simphonyPayment", new Date(), user.getAccountId());

        operationTypeRepo.save(operation);
        return operation;
    }

    @PutMapping("/updateOperationTypeConfiguration")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public Boolean updateSyncJobTypesConfiguration(@RequestBody OperationType operationType)  {
        operationTypeRepo.save(operationType);
        return true;
    }
}
