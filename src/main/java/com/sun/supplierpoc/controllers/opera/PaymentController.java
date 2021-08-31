package com.sun.supplierpoc.controllers.opera;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.sun.supplierpoc.models.opera.*;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Random;

@RestController
public class PaymentController {

    private final String PREAUTHORIZATION = "1";
    private final String PAYMENT = "2";
    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    private final String POS_MACHINE_URL = "http://192.168.1.11:8080";
    private final String MIDDLEWARE_URL = "http://192.168.1.121:8081";

    @Bean
    public MappingJackson2XmlHttpMessageConverter mappingJackson2XmlHttpMessageConverter(
            Jackson2ObjectMapperBuilder builder) {
        ObjectMapper mapper = builder.createXmlMapper(true).build();
        ((XmlMapper) mapper).enable(ToXmlGenerator.Feature.WRITE_XML_DECLARATION);
        return new MappingJackson2XmlHttpMessageConverter(mapper);
    }

    @Autowired
    private RestTemplate restTemplate;

    @PostMapping(value = "/paymentTest", produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public TransactionResponse postController(
            @RequestBody TransactionRequest transactionRequest) {


//            logger.info("Allah is the greatest");
//            logger.info("TransactionType start : "+transactionRequest);
//            logger.info("TransactionType : "+transactionRequest.getTransType());
        logger.info("1=transactionRequest=> : " + transactionRequest);

        TransactionResponse transactionResponse = null;
//        switch (transactionRequest.getTransType()){
//            case "23": transactionResponse=  getToken(transactionRequest);
//                break;
//            case "05":transactionResponse=  auth(transactionRequest);
//                break;
//            case "07":transactionResponse=  transaction(transactionRequest);
//                break;
//
//        }
        transactionResponse = paymentTransaction(transactionRequest, transactionRequest.getTransType());
        logger.info("2=transactionResponse=> : " + transactionResponse);
        return transactionResponse;
    }

    private TransactionResponse getToken(TransactionRequest transactionRequest) {
        logger.info("SequenceNo : " + transactionRequest.getSequenceNo());
        logger.info("TransactionType : " + transactionRequest.getTransType());
        logger.info("SiteId : " + transactionRequest.getSiteId());
        logger.info("WSNo : " + transactionRequest.getTransType());
        logger.info("TransDateTime : " + transactionRequest.getTransDateTime());
        logger.info("IndustryCode : " + transactionRequest.getIndustryCode());
        logger.info("ProxyInfo : " + transactionRequest.getProxyInfo());
        logger.info("POSInfo : " + transactionRequest.getPOSInfo());

        TransactionResponse transactionResponse = new TransactionResponse();
        transactionResponse.setSequenceNo(transactionRequest.getSequenceNo());
        transactionResponse.setTransType(transactionRequest.getTransType());
        transactionResponse.setRespCode("00");
        transactionResponse.setRespText("APPROVAL");
        transactionResponse.setpAN("XXXXXXXXXXXXXX2811");
        transactionResponse.setExpiryDate("2212");
        transactionResponse.setTransToken("131111111111112811");
        transactionResponse.setEntryMode("01");
        transactionResponse.setIssuerId("01");
        transactionResponse.setrRN("000000000311");

        transactionResponse.setOfflineFlag("N");
        transactionResponse.setMerchantId("1");
        transactionResponse.setTerminalId("1");
        return transactionResponse;
    }

    private TransactionResponse auth(TransactionRequest transactionRequest) {
        final String uri = "http://localhost:9090/paymentPreauthorization";
        logger.info("SequenceNo : " + transactionRequest.getSequenceNo());
        logger.info("TransactionType : " + transactionRequest.getTransType());
        logger.info("TransAmount : " + transactionRequest.getTransAmount());
        TransactionResponse transactionResponse = new TransactionResponse();
        transactionResponse.setSequenceNo(transactionRequest.getSequenceNo());
        transactionResponse.setTransType(transactionRequest.getTransType());
        transactionResponse.setTransAmount(transactionRequest.getTransAmount());

        Random random = new Random(100);

        String firstTwoDigits = String.valueOf(random.nextInt(100));
        String secondTwoDigits = String.valueOf((LocalDateTime.now().getMinute() + 10));
        String thirdTwoDigits = String.valueOf((LocalDateTime.now().getSecond() + 10));
        String uniqueEcr = firstTwoDigits.concat(secondTwoDigits).concat(thirdTwoDigits);
        TransactionObject transactionObject = new TransactionObject();
        transactionObject.setAmount(transactionRequest.getTransAmount());
        transactionObject.setEcr(uniqueEcr);
        transactionObject.setPayKind(PREAUTHORIZATION);
        transactionObject.setCachierID("1");
        TransactionObjectResponse result = null;
        try {
            result = restTemplate.postForObject(uri, transactionObject, TransactionObjectResponse.class);
        } catch (Exception e) {
            //            e.printStackTrace();
            logger.info("Payment Pre-authorization Error: " + e.getMessage());
        }
        if (result != null && result.getCardLastDigits() != null) {
            String cardLastDigits = result.cardLastDigits.replaceAll("x", "");

            String uniqueString = firstTwoDigits.concat(cardLastDigits);

            String uniqueToken = StringUtils.leftPad(uniqueEcr.concat(cardLastDigits), 18, '0');

            String uniqueValue = StringUtils.leftPad(uniqueString, 12, '0');

            transactionResponse.setRespCode("00");
            transactionResponse.setRespText("APPROVAL");
            transactionResponse.setpAN(result.cardLastDigits);
            transactionResponse.setExpiryDate("2212");
            transactionResponse.setTransToken(uniqueToken);
            transactionResponse.setEntryMode("01");
            transactionResponse.setIssuerId("01");
            transactionResponse.setrRN(uniqueValue);
            // transactionResponse.setBalance("100000000");
            transactionResponse.setOfflineFlag("N");
            transactionResponse.setMerchantId("1");
            transactionResponse.setdCCIndicator("0");
            transactionResponse.setTerminalId("1");
            transactionResponse.setAuthCode(uniqueValue);
            transactionResponse.setPrintData("Data");
            return transactionResponse;
        }
        return null;
    }

    private TransactionResponse paymentTransaction(TransactionRequest transactionRequest, String TransType) {
        TransactionResponse transactionResponse = new TransactionResponse();
        transactionResponse.setSequenceNo(transactionRequest.getSequenceNo());
        transactionResponse.setTransType(transactionRequest.getTransType());
        transactionResponse.setTransAmount(transactionRequest.getTransAmount());

        ResponseEntity<String> result = null;

        String currency = "";
        float amount = Float.parseFloat(transactionRequest.getTransAmount());

        if(transactionRequest.getTransCurrency().equals("818"))
            currency = "AED";
        else
            currency = "EGP";

        try {
            result = restTemplate.getForEntity(POS_MACHINE_URL + "?transactionAmount=" +
                    Math.round(amount) + "&currency=" + currency + "&transType=" + TransType, String.class);
        }catch (Exception e ){
            e.printStackTrace();
            logger.error(String.join("Failed in transaction method with ",e.getMessage()));
        }

        if(result != null && result.getBody() != null) {
            // Parse POS machine result
            final String[] values = result.getBody().split(",");

            // Save transaction in middleware
            OperaTransaction operaTransaction = new OperaTransaction();

            if(values[0].equalsIgnoreCase("Success")){
                String cardNumber = values[0];

                operaTransaction.setStatus("Success");
                operaTransaction.setCardNumber(cardNumber.substring(0, 4) +
                        "XXXXXXXXXX" + cardNumber.substring(cardNumber.length() - 4));

                transactionResponse.setRespCode("00");
                transactionResponse.setRespText("APPROVAL");
                transactionResponse.setpAN("XXXXXXXXXXXXXX" + cardNumber.substring(cardNumber.length() - 4));
                transactionResponse.setExpiryDate(values[2]); // 2509
                transactionResponse.setTransToken(cardNumber); // 5078031089641006
                transactionResponse.setEntryMode("01");
                transactionResponse.setIssuerId("01");
                transactionResponse.setrRN("000000000311");
                transactionResponse.setOfflineFlag("N");
                transactionResponse.setdCCIndicator("0");
                transactionResponse.setMerchantId("1");
                transactionResponse.setTerminalId("1");
                transactionResponse.setPrintData("Bank Misr");
            }
            else{
                operaTransaction.setStatus("Failed");
                operaTransaction.setReason(values[4]);
                operaTransaction.setCardNumber("XXXXXXXXXXXXXXXXXX");

                transactionResponse.setRespCode("21"); // No Action Taken
                transactionResponse.setRespText("No Action Taken");
                transactionResponse.setpAN("XXXXXXXXXXXXXX0000");
                transactionResponse.setExpiryDate("2509");
                transactionResponse.setTransToken("0000000000000000");
                transactionResponse.setEntryMode("01");
                transactionResponse.setIssuerId("01");
                transactionResponse.setrRN("000000000311");
                transactionResponse.setOfflineFlag("N");
                transactionResponse.setMerchantId("1");
                transactionResponse.setdCCIndicator("0");
                transactionResponse.setTerminalId("1");
                transactionResponse.setPrintData("Bank Misr");
            }

            operaTransaction.setAmount(amount / 100);
            operaTransaction.setCurrency("USD");
            operaTransaction.setDeleted(false);
            operaTransaction.setCreationDate(new Date());

//            try {
//                String url = MIDDLEWARE_URL + "/opera/createOperaTransaction";
//                boolean saveStatus = restTemplate.postForObject(url, operaTransaction, Boolean.class);
//                System.out.println(saveStatus);
//            }catch (Exception e ){
//                e.printStackTrace();
//                logger.error(String.join("Failed to save transactions in middleware. ",e.getMessage()));
//            }

            return transactionResponse;
        }
        else{
            transactionResponse.setRespCode("21"); // No Action Taken
            transactionResponse.setRespText("No Action Taken");
            transactionResponse.setpAN("XXXXXXXXXXXXXX0000");
            transactionResponse.setExpiryDate("2509");
            transactionResponse.setTransToken("0000000000000000");
            transactionResponse.setEntryMode("01");
            transactionResponse.setIssuerId("01");
            transactionResponse.setrRN("000000000311");
            transactionResponse.setOfflineFlag("N");
            transactionResponse.setMerchantId("1");
            transactionResponse.setdCCIndicator("0");
            transactionResponse.setTerminalId("1");
            transactionResponse.setPrintData("Bank Misr");

            return transactionResponse;
        }

    }

    HttpHeaders createHeaders(String username, String password){
        return new HttpHeaders() {{
            String auth = username + ":" + password;
            byte[] encodedAuth = Base64.encodeBase64(
                    auth.getBytes(Charset.forName("US-ASCII")) );
            String authHeader = "Basic " + new String( encodedAuth );
            set( "Authorization", authHeader );
        }};
    }

//        try {
//            Thread.sleep(4 * 1000);
//        } catch (InterruptedException ie) {
//            Thread.currentThread().interrupt();
//        }
}
