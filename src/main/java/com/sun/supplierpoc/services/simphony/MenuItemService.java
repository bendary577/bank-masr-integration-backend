package com.sun.supplierpoc.services.simphony;

import com.google.gson.Gson;
import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.models.configurations.SimphonyLocation;
import com.sun.supplierpoc.models.operationConfiguration.OperationConfiguration;
import com.sun.supplierpoc.models.simphony.*;

import com.sun.supplierpoc.models.simphony.discount.SimphonyPosApi_DiscountEx;
import com.sun.supplierpoc.models.simphony.tender.SimphonyPosApi_TmedDetailItemEx2;
import com.sun.supplierpoc.models.simphony.tender.TmedEPayment;
import com.sun.supplierpoc.models.simphony.tender.pTmedDetailEx2;
import com.sun.supplierpoc.models.simphony.transaction.PostTransactionEx2;
import com.sun.supplierpoc.models.simphony.transaction.PostTransactionEx2Response;
import com.sun.supplierpoc.models.simphony.transaction.pGuestCheck;
import com.sun.supplierpoc.repositories.SyncJobDataRepo;
import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.persistence.exceptions.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.*;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Service
public class MenuItemService {
    @Autowired
    private SyncJobDataRepo syncJobDataRepo;

    public com.sun.supplierpoc.models.Response GetConfigurationInfoEx(int empNum, int revenueCenter,
                                                                      String simphonyServerIP, int startIndex, int maxCount){
        com.sun.supplierpoc.models.Response response = new com.sun.supplierpoc.models.Response();
        Client client = ClientBuilder.newClient();
        JAXBContext jaxbContext;
        try
        {
            GetConfigurationInfoEx getConfigurationInfoEx= new GetConfigurationInfoEx();
            configInfoRequest configInfoRequest = new configInfoRequest();
            SimphonyPosApi_ConfigInfo simphonyPosApi_configInfo = new SimphonyPosApi_ConfigInfo();
            ArrayList<SimphonyPosApi_ConfigInfo> simphonyPosApi_configInfos = new ArrayList<>();

            simphonyPosApi_configInfo.setConfigurationInfoTypeID("MENUITEMDEFINITIONS");
            simphonyPosApi_configInfo.setStartIndex(startIndex);
            simphonyPosApi_configInfo.setMaxRecordCount(maxCount);
            simphonyPosApi_configInfos.add(simphonyPosApi_configInfo);

            simphonyPosApi_configInfo = new SimphonyPosApi_ConfigInfo();

            simphonyPosApi_configInfo.setConfigurationInfoTypeID("MENUITEMPRICE");
            simphonyPosApi_configInfo.setStartIndex(startIndex);
            simphonyPosApi_configInfo.setMaxRecordCount(maxCount);
            simphonyPosApi_configInfos.add(simphonyPosApi_configInfo);


            configInfoRequest.setConfigurationInfo(simphonyPosApi_configInfos);
            configInfoRequest.setEmployeeObjectNumber(empNum);
            configInfoRequest.setRVCObjectNumber(revenueCenter);

            getConfigurationInfoEx.setConfigInfoRequest(configInfoRequest);

            jaxbContext = JAXBContext.newInstance(GetConfigurationInfoEx.class);

            Marshaller marshaller = jaxbContext.createMarshaller();

            MessageFactory mfactory = MessageFactory.newInstance();
            SOAPMessage soapMessage = mfactory.createMessage();
            SOAPPart soapPart = soapMessage.getSOAPPart();
            SOAPEnvelope envelope = soapPart.getEnvelope();
            SOAPBody soapBody = envelope.getBody();
            SOAPHeader soapHeader = envelope.getHeader();
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.newDocument();
            marshaller.marshal(getConfigurationInfoEx,doc);
            soapBody.addDocument(doc);
            envelope.removeNamespaceDeclaration("SOAP-ENV");
            envelope.addNamespaceDeclaration("soap", "http://schemas.xmlsoap.org/soap/envelope/");
            envelope.setPrefix("soap");
            soapBody.setPrefix("soap");
            soapHeader.setPrefix("soap");

            soapMessage.saveChanges();
            Entity<String> payload = Entity.text(soapMessageToString(soapMessage));
            Response configInfoExResponse = client.target(simphonyServerIP + "/EGateway/SimphonyPosApiWeb.asmx")
                    .request(MediaType.TEXT_PLAIN_TYPE)
                    .header("SOAPAction", "http://micros-hosting.com/EGateway/GetConfigurationInfoEx")
                    .post(payload);

            Document responseDoc = responseToDocument(configInfoExResponse.readEntity(String.class));

            if (responseDoc != null){
                if(responseDoc.getElementsByTagName("Success").item(0).getFirstChild().getNodeValue().equals("false")){
                    String errorMessage = responseDoc.getElementsByTagName("ErrorMessage").item(0).getFirstChild().getNodeValue();
                    response.setMessage(errorMessage);
                    response.setStatus(false);
                    return response;
                }

                if (responseDoc.getElementsByTagName("MenuItemPrice").item(0)!=null &&
                        responseDoc.getElementsByTagName("MenuItemDefinitions").item(0)!=null
                ){
                    String xmlMenuItemPrice =responseDoc.getElementsByTagName("MenuItemPrice").item(0).getFirstChild().getNodeValue();
                    String xmlMenuItem =responseDoc.getElementsByTagName("MenuItemDefinitions").item(0).getFirstChild().getNodeValue();

                    jaxbContext = JAXBContext.newInstance(ArrayOfDbMenuItemDefinition.class);
                    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                    ArrayOfDbMenuItemDefinition MenuItem = (ArrayOfDbMenuItemDefinition) jaxbUnmarshaller.unmarshal(new StringReader(xmlMenuItem));

                    jaxbContext = JAXBContext.newInstance(ArrayOfDbMenuItemPrice.class);
                    jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                    ArrayOfDbMenuItemPrice MenuItemPrice = (ArrayOfDbMenuItemPrice) jaxbUnmarshaller.unmarshal(new StringReader(xmlMenuItemPrice));

                    mergeMenuItemWithPrice(MenuItem, MenuItemPrice);

                    response.setMessage("Sync menu item successfully.");
                    response.setStatus(true);
                    response.setMenuItems(MenuItem.getDbMenuItemDefinition());
                    return response;
                }
            }else {
                response.setMessage("Failed to sync menu items.");
                response.setStatus(false);
                return response;
            }

        } catch (SOAPException | JAXBException | ParserConfigurationException e) {
            e.printStackTrace();
        }

        response.setMessage("Failed to sync menu items.");
        response.setStatus(false);
        return response;
    }

    public ResponseEntity PostTransactionEx(PostTransactionEx2 checkDetails, SimphonyLocation location,
                                            OperationType operationType){

        Client client = ClientBuilder.newClient();
        JAXBContext jaxbContext;
        try
        {
            /*
            * Add extra static fields to check
            * */

            buildCheckObject(checkDetails, location, operationType);

            jaxbContext = JAXBContext.newInstance(PostTransactionEx2.class);

            Marshaller marshaller = jaxbContext.createMarshaller();

            MessageFactory mfactory = MessageFactory.newInstance();
            SOAPMessage soapMessage = mfactory.createMessage();
            SOAPPart soapPart = soapMessage.getSOAPPart();
            SOAPEnvelope envelope = soapPart.getEnvelope();
            SOAPBody soapBody = envelope.getBody();
            SOAPHeader soapHeader = envelope.getHeader();
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.newDocument();
            marshaller.marshal(checkDetails,doc);
            soapBody.addDocument(doc);
            envelope.removeNamespaceDeclaration("SOAP-ENV");
            envelope.addNamespaceDeclaration("soap", "http://schemas.xmlsoap.org/soap/envelope/");
            envelope.setPrefix("soap");
            soapBody.setPrefix("soap");
            soapHeader.setPrefix("soap");

            soapMessage.saveChanges();
            Entity<String> payload = Entity.text(soapMessageToString(soapMessage));
            Response createCheckResponse = client.target(location.getSimphonyServer() + "/EGateway/SimphonyPosApiWeb.asmx")
                    .request(MediaType.TEXT_PLAIN_TYPE)
                    .header("SOAPAction", "http://micros-hosting.com/EGateway/PostTransactionEx")
                    .post(payload);

            String createCheckResponseText = createCheckResponse.readEntity(String.class);
            Document responseDoc = responseToDocument(createCheckResponseText);

            if (responseDoc != null){
                if(responseDoc.getElementsByTagName("Success").item(0).getFirstChild().getNodeValue().equals("false")){
                    String errorMessage = responseDoc.getElementsByTagName("ErrorMessage").item(0).getFirstChild().getNodeValue();
                    return new ResponseEntity(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);

                }else {
                    JSONObject jsonObject = xmlDocToJsonObject(responseDoc);
                    assert jsonObject != null;
                    JSONObject checkJson = jsonObject.getJSONObject("soap:Envelope").getJSONObject("soap:Body").getJSONObject("PostTransactionEx2Response");
                    // convert jsonObject --> PostTransactionEx2Response
                    Gson gson = new Gson();
                    PostTransactionEx2Response response = gson.fromJson(checkJson.toString(), PostTransactionEx2Response.class);

                    try {
                        return new ResponseEntity(response, HttpStatus.OK);
                    }catch (org.json.JSONException ex){
                        ex.printStackTrace();
                        return new ResponseEntity("Failed to create new open guest check.", HttpStatus.INTERNAL_SERVER_ERROR);
                    }
                }

            }else {
                return new ResponseEntity("Failed to create new open guest check.", HttpStatus.OK);
            }

        } catch (SOAPException | ParserConfigurationException | JAXBException e) {
            e.printStackTrace();
            return new ResponseEntity("Failed to create new open guest check.", HttpStatus.INTERNAL_SERVER_ERROR);
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

    private String soapMessageToString(SOAPMessage message) {
        String result = null;

        if (message != null)
        {
            ByteArrayOutputStream baos = null;
            try
            {
                baos = new ByteArrayOutputStream();
                message.writeTo(baos);
                result = baos.toString();
            }
            catch (Exception e)
            {
            }
            finally
            {
                if (baos != null)
                {
                    try
                    {
                        baos.close();
                    }
                    catch (IOException ioe)
                    {
                    }
                }
            }
        }
        return result;
    }

    private JSONObject xmlDocToJsonObject(Document xml){

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer;
        try {
            transformer = tf.newTransformer();

            // Uncomment if you do not require XML declaration
            // transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

            //A character stream that collects its output in a string buffer,
            //which can then be used to construct a string.
            StringWriter writer = new StringWriter();

            //transform document to string
            transformer.transform(new DOMSource(xml), new StreamResult(writer));

            String xmlString = writer.getBuffer().toString();
            //  System.out.println(xmlString);
            return XML.toJSONObject(xmlString);
        } catch (JSONException je) {
            System.out.println(je.toString());
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void mergeMenuItemWithPrice(ArrayOfDbMenuItemDefinition arrayOfDbMenuItemDefinition, ArrayOfDbMenuItemPrice arrayOfDbMenuItemPrice){
        List<DbMenuItemDefinition> DbMenuItemDefinition =  arrayOfDbMenuItemDefinition.getDbMenuItemDefinition();
        List<DbMenuItemPrice> DbMenuItemPrice =  arrayOfDbMenuItemPrice.getDbMenuItemPrice();

        for (DbMenuItemDefinition dbMenuItemDefinition: DbMenuItemDefinition) {
            DbMenuItemPrice dbMenuItemPrice= getMenuItemPrice(DbMenuItemPrice, dbMenuItemDefinition.getMenuItemDefID());
            if (dbMenuItemPrice != null){
                dbMenuItemDefinition.setMenuItemPrice(dbMenuItemPrice);
            }
        }
    }

    private  static DbMenuItemPrice getMenuItemPrice(List<DbMenuItemPrice> DbMenuItemPrice, String menuItemDefID){
        for (DbMenuItemPrice dbMenuItemPrice: DbMenuItemPrice) {
            if (dbMenuItemPrice.getMenuItemDefID().equals(menuItemDefID)){
                DbMenuItemPrice.remove(dbMenuItemPrice);
                return dbMenuItemPrice;
            }
        }
        return null;
    }

    public ArrayList<SyncJobData> saveMenuItemData(ArrayList<DbMenuItemDefinition> menuItems, SyncJob syncJob) {
        ArrayList<SyncJobData> savedMenuItems = new ArrayList<>();
        for (DbMenuItemDefinition menuItem : menuItems) {
            HashMap<String, String> menuItemData = new HashMap<>();

            menuItemData.put("menuFirstName", menuItem.getName1().getStringText());
            menuItemData.put("menuSecondName", menuItem.getName2().getStringText());
            menuItemData.put("miObjectNum", menuItem.getMiMasterObjNum());
            menuItemData.put("availability", menuItem.getCheckAvailability().toString());

            if (menuItem.getMenuItemPrice() != null){
                menuItemData.put("menuItemPrice", menuItem.getMenuItemPrice().getPrice());
            }else{
                menuItemData.put("menuItemPrice", "0");
            }

            SyncJobData syncJobData = new SyncJobData(menuItemData, Constants.RECEIVED, "", new Date(),
                    syncJob.getId());
            syncJobDataRepo.save(syncJobData);
            savedMenuItems.add(syncJobData);
        }
        return savedMenuItems;
    }

    public ArrayList<HashMap<String, String>> simplifyMenuItemData(ArrayList<SyncJobData> menuItemsData) {
        ArrayList<HashMap<String, String>> menuItems = new ArrayList<>();

        for (SyncJobData data : menuItemsData ) {
            menuItems.add(data.getData());
        }
        return menuItems;
    }

    private PostTransactionEx2 buildCheckObject(SimphonyLocation location){
        pGuestCheck pGuestCheck = new pGuestCheck();
        PostTransactionEx2 postTransactionEx2 = new PostTransactionEx2();
        PCheckInfoLines PCheckInfoLines = new PCheckInfoLines();
        PCheckInfoLines.setString("Online Check");

        pGuestCheck.setCheckNum("0");
        pGuestCheck.setCheckSeq("0");
        pGuestCheck.setCheckGuestCount("0");
        pGuestCheck.setCheckStatusBits("0");
        pGuestCheck.setEventObjectNum("0");
        pGuestCheck.setCheckTableObjectNum("1");
        pGuestCheck.setCheckEmployeeObjectNum(Integer.toString(location.getEmployeeNumber()));
        pGuestCheck.setCheckRevenueCenterID(Integer.toString(location.getRevenueCenterID()));
        pGuestCheck.setCheckOrderType("1"); //E.g. Dine In and Eat Out
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        pGuestCheck.setCheckDateToFire(dtf.format(now));

        pGuestCheck.setPCheckInfoLines(PCheckInfoLines);

        List<MenuItem> menuItems = new ArrayList<>();

        MenuItem menuItem = new MenuItem();
        ItemDiscount itemDiscount = new ItemDiscount();

        menuItem.setMiSubLevel("1");
        menuItem.setMiMenuLevel("1");
        menuItem.setMiPriceLevel("0");
        menuItem.setMiDefinitionSeqNum("1");
        menuItem.setMiQuantity("2");
        menuItem.setMiObjectNum("101");

        SimphonyPosApi_DiscountEx simphonyPosApi_discountEx = new SimphonyPosApi_DiscountEx();
        simphonyPosApi_discountEx.setDiscObjectNum("0");

        itemDiscount.setSimphonyPosApi_DiscountEx(simphonyPosApi_discountEx);
        menuItem.setItemDiscount(itemDiscount);

        menuItems.add(menuItem);
        menuItems.add(menuItem);


        pTmedDetailEx2 pTmedDetailEx2 = new pTmedDetailEx2();
        SimphonyPosApi_TmedDetailItemEx2 SimphonyPosApi_TmedDetailItemEx2 = new SimphonyPosApi_TmedDetailItemEx2();
        TmedEPayment TmedEPayment = new TmedEPayment();
        TmedEPayment.setAccountType("ACCOUNT_TYPE_UNDEFINED");

        SimphonyPosApi_TmedDetailItemEx2.setTmedEPayment(TmedEPayment);
        pTmedDetailEx2.setSimphonyPosApi_TmedDetailItemEx2(SimphonyPosApi_TmedDetailItemEx2);
        SimphonyPosApi_TmedDetailItemEx2.setTmedObjectNum("3001");

        postTransactionEx2.setpGuestCheck(pGuestCheck);
//        postTransactionEx2.setPpMenuItemsEx(ppMenuItemsEx);
        postTransactionEx2.setpTmedDetailEx2(pTmedDetailEx2);

        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(postTransactionEx2);
        }catch (IOException e) {
            System.out.println("Failed to convert object to json");
        }

        return postTransactionEx2;
    }

    private void buildCheckObject(PostTransactionEx2 checkDetails, SimphonyLocation location,
                                                OperationType operationType){
        OperationConfiguration configuration = operationType.getConfiguration();
        //////////////////////////////////////// Guest Check Details ////////////////////////////////////////////////
        pGuestCheck pGuestCheck = checkDetails.getpGuestCheck();

        pGuestCheck.setCheckNum("0");
        pGuestCheck.setCheckSeq("0");
        pGuestCheck.setCheckGuestCount("1");
        pGuestCheck.setCheckStatusBits("0");
        pGuestCheck.setEventObjectNum("0");
        pGuestCheck.setCheckTableObjectNum("0");

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        pGuestCheck.setCheckDateToFire(dtf.format(now));

        pGuestCheck.setCheckEmployeeObjectNum(Integer.toString(location.getEmployeeNumber()));
        pGuestCheck.setCheckRevenueCenterID(Integer.toString(location.getRevenueCenterID()));

        ////////////////////////////////////// Tender Details ///////////////////////////////////////////////////////

        pTmedDetailEx2 pTmedDetailEx2 = new pTmedDetailEx2();
        SimphonyPosApi_TmedDetailItemEx2 SimphonyPosApi_TmedDetailItemEx2 = new SimphonyPosApi_TmedDetailItemEx2();
        TmedEPayment TmedEPayment = new TmedEPayment();
        TmedEPayment.setAccountType(configuration.getAccountType());

        SimphonyPosApi_TmedDetailItemEx2.setTmedEPayment(TmedEPayment);
        SimphonyPosApi_TmedDetailItemEx2.setTmedObjectNum(configuration.getTenderNumber());
        pTmedDetailEx2.setSimphonyPosApi_TmedDetailItemEx2(SimphonyPosApi_TmedDetailItemEx2);

        ////////////////////////////////////// Menu Items Details ///////////////////////////////////////////////////
        List<SimphonyMenuItem> menuItems = checkDetails.getPpMenuItemsEx();

        // Prepare discount object
        SimphonyPosApi_DiscountEx simphonyPosApi_discountEx = new SimphonyPosApi_DiscountEx();
        simphonyPosApi_discountEx.setDiscObjectNum(configuration.getDiscountNumber());

        ItemDiscount itemDiscount = new ItemDiscount();
        itemDiscount.setSimphonyPosApi_DiscountEx(simphonyPosApi_discountEx);

        for (SimphonyMenuItem items : menuItems) {
            // Add Discount
            items.getMenuItem().setItemDiscount(itemDiscount);

            items.getMenuItem().setMiSubLevel("1");
            items.getMenuItem().setMiMenuLevel("1");
            items.getMenuItem().setMiPriceLevel("0");
            items.getMenuItem().setMiDefinitionSeqNum("1");
        }

        checkDetails.setpGuestCheck(pGuestCheck);
        checkDetails.setPpMenuItemsEx(menuItems);
        checkDetails.setpTmedDetailEx2(pTmedDetailEx2);
    }

}

