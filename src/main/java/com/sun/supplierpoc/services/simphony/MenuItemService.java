package com.sun.supplierpoc.services.simphony;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.models.configurations.Discount;
import com.sun.supplierpoc.models.configurations.ServiceCharge;
import com.sun.supplierpoc.models.configurations.Tax;
import com.sun.supplierpoc.models.configurations.Tender;
import com.sun.supplierpoc.models.simphony.*;

import com.sun.supplierpoc.repositories.SyncJobDataRepo;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Service
public class MenuItemService {
    @Autowired
    private SyncJobDataRepo syncJobDataRepo;

    public com.sun.supplierpoc.models.Response GetConfigurationInfoEx(int empNum, int revenueCenter,
                                                                      String simphonyServerIP){
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
            simphonyPosApi_configInfo.setStartIndex(1);
            simphonyPosApi_configInfo.setMaxRecordCount(10);
            simphonyPosApi_configInfos.add(simphonyPosApi_configInfo);

            simphonyPosApi_configInfo = new SimphonyPosApi_ConfigInfo();

            simphonyPosApi_configInfo.setConfigurationInfoTypeID("MENUITEMPRICE");
            simphonyPosApi_configInfo.setStartIndex(1);
            simphonyPosApi_configInfo.setMaxRecordCount(10);
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

        } catch (SOAPException e) {
            e.printStackTrace();
        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        response.setMessage("Failed to sync menu items.");
        response.setStatus(false);
        return response;
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

    private String soapMessageToString(SOAPMessage message)
    {
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


    public void saveMenuItemData(ArrayList<DbMenuItemDefinition> menuItems, SyncJob syncJob) {
        for (DbMenuItemDefinition menuItem : menuItems) {
            HashMap<String, String> menuItemData = new HashMap<>();

            menuItemData.put("menuFirstName", menuItem.getName1().getStringText());
            menuItemData.put("menuSecondName", menuItem.getName2().getStringText());
            menuItemData.put("MiMasterObjNum", menuItem.getMiMasterObjNum());
            menuItemData.put("menuItemDefID", menuItem.getMenuItemDefID());
            menuItemData.put("Availability", menuItem.getCheckAvailability().toString());

            if (menuItem.getMenuItemPrice() != null){
                menuItemData.put("menuItemPrice", menuItem.getMenuItemPrice().getPrice());
            }else{
                menuItemData.put("menuItemPrice", "0");
            }

            SyncJobData syncJobData = new SyncJobData(menuItemData, Constants.RECEIVED, "", new Date(),
                    syncJob.getId());
            syncJobDataRepo.save(syncJobData);
        }
    }
}
