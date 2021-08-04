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
    public ResponseEntity<?> addRole(@RequestBody Role roleRequest, Principal principal){

        Response response = new Response();

        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();

        if(user != null){

            Account account = accountService.getAccount(user.getAccountId());
            if(account != null){
                Role role =  roleService.addRole(roleRequest);
                response.setData(role);
                return new ResponseEntity<>(response, HttpStatus.OK);

            }else{
                response.setMessage(Constants.NOT_ELIGIBLE_ACCOUNT);
                return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
            }

        }else{
                response.setMessage(Constants.INVALID_USER);
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

    }

    @PostMapping("/addUserRole")
    public ResponseEntity<?> addUserRole(@RequestPart("roleId") String userId,
                                         @RequestPart("userId") String roleId,
                                         Principal principal){

        Response response = new Response();

        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        if(user != null){
            if(userService.eligibleForRole(user, Constants.ADD_ROLE)){

                Optional<User> userOptional = userRepo.findById(userId);

                if(userOptional.isPresent()){

                    User requiredUser = userOptional.get();
                    requiredUser.getRoleIds().add(roleId);

                    response.setStatus(true);
                    return new ResponseEntity<>(response, HttpStatus.OK);

                }else {
                    response.setStatus(false);
                    response.setMessage(Constants.NOT_ELIGIBLE_USER);
                    return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
                }
            }else{
                response.setStatus(false);
                response.setMessage(Constants.NOT_ELIGIBLE_USER);
                return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
            }
        }else{
            response.setStatus(false);
            response.setMessage(Constants.INVALID_USER);
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("/getRoles")
    public ResponseEntity<?> getRoles(@RequestParam("userId") String userId,
                                      @RequestParam("sameUser") boolean sameUSer,
                                      Principal principal){

        Response response = new Response();

        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();

        List<Role> roleList;

        if(user != null){

            if(!userId.equals("")) {

                Optional<User> userOptional = userRepo.findById(userId);

                if (userOptional.isPresent()) {
                    roleList = roleService.getAllRoles(userOptional.get());
                } else {
                    response.setStatus(false);
                    response.setMessage(Constants.NOT_ELIGIBLE_USER);
                    return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
                }
            }else{
                roleList = roleService.getAllRoles(user);
            }
                response.setStatus(true);
                response.setData(roleList);
                return new ResponseEntity<>(response, HttpStatus.OK);
        }else{
            response.setStatus(false);
            response.setMessage(Constants.INVALID_USER);
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }
}
