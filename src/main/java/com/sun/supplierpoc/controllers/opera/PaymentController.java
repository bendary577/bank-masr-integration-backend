package com.sun.supplierpoc.controllers.opera;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.sun.supplierpoc.models.opera.TransactionObject;
import com.sun.supplierpoc.models.opera.TransactionObjectResponse;
import com.sun.supplierpoc.models.opera.TransactionRequest;
import com.sun.supplierpoc.models.opera.TransactionResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Random;

@RestController
public class PaymentController {
    @Autowired
    private RestTemplate restTemplate;

    private final String PREAUTHORIZATION = "1";
    private final String PAYMENT = "2";
    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);
    @Bean
    public MappingJackson2XmlHttpMessageConverter mappingJackson2XmlHttpMessageConverter(
            Jackson2ObjectMapperBuilder builder) {
        ObjectMapper mapper = builder.createXmlMapper(true).build();
        ((XmlMapper) mapper).enable(ToXmlGenerator.Feature.WRITE_XML_DECLARATION);
        return new MappingJackson2XmlHttpMessageConverter(mapper);
    }

    @PostMapping(value = "/opera/operaPayment",produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public TransactionResponse operaPayment(@RequestBody TransactionRequest transactionRequest) {

        logger.info("TransactionType start : " + transactionRequest);
        logger.info("TransactionType : " + transactionRequest.getTransType());

        TransactionResponse transactionResponse = new TransactionResponse();
        if(transactionRequest.getTransType() == null || transactionRequest.getTransType().equals(""))
            return transactionResponse;

        switch (transactionRequest.getTransType()){
            case "23": transactionResponse=  getToken(transactionRequest);
                break;
            case "05":transactionResponse=  auth(transactionRequest);
                break;
            case "02":transactionResponse=  paymentTransaction(transactionRequest);
                break;

        }
        return transactionResponse;
    }

    private TransactionResponse getToken(TransactionRequest transactionRequest){
        logger.info("SequenceNo : "+transactionRequest.getSequenceNo());
        logger.info("TransactionType : "+transactionRequest.getTransType());
        logger.info("SiteId : "+transactionRequest.getSiteId());
        logger.info("WSNo : "+transactionRequest.getTransType());
        logger.info("TransDateTime : "+transactionRequest.getTransDateTime());
        logger.info("IndustryCode : "+transactionRequest.getIndustryCode());
        logger.info("ProxyInfo : "+transactionRequest.getProxyInfo());
        logger.info("POSInfo : "+transactionRequest.getPOSInfo());

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

    private TransactionResponse auth(TransactionRequest transactionRequest){
        final String uri = "http://localhost:9090/paymentPreauthorization";
        logger.info("SequenceNo : "+transactionRequest.getSequenceNo());
        logger.info("TransactionType : "+transactionRequest.getTransType());
        logger.info("TransAmount : "+transactionRequest.getTransAmount());
        TransactionResponse transactionResponse = new TransactionResponse();
        transactionResponse.setSequenceNo(transactionRequest.getSequenceNo());
        transactionResponse.setTransType(transactionRequest.getTransType());
        transactionResponse.setTransAmount(transactionRequest.getTransAmount());

        Random random = new Random(100);

        String firstTwoDigits= String.valueOf(random.nextInt(100));
        String secondTwoDigits= String.valueOf((LocalDateTime.now().getMinute()+10));
        String thirdTwoDigits= String.valueOf((LocalDateTime.now().getSecond()+10));
        String uniqueEcr=firstTwoDigits.concat(secondTwoDigits).concat(thirdTwoDigits);
        TransactionObject transactionObject = new TransactionObject() ;
        transactionObject.setAmount(transactionRequest.getTransAmount());
        transactionObject.setEcr(uniqueEcr);
        transactionObject.setPayKind(PREAUTHORIZATION);
        transactionObject.setCachierID("1");
        TransactionObjectResponse result = new TransactionObjectResponse();
        try {
            result = restTemplate.postForObject(uri, transactionObject, TransactionObjectResponse.class);
        }catch (Exception e ){
            logger.info("Payment Pre-authorization Error: " + e.getMessage());
        }

        if(result!=null) {
            if(result.getCardLastDigits()==null || result.getCardLastDigits().equals("")){
                logger.info("Payment Pre-authorization Failed!");
                result.cardLastDigits = "xxxxxxxxxxxxxxxx";
            }
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
        }
        return transactionResponse;
    }

    private TransactionResponse paymentTransaction(TransactionRequest transactionRequest){
        TransactionResponse transactionResponse = new TransactionResponse();
        transactionResponse.setSequenceNo(transactionRequest.getSequenceNo());
        transactionResponse.setTransType(transactionRequest.getTransType());
        transactionResponse.setTransAmount(transactionRequest.getTransAmount());

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
        transactionResponse.setdCCIndicator("0");
        transactionResponse.setTerminalId("1");
        return transactionResponse;
    }

    @GetMapping(value = "/",produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public TransactionResponse getController() {
        logger.info("Allah is the greatest");
        TransactionResponse transactionResponse = new TransactionResponse();
        transactionResponse.setRespCode("00");
        transactionResponse.setRespText("APPROVAL");
        transactionResponse.setpAN("4918718552886251");
        transactionResponse.setExpiryDate("1123");
        transactionResponse.setTransToken("4918718552886251");
        transactionResponse.setEntryMode("01");
        transactionResponse.setIssuerId("02");
        transactionResponse.setIssuerId("000000003363");
        transactionResponse.setOfflineFlag("N");
        transactionResponse.setMerchantId("TEST");
        transactionResponse.setTerminalId("TEST");
        return transactionResponse;
    }

}
