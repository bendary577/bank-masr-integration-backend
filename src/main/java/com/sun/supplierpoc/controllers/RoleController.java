package com.sun.supplierpoc.controllers;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.Response;
import com.sun.supplierpoc.models.Role;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.repositories.UserRepo;
import com.sun.supplierpoc.services.AccountService;
import com.sun.supplierpoc.services.RoleService;
import com.sun.supplierpoc.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(("role"))
public class RoleController {

    @Autowired
    private RoleService roleService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepo userRepo;

    @PostMapping("addRole")
    public ResponseEntity<?> addRole(@RequestBody Role roleRequest, Principal principal) {

        Response response = new Response();
        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        if (user != null) {
            Account account = accountService.getAccount(user.getAccountId());
            if (account != null) {
                Role role = roleService.addRole(roleRequest);
                response.setData(role);
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                response.setMessage(Constants.NOT_ELIGIBLE_ACCOUNT);
                return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
            }
        } else {
            response.setMessage(Constants.INVALID_USER);
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

    }

    @PostMapping("/addUserRole")
    public ResponseEntity<?> addUserRole(@RequestParam("userId") String userId,
                                         @RequestParam("roleIds") List<String> roleIds,
                                         Principal principal) {

        Response response = new Response();
        User authedUser = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        if (authedUser != null) {
            if (userService.eligibleForRole(authedUser, Constants.ADD_ROLE)) {
                response = roleService.addUserRole(userId, roleIds);
                if (response.isStatus()) {
                    return new ResponseEntity<>(response, HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                }
            } else {
                response.setStatus(false);
                response.setMessage(Constants.NOT_ELIGIBLE_USER);
                return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
            }
        } else {
            response.setStatus(false);
            response.setMessage(Constants.INVALID_USER);
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("/getRoles")
    @CrossOrigin("*")
    public ResponseEntity<?> getRoles(@RequestParam("userId") String userId,
                                      @RequestParam("sameUser") boolean sameUser,
                                      Principal principal) {

        Response response = new Response();
        User authedUser = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        if (authedUser != null) {
            response = roleService.getUserRoles(userId, sameUser, authedUser);
            if(response.isStatus()) {
                return new ResponseEntity<>(response, HttpStatus.OK);
            }else{
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
        } else {
            response.setStatus(false);
            response.setMessage(Constants.INVALID_USER);
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }
}
