package com.sun.supplierpoc.services;

import com.sun.supplierpoc.models.auth.InvokerUser;
import com.sun.supplierpoc.repositories.InvokerUserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class InvokerUserService {
    @Autowired
    InvokerUserRepo invokerUserRepo;

    public InvokerUser getInvokerUser(String username, String password){
        try {
            return invokerUserRepo.findByUsernameAndPassword(username, password);
        } catch (Exception e) {
            return null;
        }
    }
}
