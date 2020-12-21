package com.sun.supplierpoc.controllers.simphony;

import com.sun.supplierpoc.models.simphony.GetConfigurationInfo;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

@RestController()
@RequestMapping("/Configuration")
public class ConfigurationController {


    @GetMapping(produces=MediaType.APPLICATION_JSON)
    public ResponseEntity<Object> GetConfigurationInfo(@RequestParam("empNum") int empNum,@RequestParam("infoType") int infoType,@RequestParam("revenueCenter") int revenueCenter){
        Client client = ClientBuilder.newClient();
        JAXBContext jaxbContext;
        try
        {
            GetConfigurationInfo GetConfigurationInfo= new GetConfigurationInfo();

            GetConfigurationInfo.setEmployeeObjectNum(empNum);

            GetConfigurationInfo.setRevenueCenter(revenueCenter);
            List<Number> list = new ArrayList<>();

            list.add(infoType);
            GetConfigurationInfo.setConfigurationInfoType(list);
            jaxbContext = JAXBContext.newInstance(GetConfigurationInfo.class);

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
            marshaller.marshal(GetConfigurationInfo,doc);
            soapBody.addDocument(doc);
            envelope.removeNamespaceDeclaration("SOAP-ENV");
            envelope.addNamespaceDeclaration("soap", "http://schemas.xmlsoap.org/soap/envelope/");
            envelope.setPrefix("soap");
            soapBody.setPrefix("soap");
            soapHeader.setPrefix("soap");
            // envelope.setPrefix("soapenv");

            soapMessage.saveChanges();
            Entity<String> payload = Entity.text(soapMessageToString(soapMessage));
            Response response = client.target("http://192.168.1.101:8080/EGateway/SimphonyPosApiWeb.asmx")
                    .request(MediaType.TEXT_PLAIN_TYPE)
                    .header("SOAPAction", "http://micros-hosting.com/EGateway/GetConfigurationInfo")
                    .post(payload);



            Document responseDoc = responseToDocument(response.toString());
            String errorMessage =responseDoc.getElementsByTagName("ErrorMessage").item(0).getFirstChild().getNodeValue();
            if(responseDoc.getElementsByTagName("Success").item(0).getFirstChild().getNodeValue().equals("false")){
                return new ResponseEntity<Object>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
            }


            if(responseDoc.getElementsByTagName("MenuItemDefinitions").item(0)==null){
                return new ResponseEntity<Object>("Invalid Info Type", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            String xml =responseDoc.getElementsByTagName("MenuItemDefinitions").item(0).getFirstChild().getNodeValue();
            Document dDoc=responseToDocument(xml);
            JSONObject jsonObject = xmlDocToJsonObject(dDoc);
            try {
                JSONObject ArrayOfDbMenuItemDefinition = jsonObject.getJSONObject("ArrayOfDbMenuItemDefinition");
                JSONArray DbMenuItemDefinition =ArrayOfDbMenuItemDefinition.getJSONArray("DbMenuItemDefinition");
                return new ResponseEntity<Object>(DbMenuItemDefinition.toString(4), HttpStatus.OK);
            }catch (JSONException ex){
                ex.printStackTrace();
            }

        } catch (SOAPException e) {
            e.printStackTrace();
        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        return null;
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

    public static byte[] asByteArray(Document doc, String encoding) throws TransformerException {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, encoding);
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

        StringWriter writer = new StringWriter();
        Result result = new StreamResult(writer);
        DOMSource source = new DOMSource(doc);
        transformer.transform(source, result);
        return writer.getBuffer().toString().getBytes();
    }

    public String soapMessageToString(SOAPMessage message)
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

    private static Document convertStringToXMLDocument(String xmlString)
    {
        //Parser that produces DOM object trees from XML content
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        //API to obtain DOM Document instance
        DocumentBuilder builder = null;
        try
        {
            //Create DocumentBuilder with default configuration
            builder = factory.newDocumentBuilder();

            //Parse the content to Document object
            Document doc = builder.parse(new InputSource(new StringReader(xmlString)));
            printDocument(doc,System.out);
            return doc;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static void printDocument(Document doc, OutputStream out) throws IOException, TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        transformer.transform(new DOMSource(doc),
                new StreamResult(new OutputStreamWriter(out, "UTF-8")));
    }
}
