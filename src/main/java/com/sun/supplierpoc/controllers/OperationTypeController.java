package com.sun.supplierpoc.controllers;

import com.sun.supplierpoc.models.OperationTypes;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.repositories.OperationTypeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.util.ArrayList;

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
    public OperationTypes getOperationTypeByName(@RequestParam String operationName, Principal principal)  {
        User user = (User)((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        return operationTypeRepo.findAllByNameAndAccountIdAndDeleted(operationName, user.getAccountId(), false);
    }
}
