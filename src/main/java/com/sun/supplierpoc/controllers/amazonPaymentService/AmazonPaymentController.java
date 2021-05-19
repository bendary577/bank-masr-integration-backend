package com.sun.supplierpoc.controllers.amazonPaymentService;

import com.sun.supplierpoc.models.opera.TransactionRequest;
import com.sun.supplierpoc.models.opera.TransactionResponse;
import com.sun.supplierpoc.services.AmazonPaymentServices.AmazonPaymentService;
import com.sun.supplierpoc.services.AmazonPaymentServices.AmazonSendForm;
import org.apache.commons.collections4.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Random;

@RestController
@RequestMapping("/amazon")
public class AmazonPaymentController {

    @Autowired
    private AmazonPaymentService amazonPaymentService;

    @Autowired
    private AmazonSendForm amazonSendForm;

    @PostMapping(value = "/amazonPaymentService",produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public TransactionResponse amazonPayment(@RequestBody TransactionRequest transactionRequest){
        TransactionResponse transactionResponse = new TransactionResponse();
        try{
            /*
            * Payment steps
            * 1- Get signature token 2- Get user token
            * 3- Get signature auth 4- paymentApi
            * */

            // Get payment details
            if(transactionRequest.getTransType() == null || transactionRequest.getTransType().equals(""))
                return transactionResponse;

            String cardSecurityCode = "123";
            String cardNumber = "4005550000000001";
            String amount = transactionRequest.getTransAmount();
            String tokenName = "";

            // Get signature token
            String orderId =  "ORDER" + transactionRequest.getSequenceNo();

            HashedMap<String, String> obj = new HashedMap<>();
            obj.put("service_command", "TOKENIZATION");
            obj.put("access_code", "Y6lL5f0wvaKSxdM8jsjr"); //==> Configuration
            obj.put("merchant_identifier", "f0db228a"); //==> Configuration
            obj.put("language", "en");
            obj.put("merchant_reference", orderId);

            String signature = amazonPaymentService.getSignature(obj);
            amazonSendForm.amazonPaymentSendTokenization(signature);

            // Get user token
//            tokenName = amazonPaymentService.amazonPaymentSendTokenization(signature, cardNumber, cardSecurityCode, orderId);
//
//            transactionResponse =  amazonPaymentService.amazonPaymentService(transactionRequest);

            return transactionResponse;
        }catch(Exception e){
            return transactionResponse;
        }
    }

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
