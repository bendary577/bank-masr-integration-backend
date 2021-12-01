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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class SimphonyPaymentService {

    private static final long MILLIS_IN_A_DAY = 1000 * 60 * 60 * 24;

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
            simphonyCheck.setRevenueCenterName(simphonyPayment.getRevenueCentreName());
            simphonyCheck.setCashierNumber(simphonyPayment.getCashierNumber());
            simphonyCheck.setEmployeeName(simphonyPayment.getEmployeeName());
            simphonyCheck.setAccountId(account.getId());
            simphonyCheck.setCreationDate(new Date());
            simphonyCheck.setDeleted(false);

        }else{
            simphonyCheck = simphonyCheckOptional.get();
            simphonyCheck.setLastUpdate(new Date());
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
                transactionResponse.setTransactionAmount(simphonyPaymentReq.getPayedAmount());
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


    public HashMap<String, Object> getCheckPayment(String startDate, String endDate, Account account) {

        HashMap<String, Object> response = new HashMap();

        double succeedTransactionCount = 0;
        double failedTransactionCount = 0;
        int totalTransactionAmount = 0;

        List<SimphonyCheck> simphonyChecks = filterSimphonyChecks(startDate, endDate, account);

        for (SimphonyCheck tempSimphonyCheck  : simphonyChecks) {
            if(tempSimphonyCheck.isPayed()) {
                succeedTransactionCount += 1;
            }else {
                failedTransactionCount += 1;
            }
            totalTransactionAmount += Integer.parseInt(tempSimphonyCheck.getCheckValue());
        }

        response.put("succeedTransactionCount", succeedTransactionCount);
        response.put("failedTransactionCount", failedTransactionCount);
        response.put("totalTransactionAmount", totalTransactionAmount);
        response.put("transactions", simphonyChecks);

        return response;

    }

    public List<SimphonyCheck> filterSimphonyChecks(String startDate, String endDate, Account account) {

        List<SimphonyCheck> operaTransactions = new ArrayList<>();
        Date start, end;
        try {

            if((startDate != null || !startDate.equals("") || startDate != null || !endDate.equals(""))) {
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    start = dateFormat.parse(startDate);
                    end = new Date(dateFormat.parse(endDate).getTime() + MILLIS_IN_A_DAY);
                } catch (Exception e) {
                    dateFormat = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss");
                    start = dateFormat.parse(startDate.substring(0, 24));
                    end = dateFormat.parse(endDate.substring(0, 24));
                }
                operaTransactions = simphonyCheckRepo.findByAccountIdAndDeletedAndCreationDateBetween(
                        account.getId(), false, start, end);

            }
            return operaTransactions;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public HashMap<String, Object> findAllByAccountIdAndDeleted(String id, boolean deleted) {

        HashMap<String, Object> response = new HashMap<>();
        double succeedTransactionCount = 0;
        double failedTransactionCount = 0;
        int totalTransactionAmount = 0;

        List<SimphonyCheck> simphonyChecks =  simphonyCheckRepo.findAllByAccountIdAndDeleted( id,  deleted);

        for (SimphonyCheck tempSimphonyCheck  : simphonyChecks) {
            if(tempSimphonyCheck.isPayed()) {
                succeedTransactionCount += 1;
            }else {
                failedTransactionCount += 1;
            }
            totalTransactionAmount += Double.parseDouble(tempSimphonyCheck.getCheckValue());
        }

        response.put("succeedTransactionCount", succeedTransactionCount);
        response.put("failedTransactionCount", failedTransactionCount);
        response.put("totalTransactionAmount", totalTransactionAmount);
        response.put("transactions", simphonyChecks);

        return response;
    }

}
