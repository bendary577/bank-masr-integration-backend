package com.sun.supplierpoc.controllers;

import com.sun.supplierpoc.models.OperationData;
import com.sun.supplierpoc.repositories.OperationDataRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class OperationDataController {


    @Autowired
    private OperationDataRepo operationDataRepo;

    @GetMapping("/getOperationDataById")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public OperationData getOpertionData(@RequestParam(name = "operationId") String operationId){

        OperationData operationData = operationDataRepo.findOperationDataByOperationIdAndDeleted(operationId, false);

        return operationData;
    }

}
