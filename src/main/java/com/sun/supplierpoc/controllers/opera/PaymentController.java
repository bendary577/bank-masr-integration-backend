package com.sun.supplierpoc.controllers.opera;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.models.auth.InvokerUser;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.models.opera.TransactionObject;
import com.sun.supplierpoc.models.opera.TransactionObjectResponse;
import com.sun.supplierpoc.models.opera.TransactionRequest;
import com.sun.supplierpoc.models.opera.TransactionResponse;
import com.sun.supplierpoc.repositories.AccountRepo;
import com.sun.supplierpoc.repositories.GeneralSettingsRepo;
import com.sun.supplierpoc.repositories.OperationTypeRepo;
import com.sun.supplierpoc.repositories.opera.OperaTransactionRepo;
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
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.security.Principal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

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
    @Autowired
    private OperaTransactionRepo operaTransactionRepo;

    private Conversions conversions = new Conversions();
    @Autowired
    private GeneralSettingsRepo generalSettingsRepo;
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private final String PREAUTHORIZATION = "1";
    private final String PAYMENT = "2";
    private static final long MILLIS_IN_A_DAY = 1000 * 60 * 60 * 24;
    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    @Bean
    public MappingJackson2XmlHttpMessageConverter mappingJackson2XmlHttpMessageConverter(
            Jackson2ObjectMapperBuilder builder) {
        ObjectMapper mapper = builder.createXmlMapper(true).build();
        ((XmlMapper) mapper).enable(ToXmlGenerator.Feature.WRITE_XML_DECLARATION);
        return new MappingJackson2XmlHttpMessageConverter(mapper);
    }

    @PostMapping(value = "/paymentTest", produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public TransactionResponse operaPayment(@RequestBody TransactionRequest transactionRequest) {

        logger.info("TransactionType start : " + transactionRequest);
        logger.info("TransactionType : " + transactionRequest.getTransType());

        TransactionResponse transactionResponse = new TransactionResponse();
        if (transactionRequest.getTransType() == null || transactionRequest.getTransType().equals(""))
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
        GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted("61111b35f8d6182e2813efe3", false);
        transactionResponse = paymentTransaction(transactionRequest, transactionRequest.getTransType(), generalSettings);
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

    private TransactionResponse paymentTransaction(TransactionRequest transactionRequest, String TransType, GeneralSettings generalSettings) {
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

        String POS_MACHINE_URL;
        if(transactionRequest.getSiteId().equals("ACT|SDMD")) {
            POS_MACHINE_URL = "http://" + generalSettings.getPosMachineMaps().get(0).getIp() +
                    ":" + generalSettings.getPosMachineMaps().get(0).getPort();
        }else{
            POS_MACHINE_URL = "http://" + generalSettings.getPosMachineMaps().get(1).getIp() +
                    ":" + generalSettings.getPosMachineMaps().get(1).getPort();
        }
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

            try {
                createOperaTransaction(operaTransaction);
            }catch (Exception e ){
                e.printStackTrace();
                logger.error(String.join("Failed to save transactions in middleware. ",e.getMessage()));
            }

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

    private TransactionResponse auth(TransactionRequest transactionRequest) {
        logger.info("SequenceNo : " + transactionRequest.getSequenceNo());
        logger.info("TransactionType : " + transactionRequest.getTransType());
        logger.info("TransAmount : " + transactionRequest.getTransAmount());
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

            String firstTwoDigits = String.valueOf(random.nextInt(100));
            String secondTwoDigits = String.valueOf((LocalDateTime.now().getMinute() + 10));
            String thirdTwoDigits = String.valueOf((LocalDateTime.now().getSecond() + 10));
            String uniqueEcr = firstTwoDigits.concat(secondTwoDigits).concat(thirdTwoDigits);
            TransactionObject transactionObject = new TransactionObject();
            transactionObject.setAmount(transactionRequest.getTransAmount());
            transactionObject.setEcr(uniqueEcr);
            transactionObject.setPayKind(PREAUTHORIZATION);
            transactionObject.setCashierID("1");
            transactionObject.setTransCurrency(transactionRequest.getTransCurrency());

            TransactionObjectResponse result = new TransactionObjectResponse();
            try {
                result = restTemplate.postForObject(uri, transactionObject, TransactionObjectResponse.class);
            } catch (Exception e) {
                logger.info("Payment Pre-authorization Error: " + e.getMessage());
            }

            if (result != null) {
                if (result.getCardLastDigits() == null || result.getCardLastDigits().equals("")) {
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

    private TransactionResponse paymentTransaction(TransactionRequest transactionRequest) {
        TransactionResponse transactionResponse = new TransactionResponse();
        transactionResponse.setSequenceNo(transactionRequest.getSequenceNo());
        transactionResponse.setTransType(transactionRequest.getTransType());
        transactionResponse.setTransAmount(transactionRequest.getTransAmount());

        transactionResponse.setRespCode("00");
        transactionResponse.setRespText("APPROVAL");
        transactionResponse.setpAN(transactionRequest.getPAN());
        transactionResponse.setExpiryDate(transactionRequest.getExpiryDate());
        transactionResponse.setTransToken(transactionResponse.getTransToken());
        transactionResponse.setEntryMode("01");
        transactionResponse.setIssuerId("02");
        transactionResponse.setrRN("000000000311");
        transactionResponse.setOfflineFlag("N");
        transactionResponse.setMerchantId("1");
        transactionResponse.setdCCIndicator("0");
        transactionResponse.setTerminalId("1");
        return transactionResponse;
    }

    @GetMapping(value = "/", produces = MediaType.APPLICATION_XML_VALUE)
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

    @PostMapping(value = "/posMachine", produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public TransactionResponse operaPosMachine(@RequestBody TransactionRequest transactionRequest) {
        System.out.println("TransactionType start : " + transactionRequest);
        System.out.println("TransactionType : " + transactionRequest.getTransType());

        TransactionResponse transactionResponse = new TransactionResponse();
        if (transactionRequest.getTransType() == null || transactionRequest.getTransType().equals(""))
            return transactionResponse;

        transactionResponse = payTransactionOnMachine(transactionRequest);
        return transactionResponse;
    }

    private TransactionResponse payTransactionOnMachine(TransactionRequest transactionRequest) {
        TransactionResponse transactionResponse = new TransactionResponse();
        TransactionObjectResponse result = new TransactionObjectResponse();

        // send payment transaction to POS Machine (HTTP)
        final String uri = "http://192.168.1.40:4040/";
        TransactionObject transactionObject = new TransactionObject();
        transactionObject.setAmount(transactionRequest.getTransAmount());
        transactionObject.setTransCurrency(transactionRequest.getTransCurrency());

        try {
            result = restTemplate.postForObject(uri, transactionObject, TransactionObjectResponse.class);
        } catch (Exception e) {
            logger.info("Payment Pre-authorization Error: " + e.getMessage());
        }

        if (result != null) {
            transactionResponse.setSequenceNo(transactionRequest.getSequenceNo());
            transactionResponse.setTransType(transactionRequest.getTransType());
            transactionResponse.setTransAmount(transactionRequest.getTransAmount());

            transactionResponse.setRespCode("00");
            transactionResponse.setRespText("APPROVAL");
            transactionResponse.setpAN(transactionRequest.getPAN());
            transactionResponse.setExpiryDate(transactionRequest.getExpiryDate());
            transactionResponse.setTransToken(transactionResponse.getTransToken());
            transactionResponse.setEntryMode("01");
            transactionResponse.setIssuerId("02");
            transactionResponse.setrRN("000000000311");
            transactionResponse.setOfflineFlag("N");
            transactionResponse.setMerchantId("1");
            transactionResponse.setdCCIndicator("0");
            transactionResponse.setTerminalId("1");
        }

        return transactionResponse;
    }


    //////////////////////////////////// Bank Misr Payment /////////////////////////////////////////////////////////

    @RequestMapping(value = "/opera/createOperaTransaction")
    @ResponseBody
    public boolean createOperaTransaction(@RequestBody OperaTransaction operaTransaction) {
//        @RequestHeader("Authorization") String authorization,
        String username, password;
        try {
//            final String[] values = conversions.convertBasicAuth(authorization);
            final String[] values = {"operaInvoker", "opera@2021"};
            if (values.length != 0) {
                username = values[0];
                password = values[1];

                InvokerUser invokerUser = invokerUserService.getInvokerUser(username, password);

                if (invokerUser != null) {
                    Optional<Account> accountOptional = accountService.getAccountOptional(invokerUser.getAccountId());

                    if (accountOptional.isPresent()) {
                        Account account = accountOptional.get();

                        OperationType operationType = operationTypeRepo.findAllByNameAndAccountIdAndDeleted(Constants.OPERA_PAYMENT, account.getId(), false);

                        if (!invokerUser.getTypeId().contains(operationType.getId())) {
//                            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
//                                    new HashMap<String, Object>() {
//                                        {
//                                            put("error", "You don't have role to save your transactions!");
//                                            put("Date", LocalDateTime.now());
//                                        }
//                                    });
                            return false;
                        }

                        // Create new transaction
                        operaTransaction.setAccountId(account.getId());
                        operaTransaction.setCreationDate(new Date());
                        operaTransactionRepo.save(operaTransaction);

                        // Response
//                        return ResponseEntity.status(HttpStatus.OK).body(
//                                new HashMap<String, Object>() {
//                                    {
//                                        put("success", "Transaction created successfully.");
//                                        put("Date", LocalDateTime.now());
//                                    }
//                                });
                        return true;

                    } else {
//                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
//                                new HashMap<String, Object>() {{
//                                    put("error", "Account doesn't exists.");
//                                    put("Date", LocalDateTime.now());
//                                }});
                        return false;
                    }
                } else {
//                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
//                            new HashMap<String, Object>() {{
//                                put("error", "Wrong username or password.");
//                                put("Date", LocalDateTime.now());
//                            }});
                    return false;
                }
            } else {
//                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
//                        new HashMap<String, Object>() {{
//                            put("error", "Wrong username or password.");
//                            put("Date", LocalDateTime.now());
//                        }});
                return false;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
//                    new HashMap<String, Object>() {
//                        {
//                            put("error", ex.getMessage());
//                            put("Date", LocalDateTime.now());
//                        }
//                    });
            return false;
        }
    }

    @GetMapping(value = "/listOperaTransaction")
    @ResponseBody
    public List<OperaTransaction> listOperaTransaction(Principal principal,
                                                       @RequestParam(name = "startDate", required = false) String startDate,
                                                       @RequestParam(name = "endDate", required = false) String endDate){
        try {
            User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
            Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
            if (accountOptional.isPresent()) {
                Account account = accountOptional.get();

                if(startDate == null || startDate.equals("") || endDate == null || endDate.equals("")){
                    return operaTransactionRepo.findAllByAccountIdAndDeleted(account.getId(), false);
                } else{
                    Date start;
                    Date end;

                    DateFormat df = new SimpleDateFormat("yyyy/MM/dd");

                    try {
                        start = df.parse(startDate);
                        end = new Date(df.parse(endDate).getTime() + MILLIS_IN_A_DAY);

                    }catch(Exception e){
                        e.printStackTrace();
                        df = new SimpleDateFormat("yyyy-M-d");
                        start = df.parse(startDate);
                        end = new Date(df.parse(endDate).getTime() + MILLIS_IN_A_DAY);
                    }

                    return operaTransactionRepo.findAllByAccountIdAndDeletedAndCreationDateBetween(
                            account.getId(), false, start, end);
                }


            }else {
                return new ArrayList<>();
            }

        }catch (Exception ex){
            ex.printStackTrace();
            return new ArrayList<>();
        }
    }

    @GetMapping(value = "/countOperaTransaction")
    @ResponseBody
    public ResponseEntity getOperaTransactionStat(Principal principal,
                                     @RequestParam(name = "startDate", required = false) String startDate,
                                     @RequestParam(name = "endDate", required = false) String endDate){
        try {
            User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
            Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
            if (accountOptional.isPresent()) {
                Account account = accountOptional.get();

                if(startDate == null || startDate.equals("") || endDate == null || endDate.equals("")){
                    int failedTransactionCount =  operaTransactionRepo.countByAccountIdAndDeletedAndStatus(account.getId(),
                            false, Constants.FAILED);

                    int succeedTransactionCount = operaTransactionRepo.countByAccountIdAndDeletedAndStatus(account.getId(),
                            false, Constants.SUCCESS);

                    double totalAmount = 0;
                    List<OperaTransaction> transactions = operaTransactionRepo.findAllByAccountIdAndDeleted(account.getId(), false);

                    for (OperaTransaction trans : transactions) {
                        totalAmount += trans.getAmount();
                    }

                    double finalTotalAmount = totalAmount;
                    return ResponseEntity.status(HttpStatus.OK).body(
                            new HashMap<String, Object>() {
                                {
                                    put("succeedTransactionCount", succeedTransactionCount);
                                    put("failedTransactionCount", failedTransactionCount);
                                    put("totalTransactionAmount", finalTotalAmount);
                                    put("Date", LocalDateTime.now());
                                }
                            });
                } else{
                    Date start;
                    Date end;

                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                    start = df.parse(startDate);
                    end = new Date(df.parse(endDate).getTime() + MILLIS_IN_A_DAY);

                    int failedTransactionCount = operaTransactionRepo.countByAccountIdAndDeletedAndStatusAndCreationDateBetween(account.getId(),
                            false, Constants.FAILED, start, end);

                    int succeedTransactionCount = operaTransactionRepo.countByAccountIdAndDeletedAndStatusAndCreationDateBetween(account.getId(),
                            false, Constants.SUCCESS, start, end);

                    double totalAmount = 0;
                    List<OperaTransaction> transactions = operaTransactionRepo.findAllByAccountIdAndDeletedAndCreationDateBetween(
                            account.getId(), false, start, end);

                    for (OperaTransaction trans : transactions) {
                        totalAmount += trans.getAmount();
                    }

                    double finalTotalAmount = totalAmount;
                    return ResponseEntity.status(HttpStatus.OK).body(
                            new HashMap<String, Object>() {
                                {
                                    put("succeedTransactionCount", succeedTransactionCount);
                                    put("failedTransactionCount", failedTransactionCount);
                                    put("totalTransactionAmount", finalTotalAmount);
                                    put("Date", LocalDateTime.now());
                                }
                            });
                }

            }else {
                return ResponseEntity.status(HttpStatus.OK).body(
                        new HashMap<String, Object>() {
                            {
                                put("succeedTransactionCount", 0);
                                put("failedTransactionCount", 0);
                                put("totalTransactionAmount", 0);
                                put("Date", LocalDateTime.now());
                            }
                        });
            }

        }catch (Exception ex){
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new HashMap<String, Object>() {
                        {
                            put("succeedTransactionCount", 0);
                            put("failedTransactionCount", 0);
                            put("totalTransactionAmount", 0);
                            put("Date", LocalDateTime.now());
                        }
                    });
        }
    }

    @GetMapping(value = "/checkAppConnection")
    @ResponseBody
    public ResponseEntity checkAppConnection(Principal principal,
                                             @RequestParam(name = "ip", required = false) String ip,
                                             @RequestParam(name = "port", required = false) String port){
        Response  respons = new Response();
        try {
            User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
            Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
            if (accountOptional.isPresent()) {
                Account account = accountOptional.get();


                if(port != null || !port.equals("") || ip != null || !ip.equals("")){
                    respons.setStatus(true);
                    respons.setMessage("Connected");
                    return ResponseEntity.status(HttpStatus.OK).body(respons);
                } else{
                    respons.setStatus(false);
                    respons.setMessage("Not Connected");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respons);
                }
            }else {
                respons.setStatus(false);
                respons.setMessage("Not Connected");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respons);
            }

        }catch (Exception ex){
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("");
        }
    }
}
