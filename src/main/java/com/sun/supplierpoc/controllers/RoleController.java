package com.sun.supplierpoc.controllers;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.models.Response;
import com.sun.supplierpoc.models.Role;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.models.roles.Roles;
import com.sun.supplierpoc.repositories.RoleRepository;
import com.sun.supplierpoc.services.RoleService;
import com.sun.supplierpoc.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/role")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @Autowired
    private UserService userService;


    @PostMapping("/test/addRole")
    public ResponseEntity<?> addRole(@RequestBody Role roleRequest) {
        Response response ;
        response = roleService.addRole(roleRequest);
        if (response.isStatus()) {
            return new ResponseEntity<>(response.getData(), HttpStatus.OK);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response.getMessage());
        }
    }

    @PostMapping("/addUserRole")
    public ResponseEntity<?> addUserRole(Principal principal,
                                         @RequestParam("userId") String userId,
                                         @RequestParam("roleIds") List<String> roleIds) {

        Response response = new Response();
        User authedUser = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        if (authedUser != null) {
            if (userService.eligibleForRole(authedUser, Roles.ADD_USER_ROLE)) {
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

    @RequestMapping(value = "/updateUserRoles", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateUserRoles(Principal principal,
                                         @RequestParam("userId") String userId,
                                             @RequestBody ArrayList<String> roleIds) {

        Response response = new Response();
        User authedUser = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        if (authedUser != null) {
            if (userService.eligibleForRole(authedUser, Roles.ADD_USER_ROLE)) {
                response = roleService.updateUserRoles(userId, roleIds);
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
        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        if (user != null) {
            response = roleService.getUserRoles(userId, sameUser, user);
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
