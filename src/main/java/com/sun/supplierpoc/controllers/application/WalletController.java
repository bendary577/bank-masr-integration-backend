package com.sun.supplierpoc.controllers.application;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.excelExporters.ActionsExcelExporter;
import com.sun.supplierpoc.excelExporters.WalletHistoryExporter;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.Response;
import com.sun.supplierpoc.models.applications.*;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.models.roles.Roles;
import com.sun.supplierpoc.repositories.AccountRepo;
import com.sun.supplierpoc.repositories.applications.ApplicationUserRepo;
import com.sun.supplierpoc.services.ActionService;
import com.sun.supplierpoc.services.RoleService;
import com.sun.supplierpoc.services.application.ActionStatsService;
import com.sun.supplierpoc.services.application.WalletService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/wallet")
public class WalletController {
    @Autowired
    ApplicationUserRepo applicationUserRepo;

    @Autowired
    private AccountRepo accountRepo;

    @Autowired
    private RoleService roleService;

    @Autowired
    private WalletService walletService;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @PostMapping("/chargeWallet")
    public ResponseEntity<?> chargeWallet(@RequestParam("userId") String userId,
                                          @RequestBody Balance balance,
                                          Principal principal){

        Response response = new Response();
        User authedUser = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();

        if(authedUser != null){

            Optional<Account> accountOptional = accountRepo.findById(authedUser.getAccountId());

            if(accountOptional.isPresent()) {
                if (roleService.hasRole(authedUser, Roles.CHARGE_WALLET)) {
                    response = walletService.chargeWallet(authedUser, userId, balance);

                    return new ResponseEntity<>(response, HttpStatus.OK);
                }else{
                    response.setStatus(false);
                    response.setMessage(Constants.NOT_ELIGIBLE_USER);
                }
            }else{
                response.setStatus(false);
                response.setMessage(Constants.NOT_ELIGIBLE_ACCOUNT);
            }
        }else{
            response.setStatus(false);
            response.setMessage(Constants.INVALID_USER);
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/deductFromWallet")
    public ResponseEntity<?> deductFromWallet(@RequestParam("userId") String userId,
                                              @RequestParam("amount") double amount,
                                              Principal principal){

        Response response = new Response();
        User authedUser = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();

        if(authedUser != null){

            Optional<Account> accountOptional = accountRepo.findById(authedUser.getAccountId());

            if(accountOptional.isPresent()) {
                if (roleService.hasRole(authedUser, Roles.DEDUCT_WALLET)) {

                    response = walletService.deductWallet(authedUser, userId, amount);

                    if(response.isStatus()){
                    return new ResponseEntity<>(response, HttpStatus.OK);
                    }else{
                        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                    }

                }else{
                    response.setStatus(false);
                    response.setMessage(Constants.NOT_ELIGIBLE_USER);
                }
            }else{
                response.setStatus(false);
                response.setMessage(Constants.NOT_ELIGIBLE_ACCOUNT);
            }
        }else{
            response.setStatus(false);
            response.setMessage(Constants.INVALID_USER);
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/undoWalletAction")
    public ResponseEntity<?> UndoWalletAction(@RequestParam("userId") String userId,
                                              @RequestParam("check") String check,
                                              Principal principal){

        Response response = new Response();
        User authedUser = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();

        if(authedUser != null){

            Optional<Account> accountOptional = accountRepo.findById(authedUser.getAccountId());

            if(accountOptional.isPresent()) {
                if (roleService.hasRole(authedUser, Roles.UNDO_WALLET_ACTION)) {

                    response = walletService.undoWalletAction(authedUser, userId, check);

                    if(response.isStatus()){
                        return new ResponseEntity<>(response, HttpStatus.OK);
                    }else{
                        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                    }

                }else{
                    response.setStatus(false);
                    response.setMessage(Constants.NOT_ELIGIBLE_USER);
                }
            }else{
                response.setStatus(false);
                response.setMessage(Constants.NOT_ELIGIBLE_ACCOUNT);
            }
        }else{
            response.setStatus(false);
            response.setMessage(Constants.INVALID_USER);
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/getWalletsRemainingTotal")
    public ResponseEntity<?> getWalletsRemainingTotal(
                                                @RequestParam(name = "fromDate", required = false) String fromDate,
                                                @RequestParam(name = "fromDate", required = false) String toDate,
                                                Principal principal){

        Response response = new Response();
        User authedUser = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();

        if(authedUser != null){

            Optional<Account> accountOptional = accountRepo.findById(authedUser.getAccountId());

            if(accountOptional.isPresent()) {
                Account account = accountOptional.get();

                if (roleService.hasRole(authedUser, Roles.GET_WALLET_TOTAL_REMAINING)) {

                    response = walletService.getWalletsRemainingTotal(account, fromDate, toDate);

                    if(response.isStatus()){
                        return ResponseEntity.status(HttpStatus.OK).body(response);
                    }else{
                        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                    }

                }else{
                    response.setStatus(false);
                    response.setMessage(Constants.NOT_ELIGIBLE_USER);
                }
            }else{
                response.setStatus(false);
                response.setMessage(Constants.NOT_ELIGIBLE_ACCOUNT);
            }
        }else{
            response.setStatus(false);
            response.setMessage(Constants.INVALID_USER);
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/exportWalletHistoryToExcel")
    public void exportWalletHistoryToExcel(@RequestParam(name = "userId") String userId,
                                           HttpServletResponse httpServletResponse,
                                           Principal principal) throws IOException {

        HashMap response = new HashMap();

        User user = (User)((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();

        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());

        if (accountOptional.isPresent()) {
            httpServletResponse.setContentType("application/octet-stream");
            DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
            String currentDateTime = dateFormatter.format(new Date());

            String headerKey = "Content-Disposition";
            String headerValue = "attachment; filename=Actions" + currentDateTime + ".xlsx";
            httpServletResponse.setHeader(headerKey, headerValue);

            Account account = accountOptional.get();

            ApplicationUser appUser = null;
            if(!userId.equals("")){
                Optional<ApplicationUser> agentOption = applicationUserRepo.findById(userId);
                if(agentOption.isPresent()){
                    appUser = agentOption.get();
                }
            }

            List<WalletHistory> walletHistory = appUser.getWallet().getWalletHistory();

            WalletHistoryExporter exporter = new WalletHistoryExporter(walletHistory);
            exporter.export(httpServletResponse);

            response.put("message", "Excel exported successfully.");
            LoggerFactory.getLogger(TransactionController.class).info(response.get("message").toString());

        }
    }

}
