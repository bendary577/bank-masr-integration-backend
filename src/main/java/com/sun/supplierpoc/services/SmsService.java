package com.sun.supplierpoc.services;

import com.sun.supplierpoc.models.SmsPojo;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;


@Component
public class SmsService {


    private final String ACCOUNT_SID = "AC98b64111bdcbcee095624d78384beb32";
    private final String AUTH_TOKEN = "3c0ea851a1dc1c45df6cb30a72eb49ae";
    private final String FROM_NUMBER = "+19412542261";
    public void send(SmsPojo sms) {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

        Message message = Message.creator(new PhoneNumber(sms.getTo()), new PhoneNumber(FROM_NUMBER), sms.getMessage())
                .create();
        System.out.println("here is my id:" + message.getSid());// Unique resource ID created to manage this transaction

    }

    public void receive(MultiValueMap<String, String> smscallback) {
    }

}