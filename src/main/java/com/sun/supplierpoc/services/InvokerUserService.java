package com.sun.supplierpoc.services;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.auth.InvokerUser;
import com.sun.supplierpoc.repositories.InvokerUserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.exceptions.UnauthorizedUserException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class InvokerUserService {

    @Autowired
    InvokerUserRepo invokerUserRepo;

    private Conversions conversions = new Conversions();

    public InvokerUser getInvokerUser(String username, String password){
        try {
            return invokerUserRepo.findByUsernameAndPassword(username, password);
        } catch (Exception e) {
            return null;
        }
    }

    public InvokerUser getAuthenticatedUser(String authorization) {

        final String[] values = conversions.convertBasicAuth(authorization);

        if (values.length != 0) {
            String username = values[0];
            String password = values[1];
            InvokerUser user = getInvokerUser(username, password);
            return user;

        }else{
            throw new UnauthorizedUserException(Constants.INVALID_USER);
        }
    }
}
