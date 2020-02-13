package com.sun.supplierpoc.soapModels;

import com.systemsunion.security.IAuthenticationVoucher;
import com.systemsunion.ssc.client.SecureSoapComponent;
import com.systemsunion.ssc.client.SecurityProvider;
import com.systemsunion.ssc.client.SoapComponent;


public class SetupServerEnvironment {
    static int PORT = 8080;
    static String HOST= "192.168.1.21";

    public void setupServerEnv(String username, String password) {
//        boolean useEncryption = false;
////        String username = "ACt";
////        String password = "P@ssw0rd";
//
//        SecurityProvider securityProvider = new SecurityProvider(HOST, useEncryption);
//        IAuthenticationVoucher voucher = securityProvider.Authenticate(username, password);
//
//        SoapComponent component = null;
//        if (useEncryption) {
//            component = new SecureSoapComponent(HOST, PORT);
//        } else {
//            component = new SoapComponent(HOST, PORT);
//        }
//        component.authenticate(voucher);

    }
}
