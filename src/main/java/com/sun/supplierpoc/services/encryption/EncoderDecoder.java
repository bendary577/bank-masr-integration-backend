package com.sun.supplierpoc.services.encryption;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.Random;

@Service
public class EncoderDecoder {

    public String encode(String toBeEncoded) {

        // Encode into Base64 format
        String BasicBase64format = Base64.getEncoder().encodeToString(toBeEncoded.getBytes());

        String secondBase46Format = Base64.getEncoder().encodeToString(BasicBase64format.getBytes());

//        int BasicBase64formatLength = BasicBase64format.length();
//
//        if(BasicBase64formatLength % 2 == 0){
//
//            BasicBase64format = getRandom() + BasicBase64format.substring(0, BasicBase64formatLength/2) + getRandom() +
//                    BasicBase64format.substring(BasicBase64formatLength/2) + getRandom();
//
//        }else if(BasicBase64formatLength % 2 != 0){
//
//            BasicBase64format = getRandom() + BasicBase64format.substring(0, (BasicBase64formatLength - 1)/2) + getRandom() +
//                    BasicBase64format.substring( (BasicBase64formatLength - 1) / 2 ) + getRandom();
//        }

        return secondBase46Format;

    }

    public String decode(String toBeDecoded) {

//        int toBeDecodedLength =  toBeDecoded.length();
//
//
//        if(toBeDecodedLength % 2 == 0){
//
//            toBeDecoded = toBeDecoded.substring(2, toBeDecodedLength - 2);
//
//            toBeDecodedLength = toBeDecodedLength - 4 ;
//
//            toBeDecoded = toBeDecoded.substring(0 , ( (toBeDecodedLength/2)  - 1) ) + toBeDecoded.substring(( (toBeDecodedLength/2)  + 1) );
//
//        }

        // Encode into Base64 format
        byte[] firstByte = Base64.getDecoder().decode(toBeDecoded);

        String firstString = new String(firstByte);

        byte[] actualByte = Base64.getDecoder().decode(firstString);

        String actualString = new String(actualByte);

        return actualString;

    }

    //...
    public String getRandom(){
        Random r = new Random();
        String alphabet = "7HI8JKLM9A1B2C3D4E5F6Gabcdef1ghijklmnopqrstuvwxyzNOPQRSTUVWXYZ";
        String alphabet1 = "";
        for (int i = 0; i < 2; i++) {
            alphabet1 +=  alphabet.charAt(r.nextInt(alphabet.length()));
        }
        return alphabet1;
    }

}
