package com.sun.supplierpoc.controllers.simphony;
import com.sun.supplierpoc.repositories.AccountRepo;
import com.sun.supplierpoc.repositories.SyncJobRepo;
import com.sun.supplierpoc.repositories.SyncJobTypeRepo;
import com.sun.supplierpoc.services.simphony.MenuItemService;
import org.eclipse.persistence.exceptions.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringReader;
import java.io.StringWriter;

@RestController()
@RequestMapping("/CreateOrder")
public class CreateOrder {
    @Autowired
    private AccountRepo accountRepo;
    @Autowired
    private SyncJobRepo syncJobRepo;
    @Autowired
    private SyncJobTypeRepo syncJobTypeRepo;
    @Autowired
    MenuItemService menuItemService;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//    @PostMapping(produces= MediaType.APPLICATION_JSON)
//    public ResponseEntity<Object> PostOrder(){
//        Client client = ClientBuilder.newClient();
//        JAXBContext jaxbContext;
//        try
//        {
//          String xmll="<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
//                  "  <soap:Body>\n" +
//                  "        <PostTransactionEx xmlns=\"http://micros-hosting.com/EGateway/\">\n" +
//                  "            <vendorCode />\n" +
//                  "            <pGuestCheck>\n" +
//                  "                <CheckDateToFire>2020-08-24T17:03:25</CheckDateToFire>\n" +
//                  "                <CheckEmployeeObjectNum>35</CheckEmployeeObjectNum>\n" +
//                  "                <CheckGuestCount>0</CheckGuestCount>\n" +
//                  "                <CheckID />\n" +
//                  "                <CheckNum>0</CheckNum>\n" +
//                  "                <CheckOrderType>1</CheckOrderType>\n" +
//                  "                <CheckRevenueCenterID>100</CheckRevenueCenterID>\n" +
//                  "                <CheckSeq>0</CheckSeq>\n" +
//                  "                <CheckStatusBits>0</CheckStatusBits>\n" +
//                  "                <CheckTableObjectNum>1</CheckTableObjectNum>\n" +
//                  "                <PCheckInfoLines>\n" +
//                  "                    <string />\n" +
//                  "                    <string />\n" +
//                  "                </PCheckInfoLines>\n" +
//                  "                <EventObjectNum>0</EventObjectNum>\n" +
//                  "            </pGuestCheck>\n" +
//                  "            <ppMenuItems>\n" +
//                  "                <SimphonyPosApi_MenuItem>\n" +
//                  "                    <Condiments/>\n" +
//                  "                    <MenuItem>\n" +
//                  "                        <ItemDiscount>\n" +
//                  "                            <DiscObjectNum>0</DiscObjectNum>\n" +
//                  "                        </ItemDiscount>\n" +
//                  "                        <MiObjectNum>101</MiObjectNum>\n" +
//                  "                        <MiOverridePrice >200</MiOverridePrice>\n" +
//                  "                        <MiReference />\n" +
//                  "                        <MiWeight />\n" +
//                  "                        <MiMenuLevel>0</MiMenuLevel>\n" +
//                  "                        <MiSubLevel>0</MiSubLevel>\n" +
//                  "                        <MiPriveLevel>0</MiPriveLevel>\n" +
//                  "                    </MenuItem>\n" +
//                  "                </SimphonyPosApi_MenuItem>\n" +
//                  "            </ppMenuItems>\n" +
//                  "            <ppComboMeals />\n" +
//                  "            <pServiceChg/>\n" +
//                  "            <pSubTotalDiscount>\n" +
//                  "                <DiscObjectNum>0</DiscObjectNum>\n" +
//                  "            </pSubTotalDiscount>\n" +
//                  "            <pTmedDetail>\n" +
//                  "                <TmedEPayment>\n" +
//                  "                    <AccountDataSource>SOURCE_UNDEFINED</AccountDataSource>\n" +
//                  "                    <AccountType>ACCOUNT_TYPE_UNDEFINED</AccountType>\n" +
//                  "                    <ExpirationDate>0001-01-01T00:00:00</ExpirationDate>\n" +
//                  "                    <IssueNumber>0</IssueNumber>\n" +
//                  "                    <PaymentCommand>NO_E_PAYMENT</PaymentCommand>\n" +
//                  "                    <StartDate>0001-01-01T00:00:00</StartDate>\n" +
//                  "                </TmedEPayment>\n" +
//                  "                <TmedObjectNum>3001</TmedObjectNum>\n" +
//                  "                <TmedPartialPayment />\n" +
//                  "                <TmedReference />\n" +
//                  "            </pTmedDetail>\n" +
//                  "            <pTotalsResponse />\n" +
//                  "            <ppCheckPrintLines>\n" +
//                  "                <string />\n" +
//                  "            </ppCheckPrintLines>\n" +
//                  "            <ppVoucherOutput>\n" +
//                  "                <string />\n" +
//                  "            </ppVoucherOutput>\n" +
//                  "        </PostTransactionEx>\n" +
//                  "    </soap:Body>\n" +
//                  "</soap:Envelope>";
//            Entity<String> payload = Entity.text(xmll);
//            Response response = client.target("http://192.168.1.101:8080/EGateway/SimphonyPosApiWeb.asmx")
//                    .request(MediaType.TEXT_PLAIN_TYPE)
//                    .header("SOAPAction", "http://micros-hosting.com/EGateway/PostTransactionEx")
//                    .post(payload);
//
//
//
//            Document responseDoc = responseToDocument(response.readEntity(String.class));
//
//            JSONObject jsonObject = xmlDocToJsonObject(responseDoc);
//            try {
//
//                return new ResponseEntity<Object>(jsonObject.toString(4), HttpStatus.OK);
//            }catch (org.json.JSONException ex){
//                ex.printStackTrace();
//            }
//
//        }catch (org.json.JSONException ex){
//            ex.printStackTrace();
//        }
//
//        return null;
//    }

    @PostMapping(produces= MediaType.APPLICATION_JSON)
    public ResponseEntity<Object> CreateOpenCheckRequest() {
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
