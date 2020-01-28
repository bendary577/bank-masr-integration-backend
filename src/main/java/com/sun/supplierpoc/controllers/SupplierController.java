package com.sun.supplierpoc.controllers;
//Import SSC Client Library stuff from connect-client.jar
import com.sun.supplierpoc.models.SSC;
import com.sun.supplierpoc.models.Supplier;
import com.systemsunion.security.IAuthenticationVoucher;
import com.systemsunion.ssc.client.*;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.util.ArrayList;


@RestController
public class SupplierController {
    //Declare server and port variables
    static String HOST= "localhost";
    static int PORT = 8080;

    @RequestMapping("/getSuppliers")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ArrayList<Supplier> getSuppliers() throws SoapFaultException, ComponentException {

        boolean useEncryption = false;

        String username = "ACt";
        String password = "P@ssw0rd";

        // obtain a SunSystems Security Voucher via SecurityProvider SOAP API
        SecurityProvider securityProvider = new SecurityProvider(HOST, useEncryption);
        IAuthenticationVoucher voucher = securityProvider.Authenticate(username, password);

        // setup and authenticate a SOAP API component proxy
        SoapComponent component = null;
        if (useEncryption) {
            component = new SecureSoapComponent(HOST, PORT);
        } else {
            component = new SoapComponent(HOST, PORT);
        }
        component.authenticate(voucher);

        // call 'Query' method via SOAP API ...
        String inputPayload =   "<SSC>" +
                "   <User>" +
                "       <Name>" + username + "</Name>" +
                "   </User>" +
                "   <SunSystemsContext>" +
                "       <BusinessUnit>PK1</BusinessUnit>" +
                "   </SunSystemsContext>" +
                "   <Payload>" +
//                "   <OutputLimit>5</OutputLimit>" +
                    "<Select>" +
                        "<Supplier>" +
                            "<AccountCode/>" +
                            "<SupplierName/>" +
                        "</Supplier>" +
                    "</Select>" +
                "   </Payload>" +
                "</SSC>";
        String strOut = component.execute("Supplier", "Query", inputPayload);

        // Convert XML to Object
        JAXBContext jaxbContext;
        try
        {
            jaxbContext = JAXBContext.newInstance(SSC.class);

            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

            SSC query = (SSC) jaxbUnmarshaller.unmarshal(new StringReader(strOut));

            System.out.println(query);

            return query.getPayload();
        }
        catch (JAXBException e)
        {
            e.printStackTrace();
        }


//        System.out.println(strOut);
        return null;
    }
}
