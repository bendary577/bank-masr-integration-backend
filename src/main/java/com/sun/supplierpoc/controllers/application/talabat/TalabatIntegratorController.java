package com.sun.supplierpoc.controllers.application.talabat;

import com.sun.supplierpoc.models.Response;

import com.sun.supplierpoc.models.talabat.TalabatRest.RestOrder;
import com.sun.supplierpoc.models.talabat.TalabatRest.TalabatOrder;
import com.sun.supplierpoc.models.talabat.login.Token;
import com.sun.supplierpoc.services.TalabatIntegratorService;
import com.sun.supplierpoc.services.restTemplate.TalabatRestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/talabat")
public class TalabatIntegratorController {

    @Autowired
    private TalabatRestService talabatRestService;

    @Autowired
    private TalabatIntegratorService talabatIntegratorService;

    @GetMapping
    public ResponseEntity<?> login(){

        Response response = talabatRestService.loginRequest();

        response = talabatRestService.getOrders((Token) response.getData());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("branch")
    public ResponseEntity<?> getBranchOrders(@RequestParam("branch") String branch){

        Response response = talabatRestService.loginRequest();

        response = talabatRestService.getOrders((Token) response.getData(), branch);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/orders")
    public ResponseEntity<?> getOrders(@RequestBody Token token){

        Response response = talabatRestService.getOrders(token);

        talabatIntegratorService.sendReceivedOrders( (TalabatOrder) response.getData() );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/order")
    public ResponseEntity<?> getOrderById(@RequestBody RestOrder order){

        Response response = talabatRestService.loginRequest();

        response = talabatRestService.getOrderById(order, (Token) response.getData());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
