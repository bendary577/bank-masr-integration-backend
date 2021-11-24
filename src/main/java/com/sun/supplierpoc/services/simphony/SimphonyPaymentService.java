package com.sun.supplierpoc.services.simphony;

import com.google.gson.Gson;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.GeneralSettings;
import com.sun.supplierpoc.models.Response;
import com.sun.supplierpoc.models.simphony.simphonyCheck.SimphonyCheck;
import com.sun.supplierpoc.models.simphony.simphonyCheck.SimphonyPaymentRes;
import com.sun.supplierpoc.models.simphony.simphonyCheck.TransactionResponse;
import com.sun.supplierpoc.models.simphony.simphonyCheck.SimphonyPaymentReq;
import com.sun.supplierpoc.repositories.GeneralSettingsRepo;
import com.sun.supplierpoc.repositories.simphony.SplittableCheckRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
public class SimphonyPaymentService {

    @Autowired
    private GeneralSettingsRepo generalSettingsRepo;

    @Autowired
    private SplittableCheckRepo simphonyCheckRepo;

    Logger logger = LoggerFactory.getLogger("SimphonyPaymentService");

    RestTemplate restTemplate = new RestTemplate(getClientHttpRequestFactory());

    //Override timeouts in request factory
    private SimpleClientHttpRequestFactory getClientHttpRequestFactory()
    {
        SimpleClientHttpRequestFactory clientHttpRequestFactory
                = new SimpleClientHttpRequestFactory();
        //Connect timeout
        clientHttpRequestFactory.setConnectTimeout(100_000);

        //Read timeout
        clientHttpRequestFactory.setReadTimeout(100_000);
        return clientHttpRequestFactory;
    }

    public Response createSimphonyPaymentTransaction (SimphonyPaymentReq simphonyPayment, Account account) {

        Response response = new Response();
        SimphonyPaymentRes simphonyPaymentRes = new SimphonyPaymentRes();

        GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);

        TransactionResponse paymentTransaction = paymentTransaction(simphonyPayment, "12", generalSettings);

        Optional<SimphonyCheck> simphonyCheckOptional = simphonyCheckRepo.
                findByAccountIdAndRevenueCenterIdAndCheckNumber(account.getId(), simphonyPayment.getRevenueCentreId(), simphonyPayment.getCheckNumber());

        SimphonyCheck simphonyCheck;

        if(!simphonyCheckOptional.isPresent()){

            simphonyCheck = new SimphonyCheck();

            simphonyCheck.setCheckNumber(simphonyPayment.getCheckNumber());
            simphonyCheck.setCheckValue(simphonyPayment.getTotalDue());
            simphonyCheck.setEmployeeId(simphonyPayment.getEmployeeId());
            simphonyCheck.setRevenueCenterId(simphonyPayment.getRevenueCentreId());
            simphonyCheck.setRevenueCenterName(simphonyPayment.getCheckNumber());
            simphonyCheck.setCashierNumber(simphonyPayment.getCashierNumber());
            simphonyCheck.setEmployeeName(simphonyPayment.getEmployeeName());
            simphonyCheck.setAccountId(account.getId());

        }else{
            simphonyCheck = simphonyCheckOptional.get();
        }

        if(paymentTransaction != null) {

            simphonyCheck.getTransactionResponses().add(paymentTransaction);

            if(Integer.parseInt(paymentTransaction.getResponseCode()) == 0) {

                Float checkValue = Float.parseFloat(simphonyPayment.getTotalDue());
                Float payedValue = Float.parseFloat(paymentTransaction.getTransactionAmount());

                simphonyPaymentRes.setPayment(true);
                simphonyPaymentRes.setMessage("Successful Payment");

                if( checkValue < payedValue){
                    Float tips = payedValue - checkValue;
                    simphonyCheck.setTips(String.valueOf(tips));

                    simphonyPaymentRes.setAmount(String.valueOf(payedValue));
                    simphonyPaymentRes.setTips(String.valueOf(tips));
                    simphonyCheck.setPayed(true);
                }else{
                    simphonyPaymentRes.setAmount(String.valueOf(payedValue));
                }

            }else{
                simphonyPaymentRes.setPayment(false);
                simphonyPaymentRes.setMessage(paymentTransaction.getResponseMessage());
            }

            response.setSimphonyPaymentRes(simphonyPaymentRes);
        }else{
            simphonyPaymentRes.setPayment(false);
            simphonyPaymentRes.setMessage("The connection to the POS machine was broken.");
        }

        simphonyCheckRepo.save(simphonyCheck);
        return response;
    }

    private TransactionResponse paymentTransaction(SimphonyPaymentReq simphonyPaymentReq, String transType, GeneralSettings generalSettings) {

        TransactionResponse transactionResponse = new TransactionResponse();

        transactionResponse.setCreationDate(new Date());
        String currency = "EGP";
        transactionResponse.setCurrency(currency);

        float amount = Float.parseFloat(simphonyPaymentReq.getTotalDue()) * 100;

        String POS_MACHINE_URL = "http://192.168.1.4:7070";

//        if(transactionRequest.getSiteId() != null && transactionRequest.getSiteId().equals("ACT|SDMD")) {
//            POS_MACHINE_URL = "http://" + generalSettings.getPosMachineMaps().get(0).getIp() +
//                    ":" + generalSettings.getPosMachineMaps().get(0).getPort();
//        }else{
//            POS_MACHINE_URL = "http://" + generalSettings.getPosMachineMaps().get(1).getIp() +
//                    ":" + generalSettings.getPosMachineMaps().get(1).getPort();
//        }

        ResponseEntity<String> result = null;

        try {

            result = restTemplate.getForEntity(POS_MACHINE_URL + "?transactionAmount=" +
                    Math.round(1f) + "&currency=" + currency + "&transType=" + transType, String.class);

        } catch (Exception e) {

            e.printStackTrace();
            logger.error(String.join("Failed in transaction method with ", e.getMessage()));

        }

        if (result != null && result.getBody() != null) {
            // Parse POS machine result

            TerminalResponse terminalResponse = new Gson().fromJson(result.getBody(), TerminalResponse.class);

            if (terminalResponse.getRespCode().equals("0")) {
                transactionResponse.setStatus("Success");
//                transactionResponse.setTransactionAmount(Float.parseFloat(terminalResponse.getAmount())/100 );
                transactionResponse.setTransactionAmount("40");
                transactionResponse.setResponseMessage(terminalResponse.getMessage());

            } else {
                transactionResponse.setStatus("Failed");
                if(terminalResponse.getMessage().equals("")) {
                    transactionResponse.setResponseMessage("Undefined Reason");
                }else{
                    transactionResponse.setResponseMessage(terminalResponse.getMessage());
                }
            }

            transactionResponse.setCardNumber(terminalResponse.getCardNo());
            if(terminalResponse.getCardNo() == null || terminalResponse.getCardNo().equals("")){
                transactionResponse.setAuthedCardNumber("xxxxxxxxxxxxxxxx");
            }else{
                transactionResponse.setAuthedCardNumber(terminalResponse.getCardNo());
            }
            if(terminalResponse.getCardExp() == null || terminalResponse.getCardExp().equals("")){
                transactionResponse.setExpiryDate("xxxx");
            }else{
                transactionResponse.setExpiryDate(terminalResponse.getCardExp());
            }
            transactionResponse.setMerchantName(terminalResponse.getMerchantName());
            transactionResponse.setMerchantId(terminalResponse.getMerchantId());
            transactionResponse.setTerminalId(terminalResponse.getTerminalId());
            transactionResponse.setReferenceNumber(terminalResponse.getRefNo());
            transactionResponse.setResponseCode(terminalResponse.getRespCode());

            return transactionResponse;
        } else {
            transactionResponse.setStatus("Failed");
            transactionResponse.setAuthedCardNumber("xxxxxxxxxxxxxxxx");
            transactionResponse.setExpiryDate("xxxx");
            transactionResponse.setReason("The connection to the POS machine was broken.");
            return transactionResponse;
        }
    }

    private TransactionResponse paymentTransaction1(SimphonyPaymentReq simphonyPaymentReq, String transType, GeneralSettings generalSettings) {

        TransactionResponse transactionResponse = new TransactionResponse();
        transactionResponse.setTransactionType(transType);
        transactionResponse.setTransactionAmount(simphonyPaymentReq.getTotalDue());

//        transactionResponse.setSequenceNo(transactionRequest.getSequenceNo());
//        operaTransaction.setGuestNumber(transactionRequest.getGuestNo());
//        operaTransaction.setCheckInDate(transactionRequest.getCheckInDate());
//        operaTransaction.setCheckOutDate(transactionRequest.getCheckOutDate());
//        operaTransaction.setDeleted(false);

        transactionResponse.setCreationDate(new Date());
        String currency = "EGP";
        transactionResponse.setCurrency(currency);

        float amount = Float.parseFloat(simphonyPaymentReq.getTotalDue());

//        if(transactionRequest.getTransCurrency().equals("818"))
//            currency = "AED";
//        else
//            currency = "EGP";

        String POS_MACHINE_URL = "http://192.168.1.16:7070";

//        if(transactionRequest.getSiteId() != null && transactionRequest.getSiteId().equals("ACT|SDMD")) {
//            POS_MACHINE_URL = "http://" + generalSettings.getPosMachineMaps().get(0).getIp() +
//                    ":" + generalSettings.getPosMachineMaps().get(0).getPort();
//        }else{
//            POS_MACHINE_URL = "http://" + generalSettings.getPosMachineMaps().get(1).getIp() +
//                    ":" + generalSettings.getPosMachineMaps().get(1).getPort();
//        }

        ResponseEntity<String> result = null;

        try {

            result = restTemplate.getForEntity(POS_MACHINE_URL + "?transactionAmount=" +
                    Math.round(amount) + "&currency=" + currency + "&transType=" + transType, String.class);

        } catch (Exception e) {

            e.printStackTrace();
            logger.error(String.join("Failed in transaction method with ", e.getMessage()));

        }

        // Save transaction in middleware
        /*
         * Success,Transaction Completed,526439******0435,3,,ACT Test Merchant,1010000907,82063669,000015000000,null
         *         result = new ResponseEntity<>("Success,Transaction Completed,526439******0435,3," +
                ",ACT Test Merchant,1010000907,82063669,000015000000,null", HttpStatus.OK);
         * */

        if (result != null && result.getBody() != null) {
            // Parse POS machine result
            final String[] values = result.getBody().split(",");

            HashMap<String, Object> terminalResponse = new Gson().fromJson(result.getBody(), HashMap.class);


            if (values[0].equalsIgnoreCase("Success")) {

                String reason = values[1];
                String cardNumber = values[2];
                String cardType = values[3];
                String expiryDate = values[2];

                transactionResponse.setStatus("Success");
                transactionResponse.setReason(reason);
                transactionResponse.setCardNumber(cardNumber);
                transactionResponse.setAuthedCardNumber(cardNumber.substring(0, 4) + "XXXXXXXX" + cardNumber.substring(cardNumber.length() - 4));
                transactionResponse.setExpiryDate(expiryDate);

                if(values.length > 5)
                    transactionResponse.setMerchantName(values[5]);
                if(values.length > 6)
                    transactionResponse.setMerchantId(values[6]);
                if(values.length > 7)
                    transactionResponse.setTerminalId(values[7]);
                if(values.length > 8)
                    transactionResponse.setReferenceNumber(values[8]);

                if (cardType.equals("3")) {
                    transactionResponse.setIssuerId("24");
                }else {
                    transactionResponse.setIssuerId("01");
                }

                transactionResponse.setResponseCode("00");
                transactionResponse.setResponseMessage("APPROVAL");
                transactionResponse.setPrintData("Bank Misr");
                transactionResponse.setEntryMode("01");

            } else {
                transactionResponse.setStatus("Failed");
                transactionResponse.setReason(values[1]);
                transactionResponse.setCardNumber("XXXXXXXXXXXXXXXX");

                transactionResponse.setExpiryDate("2509");
                transactionResponse.setEntryMode("01");
                transactionResponse.setIssuerId("01");
                transactionResponse.setMerchantId("1");
                transactionResponse.setTerminalId("1");
                transactionResponse.setPrintData("Bank Misr");
            }

            return transactionResponse;
        } else {
            transactionResponse.setStatus("Failed");
            transactionResponse.setReason("The connection to the POS machine was broken.");
            transactionResponse.setCardNumber("XXXXXXXXXXXXXXXX");

            return transactionResponse;
        }
    }

    public List<SimphonyCheck> getCheckPayment() {

        return simphonyCheckRepo.findAll();
    }
}
