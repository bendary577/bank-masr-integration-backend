package com.sun.supplierpoc.controllers.amazonPaymentService;

import com.sun.supplierpoc.models.amazonPayment.AmazonPaymentServiceBody;
import com.sun.supplierpoc.services.AmazonPaymentServices.AmazonPaymentService;
import com.sun.supplierpoc.services.AmazonPaymentServices.AmazonSendForm;
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

    @Autowired
    private AmazonSendForm amazonSendForm;

    @RequestMapping("/auth")
    public ResponseEntity authRequest(@RequestBody Object obj){

        try{

            String signature = amazonPaymentService.getSignature(obj);

            amazonSendForm.amazonPaymentSendTokenization(signature);

//            amazonPaymentService.amazonPaymentService(amazonPaymentServiceBody);

        }catch(Exception e){

        }

        return new ResponseEntity("", HttpStatus.OK);

    }

}
