package com.sun.supplierpoc.controllers.simphony;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.repositories.AccountRepo;
import com.sun.supplierpoc.repositories.SyncJobRepo;
import com.sun.supplierpoc.repositories.SyncJobTypeRepo;
import com.sun.supplierpoc.services.simphony.MenuItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import javax.ws.rs.core.MediaType;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;

@RestController()
@RequestMapping(value = {"/Simphony.ent"})
public class CreateOrder {
    @Autowired
    private AccountRepo accountRepo;
    @Autowired
    private SyncJobRepo syncJobRepo;
    @Autowired
    private SyncJobTypeRepo syncJobTypeRepo;
    @Autowired
    MenuItemService menuItemService;
    private Conversions conversions = new Conversions();
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @PostMapping(path ="/CreateOrder",produces= MediaType.APPLICATION_JSON)
    public ResponseEntity CreateOpenCheckRequest(@RequestHeader("Authorization") String authorization) {
        String username, password;

        final String[] values = conversions.convertBasicAuth(authorization);
        if (values.length != 0) {
            username = values[0];
            password = values[1];

        }
        return CreateOpenCheck();
    }

    public ResponseEntity<Object> CreateOpenCheck() {
        try {
            //////////////////////////////////////// Validation ////////////////////////////////////////////////////////
            int empNum = 40;
            int revenueCenter = 100;
            String simphonyPosApiWeb = "http://192.168.1.101:8080/";
            //////////////////////////////////////// En of Validation //////////////////////////////////////////////////
            return this.menuItemService.PostTransactionEx(empNum, revenueCenter, simphonyPosApiWeb);

        }catch (Exception e){
            return new ResponseEntity<Object>("", HttpStatus.EXPECTATION_FAILED);
        }
    }

    private Document responseToDocument(String response){
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        Document dDoc = null;
        try {
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            dDoc = builder.parse(new InputSource(new StringReader(response)));
            return dDoc;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
