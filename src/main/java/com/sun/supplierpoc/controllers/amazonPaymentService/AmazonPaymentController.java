package com.sun.supplierpoc.controllers.amazonPaymentService;

import com.sun.supplierpoc.models.amazonPayment.AmazonPaymentServiceBody;
import com.sun.supplierpoc.services.AmazonPaymentServices.AmazonPaymentService;
import com.sun.supplierpoc.services.AmazonPaymentServices.AmazonSendForm;
import org.apache.commons.collections4.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
@RequestMapping("/amazon")
public class AmazonPaymentController {

    @Autowired
    private AmazonPaymentService amazonPaymentService;

    @Autowired
    private AmazonSendForm amazonSendForm;

    @RequestMapping("/auth")
    public ResponseEntity authRequest(@RequestBody HashedMap obj){

        try{

            String signature = amazonPaymentService.getSignature(obj);

            amazonSendForm.amazonPaymentSendTokenization(signature);

//            amazonPaymentService.amazonPaymentService(amazonPaymentServiceBody);

            return new ResponseEntity(signature, HttpStatus.OK);

        }catch(Exception e){

        }

        return new ResponseEntity("", HttpStatus.OK);

    }

    @RequestMapping("/acceptRequest")
    public ResponseEntity acceptRequest(){

        try{

            return new ResponseEntity("", HttpStatus.OK);

        }catch(Exception e){

        }

        return new ResponseEntity("", HttpStatus.OK);

    }

}
