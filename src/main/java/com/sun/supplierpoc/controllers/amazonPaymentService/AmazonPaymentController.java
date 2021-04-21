package com.sun.supplierpoc.controllers.amazonPaymentService;

import com.sun.supplierpoc.models.amazonPayment.AmazonPaymentServiceBody;
import com.sun.supplierpoc.services.AmazonPaymentServices.AmazonPaymentService;
import com.sun.supplierpoc.services.simphony.CallRestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/amazon")
public class AmazonPaymentController {

    @Autowired
    private AmazonPaymentService amazonPaymentService;

    @RequestMapping("/auth")
    public ResponseEntity authRequest(@RequestBody AmazonPaymentServiceBody amazonPaymentServiceBody){

        try{
            amazonPaymentService.amazonPaymentService(amazonPaymentServiceBody);

        }catch(Exception e){

        }

        return new ResponseEntity("", HttpStatus.OK);

    }
}
