package com.sun.supplierpoc.services;

import com.sun.supplierpoc.models.SmsPojo;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;


@Component
public class SmsService {


    private final String ACCOUNT_SID = "ACb7d89db638622c781e8508fa1c92b06f";
    private final String AUTH_TOKEN = "ff0ce2661910dd61a70a98fad0672846";
    private final String FROM_NUMBER = "+17205006305";
    public void send(SmsPojo sms) {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

        Message message = Message.creator(new PhoneNumber(sms.getTo()), new PhoneNumber(FROM_NUMBER), sms.getMessage())
                .create();
        System.out.println("here is my id:" + message.getSid());// Unique resource ID created to manage this transaction

    }

    public void receive(MultiValueMap<String, String> smscallback) {
    }

}