package com.sun.supplierpoc.controllers;

import com.sun.supplierpoc.models.Operation;
import com.sun.supplierpoc.models.OperationType;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.repositories.OperationRepo;
import com.sun.supplierpoc.repositories.OperationTypeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
public class OperationController {

    @Autowired
    private OperationTypeRepo operationTypeRepo;

    @Autowired
    private OperationRepo operationRepo;

    @GetMapping("/getOperation")
    @CrossOrigin(origins = "*")
    public List<Operation> getOperation(@RequestParam(name = "typeName") String syncJobTypeId, Principal principal){
        User user = (User)((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        OperationType operationType =  operationTypeRepo.findAllByNameAndAccountIdAndDeleted(syncJobTypeId, user.getAccountId(), false);
        return operationRepo.findByoperationTypeIdAndDeletedOrderByCreationDateDesc(operationType.getId(), false);
    }

}
