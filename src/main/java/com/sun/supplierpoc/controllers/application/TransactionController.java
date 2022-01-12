package com.sun.supplierpoc.controllers.application;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.excelExporters.TransactionExcelExport;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.Response;
import com.sun.supplierpoc.models.TransactionType;
import com.sun.supplierpoc.models.Transactions;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.models.simphony.response.TransInRange;
import com.sun.supplierpoc.repositories.AccountRepo;
import com.sun.supplierpoc.repositories.TransactionRepo;
import com.sun.supplierpoc.services.AccountService;
import com.sun.supplierpoc.services.application.TransactionService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/transaction")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepo accountRepo;

    @Autowired
    private TransactionRepo transactionsRepo;

    @RequestMapping("/getTransactionsCount")
    public int getTransactionsCount(Principal principal,
                                                   @RequestParam(name = "fromDate", required = false) String fromDate,
                                                   @RequestParam(name = "toDate", required = false) String toDate,
                                                   @RequestParam(name = "group", required = false) String group,
                                                   @RequestParam(name = "transactionType", required = false) String transactionType) {
        User user = (User)((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();

        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());

        if (accountOptional.isPresent()) {

            Account account = accountOptional.get();

            int transactions = transactionService.getTransactionByTypeCount(fromDate, toDate, group, account);

            return transactions;
        }else{
            return 0;
        }
    }

    @RequestMapping("/getTransactions")
    public List<Transactions> getTransactionByType(Principal principal,
                                                   @RequestParam(name = "pageNumber") int pageNumber,
                                                   @RequestParam(name = "limit") int limit,
                                                   @RequestParam(name = "fromDate", required = false) String fromDate,
                                                   @RequestParam(name = "toDate", required = false) String toDate,
                                                   @RequestParam(name = "group", required = false) String group,
                                                   @RequestParam("transactionType") String transactionType) {
        User user = (User)((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();

        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());

        if (accountOptional.isPresent()) {

            Account account = accountOptional.get();

            List<Transactions> transactions = transactionService.getTransactionByTypePaginated(transactionType, fromDate,
                    toDate, group, account, pageNumber, limit);

            return transactions;
        }else{
            return new ArrayList<>();
        }
    }

    @RequestMapping("/getTotalSpendTransactions")
    public ResponseEntity getTotalSpendTransactions(Principal principal,
                                                    @RequestParam("transactionType") String transactionType,
                                                    @RequestParam("dateFlag") String dateFlag) {

        HashMap response = new HashMap();
        User user = (User)((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();
            response = transactionService.getTotalSpendTransactions(dateFlag, transactionType, account);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("");
        }
    }

    @RequestMapping("/getTransactionsInRange")
    @CrossOrigin("*")
    public ResponseEntity getTransactionsInRange(Principal principal,
                                                      @RequestParam("transactionType") String transactionType,
                                                      @RequestParam("startTime") String startTime,
                                                      @RequestParam("endTime") String endTime,
                                                      @RequestParam("group") String group) {

        HashMap response = new HashMap();

        User user = (User)((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        if (accountOptional.isPresent()) {

            Account account = accountOptional.get();

            TransInRange transInRange = transactionService.getTotalSpendTransactionsInRang(startTime, endTime, transactionType, group, account);

            if(transInRange.getTransactions().size() == 0){
                response.put("message", "No transactions in This range.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            return ResponseEntity.status(HttpStatus.OK).body(transInRange);

        }else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Constants.INVALID_USER);
        }
    }

    @PostMapping("/createTransactionType")
    public ResponseEntity<?> createTransactionType(Principal principal,
                                                   @RequestBody TransactionType transactionType, BindingResult result) {

        HashMap response = new HashMap();

        try {

            if (result.hasErrors()) {
                response.put("error", result.getAllErrors());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
            Account account = accountService.getAccount(user.getAccountId());

            transactionService.createTransactionType(account, transactionType);

            response.put("isSuccess", Constants.SUCCESS);
            response.put("transactionType", transactionType);

            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (Exception e) {

            response.put("isSuccess", Constants.FAILED);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        }
    }

    @PostMapping("/exportExcelSheet")
    public void exportExcelSheet(@RequestBody List<Transactions> transactions,
                                              HttpServletResponse httpServletResponse,
                                              Principal principal) throws IOException{

        HashMap response = new HashMap();

        User user = (User)((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();

        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());

        if (accountOptional.isPresent()) {

            httpServletResponse.setContentType("application/octet-stream");
            DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
            String currentDateTime = dateFormatter.format(new Date());

            String headerKey = "Content-Disposition";
            String headerValue = "attachment; filename=Transactions" + currentDateTime + ".xlsx";
            httpServletResponse.setHeader(headerKey, headerValue);

            Account account = accountOptional.get();

            TransactionExcelExport trans = new TransactionExcelExport(transactions);
            trans.export(httpServletResponse);

            response.put("message", "Excel exported successfully.");
            LoggerFactory.getLogger(TransactionController.class).info(response.get("message").toString());

        }else{

        }
    }

    @GetMapping("/transactionPagination")
    public ResponseEntity transactionPagination(@RequestParam(name="page", defaultValue = "0") int page,
                                                @RequestParam(name="size", defaultValue = "3") int size){

        Response response = new Response();
        Pageable pageable = PageRequest.of(page, size);
        Page<Transactions> transactionsPage = transactionsRepo.findAll(pageable);
        response.setData(transactionsPage);
        return new ResponseEntity(response, HttpStatus.OK);
    }

    @GetMapping("/test/updateTrans")
    public ResponseEntity<?> createTransaction(){
        transactionService.updateTransactions();
        return new ResponseEntity<>("", HttpStatus.OK);
    }

}
