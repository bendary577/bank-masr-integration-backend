package com.sun.supplierpoc.controllers;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.controllers.application.TransactionController;
import com.sun.supplierpoc.excelExporters.ActionsExcelExporter;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.applications.Action;
import com.sun.supplierpoc.models.applications.ActionStats;
import com.sun.supplierpoc.models.auth.InvokerUser;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.repositories.AccountRepo;
import com.sun.supplierpoc.repositories.InvokerUserRepo;
import com.sun.supplierpoc.repositories.UserRepo;
import com.sun.supplierpoc.services.ActionService;
import com.sun.supplierpoc.services.application.ActionStatsService;
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
import java.util.*;

@RestController
public class UserController {
    @Autowired
    UserRepo userRepo;

    @Autowired
    InvokerUserRepo invokerUserRepo;

    @Autowired
    private AccountRepo accountRepo;

    @Autowired
    private ActionService actionService;

    @Autowired
    private ActionStatsService actionStatsService;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @RequestMapping("/getUsers")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public List<User> getUsers(Principal principal) {
        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();

        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        Account account = accountOptional.get();

        return userRepo.findByAccountId(account.getId());
    }

    @RequestMapping("/updateUser")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity updateUser(Principal principal, @RequestBody User updatedUser) {
        try {
            User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
            if(user == null){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Constants.INVALID_USER);
            }

            Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
            if (accountOptional.isPresent()) {
                Account account = accountOptional.get();

                updatedUser.setUpdateDate(new Date());
                userRepo.save(updatedUser);
                return ResponseEntity.status(HttpStatus.OK).body("");
            }else{
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Constants.INVALID_ACCOUNT);
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to add web service invoker.");
        }
    }

    @RequestMapping("/addInvokerUser")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity addInvokerUser(@RequestBody InvokerUser invoker, Principal principal) {
        try {
            User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
            Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
            if (accountOptional.isPresent()) {
                Account account = accountOptional.get();

                invoker.setAccountId(account.getId());
                invoker.setTypeId(invoker.getTypeId());

                // check existence
                if (invokerUserRepo.countAllByUsername(invoker.getUsername()) > 0) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username already exists!");
                } else {
                    InvokerUser invokerUser = new InvokerUser(invoker.getUsername(), invoker.getPassword(), account.getId(),
                            invoker.getTypeId(), new Date());
                    invokerUserRepo.save(invokerUser);
                    return ResponseEntity.status(HttpStatus.OK).body("");
                }
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to add web service invoker.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to add web service invoker.");
        }
    }

    @RequestMapping("/getInvokerUser")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity getInvokerUser(@RequestParam(name = "syncJobTypeId") String syncJobTypeId) {
        try {
            ArrayList<InvokerUser> invokerUsers = invokerUserRepo.findAllByTypeId(syncJobTypeId);
            return ResponseEntity.status(HttpStatus.OK).body(invokerUsers);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to get web service invoker.");
        }
    }

    /* Entry System Web Services */
    @RequestMapping("/countAgentActions")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity countAgentActions(Principal principal,
                                          @RequestParam(name = "userId") String userId,
                                          @RequestParam(name = "actionType") String actionType,
                                          @RequestParam(name = "fromDate", required = false) String fromDate,
                                          @RequestParam(name = "toDate", required = false) String toDate) {
        try {
            User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
            if(user == null){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Constants.INVALID_USER);
            }

            Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
            if (accountOptional.isPresent()) {
                Account account = accountOptional.get();

                User agentUser = null;
                if(!userId.equals("")){
                    Optional<User> agentOption = userRepo.findById(userId);
                    if(agentOption.isPresent()){
                        agentUser = agentOption.get();
                    }
                }

                int actions = actionService.getUserActionCount(agentUser, account.getId(), actionType,
                        fromDate, toDate);
                return ResponseEntity.status(HttpStatus.OK).body(actions);
            }else{
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Constants.INVALID_ACCOUNT);
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @RequestMapping("/getAgentActions")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity getAgentActions(Principal principal,
                                          @RequestParam(name = "pageNumber") int pageNumber,
                                          @RequestParam(name = "limit") int limit,
                                          @RequestParam(name = "userId") String userId,
                                          @RequestParam(name = "actionType") String actionType,
                                          @RequestParam(name = "fromDate", required = false) String fromDate,
                                          @RequestParam(name = "toDate", required = false) String toDate) {
        try {
            User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
            if(user == null){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Constants.INVALID_USER);
            }

            Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
            if (accountOptional.isPresent()) {
                Account account = accountOptional.get();

                User agentUser = null;
                if(!userId.equals("")){
                    Optional<User> agentOption = userRepo.findById(userId);
                    if(agentOption.isPresent()){
                        agentUser = agentOption.get();
                    }
                }

                ArrayList<Action> actions = actionService.getUserActionPaginated(agentUser, account.getId(), actionType,
                        fromDate, toDate, pageNumber, limit);
                return ResponseEntity.status(HttpStatus.OK).body(actions);
            }else{
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Constants.INVALID_ACCOUNT);
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @RequestMapping("/getAgentActionsSummary")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity getAgentActionsSummary(Principal principal) {
        try {
            User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
            if(user == null){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Constants.INVALID_USER);
            }

            Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
            if (accountOptional.isPresent()) {
                Account account = accountOptional.get();

                ArrayList<ActionStats> actions = actionStatsService.findActionStatsByAccount(account.getId());
                return ResponseEntity.status(HttpStatus.OK).body(actions);
            }else{
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Constants.INVALID_ACCOUNT);
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/exportAgentActionToExcel")
    public void exportAgentActionsToExcel(@RequestParam(name = "userId") String userId,
                                          @RequestParam(name = "actionType") String actionType,
                                          @RequestParam(name = "fromDate", required = false) String fromDate,
                                          @RequestParam(name = "toDate", required = false) String toDate,
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

            User agentUser = null;
            if(!userId.equals("")){
                Optional<User> agentOption = userRepo.findById(userId);
                if(agentOption.isPresent()){
                    agentUser = agentOption.get();
                }
            }

            List<Action> actions = actionService.getUserAction(agentUser, account.getId(), actionType,
                    fromDate, toDate);

            ActionsExcelExporter exporter = new ActionsExcelExporter(actions);
            exporter.export(httpServletResponse);

            response.put("message", "Excel exported successfully.");
            LoggerFactory.getLogger(TransactionController.class).info(response.get("message").toString());

        }
    }

}
