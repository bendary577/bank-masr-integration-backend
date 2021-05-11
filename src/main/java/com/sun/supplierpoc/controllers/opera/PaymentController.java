package com.sun.supplierpoc.controllers.opera;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.OperationType;
import com.sun.supplierpoc.models.auth.InvokerUser;
import com.sun.supplierpoc.models.opera.TransactionObject;
import com.sun.supplierpoc.models.opera.TransactionObjectResponse;
import com.sun.supplierpoc.models.opera.TransactionRequest;
import com.sun.supplierpoc.models.opera.TransactionResponse;
import com.sun.supplierpoc.repositories.AccountRepo;
import com.sun.supplierpoc.repositories.OperationTypeRepo;
import com.sun.supplierpoc.services.AccountService;
import com.sun.supplierpoc.services.InvokerUserService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@RestController
public class PaymentController {
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private AccountRepo accountRepo;
    @Autowired
    private OperationTypeRepo operationTypeRepo;
    @Autowired
    AccountService accountService;
    @Autowired
    InvokerUserService invokerUserService;

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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

    //paymentTest/opera/operaPayment
    @PostMapping(value = "/paymentTest", produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public TransactionResponse operaPayment(@RequestBody TransactionRequest transactionRequest) {

        logger.info("TransactionType start : " + transactionRequest);
        logger.info("TransactionType : " + transactionRequest.getTransType());

        TransactionResponse transactionResponse = new TransactionResponse();
        if(transactionRequest.getTransType() == null || transactionRequest.getTransType().equals(""))
            return transactionResponse;

//        switch (transactionRequest.getTransType()){
//            case "23": transactionResponse=  getToken(transactionRequest);
//                break;
//            case "05":transactionResponse=  auth(transactionRequest);
//                break;
//            case "02":transactionResponse=  paymentTransaction(transactionRequest);
//                break;
//            case "07":transactionResponse=  paymentTransaction(transactionRequest);
//                break;
//
//        }
        
        transactionResponse=  paymentTransaction(transactionRequest);
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
        logger.info("SequenceNo : "+transactionRequest.getSequenceNo());
        logger.info("TransactionType : "+transactionRequest.getTransType());
        logger.info("TransAmount : "+transactionRequest.getTransAmount());
        TransactionResponse transactionResponse = new TransactionResponse();

        String username = "operaPayment";
        String password = "operaPayment345";
        InvokerUser invokerUser = invokerUserService.getInvokerUser(username, password);
        Optional<Account> accountOptional = accountRepo.findById(invokerUser.getAccountId());

        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();

            OperationType operationType = operationTypeRepo.findAllByNameAndAccountIdAndDeleted(Constants.OPERA_PAYMENT, account.getId(), false);

            if (!invokerUser.getTypeId().equals(operationType.getId()))
                return transactionResponse;

//            http://localhost:9090
            final String uri = operationType.getConfiguration().getTpeConnectorLink() + "/paymentPreauthorization";

            Random random = new Random(100);

            String firstTwoDigits= String.valueOf(random.nextInt(100));
            String secondTwoDigits= String.valueOf((LocalDateTime.now().getMinute()+10));
            String thirdTwoDigits= String.valueOf((LocalDateTime.now().getSecond()+10));
            String uniqueEcr=firstTwoDigits.concat(secondTwoDigits).concat(thirdTwoDigits);
            TransactionObject transactionObject = new TransactionObject() ;
            transactionObject.setAmount(transactionRequest.getTransAmount());
            transactionObject.setEcr(uniqueEcr);
            transactionObject.setPayKind(PREAUTHORIZATION);
            transactionObject.setCashierID("1");
            transactionObject.setTransCurrency(transactionRequest.getTransCurrency());

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

                transactionResponse.setSequenceNo(transactionRequest.getSequenceNo());
                transactionResponse.setTransType(transactionRequest.getTransType());
                transactionResponse.setTransAmount(transactionRequest.getTransAmount());
                transactionResponse.setRespCode("00");
                transactionResponse.setRespText("APPROVAL");
                transactionResponse.setrRN(uniqueValue);
                transactionResponse.setOfflineFlag("N");
                transactionResponse.setTransToken(uniqueToken);
                transactionResponse.setIssuerId("01");
                transactionResponse.setpAN(result.cardLastDigits);
                transactionResponse.setExpiryDate("2212");
                transactionResponse.setEntryMode("01");
                transactionResponse.setAuthCode(uniqueValue);
                transactionResponse.setdCCIndicator("0");
                transactionResponse.setMerchantId("1");
                transactionResponse.setTerminalId("1");
                transactionResponse.setPrintData("Data");
            }
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
